/*
 * The Gemma project
 * 
 * Copyright (c) 2005 Columbia University
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
package edu.columbia.gemma.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.columbia.gemma.genome.biosequence.BioSequence;
import edu.columbia.gemma.genome.sequenceAnalysis.BlatResult;
import edu.columbia.gemma.loader.genome.BlatResultParser;

/**
 * Class to manage the gfServer and run BLAT searches.
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class Blat {

    private static final Log log = LogFactory.getLog( Blat.class );
    private static String os = System.getProperty( "os.name" ).toLowerCase();
    static {
        if ( !os.toLowerCase().startsWith( "windows" ) ) {
            try {
                log.info( "Loading gfClient library, looking in " + System.getProperty( "java.library.path" ) );
                System.loadLibrary( "Blat" );
                log.info( "Loaded Blat library successfully" );
            } catch ( UnsatisfiedLinkError e ) {
                log.error( e, e );
                throw new ExceptionInInitializerError( "Unable to locate or load the Blat native library: "
                        + e.getMessage() );
            }
        }
    }
    private boolean doShutdown = true;
    private String gfClientExe = "C:/bin/cygwini686/gfClient.exe";
    private String gfServerExe = "C:/bin/cygwini686/gfServer.exe";
    private String host = "localhost";
    private String port = "177777";
    private String seqDir = "/";

    private String seqFiles;

    private Process serverProcess;

    /**
     * Create a blat object with settings read from the config file.
     */
    public Blat() {
        try {
            init();
        } catch ( ConfigurationException e ) {
            throw new RuntimeException( "Could not load configuration", e );
        }
    }

    /**
     * @param host
     * @param port
     * @param seqDir
     */
    public Blat( String host, String port, String seqDir ) {

        if ( host == null || port == null || seqDir == null )
            throw new IllegalArgumentException( "All values must be non-null" );
        this.host = host;
        this.port = port;
        this.seqDir = seqDir;
    }

    /**
     * @return Returns the gfClientExe.
     */
    public String getGfClientExe() {
        return this.gfClientExe;
    }

    /**
     * @return Returns the gfServerExe.
     */
    public String getGfServerExe() {
        return this.gfServerExe;
    }

    /**
     * @return Returns the host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * @return Returns the port.
     */
    public String getPort() {
        return this.port;
    }

    /**
     * @return Returns the seqDir.
     */
    public String getSeqDir() {
        return this.seqDir;
    }

    /**
     * @return Returns the seqFiles.
     */
    public String getSeqFiles() {
        return this.seqFiles;
    }

    /**
     * Run a BLAT search using the gfClient.
     * 
     * @param b
     * @return Collection of BlatResult objects.
     * @throws IOException
     */
    public Collection<Object> GfClient( BioSequence b ) throws IOException {
        assert seqDir != null;
        assert port != null;
        // write the sequence to a temporary file.
        File querySequenceFile = File.createTempFile( "pattern", ".fa" );

        BufferedWriter out = new BufferedWriter( new FileWriter( querySequenceFile ) );
        out.write( ">" + b.getName() + "\n" + b.getSequence() );
        out.close();
        log.info( "Wrote sequence to " + querySequenceFile.getPath() );

        String outputPath = getTmpPslFilePath();

        Collection<Object> results = gfClient( querySequenceFile, outputPath );

        // clean up.
        querySequenceFile.delete();
        ( new File( outputPath ) ).delete();
        return results;

    }

    /**
     * @param sequences
     * @return map of the input sequence names to a corresponding collection of blat result(s)
     * @throws IOException
     */
    public Map<String, Collection<BlatResult>> GfClient( Collection<BioSequence> sequences ) throws IOException {
        Map<String, Collection<BlatResult>> results = new HashMap<String, Collection<BlatResult>>();

        File querySequenceFile = File.createTempFile( "pattern", ".fa" );
        querySequenceFile.deleteOnExit();
        BufferedWriter out = new BufferedWriter( new FileWriter( querySequenceFile ) );

        for ( BioSequence b : sequences ) {
            out.write( ">" + b.getName() + "\n" + b.getSequence() );
        }
        out.close();

        String outputPath = getTmpPslFilePath();

        gfClient( querySequenceFile, outputPath );

        Collection<Object> rawResults = this.processPsl( outputPath );
        for ( Object object : rawResults ) {
            assert object instanceof BlatResult;
            String name = ( ( BlatResult ) object ).getQuerySequence().getName();

            if ( !results.containsKey( name ) ) {
                results.put( name, new HashSet<BlatResult>() );
            }

            results.get( name ).add( ( BlatResult ) object );
        }

        querySequenceFile.delete();
        return results;
    }

    /**
     * Start the server, if the port isn't already being used. If the port is in use, we assume it is a gfServer.
     */
    public void startServer() throws IOException {
        try {
            new Socket( host, Integer.parseInt( port ) );
            log.info( "There is already a server on port " + port );
            this.doShutdown = false;
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( "Invalid port " + port, e );
        } catch ( UnknownHostException e ) {
            throw new RuntimeException( "Unknown host " + host, e );
        } catch ( IOException e ) {
            String cmd = this.getGfServerExe() + " start " + this.getHost() + " " + this.getPort() + " "
                    + this.getSeqFiles();
            log.info( "Starting gfServer with command " + cmd );
            this.serverProcess = Runtime.getRuntime().exec( cmd, null, new File( this.getSeqDir() ) );
        }
    }

    /**
     * Rather forcibly stop the gfServer, if it was started by this.
     */
    public void stopServer() {
        if ( false && !doShutdown ) {
            return;
        }
        log.info( "Shutting down gfServer" );

        if ( serverProcess == null ) return;
        serverProcess.destroy();

        // this doesn't work.
        // Process server = Runtime.getRuntime().exec(
        // this.getGfServerExe() + " stop " + this.getHost() + " " + this.getPort() );
        //
        // //
        // try {
        // server.waitFor();
        // int exit = server.exitValue();
        // log.info( "Server shut down with exit value " + exit );
        // } catch ( InterruptedException e ) {
        // 
        // e.printStackTrace();
        // }

    }

    /**
     * Run a gfClient query, using a call to exec(). This runs in a separate thread.
     * 
     * @param querySequenceFile
     * @param outputPath
     * @return
     */
    private Collection<Object> execGfClient( File querySequenceFile, String outputPath ) throws IOException {
        final String cmd = gfClientExe + " -nohead " + host + " " + port + " " + seqDir + " "
                + querySequenceFile.getAbsolutePath() + " " + outputPath;

        FutureTask<Process> future = new FutureTask<Process>( new Callable<Process>() {
            public Process call() throws IOException {
                try {
                    Process run = Runtime.getRuntime().exec( cmd );
                    try {
                        run.waitFor();
                    } catch ( InterruptedException e ) {
                        ;
                    }
                    return run;
                } catch ( IOException e ) {
                    String message = e.getMessage();
                    if ( message.startsWith( "CreateProcess" ) && message.indexOf( "error=" ) > 0 ) {
                        int errorCode = Integer.parseInt( message.substring( e.getMessage().lastIndexOf( "error=" )
                                + ( new String( "error=" ) ).length() ) );

                        if ( errorCode == 2 )
                            throw new IOException( "Could not locate executable to run command (Error " + errorCode
                                    + ") " + cmd );

                        throw new IOException( "Error " + errorCode + ") " + cmd );
                    }
                    return null;
                }
            }
        } );

        Executors.newSingleThreadExecutor().execute( future );
        while ( !future.isDone() ) {
            try {
                Thread.sleep( 10 );
            } catch ( InterruptedException ie ) {
                ;
            }
        }

        try {

            if ( future.get() == null ) {
                log.error( "GfClient Failed" );
                throw new RuntimeException( "GfClient Failed" );
            } else if ( future.get().exitValue() != 0 ) {

                InputStream result = future.get().getErrorStream();
                BufferedReader br = new BufferedReader( new InputStreamReader( result ) );
                String l = null;
                StringBuffer buf = new StringBuffer();
                while ( ( l = br.readLine() ) != null ) {
                    buf.append( l + "\n" );
                }

                log.error( "GfClient Error : " + future.get().exitValue() );
                log.error( "Command was '" + cmd + "'" );
                log.error( "Error message was: " + buf );
                throw new RuntimeException( "GfClient Error : " + future.get().exitValue() );
            } else {
                log.info( "GfClient Success" );
            }
        } catch ( ExecutionException e ) {
            log.error( e, e );
            throw new RuntimeException( "GfClient Failed", e );
        } catch ( InterruptedException e ) {
            log.error( e, e );
            throw new RuntimeException( "GfClient Failed (Interrupted)", e );
        }
        return processPsl( outputPath );
    }

    /**
     * Get a temporary file name.
     * 
     * @throws IOException
     */
    private String getTmpPslFilePath() throws IOException {
        return File.createTempFile( "pattern", ".psl" ).getPath();
    }

    /**
     * @param querySequenceFile
     * @param outputPath
     * @return processed results.
     * @throws IOException
     */
    private Collection<Object> gfClient( File querySequenceFile, String outputPath ) throws IOException {
        if ( !os.startsWith( "windows" ) ) return jniGfClientCall( querySequenceFile, outputPath );

        return execGfClient( querySequenceFile, outputPath );
    }

    /**
     * @param host
     * @param port
     * @param seqDir
     * @param inputFile
     * @param outputFile
     */
    private native void GfClientCall( String h, String p, String dir, String input, String output );

    /**
     * @throws ConfigurationException
     */
    private void init() throws ConfigurationException {

        URL universalConfigFileLocation = ConfigurationUtils.locate( "Gemma.properties" );
        if ( universalConfigFileLocation == null ) throw new ConfigurationException( "Cannot find config file" );
        Configuration universalConfig = new PropertiesConfiguration( universalConfigFileLocation );

        URL userSpecificConfigFileLocation = ConfigurationUtils.locate( "build.properties" );
        Configuration userConfig = new PropertiesConfiguration( userSpecificConfigFileLocation );

        if ( userConfig == null ) {
            this.host = universalConfig.getString( "gfClient.host" );
            this.seqDir = universalConfig.getString( "gfClient.seqDir" );
            this.seqFiles = universalConfig.getString( "gfClient.seqFiles" );
            this.gfClientExe = universalConfig.getString( "gfClient.exe" );
            this.gfServerExe = universalConfig.getString( "gfServer.exe" );
        }

        try {
            this.port = userConfig.getString( "gfClient.port" );
        } catch ( NoSuchElementException e ) {
            this.port = universalConfig.getString( "gfClient.port" );
        }
        try {
            this.host = userConfig.getString( "gfClient.host" );
        } catch ( NoSuchElementException e ) {
            this.host = universalConfig.getString( "gfClient.host" );
        }
        try {
            this.seqDir = userConfig.getString( "gfClient.seqDir" );
        } catch ( NoSuchElementException e ) {
            this.seqDir = universalConfig.getString( "gfClient.seqDir" );
        }
        try {
            this.gfClientExe = userConfig.getString( "gfClient.exe" );
        } catch ( NoSuchElementException e ) {
            this.gfClientExe = universalConfig.getString( "gfClient.exe" );
        }
        try {
            this.gfServerExe = userConfig.getString( "gfServer.exe" );
        } catch ( NoSuchElementException e ) {
            this.gfServerExe = universalConfig.getString( "gfServer.exe" );
        }
        try {
            this.seqFiles = userConfig.getString( "gfClient.seqFiles" );
        } catch ( NoSuchElementException e ) {
            this.seqFiles = universalConfig.getString( "gfClient.seqFiles" );
        }

    }

    /**
     * @param querySequenceFile
     * @param outputPath
     * @return processed results.
     */
    private Collection<Object> jniGfClientCall( File querySequenceFile, String outputPath ) throws IOException {
        try {
            this.GfClientCall( host, port, seqDir, querySequenceFile.getPath(), outputPath );
        } catch ( UnsatisfiedLinkError e ) {
            log.error( e, e );
            // throw new RuntimeException( "Failed call to native gfClient: " + e.getMessage() );
            log.info( "Falling back on exec()" );
            this.execGfClient( querySequenceFile, outputPath );
        }
        return this.processPsl( outputPath );
    }

    /**
     * @param outputPath
     * @return processed results.
     */
    private Collection<Object> processPsl( String outputPath ) throws IOException {
        BlatResultParser brp = new BlatResultParser();
        brp.parse( outputPath );
        return brp.getResults();
    }

}
