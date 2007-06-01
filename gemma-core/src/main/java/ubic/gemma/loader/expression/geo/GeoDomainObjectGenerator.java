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
package ubic.gemma.loader.expression.geo;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.loader.expression.geo.fetcher.DatasetFetcher;
import ubic.gemma.loader.expression.geo.fetcher.PlatformFetcher;
import ubic.gemma.loader.expression.geo.fetcher.RawDataFetcher;
import ubic.gemma.loader.expression.geo.fetcher.SeriesFetcher;
import ubic.gemma.loader.expression.geo.model.GeoDataset;
import ubic.gemma.loader.expression.geo.model.GeoPlatform;
import ubic.gemma.loader.expression.geo.model.GeoSeries;
import ubic.gemma.loader.util.fetcher.Fetcher;
import ubic.gemma.loader.util.sdo.SourceDomainObjectGenerator;
import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.common.description.ExternalDatabase;
import ubic.gemma.model.common.description.LocalFile;

/**
 * Handle fetching and parsing GEO files.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeoDomainObjectGenerator implements SourceDomainObjectGenerator {

    protected static Log log = LogFactory.getLog( GeoDomainObjectGenerator.class.getName() );

    protected Fetcher datasetFetcher;
    protected Fetcher seriesFetcher;
    protected Fetcher platformFetcher;

    protected GeoFamilyParser parser;

    private boolean processPlatformsOnly;

    private boolean doSampleMatching = true;

    public void setDoSampleMatching( boolean doSampleMatching ) {
        this.doSampleMatching = doSampleMatching;
    }

    /**
     * 
     *
     */
    public GeoDomainObjectGenerator() {
        this.intialize();
    }

    /**
     * Initialize fetchers, clear out any data that was already generated by this Generator.
     */
    public void intialize() {
        parser = new GeoFamilyParser();
        datasetFetcher = new DatasetFetcher();
        seriesFetcher = new SeriesFetcher();
        platformFetcher = new PlatformFetcher();
    }

    /**
     * Process a data set and add it to the series
     * 
     * @param series
     * @param dataSetAccession
     */
    public void processDataSet( GeoSeries series, String dataSetAccession ) {
        log.info( "Processing " + dataSetAccession );
        GeoDataset gds = processDataSet( dataSetAccession );
        assert gds != null;
        series.addDataSet( gds );
        gds.getSeries().add( series );
    }

    /**
     * Process a data set from an accession values
     * 
     * @param dataSetAccession
     * @return A GeoDataset object
     */
    private GeoDataset processDataSet( String dataSetAccession ) {
        if ( !dataSetAccession.startsWith( "GDS" ) ) {
            throw new IllegalArgumentException( "Invalid GEO dataset accession " + dataSetAccession );
        }
        String dataSetPath = fetchDataSetToLocalFile( dataSetAccession );
        GeoDataset gds = null;
        try {
            gds = processDataSet( dataSetAccession, dataSetPath );

        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        return gds;
    }

    /**
     * @param geoAccession, either a GPL, GDS or GSE value.
     * @return If processPlatformsOnly is true, a collection of GeoPlatforms. Otherwise a Collection of series (just
     *         one). If the accession is a GPL then processPlatformsOnly is set to true and any sample data is ignored.
     */
    public Collection<?> generate( String geoAccession ) {
        log.info( "Generating objects for " + geoAccession + " using " + this.getClass().getSimpleName() );
        Collection<Object> result = new HashSet<Object>();
        if ( geoAccession.startsWith( "GPL" ) ) {
            GeoPlatform platform = processPlatform( geoAccession );
            result.add( platform );
        } else if ( geoAccession.startsWith( "GDS" ) ) {
            // common starting point.
            Collection<String> seriesAccessions = DatasetCombiner.findGSEforGDS( geoAccession );
            if ( processPlatformsOnly ) {
                return processSeriesPlatforms( seriesAccessions ); // FIXME, this is ugly.
            }
            log.info( geoAccession + " corresponds to " + seriesAccessions );
            for ( String seriesAccession : seriesAccessions ) {
                GeoSeries series = processSeries( seriesAccession, this.doSampleMatching );
                if ( series == null ) continue;
                result.add( series );
            }
        } else if ( geoAccession.startsWith( "GSE" ) ) {
            if ( processPlatformsOnly ) {
                return processSeriesPlatforms( geoAccession ); // FIXME, this is ugly.
            }
            GeoSeries series = processSeries( geoAccession, this.doSampleMatching );
            if ( series == null ) return result;
            result.add( series );
            return result;
        } else {
            throw new IllegalArgumentException( "Cannot handle acccession: " + geoAccession
                    + ", must be a GDS, GSE or GPL" );
        }
        return result;

    }

    // /**
    // * @param datasetsToProcess Collection of GDS accession ids to process.
    // * @return
    // */
    // private Collection<Object> processDatasets( Collection<String> datasetsToProcess ) {
    // Collection<Object> result = new HashSet<Object>();
    // for ( String accession : datasetsToProcess ) {
    // result.add( processDataSet( accession ) );
    // }
    // return result;
    // }

    /**
     * @param geoAccession
     * @return
     */
    private GeoPlatform processPlatform( String geoAccession ) {
        assert platformFetcher != null;
        Collection<LocalFile> platforms = platformFetcher.fetch( geoAccession );
        if ( platforms == null ) {
            throw new RuntimeException( "No series file found for " + geoAccession );
        }
        LocalFile platformFile = ( platforms.iterator() ).next();
        String platformPath;

        platformPath = platformFile.getLocalURL().getPath();

        parser.setProcessPlatformsOnly( true );
        try {
            parser.parse( platformPath );
        } catch ( IOException e1 ) {
            throw new RuntimeException( e1 );
        }

        return ( ( GeoParseResult ) parser.getResults().iterator().next() ).getPlatformMap().get( geoAccession );

    }

    /**
     * Download and parse a GEO series.
     * 
     * @param seriesAccession
     * @param doSampleMatching Whether we should attempt to match the samples. In some cases we might know that this is
     *        not a good idea.
     */
    private GeoSeries processSeries( String seriesAccession, boolean doSampleMatching ) {

        Collection<String> datasetsToProcess = DatasetCombiner.findGDSforGSE( seriesAccession );

        if ( datasetsToProcess == null || datasetsToProcess.size() == 0 ) {
            log.warn( "No data set found for " + seriesAccession );
        }

        Collection<LocalFile> fullSeries = seriesFetcher.fetch( seriesAccession );
        if ( fullSeries == null ) {
            log.warn( "No series file found for " + seriesAccession );
            return null;
        }
        LocalFile seriesFile = ( fullSeries.iterator() ).next();
        String seriesPath = seriesFile.getLocalURL().getPath();

        parser.setProcessPlatformsOnly( this.processPlatformsOnly );
        try {
            parser.parse( seriesPath );
        } catch ( IOException e1 ) {
            throw new RuntimeException( e1 );
        }

        // Only allow one series...
        GeoSeries series = ( ( GeoParseResult ) parser.getResults().iterator().next() ).getSeriesMap().get(
                seriesAccession );

        if ( series == null ) {
            throw new RuntimeException( "No series was parsed for " + seriesAccession );
        }

        // FIXME put this back...or something.
        // Raw data files have been added to series object as a path (during parsing).
        // processRawData( series )

        for ( String dataSetAccession : datasetsToProcess ) {
            log.info( "Processing " + dataSetAccession );
            processDataSet( series, dataSetAccession );
        }
        DatasetCombiner datasetCombiner = new DatasetCombiner( doSampleMatching );

        GeoSampleCorrespondence correspondence = datasetCombiner.findGSECorrespondence( series );
        assert correspondence != null;
        series.setSampleCorrespondence( correspondence );

        return series;
    }

    /**
     * Download and parse GEO platform(s) using series accession(s).
     * 
     * @param seriesAccession
     */
    private Collection<?> processSeriesPlatforms( Collection<String> seriesAccessions ) {
        for ( String seriesAccession : seriesAccessions ) {
            processSeriesPlatforms( seriesAccession );
        }
        return ( ( GeoParseResult ) parser.getResults().iterator().next() ).getPlatformMap().values();

    }

    /**
     * @param seriesAccession
     */
    private Collection<?> processSeriesPlatforms( String seriesAccession ) {
        Collection<LocalFile> fullSeries = seriesFetcher.fetch( seriesAccession );
        if ( fullSeries == null ) {
            throw new RuntimeException( "No series file found for " + seriesAccession );
        }
        LocalFile seriesFile = ( fullSeries.iterator() ).next();
        String seriesPath;

        seriesPath = seriesFile.getLocalURL().getPath();

        parser.setProcessPlatformsOnly( this.processPlatformsOnly );
        try {
            parser.parse( seriesPath );
        } catch ( IOException e1 ) {
            throw new RuntimeException( e1 );
        }
        return ( ( GeoParseResult ) parser.getResults().iterator().next() ).getPlatformMap().values();
    }

    /**
     * Parse a GEO GDS file, return the extracted GeoDataset.
     * 
     * @param geoDataSetAccession
     * @param dataSetPath
     * @return GeoDataset
     * @throws IOException
     */
    private GeoDataset processDataSet( String geoDataSetAccession, String dataSetPath ) throws IOException {
        parser.parse( dataSetPath );

        // first result is where we start.
        GeoParseResult results = ( GeoParseResult ) parser.getResults().iterator().next();

        Map<String, GeoDataset> datasetMap = results.getDatasets();
        if ( !datasetMap.containsKey( geoDataSetAccession ) ) {
            throw new IllegalStateException( "Failed to get parse of " + geoDataSetAccession );
        }

        GeoDataset gds = datasetMap.get( geoDataSetAccession );
        return gds;
    }

    /**
     * @param geoDataSetAccession
     * @return
     */
    private String fetchDataSetToLocalFile( String geoDataSetAccession ) {
        Collection<LocalFile> result = datasetFetcher.fetch( geoDataSetAccession );

        if ( result == null ) return null;

        if ( result.size() != 1 ) {
            throw new IllegalStateException( "Got " + result.size() + " files for " + geoDataSetAccession
                    + ", expected only one." );
        }

        LocalFile dataSetFile = ( result.iterator() ).next();
        String dataSetPath;

        dataSetPath = dataSetFile.getLocalURL().getPath();

        return dataSetPath;
    }

    /**
     * Fetch any raw data files
     * 
     * @param series
     */
    private void processRawData( GeoSeries series ) {
        if ( StringUtils.isBlank( series.getSupplementaryFile() ) ) {
            return;
        }

        RawDataFetcher rawFetcher = new RawDataFetcher();
        Collection<LocalFile> rawFiles = rawFetcher.fetch( series.getSupplementaryFile() );
        if ( rawFiles != null ) {
            // FIXME maybe do something more. These are usually (always?) CEL files so they can be parsed and
            // assembled or left alone.
            log.info( "Downloaded raw data files" );
        }
    }

    /**
     * @param datasetFetcher The datasetFetcher to set.
     */
    public void setDatasetFetcher( Fetcher df ) {
        this.datasetFetcher = df;
    }

    /**
     * @param seriesFetcher The seriesFetcher to set.
     */
    public void setSeriesFetcher( Fetcher seriesFetcher ) {
        this.seriesFetcher = seriesFetcher;
    }

    /**
     * @param b
     */
    public void setProcessPlatformsOnly( boolean b ) {
        this.processPlatformsOnly = b;
    }

    /**
     * @param platformFetcher The platformFetcher to set.
     */
    public void setPlatformFetcher( Fetcher platformFetcher ) {
        this.platformFetcher = platformFetcher;
    }

    /**
     * Determine the set of external accession values that will be generated during parsing. This can be used to
     * pre-empt time-consuming fetch and download of data we already have.
     * 
     * @param geoAccession
     * @return
     */
    public Collection<DatabaseEntry> getProjectedAccessions( String geoAccession ) {
        ExternalDatabase ed = ExternalDatabase.Factory.newInstance();
        ed.setName( "GEO" );
        Collection<DatabaseEntry> accessions = new HashSet<DatabaseEntry>();
        // DatabaseEntry

        String seriesAccession = null;
        if ( geoAccession.startsWith( "GSE" ) ) {
            seriesAccession = geoAccession;
        } else if ( geoAccession.startsWith( "GPL" ) ) {
            // hmm.. FIXME
            log.warn( "Determining if the data already exist for a GPL (" + geoAccession + ") is not implemented." );
            return null;
        } else if ( geoAccession.startsWith( "GDS" ) ) {
            Collection<String> seriesAccessions = DatasetCombiner.findGSEforGDS( geoAccession );
            if ( seriesAccessions == null || seriesAccessions.size() == 0 ) {
                throw new InvalidAccessionException( "There is no series (GSE) for the accession " + geoAccession );
            }
            for ( String string : seriesAccessions ) {
                seriesAccession += string + ",";
            }
            seriesAccession = StringUtils.removeEnd( seriesAccession, "," );
        } else {
            if ( geoAccession == null || geoAccession.length() == 0 ) {
                throw new InvalidAccessionException( "GEO accession must not be blank. Enter a  GSE, GDS or GPL" );
            }
            throw new InvalidAccessionException( "'" + geoAccession
                    + "' is not understood by Gemma; must be a GSE, GDS or GPL" );
        }

        DatabaseEntry de = DatabaseEntry.Factory.newInstance( ed );

        de.setAccession( seriesAccession );
        accessions.add( de );

        return accessions;
    }
}
