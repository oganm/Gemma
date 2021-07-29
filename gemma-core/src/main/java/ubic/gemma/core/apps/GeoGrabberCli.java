/*
 * The Gemma project
 *
 * Copyright (c) 2010 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.core.apps;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import ubic.gemma.core.apps.GemmaCLI.CommandGroup;
import ubic.gemma.core.loader.expression.geo.model.GeoRecord;
import ubic.gemma.core.loader.expression.geo.service.GeoBrowser;
import ubic.gemma.core.util.AbstractCLI;
import ubic.gemma.core.util.AbstractCLIContextCLI;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scans GEO for experiments that are not in Gemma, subject to some filtering criteria, outputs to a file for further
 * screening. See https://github.com/PavlidisLab/Gemma/issues/169
 *
 * @author paul
 */
public class GeoGrabberCli extends AbstractCLIContextCLI {

    private Date dateLimit;
    private String gseLimit;
    private String outputFileName = "";

    @Override
    public CommandGroup getCommandGroup() {
        return CommandGroup.ANALYSIS;
    }

    @Override
    public String getCommandName() {
        return "listGEOData";
    }

    @Override
    protected void buildOptions( Options options ) {
        options.addOption(
                Option.builder( "date" ).longOpt( null ).desc( "A release date to stop the search in format yyyy.MM.dd e.g. 2010.01.12" )
                        .argName( "date limit" ).hasArg().build() );
        options.addOption(
                Option.builder( "gselimit" ).longOpt( null ).desc( "A GSE at which to stop the search" ).argName( "GSE identifier" ).hasArg()
                        .build() );

        options.addOption( Option.builder( "output" ).desc( "File path for output (required)" ).argName( "path" ).hasArg().required().build() );

    }

    @Override
    protected void processOptions( CommandLine commandLine ) throws Exception {
        if ( commandLine.hasOption( "date" ) ) {
            try {
                this.dateLimit = DateUtils.parseDate( commandLine.getOptionValue( "date" ), new String[] { "yyyy.MM.dd" } );
            } catch ( ParseException e ) {
                throw new IllegalArgumentException( "Could not parse date " + commandLine.getOptionValue( "date" ) );
            }
        }
        if ( commandLine.hasOption( "gselimit" ) ) {
            this.gseLimit = commandLine.getOptionValue( "gselimit" );
        }

        if ( !commandLine.hasOption( "output" ) ) {
            throw new IllegalArgumentException( "You must provide an output file name" );
        }
        this.outputFileName = commandLine.getOptionValue( "output" );
    }

    @Override
    protected void doWork() throws Exception {
        Set<String> seen = new HashSet<>();
        GeoBrowser gbs = new GeoBrowser();
        ExpressionExperimentService ees = this.getBean( ExpressionExperimentService.class );
        TaxonService ts = this.getBean( TaxonService.class );
        ArrayDesignService ads = this.getBean( ArrayDesignService.class );

        int start = 0;
        int numfails = 0;
        int chunksize = 100;

        assert outputFileName != null;
        log.info( "Writing output to " + outputFileName );
        File outputFile = new File( outputFileName );

        if ( outputFile.exists() ) {
            log.warn( "Overwriting existing file ..." );
            Thread.sleep( 500 );
        }

        outputFile.createNewFile();

        log.info( "Writing results to " + outputFileName );

        try (Writer os = new FileWriter( outputFile )) {

            DateFormat dateFormat = new SimpleDateFormat( "yyyy.MM.dd" );

            os.append( "Acc\tReleaseDate\tTaxa\tPlatforms\tAllPlatformsInGemma\tNumSamples\tType\tSuperSeries\tSubSeriesOf"
                    + "\tPubMed\tTitle\tSummary\tMeSH\tSampleTerms\n" );
            os.flush();

            int numProcessed = 0;
            int numUsed = 0;
            boolean keepGoing = true;

            Collection<String> allowedTaxa = new HashSet<>();
            for ( Taxon t : ts.loadAll() ) {
                allowedTaxa.add( t.getScientificName() );
            }
            log.info( allowedTaxa.size() + " Taxa considered usable" );

            while ( keepGoing ) {

                log.debug( "Searching from " + start + ", seeking " + chunksize + " records" );

                List<GeoRecord> recs = gbs.getGeoRecordsBySearchTerm( null, start, chunksize, true /* details */, allowedTaxa );

                if ( recs.isEmpty() ) {
                    AbstractCLI.log.info( "No records received for start=" + start );
                    numfails++;

                    if ( numfails > 10 ) {
                        AbstractCLI.log.info( "Giving up" );
                        break;
                    }

                    try {
                        Thread.sleep( 500 );
                    } catch ( InterruptedException ignored ) {
                    }

                    start++; // increment until we hit something
                    continue;
                }

                log.debug( "Retrieved " + recs.size() ); // we skip ones that are not using taxa of interest
                start += chunksize; // this seems the best way to avoid hitting them more than once.

                for ( GeoRecord geoRecord : recs ) {

                    numProcessed++;

                    if ( numProcessed % 50 == 0 ) {
                        System.err.println( "Processed " + numProcessed + " GEO records, retained " + numUsed + " so far" );
                    }

                    if ( this.dateLimit != null && dateLimit.after( geoRecord.getReleaseDate() ) ) {
                        log.info( "Stopping as reached date limit" );
                        keepGoing = false;
                        break;
                    }

                    if ( this.gseLimit != null && geoRecord.getGeoAccession().equals( this.gseLimit ) ) {
                        log.info( "Stopping as have reached " + gseLimit );
                        keepGoing = false;
                        break;
                    }

                    if ( seen.contains( geoRecord.getGeoAccession() ) ) {
                        log.info( "Already saw " + geoRecord.getGeoAccession() ); // this would be a bug IMO, want to avoid!
                        continue;
                    }

                    if ( ees.isBlackListed( geoRecord.getGeoAccession() ) ) {
                        continue;
                    }

                    if ( ees.findByShortName( geoRecord.getGeoAccession() ) != null ) {
                        continue;
                    }

                    if ( !ees.findByAccession( geoRecord.getGeoAccession() ).isEmpty() ) {
                        continue;
                    }

                    boolean platformIsInGemma = true;
                    boolean anyNonBlacklistedPlatforms = false;
                    String[] platforms = geoRecord.getPlatform().split( ";" );
                    for ( String p : platforms ) {
                        if ( ads.findByShortName( p ) == null ) {
                            platformIsInGemma = false;
                            break;
                        }
                        if ( !ees.isBlackListed( p ) ) {
                            anyNonBlacklistedPlatforms = true;
                        }
                    }

                    // we skip if all the platforms for the GSE are blacklisted
                    if ( !anyNonBlacklistedPlatforms ) {
                        continue;
                    }

                    os.write(
                            geoRecord.getGeoAccession()
                                    + "\t" + dateFormat.format( geoRecord.getReleaseDate() )
                                    + "\t" + StringUtils.join( geoRecord.getOrganisms(), "," )
                                    + "\t" + geoRecord.getPlatform()
                                    + "\t" + platformIsInGemma
                                    + "\t" + geoRecord.getNumSamples()
                                    + "\t" + geoRecord.getSeriesType()
                                    + "\t" + geoRecord.isSuperSeries()
                                    + "\t" + geoRecord.getSubSeriesOf()
                                    + "\t" + geoRecord.getPubMedIds()
                                    + "\t" + geoRecord.getTitle()
                                    + "\t" + geoRecord.getSummary()
                                    + "\t" + geoRecord.getMeshHeadings()
                                    + "\t" + geoRecord.getSampleDetails() + "\n" );

                    seen.add( geoRecord.getGeoAccession() );

                    numUsed++;
                }
                os.flush();

            }
        }

    }

    @Override
    public String getShortDesc() {
        return "Grab information on GEO data sets not yet in the system";
    }
}
