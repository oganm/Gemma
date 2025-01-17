/*
 * The Gemma project
 *
 * Copyright (c) 2006 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.gemma.core.util;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.LoggerContext;
import ubic.basecode.util.DateUtil;
import ubic.gemma.core.apps.GemmaCLI;
import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;
import ubic.gemma.persistence.util.Settings;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * Base Command Line Interface. Provides some default functionality.
 *
 * To use this, in your concrete subclass, implement a main method. You must implement buildOptions and processOptions
 * to handle any application-specific options (they can be no-ops).
 *
 * To facilitate testing of your subclass, your main method must call a non-static 'doWork' method, that will be exposed
 * for testing. In that method call processCommandline. You should return any non-null return value from
 * processCommandLine.
 *
 * @author pavlidis
 */
@SuppressWarnings({ "unused", "WeakerAccess" }) // Possible external use
public abstract class AbstractCLI implements CLI {

    /**
     * Exit code used for a successful doWork execution.
     */
    public static final int SUCCESS = 0;
    /**
     * Exit code used for a failed doWork execution.
     */
    public static final int FAILURE = 1;
    /**
     * Exit code used for a successful doWork execution that resulted in failed error objects.
     */
    public static final int FAILURE_FROM_ERROR_OBJECTS = 1;

    public static final String FOOTER = "The Gemma project, Copyright (c) 2007-2021 University of British Columbia.";
    protected static final String AUTO_OPTION_NAME = "auto";
    protected static final String THREADS_OPTION = "threads";
    protected static final Log log = LogFactory.getLog( AbstractCLI.class );
    private static final String LOGGER_OPTION = "logger";
    private static final int DEFAULT_PORT = 3306;
    private static final String HEADER = "Options:";
    private static final String HOST_OPTION = "H";
    private static final String PORT_OPTION = "P";
    private static final String VERBOSITY_OPTION = "v";

    /* support for convenience options */
    private final String DEFAULT_HOST = "localhost";
    private final Map<String, Level> originalLoggingLevels = new HashMap<>();
    /**
     * Automatically identify which entities to run the tool on. To enable call addAutoOption.
     */
    protected boolean autoSeek = false;
    /**
     * The event type to look for the lack of, when using auto-seek.
     */
    protected Class<? extends AuditEventType> autoSeekEventType = null;
    /**
     * Date used to identify which entities to run the tool on (e.g., those which were run less recently than mDate). To
     * enable call addDateOption.
     */
    protected String mDate = null;
    protected int numThreads = 1;
    protected String host = DEFAULT_HOST;
    protected int port = AbstractCLI.DEFAULT_PORT;
    private ExecutorService executorService;

    // hold the results of the command execution
    // needs to be concurrently modifiable and kept in-order
    private final List<BatchProcessingResult> errorObjects = Collections.synchronizedList( new ArrayList<BatchProcessingResult>() );
    private final List<BatchProcessingResult> successObjects = Collections.synchronizedList( new ArrayList<BatchProcessingResult>() );

    /**
     * Run the command.
     *
     * Parse and process CLI arguments, invoke the command doWork implementation, and print basic statistics about time
     * usage.
     *
     * @param args arguments Arguments to pass to {@link #processCommandLine(Options, String[])}
     * @return Exit code intended to be used with {@link System#exit(int)} to indicate a success or failure to the
     * end-user. Any exception raised by doWork results in a value of {@link #FAILURE}, and any error set in the
     * internal error objects will result in a value of {@link #FAILURE_FROM_ERROR_OBJECTS}.
     */
    @Override
    public int executeCommand( String[] args ) {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Options options = new Options();
            buildStandardOptions( options );
            buildOptions( options );
            CommandLine commandLine = processCommandLine( options, args );
            // check if -h/--help is provided before pursuing option processing
            if ( commandLine.hasOption( 'h' ) ) {
                printHelp( options );
                return SUCCESS;
            }
            processStandardOptions( commandLine );
            processOptions( commandLine );
            doWork();
            return errorObjects.isEmpty() ? SUCCESS : FAILURE_FROM_ERROR_OBJECTS;
        } catch ( Exception e ) {
            log.error( getCommandName() + " failed.", e );
            return FAILURE;
        } finally {
            // always summarize processing, even if an error is thrown
            summarizeProcessing();
            resetLogging();
            AbstractCLI.log.info( "Elapsed time: " + watch.getTime() / 1000 + " seconds." );
        }
    }

    /**
     * You must implement the handling for this option.
     * @param options
     */
    @SuppressWarnings("static-access")
    protected void addAutoOption( Options options ) {
        Option autoSeekOption = Option.builder( AUTO_OPTION_NAME )
                .desc( "Attempt to process entities that need processing based on workflow criteria." )
                .build();

        options.addOption( autoSeekOption );
    }

    @SuppressWarnings("static-access")
    protected void addDateOption( Options options ) {
        Option dateOption = Option.builder( "mdate" ).hasArg().desc(
                        "Constrain to run only on entities with analyses older than the given date. "
                                + "For example, to run only on entities that have not been analyzed in the last 10 days, use '-10d'. "
                                + "If there is no record of when the analysis was last run, it will be run." )
                .build();

        options.addOption( dateOption );
    }

    /**
     * Convenience method to add a standard pair of options to intake a host name and port number. *
     *
     * @param options
     * @param hostRequired Whether the host name is required
     * @param portRequired Whether the port is required
     */
    @SuppressWarnings("static-access")
    protected void addHostAndPortOptions( Options options, boolean hostRequired, boolean portRequired ) {
        Option hostOpt = Option.builder( HOST_OPTION ).argName( "host name" ).longOpt( "host" ).hasArg()
                .desc( "Hostname to use (Default = " + DEFAULT_HOST + ")" )
                .build();

        hostOpt.setRequired( hostRequired );

        Option portOpt = Option.builder( PORT_OPTION ).argName( "port" ).longOpt( "port" ).hasArg()
                .desc( "Port to use on host (Default = " + AbstractCLI.DEFAULT_PORT + ")" )
                .build();

        portOpt.setRequired( portRequired );

        options.addOption( hostOpt );
        options.addOption( portOpt );
    }

    /**
     * Convenience method to add an option for parallel processing option.
     * @param options
     */
    @SuppressWarnings("static-access")
    protected void addThreadsOption( Options options ) {
        Option threadsOpt = Option.builder( THREADS_OPTION ).argName( "numThreads" ).hasArg()
                .desc( "Number of threads to use for batch processing." )
                .build();
        options.addOption( threadsOpt );
    }

    /**
     * Build option implementation.
     *
     * Implement this method to add options to your command line, using the OptionBuilder.
     *
     * This is called right after {@link #buildStandardOptions(Options)} so the options will be added after standard options.
     * @param options
     */
    protected abstract void buildOptions( Options options );

    @SuppressWarnings("static-access")
    protected void buildStandardOptions( Options options ) {
        AbstractCLI.log.debug( "Creating standard options" );
        Option helpOpt = new Option( "h", "help", false, "Print this message" );
        Option testOpt = new Option( "testing", false, "Use the test environment" );
        Option logOpt = new Option( "v", "verbosity", true,
                "Set verbosity level for all loggers (0=silent, 5=very verbose; default is custom, see log4j.properties)" );
        Option otherLogOpt = Option.builder().longOpt( "logger" ).hasArg().argName( "logger" ).desc( "Configure a specific logger verbosity"
                        + "For example, '--logger ubic.gemma=5' or --logger log4j.logger.org.hibernate.SQL=5" )
                .build();

        options.addOption( otherLogOpt );
        options.addOption( logOpt );
        options.addOption( helpOpt );
        options.addOption( testOpt );

    }

    /**
     * Command line implementation.
     *
     * This is called after {@link #buildOptions(Options)} and {@link #processOptions(CommandLine)}, so the implementation can assume that
     * all its arguments have already been initialized.
     *
     * @throws Exception in case of unrecoverable failure, an exception is thrown and will result in a {@link #FAILURE}
     *                   exit code, otherwise use {@link #addErrorObject}
     */
    protected abstract void doWork() throws Exception;

    protected final double getDoubleOptionValue( CommandLine commandLine, char option ) {
        try {
            return Double.parseDouble( commandLine.getOptionValue( option ) );
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( this.invalidOptionString( commandLine, "" + option ) + ", not a valid double", e );
        }
    }

    protected final double getDoubleOptionValue( CommandLine commandLine, String option ) {
        try {
            return Double.parseDouble( commandLine.getOptionValue( option ) );
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( this.invalidOptionString( commandLine, option ) + ", not a valid double", e );
        }
    }

    protected final String getFileNameOptionValue( CommandLine commandLine, char c ) {
        String fileName = commandLine.getOptionValue( c );
        File f = new File( fileName );
        if ( !f.canRead() ) {
            throw new RuntimeException( this.invalidOptionString( commandLine, "" + c ) + ", cannot read from file" );
        }
        return fileName;
    }

    protected final String getFileNameOptionValue( CommandLine commandLine, String c ) {
        String fileName = commandLine.getOptionValue( c );
        File f = new File( fileName );
        if ( !f.canRead() ) {
            throw new RuntimeException( this.invalidOptionString( commandLine, "" + c ) + ", cannot read from file" );
        }
        return fileName;
    }

    protected final int getIntegerOptionValue( CommandLine commandLine, char option ) {
        try {
            return Integer.parseInt( commandLine.getOptionValue( option ) );
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( this.invalidOptionString( commandLine, "" + option ) + ", not a valid integer", e );
        }
    }

    protected final int getIntegerOptionValue( CommandLine commandLine, String option ) {
        try {
            return Integer.parseInt( commandLine.getOptionValue( option ) );
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( this.invalidOptionString( commandLine, option ) + ", not a valid integer", e );
        }
    }

    protected Date getLimitingDate() {
        Date skipIfLastRunLaterThan = null;
        if ( StringUtils.isNotBlank( mDate ) ) {
            skipIfLastRunLaterThan = DateUtil.getRelativeDate( new Date(), mDate );
            AbstractCLI.log.info( "Analyses will be run only if last was older than " + skipIfLastRunLaterThan );
        }
        return skipIfLastRunLaterThan;
    }

    protected void printHelp( Options options ) {
        HelpFormatter h = new HelpFormatter();
        h.setWidth( 150 );
        h.printHelp( this.getCommandName() + " [options]", this.getShortDesc() + "\n" + AbstractCLI.HEADER, options,
                AbstractCLI.FOOTER );
    }

    /**
     * This must be called in your main method. It triggers parsing of the command line and processing of the options.
     * Check the error code to decide whether execution of your program should proceed.
     *
     * @param args args
     * @return Exception; null if nothing went wrong.
     */
    private final CommandLine processCommandLine( Options options, String[] args ) throws Exception {
        /* COMMAND LINE PARSER STAGE */
        DefaultParser parser = new DefaultParser();
        String appVersion = Settings.getAppVersion();
        if ( appVersion == null )
            appVersion = "?";
        System.err.println( "Gemma version " + appVersion );

        if ( args == null ) {
            this.printHelp( options );
            throw new Exception( "No arguments" );
        }

        try {
            return parser.parse( options, args );
        } catch ( ParseException e ) {
            if ( e instanceof MissingOptionException ) {
                System.err.println( "Required option(s) were not supplied: " + e.getMessage() );
            } else if ( e instanceof AlreadySelectedException ) {
                System.err.println( "The option(s) " + e.getMessage() + " were already selected" );
            } else if ( e instanceof MissingArgumentException ) {
                System.err.println( "Missing argument: " + e.getMessage() );
            } else if ( e instanceof UnrecognizedOptionException ) {
                System.err.println( "Unrecognized option: " + e.getMessage() );
            } else {
                e.printStackTrace();
            }

            this.printHelp( options );

            if ( AbstractCLI.log.isDebugEnabled() ) {
                AbstractCLI.log.debug( e );
            }

            throw e;
        }
    }

    /**
     * Process command line options.
     *
     * Implement this to provide processing of options. It is called after {@link #buildOptions(Options)} and right before
     * {@link #doWork()}.
     *
     * @throws Exception in case of unrecoverable failure (i.e. missing option or invalid value), an exception can be
     *                   raised and will result in an exit code of {@link #FAILURE}.
     * @param commandLine
     */
    protected abstract void processOptions( CommandLine commandLine ) throws Exception;

    /**
     * This is needed for CLIs that run in tests, so the logging settings get reset.
     */
    protected void resetLogging() {
        LoggerContext context = ( LoggerContext ) LogManager.getContext( false );
        Configuration config = context.getConfiguration();
        for ( String loggerName : originalLoggingLevels.keySet() ) {
            config.getLoggerConfig( loggerName ).setLevel( originalLoggingLevels.get( loggerName ) );
        }
        context.updateLoggers();
    }

    /**
     * Add a success object to indicate success in a batch processing.
     *
     * This is further used in {@link #summarizeProcessing()} to summarize the execution of the command.
     *
     * @param successObject object that was processed
     * @param message       success message
     */
    protected void addSuccessObject( Object successObject, String message ) {
        successObjects.add( new BatchProcessingResult( successObject, message ) );
        log.info( successObject + ": " + message );
    }

    /**
     * Add an error object with a stacktrace to indicate failure in a batch processing.
     *
     * This is further used in {@link #summarizeProcessing()} to summarize the execution of the command.
     *
     * This is intended to be used when an {@link Exception} is caught.
     *
     * @param errorObject object that was processed
     * @param message     error message
     * @param throwable   throwable to produce a stacktrace
     */
    protected void addErrorObject( Object errorObject, String message, Throwable throwable ) {
        errorObjects.add( new BatchProcessingResult( errorObject, message ) );
        log.error( errorObject + ": " + message, throwable );
    }

    /**
     * Add an error object to indicate failure in a batch processing.
     *
     * This is further used in {@link #summarizeProcessing()} to summarize the execution of the command.
     */
    protected void addErrorObject( Object errorObject, String message ) {
        errorObjects.add( new BatchProcessingResult( errorObject, message ) );
        log.error( errorObject + ": " + message );
    }

    /**
     * Print out a summary of what the program did. Useful when analyzing lists of experiments etc. Use the
     * 'successObjects' and 'errorObjects'
     */
    private void summarizeProcessing() {
        if ( successObjects.size() > 0 ) {
            StringBuilder buf = new StringBuilder();
            buf.append( "\n---------------------\nSuccessfully processed " ).append( successObjects.size() )
                    .append( " objects:\n" );
            for ( BatchProcessingResult result : successObjects ) {
                buf.append( "Success\t" )
                        .append( result.source ).append( ": " )
                        .append( result.message ).append( "\n" );
            }
            buf.append( "---------------------\n" );

            AbstractCLI.log.info( buf );
        }

        if ( errorObjects.size() > 0 ) {
            StringBuilder buf = new StringBuilder();
            buf.append( "\n---------------------\nErrors occurred during the processing of " )
                    .append( errorObjects.size() ).append( " objects:\n" );
            for ( BatchProcessingResult result : errorObjects ) {
                buf.append( "Error\t" )
                        .append( result.source ).append( ": " )
                        .append( result.message ).append( "\n" );
            }
            buf.append( "---------------------\n" );
            AbstractCLI.log.error( buf );
        }
    }

    /**
     * Execute batch tasks using a preconfigured {@link ExecutorService} and return all the resulting tasks results.
     *
     * @param tasks
     * @throws InterruptedException
     */
    protected <T> List<T> executeBatchTasks( Collection<? extends Callable<T>> tasks ) throws InterruptedException {
        List<Future<T>> futures = executorService.invokeAll( tasks );
        List<T> futureResults = new ArrayList<>( futures.size() );
        for ( Future<T> future : futures ) {
            try {
                futureResults.add( future.get() );
            } catch ( ExecutionException | InterruptedException e ) {
                addErrorObject( null, "Batch task failed.", e );
            }
        }
        return futureResults;
    }

    private void configureAllLoggers( int v ) {
        LoggerContext context = ( LoggerContext ) LogManager.getContext( false );
        Configuration config = context.getConfiguration();

        Level newLevel = toLog4jLevel( v );

        // configure all individual loggers
        for ( Map.Entry<String, LoggerConfig> e : config.getLoggers().entrySet() ) {
            configureLogging( e.getKey(), e.getValue(), newLevel );
        }

        // This causes all Loggers to refetch information from their LoggerConfig.
        context.updateLoggers();
    }

    /**
     * Set up logging according to the user-selected (or default) verbosity level.
     */
    private void configureLogging( String loggerName, LoggerConfig loggerConfig, Level newLevel ) {
        if ( loggerName.equals( loggerConfig.getName() ) ) {
            AbstractCLI.log.info( String.format( "Setting logging level of '%s' to %s.", loggerConfig.getName(), newLevel ) );
        } else {
            // effective logger differs, this means that there no configuration
            LoggerContext context = ( LoggerContext ) LogManager.getContext( false );
            AbstractCLI.log.warn( String.format( "Setting logging level of '%s' to %s since there's no logger named '%s'. To prevent this, add an entry for '%s' in log4j.properties.",
                    loggerConfig.getName(), newLevel, loggerName, loggerName ) );
        }
        if ( !originalLoggingLevels.containsKey( loggerConfig.getName() ) ) {
            originalLoggingLevels.put( loggerConfig.getName(), loggerConfig.getLevel() );
        }
        loggerConfig.setLevel( newLevel );
    }

    private String invalidOptionString( CommandLine commandLine, String option ) {
        return "Invalid value '" + commandLine.getOptionValue( option ) + " for option " + option;
    }

    /**
     * Somewhat annoying: This causes subclasses to be unable to safely use 'h', 'p', 'u' and 'P' etc for their own
     * purposes.
     */
    protected void processStandardOptions( CommandLine commandLine ) {

        if ( commandLine.hasOption( AbstractCLI.HOST_OPTION ) ) {
            this.host = commandLine.getOptionValue( AbstractCLI.HOST_OPTION );
        } else {
            this.host = DEFAULT_HOST;
        }

        if ( commandLine.hasOption( AbstractCLI.PORT_OPTION ) ) {
            this.port = this.getIntegerOptionValue( commandLine, AbstractCLI.PORT_OPTION );
        } else {
            this.port = AbstractCLI.DEFAULT_PORT;
        }

        if ( commandLine.hasOption( AbstractCLI.VERBOSITY_OPTION ) ) {
            int verbosity = this.getIntegerOptionValue( commandLine, AbstractCLI.VERBOSITY_OPTION );
            if ( verbosity < 0 || verbosity > 5 ) {
                throw new IllegalArgumentException( "Verbosity must be from 0 to 5." );
            }
            this.configureAllLoggers( verbosity );
        }

        if ( commandLine.hasOption( AbstractCLI.LOGGER_OPTION ) ) {
            LoggerContext context = ( LoggerContext ) LogManager.getContext( false );
            Configuration config = context.getConfiguration();
            for ( String value : commandLine.getOptionValues( AbstractCLI.LOGGER_OPTION ) ) {
                String[] vals = value.split( "=" );
                if ( vals.length != 2 )
                    throw new IllegalArgumentException( "Logging value must in format [loggerName]=[value]." );
                try {
                    configureLogging( vals[0], config.getLoggerConfig( vals[0] ), toLog4jLevel( Integer.parseInt( vals[1] ) ) );
                } catch ( NumberFormatException e ) {
                    throw new IllegalArgumentException( "Logging level must be an integer between 0 and 5." );
                }
            }
            context.updateLoggers();
        }

        if ( commandLine.hasOption( "mdate" ) ) {
            this.mDate = commandLine.getOptionValue( "mdate" );
        }

        if ( this.numThreads < 1 ) {
            throw new IllegalArgumentException( "Number of threads must be greater than 1." );
        }
        this.executorService = new ForkJoinPool( this.numThreads );
    }

    private static Level toLog4jLevel( int level ) {
        switch ( level ) {
            case 0:
                return Level.OFF;
            case 1:
                return Level.FATAL;
            case 2:
                return Level.ERROR;
            case 3:
                return Level.WARN;
            case 4:
                return Level.INFO;
            case 5:
                return Level.DEBUG;
            default:
                throw new IllegalArgumentException( "Verbosity must be from 0 to 5" );
        }
    }

    /**
     * Represents an individual result in a batch processing.
     */
    private static class BatchProcessingResult {
        private Object source;
        private String message;
        private Throwable throwable;

        public BatchProcessingResult( Object source, String message ) {
            this.source = source;
            this.message = message;
        }

        public BatchProcessingResult( Object source, String message, Throwable throwable ) {
            this.source = source;
            this.message = message;
            this.throwable = throwable;
        }
    }
}
