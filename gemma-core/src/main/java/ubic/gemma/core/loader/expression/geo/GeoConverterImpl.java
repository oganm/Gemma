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
package ubic.gemma.core.loader.expression.geo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ubic.basecode.io.ByteArrayConverter;
import ubic.gemma.core.loader.expression.arrayDesign.ArrayDesignSequenceProcessingServiceImpl;
import ubic.gemma.core.loader.expression.geo.model.GeoChannel;
import ubic.gemma.core.loader.expression.geo.model.GeoContact;
import ubic.gemma.core.loader.expression.geo.model.GeoData;
import ubic.gemma.core.loader.expression.geo.model.GeoDataset;
import ubic.gemma.core.loader.expression.geo.model.GeoDataset.ExperimentType;
import ubic.gemma.core.loader.expression.geo.model.GeoDataset.PlatformType;
import ubic.gemma.core.loader.expression.geo.model.GeoPlatform;
import ubic.gemma.core.loader.expression.geo.model.GeoReplication;
import ubic.gemma.core.loader.expression.geo.model.GeoReplication.ReplicationType;
import ubic.gemma.core.loader.expression.geo.model.GeoSample;
import ubic.gemma.core.loader.expression.geo.model.GeoSeries;
import ubic.gemma.core.loader.expression.geo.model.GeoSeries.SeriesType;
import ubic.gemma.core.loader.expression.geo.model.GeoSubset;
import ubic.gemma.core.loader.expression.geo.model.GeoValues;
import ubic.gemma.core.loader.expression.geo.model.GeoVariable;
import ubic.gemma.core.loader.expression.geo.model.GeoVariable.VariableType;
import ubic.gemma.core.loader.expression.geo.util.GeoConstants;
import ubic.gemma.core.loader.util.parser.ExternalDatabaseUtils;
import ubic.gemma.model.association.GOEvidenceCode;
import ubic.gemma.model.common.auditAndSecurity.Contact;
import ubic.gemma.model.common.auditAndSecurity.Person;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.common.description.DatabaseType;
import ubic.gemma.model.common.description.ExternalDatabase;
import ubic.gemma.model.common.description.LocalFile;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.common.quantitationtype.PrimitiveType;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.TechnologyType;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimension;
import ubic.gemma.model.expression.bioAssayData.RawExpressionDataVector;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.biomaterial.Treatment;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExperimentalDesign;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.FactorType;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.biosequence.PolymerType;
import ubic.gemma.model.genome.biosequence.SequenceType;
import ubic.gemma.persistence.service.common.description.ExternalDatabaseService;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;
import ubic.gemma.persistence.util.Settings;

/**
 * Convert GEO domain objects into Gemma objects. Usually we trigger this by passing in GeoSeries objects.
 * GEO has four basic kinds of objects: Platforms (ArrayDesigns), Samples (BioAssays), Series (Experiments) and DataSets
 * (which are curated Experiments). Note that a sample can belong to more than one series. A series can include more
 * than one dataset. GEO also supports the concept of a superseries. See
 * http://www.ncbi.nlm.nih.gov/projects/geo/info/soft2.html.
 * A curated expression data set is at first represented by a GEO "GDS" number (a curated dataset), which maps to a
 * series (GSE). HOWEVER, multiple datasets may go together to form a series (GSE). This can happen when the "A" and "B"
 * arrays were both run on the same samples. Thus we actually normally go by GSE.
 * This service can be used in database-aware or unaware states. However, it has prototype scope as it has some 'global'
 * data structures used during processing.
 *
 * @author keshav
 * @author pavlidis
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GeoConverterImpl implements GeoConverter {

    private static final int DEFAULT_DEFINITION_OF_TOO_MANY_ELEMENTS = 100000;

    /**
     * This string is inserted into the descriptions of constructed biomaterials.
     */
    private static final String BIOMATERIAL_DESCRIPTION_PREFIX = "Generated by Gemma for: ";

    /**
     * This string is inserted into the names of constructed biomaterials, so you get names like GSE5929_BioMat_58.
     */
    private static final String BIOMATERIAL_NAME_TAG = "_Biomat_";

    /**
     * How often we tell the user about data processing (items per update)
     */
    private static final int LOGGING_VECTOR_COUNT_UPDATE = 2000;
    /**
     * Initial guess at how many designelementdatavectors to allocate space for.
     */
    private static final int INITIAL_VECTOR_CAPACITY = 10000;
    /**
     * The scientific name used for rat species. FIXME this should be updated elsewhere; avoid this hardcoding.
     */
    private static final String RAT = "Rattus norvegicus";
    private static final Log log = LogFactory.getLog( ArrayDesignSequenceProcessingServiceImpl.class.getName() );
    private static final Map<String, String> organismDatabases = new HashMap<>();

    static {
        organismDatabases.put( "Saccharomyces cerevisiae", "SGD" );
        organismDatabases.put( "Schizosaccharomyces pombe", "GeneDB" );
    }

    private final ByteArrayConverter byteArrayConverter = new ByteArrayConverter();
    private final Map<String, Taxon> taxonScientificNameMap = new HashMap<>();
    private final Map<String, Taxon> taxonAbbreviationMap = new HashMap<>();
    /**
     * More than this and we apply stricter selection criteria for choosing elements to keep on a platform.
     */
    private int tooManyElements = Settings
            .getInt( "geo.platform.import.maxelements", DEFAULT_DEFINITION_OF_TOO_MANY_ELEMENTS );
    @Autowired
    private ExternalDatabaseService externalDatabaseService;
    @Autowired
    private TaxonService taxonService;
    private ExternalDatabase geoDatabase;
    private Map<String, Map<String, CompositeSequence>> platformDesignElementMap = new HashMap<>();
    private Collection<Object> results = new HashSet<>();
    private Map<String, ArrayDesign> seenPlatforms = new HashMap<>();
    private ExternalDatabase genbank;
    private boolean splitByPlatform = false;
    private boolean forceConvertElements = false;

    @Override
    public void clear() {
        results = new HashSet<>();
        seenPlatforms = new HashMap<>();
        platformDesignElementMap = new HashMap<>();
        taxonAbbreviationMap.clear();
        taxonScientificNameMap.clear();
    }

    @Override
    public Collection<Object> convert( Collection<? extends GeoData> geoObjects ) {
        for ( Object geoObject : geoObjects ) {
            Object convertedObject = convert( ( GeoData ) geoObject );
            if ( convertedObject != null ) {
                if ( convertedObject instanceof Collection ) {
                    results.addAll( ( Collection<?> ) convertedObject );
                } else {
                    results.add( convertedObject );
                }
            }
        }

        log.info( "Converted object tally:\n" + this );

        return results;
    }

    @Override
    public Object convert( GeoData geoObject ) {
        if ( geoObject == null ) {
            log.warn( "Null object" );
            return null;
        }
        if ( geoObject instanceof Collection ) {
            //noinspection unchecked
            return convert( ( Collection<GeoData> ) geoObject );
        } else if ( geoObject instanceof GeoDataset ) {
            return convertDataset( ( GeoDataset ) geoObject );
        } else if ( geoObject instanceof GeoSeries ) { // typically we start here, with a series.
            return convertSeries( ( GeoSeries ) geoObject );
        } else if ( geoObject instanceof GeoSubset ) {
            throw new IllegalArgumentException(
                    "Can't deal with " + geoObject.getClass().getName() + " ('" + geoObject + "')" );
        } else if ( geoObject instanceof GeoSample ) {
            throw new IllegalArgumentException(
                    "Can't deal with " + geoObject.getClass().getName() + " ('" + geoObject + "')" );
        } else if ( geoObject instanceof GeoPlatform ) {
            return convertPlatform( ( GeoPlatform ) geoObject );
        } else {
            throw new IllegalArgumentException(
                    "Can't deal with " + geoObject.getClass().getName() + " ('" + geoObject + "')" );
        }

    }

    /**
     * Convert a vector of strings into a byte[] for saving in the database. . Blanks(missing values) are treated as NAN
     * (double), 0 (integer), false (booleans) or just empty strings (strings). Other invalid values are treated the
     * same way as missing data (to keep the parser from failing when dealing with strange GEO files that have values
     * like "Error" for an expression value).
     *
     * @param vector of Strings to be converted to primitive values (double, int etc)
     * @param qt The quantitation type for the values to be converted.
     */
    @Override
    public byte[] convertData( List<Object> vector, QuantitationType qt ) {

        if ( vector == null || vector.size() == 0 )
            return null;

        boolean containsAtLeastOneNonNull = false;
        for ( Object string : vector ) {
            if ( string != null ) {
                containsAtLeastOneNonNull = true;
                break;
            }
        }

        if ( !containsAtLeastOneNonNull ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "No data for " + qt + " in vector of length " + vector.size() );
            }
            return null;
        }

        List<Object> toConvert = new ArrayList<>();
        PrimitiveType pt = qt.getRepresentation();
        int numMissing = 0;
        for ( Object rawValue : vector ) {
            if ( rawValue == null ) {
                numMissing++;
                handleMissing( toConvert, pt );
            } else if ( rawValue instanceof String ) { // needs to be coverted.
                String valueString = ( String ) rawValue;
                if ( StringUtils.isBlank( valueString ) ) {
                    numMissing++;
                    handleMissing( toConvert, pt );
                    continue;
                }
                try {
                    if ( pt.equals( PrimitiveType.DOUBLE ) ) {
                        toConvert.add( Double.parseDouble( valueString ) );
                    } else if ( pt.equals( PrimitiveType.STRING ) ) {
                        toConvert.add( rawValue );
                    } else if ( pt.equals( PrimitiveType.CHAR ) ) {
                        if ( valueString.length() != 1 ) {
                            throw new IllegalStateException(
                                    "Attempt to cast a string of length " + valueString.length() + " to a char: "
                                            + rawValue + "(quantitation type =" + qt );
                        }
                        toConvert.add( valueString.toCharArray()[0] );
                    } else if ( pt.equals( PrimitiveType.INT ) ) {
                        toConvert.add( Integer.parseInt( valueString ) );
                    } else if ( pt.equals( PrimitiveType.BOOLEAN ) ) {
                        toConvert.add( Boolean.parseBoolean( valueString ) );
                    } else {
                        throw new UnsupportedOperationException( "Data vectors of type " + pt + " not supported" );
                    }
                } catch ( NumberFormatException e ) {
                    numMissing++;
                    handleMissing( toConvert, pt );
                }
            } else { // use as is.
                toConvert.add( rawValue );
            }
        }

        if ( numMissing == vector.size() ) {
            return null;
        }

        byte[] bytes = byteArrayConverter.toBytes( toConvert.toArray() );

        /*
         * Debugging - absolutely make sure we can convert the data back.
         */
        if ( pt.equals( PrimitiveType.DOUBLE ) ) {
            double[] byteArrayToDoubles = byteArrayConverter.byteArrayToDoubles( bytes );
            if ( byteArrayToDoubles.length != vector.size() ) {
                throw new IllegalStateException(
                        "Expected " + vector.size() + " got " + byteArrayToDoubles.length + " doubles" );
            }
        } else if ( pt.equals( PrimitiveType.INT ) ) {
            int[] byteArrayToInts = byteArrayConverter.byteArrayToInts( bytes );
            if ( byteArrayToInts.length != vector.size() ) {
                throw new IllegalStateException(
                        "Expected " + vector.size() + " got " + byteArrayToInts.length + " ints" );
            }
        } else if ( pt.equals( PrimitiveType.BOOLEAN ) ) {
            boolean[] byteArrayToBooleans = byteArrayConverter.byteArrayToBooleans( bytes );
            if ( byteArrayToBooleans.length != vector.size() ) {
                throw new IllegalStateException(
                        "Expected " + vector.size() + " got " + byteArrayToBooleans.length + " booleans" );
            }
        }

        return bytes;
    }

    @Override
    public void convertSubsetToExperimentalFactor( ExpressionExperiment expExp, GeoSubset geoSubSet ) {

        ExperimentalDesign experimentalDesign = expExp.getExperimentalDesign();
        Collection<ExperimentalFactor> existingExperimentalFactors = experimentalDesign.getExperimentalFactors();

        ExperimentalFactor experimentalFactor = ExperimentalFactor.Factory.newInstance();
        experimentalFactor.setName( geoSubSet.getType().toString() );
        VocabCharacteristic term = VocabCharacteristic.Factory.newInstance();
        convertVariableType( term, geoSubSet.getType() );
        term.setDescription( "Converted from GEO subset " + geoSubSet.getGeoAccession() );
        term.setValue( term.getCategory() );
        term.setValueUri( term.getCategoryUri() );
        experimentalFactor.setCategory( term );

        experimentalFactor.setType( FactorType.CATEGORICAL );
        experimentalFactor.setDescription( "Converted from GEO subset " + geoSubSet.getGeoAccession() );

        boolean duplicateExists = false;
        for ( ExperimentalFactor existingExperimentalFactor : existingExperimentalFactors ) {
            if ( ( experimentalFactor.getName() ).equalsIgnoreCase( existingExperimentalFactor.getName() ) ) {
                duplicateExists = true;
                experimentalFactor = existingExperimentalFactor;
                if ( log.isDebugEnabled() )
                    log.debug( experimentalFactor.getName()
                            + " already exists.  Not adding to list of experimental factors." );
                break;
            }
        }

        if ( !duplicateExists ) {
            experimentalDesign.getExperimentalFactors().add( experimentalFactor );
        }

        /* bi-directional ... don't forget this. */
        experimentalFactor.setExperimentalDesign( experimentalDesign );

        FactorValue factorValue = convertSubsetDescriptionToFactorValue( geoSubSet, experimentalFactor );
        addFactorValueToBioMaterial( expExp, geoSubSet, factorValue );
    }

    @Override
    public LocalFile convertSupplementaryFileToLocalFile( Object object ) {

        URL remoteFileUrl = null;
        LocalFile remoteFile = null;

        if ( object instanceof GeoSeries ) {
            GeoSeries series = ( GeoSeries ) object;
            String file = series.getSupplementaryFile();
            if ( !StringUtils.isEmpty( file ) && !StringUtils.equalsIgnoreCase( file, "NONE" ) ) {
                remoteFile = LocalFile.Factory.newInstance();
                remoteFileUrl = tryGetRemoteFileUrl( file );
            }
        } else if ( object instanceof GeoSample ) {
            GeoSample sample = ( GeoSample ) object;
            String file = sample.getSupplementaryFile();
            if ( !StringUtils.isEmpty( file ) && !StringUtils.equalsIgnoreCase( file, "NONE" ) ) {
                remoteFile = LocalFile.Factory.newInstance();
                remoteFileUrl = tryGetRemoteFileUrl( file );
            }
        } else if ( object instanceof GeoPlatform ) {
            GeoPlatform platform = ( GeoPlatform ) object;
            String file = platform.getSupplementaryFile();
            if ( !StringUtils.isEmpty( file ) && !StringUtils.equalsIgnoreCase( file, "NONE" ) ) {
                remoteFile = LocalFile.Factory.newInstance();
                remoteFileUrl = tryGetRemoteFileUrl( file );
            }
        }

        /* nulls allowed in remoteFile ... deal with later. */
        if ( remoteFile != null )
            remoteFile.setRemoteURL( remoteFileUrl );

        return remoteFile;
    }

    /**
     * This method determines the primary taxon on the array: There are 4 main branches of logic. 1.First it checks if
     * there is only one platform taxon defined on the GEO submission: If there is that is the primary taxon. 2.If
     * multiple taxa are given for the platform then the taxa are checked to see if they share a common parent if so
     * that is the primary taxon e.g. salmonid where atlantic salmon and rainbow trout are given. 3.Finally the
     * probeTaxa are looked at and the most common probe taxa is calculated as the primary taxon 4. No taxon found
     * throws an error
     *
     * @param platformTaxa Collection of taxa that were given on the GEO array submission as platform taxa
     * @param probeTaxa Collection of taxa strings defining the taxon of each probe on the array.
     * @return Primary taxon of array as determined by this method
     */
    @Override
    public Taxon getPrimaryArrayTaxon( Collection<Taxon> platformTaxa, Collection<String> probeTaxa )
            throws IllegalArgumentException {

        if ( platformTaxa == null || platformTaxa.isEmpty() ) {
            return null;
        }

        // if there is only 1 taxon on the platform submission then this is the primary taxon
        if ( platformTaxa.size() == 1 ) {
            log.debug( "Only 1 taxon given on GEO platform: " + platformTaxa.iterator().next() );
            return platformTaxa.iterator().next();
        }

        // If there are multiple taxa on array
        else if ( platformTaxa.size() > 1 ) {
            log.debug( platformTaxa.size() + " taxa in GEO platform" );
            // check if they share a common parent taxon to use as primary taxa.
            Collection<Taxon> parentTaxa = new HashSet<>();
            for ( Taxon platformTaxon : platformTaxa ) {
                // thaw to get parent taxon
                this.taxonService.thaw( platformTaxon );
                Taxon platformParentTaxon = platformTaxon.getParentTaxon();
                parentTaxa.add( platformParentTaxon );
            }
            // check now if we only have one parent taxon and check if not null, if a null then there was a taxon with
            // no
            // parent
            if ( !( parentTaxa.contains( null ) ) && parentTaxa.size() == 1 ) {
                log.debug( "Parent taxon found " + parentTaxa );
                return parentTaxa.iterator().next();
            }
            // No common parent then calculate based on probe taxa:

            log.debug( "Looking at probe taxa to determine 'primary' taxon" );
            // create a hashmap keyed on taxon with a counter to count the number of probes for that taxon.
            Map<String, Integer> taxonProbeNumberList = new HashMap<>();

            if ( probeTaxa != null ) {
                for ( String probeTaxon : probeTaxa ) {
                    // reset each iteration so if no probes already processed set to 1
                    Integer counter = 1;
                    if ( taxonProbeNumberList.containsKey( probeTaxon ) ) {
                        counter = taxonProbeNumberList.get( probeTaxon ) + 1;
                        taxonProbeNumberList.put( probeTaxon, counter );
                    }
                    taxonProbeNumberList.put( probeTaxon, counter );
                }
            }

            String primaryTaxonName = "";
            Integer highestScore = 0;
            for ( String taxon : taxonProbeNumberList.keySet() ) {
                // filter out those probes that have no taxon set control spots. Here's that 'n/a' again, kind of
                // ugly but we see it in some arrays
                if ( !taxon.equals( "n/a" ) && StringUtils.isNotBlank( taxon )
                        && taxonProbeNumberList.get( taxon ) > highestScore ) {
                    primaryTaxonName = taxon;
                    highestScore = taxonProbeNumberList.get( taxon );
                }
            }
            if ( StringUtils.isNotBlank( primaryTaxonName ) ) {
                return this.convertProbeOrganism( primaryTaxonName );
            }

        }
        // error no taxon on array submission

        throw new IllegalArgumentException( "No taxon could be determined for GEO platform " );

    }

    @Override
    public void setElementLimitForStrictness( int tooManyElements ) {
        this.tooManyElements = tooManyElements;
    }

    @Override
    public void setForceConvertElements( boolean forceConvertElements ) {
        this.forceConvertElements = forceConvertElements;
    }

    @Override
    public void setSplitByPlatform( boolean splitByPlatform ) {
        this.splitByPlatform = splitByPlatform;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        Map<String, Integer> tally = new HashMap<>();
        for ( Object element : results ) {
            String clazz = element.getClass().getName();
            if ( !tally.containsKey( clazz ) ) {
                tally.put( clazz, 0 );
            }
            tally.put( clazz, tally.get( clazz ) + 1 );
        }
        for ( String clazz : tally.keySet() ) {
            buf.append( tally.get( clazz ) ).append( " " ).append( clazz ).append( "s\n" );
        }

        return buf.toString();
    }

    private void addFactorValueToBioMaterial( ExpressionExperiment expExp, GeoSubset geoSubSet,
            FactorValue factorValue ) {
        // fill in biomaterial-->factorvalue.
        for ( GeoSample sample : geoSubSet.getSamples() ) {

            // find the matching biomaterial(s) in the expression experiment.
            for ( BioAssay bioAssay : expExp.getBioAssays() ) {
                if ( bioAssay.getAccession().getAccession().equals( sample.getGeoAccession() ) ) {
                    BioMaterial material = bioAssay.getSampleUsed();
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Adding " + factorValue.getExperimentalFactor() + " : " + factorValue + " to "
                                + material );
                    }
                    material.getFactorValues().add( factorValue );
                }

            }

        }
    }

    /**
     * @return true if the biomaterial already has a factorvalue for the given experimentalFactor; false otherwise.
     */
    private boolean alreadyHasFactorValueForFactor( BioMaterial bioMaterial, ExperimentalFactor experimentalFactor ) {
        for ( FactorValue fv : bioMaterial.getFactorValues() ) {
            ExperimentalFactor existingEf = fv.getExperimentalFactor();
            // This is a weak form of 'equals' - we just check the name.
            if ( existingEf.getName().equals( experimentalFactor.getName() ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Flag as unneeded data that are not from experiments types that we support, such as ChIP.
     */
    private void checkForDataToSkip( GeoSeries series, Collection<String> dataSetsToSkip,
            Collection<GeoSample> samplesToSkip ) {

        for ( GeoDataset dataset : series.getDatasets() ) {
            // This doesn't cover every possibility...
            if ( dataset.getExperimentType().equals( ExperimentType.arrayCGH ) || dataset.getExperimentType()
                    .equals( ExperimentType.ChIPChip ) || dataset.getExperimentType()
                            .equals( ExperimentType.geneExpressionSAGEbased )
                    || dataset.getExperimentType()
                            .equals( ExperimentType.Other ) ) {
                log.warn( "Gemma does not know how to handle experiment type=" + dataset.getExperimentType() );

                if ( series.getDatasets().size() == 1 ) {
                    log.warn( "Because the experiment type cannot be handled, "
                            + "and there is only one data set in this series, nothing will be returned!" );
                }
                samplesToSkip.addAll( this.getDatasetSamples( dataset ) );
                dataSetsToSkip.add( dataset.getGeoAccession() );
            } else {
                log.info( "Data from " + dataset + " is of type " + dataset.getExperimentType() + ", "
                        + getDatasetSamples( dataset ).size() + " samples." );
            }
        }

    }

    /**
     * Used for the case where we want to split the GSE into two (or more) separate ExpressionExperiments based on
     * platform. This is necessary when the two platforms are completely incompatible.
     */
    private void convertByPlatform( GeoSeries series, Collection<ExpressionExperiment> converted,
            Map<GeoPlatform, Collection<GeoData>> platformDatasetMap, int i, GeoPlatform platform ) {
        GeoSeries platformSpecific = new GeoSeries();

        Collection<GeoData> datasets = platformDatasetMap.get( platform );
        assert datasets.size() > 0;

        for ( GeoSample sample : series.getSamples() ) {
            // ugly, we have to assume there is only one platform per sample.
            if ( sample.getPlatforms().iterator().next().equals( platform ) ) {
                platformSpecific.addSample( sample );
            }
        }

        // strip out samples that aren't from this platform.
        for ( GeoData dataset : datasets ) {
            if ( dataset instanceof GeoDataset ) {
                ( ( GeoDataset ) dataset ).dissociateFromSeries( series );
                platformSpecific.addDataSet( ( GeoDataset ) dataset );
            }
        }

        /*
         * Basically copy over most of the information
         */
        platformSpecific.setContact( series.getContact() );
        platformSpecific.setContributers( series.getContributers() );
        platformSpecific.setGeoAccession( series.getGeoAccession() + "." + i );
        platformSpecific.setKeyWords( series.getKeyWords() );
        platformSpecific.setOverallDesign( series.getOverallDesign() );
        platformSpecific.setPubmedIds( series.getPubmedIds() );
        platformSpecific.setReplicates( series.getReplicates() );
        platformSpecific.setSampleCorrespondence( series.getSampleCorrespondence() );
        platformSpecific.setSummaries( series.getSummaries() );
        platformSpecific.setTitle( series.getTitle() + " - " + platform.getGeoAccession() );
        platformSpecific.setWebLinks( series.getWebLinks() );
        platformSpecific.setValues( series.getValues() );
        platformSpecific.getSeriesTypes().addAll( series.getSeriesTypes() );

        converted.add( convertSeriesSingle( platformSpecific ) );

    }

    /**
     * GEO does not keep track of 'biomaterials' that make up different channels. Therefore the two channels effectively
     * make up a single biomaterial, as far as we're concerned. We're losing information here.
     */
    private BioMaterial convertChannel( GeoSample sample, GeoChannel channel, BioMaterial bioMaterial ) {
        if ( bioMaterial == null )
            return null;
        log.debug( "Sample: " + sample.getGeoAccession() + " - Converting channel " + channel.getSourceName() );

        bioMaterial.setDescription(
                ( bioMaterial.getDescription() == null ? "" : bioMaterial.getDescription() + ";" ) + "Channel "
                        + channel.getChannelNumber() );

        if ( !StringUtils.isBlank( channel.getGrowthProtocol() ) ) {
            Treatment treatment = Treatment.Factory.newInstance();
            treatment.setName( sample.getGeoAccession() + " channel " + channel.getChannelNumber() + " treatment" );
            treatment.setDescription( channel.getGrowthProtocol() );
            bioMaterial.getTreatments().add( treatment );
        }

        if ( !StringUtils.isBlank( channel.getTreatmentProtocol() ) ) {
            Treatment treatment = Treatment.Factory.newInstance();
            treatment.setName( sample.getGeoAccession() + " channel " + channel.getChannelNumber() + " growth" );
            treatment.setDescription( channel.getTreatmentProtocol() );
            bioMaterial.getTreatments().add( treatment );
        }

        if ( !StringUtils.isBlank( channel.getExtractProtocol() ) ) {
            Treatment treatment = Treatment.Factory.newInstance();
            treatment.setName( sample.getGeoAccession() + " channel " + channel.getChannelNumber() + " extraction" );
            treatment.setDescription( channel.getExtractProtocol() );
            bioMaterial.getTreatments().add( treatment );
        }

        if ( !StringUtils.isBlank( channel.getLabelProtocol() ) ) {
            Treatment treatment = Treatment.Factory.newInstance();
            treatment.setName( sample.getGeoAccession() + " channel " + channel.getChannelNumber() + " labeling" );
            treatment.setDescription( channel.getLabelProtocol() );
            bioMaterial.getTreatments().add( treatment );
        }

        for ( String characteristic : channel.getCharacteristics() ) {

            characteristic = trimString( characteristic );

            /*
             * Sometimes values are like Age:8 weeks, so we can try to convert them.
             */
            String[] fields = characteristic.split( ":" );
            String defaultDescription = "GEO Sample characteristic";
            if ( fields.length == 2 ) {

                String category = fields[0].trim();
                String value = fields[1].trim();

                try {
                    Characteristic gemmaChar = Characteristic.Factory.newInstance();
                    convertVariableType( gemmaChar, GeoVariable.convertStringToType( category ) );
                    if ( gemmaChar.getCategory() == null ) {
                        continue;
                    }
                    gemmaChar.setDescription( defaultDescription );
                    gemmaChar.setValue( value );
                    gemmaChar.setEvidenceCode( GOEvidenceCode.IIA );
                    bioMaterial.getCharacteristics().add( gemmaChar );
                } catch ( Exception e ) {
                    // conversion didn't work, fall back.
                    doFallback( bioMaterial, characteristic, defaultDescription );
                }

            } else {
                // no colon, just use raw (same as fallback above)
                doFallback( bioMaterial, characteristic, defaultDescription );
            }

        }

        if ( StringUtils.isNotBlank( channel.getSourceName() ) ) {
            Characteristic sourceChar = Characteristic.Factory.newInstance();
            sourceChar.setDescription( "GEO Sample source" );
            String characteristic = trimString( channel.getSourceName() );
            sourceChar.setCategory( "BioSource" );
            sourceChar.setCategoryUri( "http://www.ebi.ac.uk/efo/EFO_0000635" /*
                                                                               * organism part; used to be 'biosource'
                                                                               */ );
            sourceChar.setValue( characteristic );
            sourceChar.setEvidenceCode( GOEvidenceCode.IIA );
            bioMaterial.getCharacteristics().add( sourceChar );
        }

        if ( StringUtils.isNotBlank( channel.getOrganism() ) ) {
            // if we have a case where the two channels have different taxon throw an exception.
            String currentChannelTaxon = channel.getOrganism();
            if ( bioMaterial.getSourceTaxon() != null ) {
                String previousChannelTaxon = bioMaterial.getSourceTaxon().getScientificName();
                if ( previousChannelTaxon != null && !( previousChannelTaxon.equals( currentChannelTaxon ) ) ) {
                    throw new IllegalArgumentException(
                            "Channel 1 taxon is " + bioMaterial.getSourceTaxon().getScientificName()
                                    + " Channel 2 taxon is " + currentChannelTaxon
                                    + " Check that is expected for sample " + sample.getGeoAccession() );
                }

            } else {
                // get it from the channel.
                Taxon taxon = Taxon.Factory.newInstance();
                taxon.setIsSpecies( true );
                taxon.setScientificName( channel.getOrganism() );
                taxon.setIsGenesUsable( true ); // plausible default, doesn't matter.
                bioMaterial.setSourceTaxon( taxon );
            }

        }

        if ( channel.getMolecule() != null ) {
            // this we can convert automatically pretty easily.
            Characteristic c = channel.getMoleculeAsCharacteristic();
            bioMaterial.getCharacteristics().add( c );
        }

        if ( StringUtils.isNotBlank( channel.getLabel() ) ) {
            String characteristic = trimString( channel.getLabel() );
            // This is typically something like "biotin-labeled nucleotides", which we can convert later.
            Characteristic labelChar = Characteristic.Factory.newInstance();
            labelChar.setDescription( "GEO Sample label" );
            labelChar.setCategory( "LabelCompound" );
            labelChar.setCategoryUri( "http://www.ebi.ac.uk/efo/EFO_0000562" /* labeling; used to be LabelCompound */ );
            labelChar.setValue( characteristic );
            labelChar.setEvidenceCode( GOEvidenceCode.IIA );
            bioMaterial.getCharacteristics().add( labelChar );
        }
        return bioMaterial;
    }

    private Person convertContact( GeoContact contact ) {
        Person result = Person.Factory.newInstance();
        result.setName( contact.getName() );
        result.setEmail( contact.getEmail() );
        return result;
    }

    /**
     * Take contact and contributer information from a GeoSeries and put it in the ExpressionExperiment.
     */
    private void convertContacts( GeoSeries series, ExpressionExperiment expExp ) {
        expExp.getInvestigators().add( convertContact( series.getContact() ) );
        if ( series.getContributers().size() > 0 ) {
            expExp.setDescription( expExp.getDescription() + "\nContributers: " );
            for ( GeoContact contributer : series.getContributers() ) {
                expExp.setDescription( expExp.getDescription() + " " + contributer.getName() );
                expExp.getInvestigators().add( convertContact( contributer ) );
            }
            expExp.setDescription( expExp.getDescription() + "\n" );
        }
    }

    /**
     * Often-needed generation of a valid databaseentry object.
     */
    private DatabaseEntry convertDatabaseEntry( GeoData geoData ) {
        DatabaseEntry result = DatabaseEntry.Factory.newInstance();

        initGeoExternalDatabase();

        result.setExternalDatabase( this.geoDatabase );

        // remove trailing ".1" etc. in case it was split.
        result.setAccession( geoData.getGeoAccession().replaceAll( "\\.[0-9]+$", "" ) );

        return result;
    }

    private ExpressionExperiment convertDataset( GeoDataset geoDataset ) {

        if ( geoDataset.getSeries().size() == 0 ) {
            throw new IllegalArgumentException( "GEO Dataset must have associated series" );
        }

        if ( geoDataset.getSeries().size() > 1 ) {
            throw new UnsupportedOperationException( "GEO Dataset can only be associated with one series" );
        }

        Collection<ExpressionExperiment> seriesResults = this.convertSeries( geoDataset.getSeries().iterator().next() );
        assert seriesResults.size() == 1; // unless we have multiple species, not possible.
        return seriesResults.iterator().next();
    }

    private ExpressionExperiment convertDataset( GeoDataset geoDataset, ExpressionExperiment expExp ) {

        /*
         * First figure out of there are any samples for this data set. It could be that they were duplicates of ones
         * found in other series, so were skipped. See GeoService
         */
        if ( this.getDatasetSamples( geoDataset ).size() == 0 ) {
            log.info( "No samples remain for " + geoDataset + ", nothing to do" );
            return expExp;
        }

        log.info( "Converting dataset:" + geoDataset );

        convertDatasetDescriptions( geoDataset, expExp );

        GeoPlatform platform = geoDataset.getPlatform();
        ArrayDesign ad = seenPlatforms.get( platform.getGeoAccession() );
        if ( ad == null ) {
            /*
             * See bug 1672. Sometimes the platform for the dataset is wrong so we should just go on. The exception was
             * otherwise catching a case we don't see under normal use.
             */
            throw new IllegalStateException(
                    "ArrayDesigns must be converted before datasets - didn't find " + geoDataset.getPlatform()
                            + "; possibly dataset has incorrect platform?" );
        }
        ad.setDescription( ad.getDescription() + "\nFrom " + platform.getGeoAccession() + "\nLast Updated: " + platform
                .getLastUpdateDate() );

        LocalFile arrayDesignRawFile = convertSupplementaryFileToLocalFile( platform );
        if ( arrayDesignRawFile != null ) {
            Collection<LocalFile> arrayDesignLocalFiles = ad.getLocalFiles();
            if ( arrayDesignLocalFiles == null ) {
                arrayDesignLocalFiles = new HashSet<>();
            }
            arrayDesignLocalFiles.add( arrayDesignRawFile );
            ad.setLocalFiles( arrayDesignLocalFiles );
        }

        convertDataSetDataVectors( geoDataset.getSeries().iterator().next().getValues(), geoDataset, expExp );

        convertSubsetAssociations( expExp, geoDataset );
        return expExp;

    }

    /**
     * Convert the GEO data into DesignElementDataVectors associated with the ExpressionExperiment
     *
     * @param geoDataset Source of the data
     * @param expExp ExpressionExperiment to fill in.
     */
    private void convertDataSetDataVectors( GeoValues values, GeoDataset geoDataset, ExpressionExperiment expExp ) {
        List<GeoSample> datasetSamples = new ArrayList<>( getDatasetSamples( geoDataset ) );
        log.info( datasetSamples.size() + " samples in " + geoDataset );
        GeoPlatform geoPlatform = geoDataset.getPlatform();

        convertVectorsForPlatform( values, expExp, datasetSamples, geoPlatform );

        values.clear( geoPlatform );
    }

    private void convertDatasetDescriptions( GeoDataset geoDataset, ExpressionExperiment expExp ) {
        if ( StringUtils.isEmpty( expExp.getDescription() ) ) {
            expExp.setDescription( geoDataset.getDescription() ); // probably not empty.
        }

        expExp.setDescription( expExp.getDescription() + "\nIncludes " + geoDataset.getGeoAccession() + ".\n" );
        if ( StringUtils.isNotEmpty( geoDataset.getUpdateDate() ) ) {
            expExp.setDescription( expExp.getDescription() + " Update date: " + geoDataset.getUpdateDate() + ".\n" );
        }

        if ( StringUtils.isEmpty( expExp.getName() ) ) {
            expExp.setName( geoDataset.getTitle() );
        } else {
            expExp.setDescription(
                    expExp.getDescription() + " Dataset description " + geoDataset.getGeoAccession() + ": " + geoDataset
                            .getTitle() + ".\n" );
        }
    }

    /**
     * @param dataVector to convert.
     * @return vector, or null if the dataVector was null or empty.
     */
    private RawExpressionDataVector convertDesignElementDataVector( GeoPlatform geoPlatform,
            ExpressionExperiment expExp, BioAssayDimension bioAssayDimension, String designElementName,
            List<Object> dataVector, QuantitationType qt ) {

        if ( dataVector == null || dataVector.size() == 0 )
            return null;

        int numValuesExpected = bioAssayDimension.getBioAssays().size();
        if ( dataVector.size() != numValuesExpected ) {
            throw new IllegalArgumentException(
                    "Expected " + numValuesExpected + " in bioassaydimension, data contains " + dataVector.size() );
        }
        byte[] blob = convertData( dataVector, qt );
        if ( blob == null ) { // all missing etc.
            if ( log.isDebugEnabled() )
                log.debug( "All missing values for DE=" + designElementName + " QT=" + qt );
            return null;
        }
        if ( log.isDebugEnabled() ) {
            log.debug( blob.length + " bytes for " + dataVector.size() + " raw elements" );
        }

        ArrayDesign p = convertPlatform( geoPlatform );
        assert p != null;

        Map<String, CompositeSequence> designMap = platformDesignElementMap.get( p.getShortName() );
        assert designMap != null;

        /*
         * Replace name with the one we're using in the array design after conversion. This information gets filled in
         * earlier in the conversion process (see GeoService)
         */
        String mappedName = geoPlatform.getProbeNamesInGemma().get( designElementName );

        if ( mappedName == null ) {
            // Sigh..this is unlikely to work in general, but see bug 1709.
            mappedName = geoPlatform.getProbeNamesInGemma().get( designElementName.toUpperCase() );
        }

        if ( mappedName == null ) {
            throw new IllegalStateException( "There is  no probe matching " + designElementName );
        }

        CompositeSequence compositeSequence = designMap.get( mappedName );

        if ( compositeSequence == null )
            throw new IllegalStateException( "No composite sequence " + designElementName );

        if ( compositeSequence.getBiologicalCharacteristic() != null
                && compositeSequence.getBiologicalCharacteristic().getSequenceDatabaseEntry() != null &&
                compositeSequence.getBiologicalCharacteristic().getSequenceDatabaseEntry().getExternalDatabase()
                        .getName() == null ) {
            // this is obscure.
            throw new IllegalStateException( compositeSequence + " sequence accession external database lacks name" );
        }

        if ( log.isDebugEnabled() )
            log.debug( "Associating " + compositeSequence + " with dedv" );
        RawExpressionDataVector vector = RawExpressionDataVector.Factory.newInstance();
        vector.setDesignElement( compositeSequence );
        vector.setExpressionExperiment( expExp );

        vector.setBioAssayDimension( bioAssayDimension );
        vector.setQuantitationType( qt );
        vector.setData( blob );
        return vector;
    }

    /**
     * @param datasetSamples List of GeoSamples to be matched up with BioAssays.
     * @param expExp ExpresssionExperiment
     * @return BioAssayDimension representing the samples.
     */
    private BioAssayDimension convertGeoSampleList( List<GeoSample> datasetSamples, ExpressionExperiment expExp ) {
        BioAssayDimension resultBioAssayDimension = BioAssayDimension.Factory.newInstance();

        StringBuilder bioAssayDimName = new StringBuilder();
        Collections.sort( datasetSamples );
        bioAssayDimName.append( expExp.getShortName() ).append( ": " );
        for ( GeoSample sample : datasetSamples ) {
            boolean found;
            String sampleAcc = sample.getGeoAccession();
            bioAssayDimName.append( sampleAcc ).append( "," ); // FIXME this is rather silly!
            found = matchSampleToBioAssay( expExp, resultBioAssayDimension, sampleAcc );
            if ( !found ) {
                // this is normal because not all headings are
                // sample ids.
                log.warn( "No bioassay match for " + sampleAcc );
            }
        }
        log.debug( resultBioAssayDimension.getBioAssays().size() + " Bioassays in biodimension" );
        resultBioAssayDimension.setName( formatName( bioAssayDimName ) );
        resultBioAssayDimension.setDescription( bioAssayDimName.toString() );
        return resultBioAssayDimension;
    }

    /**
     * Given an organisms name from GEO, create or find the taxon in the DB.
     *
     * @param taxonScientificName name as provided by GEO presumed to be a scientific name
     * @return Taxon details
     */
    private Taxon convertOrganismToTaxon( String taxonScientificName ) {
        assert taxonScientificName != null;

        /* if not, either create a new one and persist, or get from db and put in map. */

        if ( taxonScientificName.toLowerCase().startsWith( GeoConverterImpl.RAT ) ) {
            taxonScientificName = GeoConverterImpl.RAT; // we don't distinguish between species.
        }

        Taxon taxon = Taxon.Factory.newInstance();
        taxon.setScientificName( taxonScientificName );
        taxon.setIsSpecies( true );
        taxon.setIsGenesUsable( false );
        if ( taxonService != null ) {
            Taxon t = taxonService.findOrCreate( taxon );
            if ( t != null ) {
                taxon = t;
            }
        }

        taxonScientificNameMap.put( taxonScientificName, taxon );
        return taxon;

    }

    private ArrayDesign convertPlatform( GeoPlatform platform ) {

        if ( seenPlatforms.containsKey( platform.getGeoAccession() ) ) {
            return ( seenPlatforms.get( platform.getGeoAccession() ) );
        }

        ArrayDesign arrayDesign = createMinimalArrayDesign( platform );

        log.info( "Converting platform: " + platform.getGeoAccession() );
        platformDesignElementMap.put( arrayDesign.getShortName(), new HashMap<String, CompositeSequence>() );

        // convert the design element information.
        String identifier = platform.getIdColumnName();
        if ( identifier == null && !platform.getColumnNames().isEmpty() ) {
            throw new IllegalStateException(
                    "Cannot determine the platform design element id column for " + platform + "; " + platform
                            .getColumnNames().size() + " column names available." );
        }

        Collection<String> externalReferences = determinePlatformExternalReferenceIdentifier( platform );
        String descriptionColumn = determinePlatformDescriptionColumn( platform );
        String sequenceColumn = determinePlatformSequenceColumn( platform );
        ExternalDatabase externalDb = determinePlatformExternalDatabase( platform );

        List<String> descriptions = platform.getColumnData( descriptionColumn );

        List<String> sequences = null;
        if ( sequenceColumn != null ) {
            sequences = platform.getColumnData( sequenceColumn );
        }
        // The primary taxon for the array: this should be a taxon that is listed as the platform taxon on geo
        // submission
        String probeOrganismColumn = determinePlatformProbeOrganismColumn( platform );
        Collection<Taxon> platformTaxa = convertPlatformOrganisms( platform, probeOrganismColumn );

        // represent taxa for the probes
        List<String> probeOrganism = null;
        if ( probeOrganismColumn != null ) {
            log.debug( "Organism details found for probes on array " + platform.getGeoAccession() );
            probeOrganism = platform.getColumnData( probeOrganismColumn );
        }

        // The primary taxon for the array: either taxon listed on geo submission, or parent taxon listed on geo
        // submission or predominant probe taxon
        // calcualted using platformTaxa or probeOrganismColumn
        Taxon primaryTaxon = this.getPrimaryArrayTaxon( platformTaxa, probeOrganism );

        if ( primaryTaxon == null ) {
            throw new IllegalStateException( "No taxon could be determined for platform: " + arrayDesign );
        }

        arrayDesign.setPrimaryTaxon( primaryTaxon );

        // We don't get reporters from GEO SOFT files.
        // arrayDesign.setReporters( new HashSet() );

        if ( StringUtils.isNotBlank( platform.getManufacturer() ) ) {
            Contact manufacturer = Contact.Factory.newInstance();
            manufacturer.setName( platform.getManufacturer() );
            arrayDesign.setDesignProvider( manufacturer );
        }

        arrayDesign.getExternalReferences().add( convertDatabaseEntry( platform ) );

        seenPlatforms.put( platform.getGeoAccession(), arrayDesign );

        if ( identifier == null ) {
            // we don't get any probe information; e.g., MPSS, SAGE, Exon arrays.
            log.warn( "No identifiers, so platform elements will be skipped" );
            return arrayDesign;
        }

        convertPlatformElements( identifier, platform, arrayDesign, externalReferences, probeOrganismColumn, externalDb,
                descriptions, sequences, probeOrganism, primaryTaxon );

        return arrayDesign;
    }

    /**
     * Convert the elements/probes.
     */
    private void convertPlatformElements( String identifier, GeoPlatform platform, ArrayDesign arrayDesign,
            Collection<String> externalReferences, String probeOrganismColumn, ExternalDatabase externalDb,
            List<String> descriptions, List<String> sequences, List<String> probeOrganism, Taxon primaryTaxon ) {

        /*
         * This is a very commonly found column name in files, it seems standard in GEO. If we don't find it, it's okay.
         */
        List<String> cloneIdentifiers = platform.getColumnData( "CLONE_ID" );
        List<String> identifiers = platform.getColumnData( identifier );

        if ( identifiers == null ) {
            // we don't get any probe information; e.g., MPSS, SAGE, Exon arrays.
            log.warn( "No identifiers, so platform elements will be skipped" );
            return;
        }

        if ( !platform.useDataFromGeo() && !forceConvertElements ) {
            log.warn( "Will not convert elements for this platform - set forceConvertElements to override" );
            return;
        }

        assert cloneIdentifiers == null || cloneIdentifiers.size() == identifiers.size();

        List<List<String>> externalRefs = null;
        if ( externalReferences != null ) {
            externalRefs = platform.getColumnData( externalReferences );
        }

        if ( externalRefs != null ) {
            assert externalRefs.iterator().next().size() == identifiers.size() : "Unequal numbers of identifiers and external references! "
                    + externalRefs.iterator().next().size()
                    + " != " + identifiers.size();
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Converting " + identifiers.size() + " probe identifiers on GEO platform " + platform
                    .getGeoAccession() );
        }

        Iterator<String> descIter = null;

        if ( descriptions != null ) {
            descIter = descriptions.iterator();
        }

        // http://www.ncbi.nlm.nih.gov/RefSeq/key.html#accessions : "RefSeq accession numbers can be
        // distinguished from GenBank accessions by their prefix distinct format of [2 characters|underbar]"
        Pattern refSeqAccessionPattern = Pattern.compile( "^[A-Z]{2}_" );

        boolean strictSelection = false;

        if ( identifiers.size() > tooManyElements ) {
            // something odd like an exon array.
            log.warn( "Platform has more elements than expected, turning on strict selection method" );
            strictSelection = true;
        }

        List<String> skipped = new ArrayList<>();
        Collection<CompositeSequence> compositeSequences = new ArrayList<>( 5000 );
        int i = 0; // to get sequences, if we have them, and clone identifiers.
        for ( String id : identifiers ) {
            String externalAccession = null;
            if ( externalRefs != null ) {
                externalAccession = getExternalAccession( externalRefs, i );
            }

            if ( strictSelection && StringUtils.isBlank( externalAccession ) ) {

                // currently this is crafted to deal with affymetrix exon arrays, but could be expanded.

                // mrna_assignment is less strict than gene_assignement

                // salvage it if it has a gene assignment.
                // String filteringColumn = "gene_assignment";
                String filteringColumn = "gene_assignment";
                if ( platform.getColumnNames().contains( filteringColumn ) ) {
                    String cd = platform.getColumnData( filteringColumn ).get( i );
                    if ( StringUtils.isBlank( cd ) || cd.equals( "---" ) ) {

                        skipped.add( id );
                        if ( skipped.size() % 10000 == 0 ) {
                            log.info(
                                    "Skipped " + skipped.size() + " elements due to strict selection; last was " + id );
                        }
                        i++;
                        continue;
                    }
                    // keep it.
                } else {
                    // we just skip ones that don't have an external accession.
                    continue;
                }

                // remaining case here: externalAccession is blank, but there is another column that we think saves it.
            }

            String cloneIdentifier = cloneIdentifiers == null ? null : cloneIdentifiers.get( i );

            String description = "";
            if ( externalAccession != null ) {
                String[] refs = externalAccession.split( "," );
                if ( refs.length > 1 ) {
                    description = "Multiple external sequence references: " + externalAccession + "; ";
                    externalAccession = refs[0];
                }
            }

            if ( descIter != null )
                description = description + " " + descIter.next();

            CompositeSequence cs = CompositeSequence.Factory.newInstance();
            String probeName = platform.getProbeNamesInGemma().get( id );
            if ( probeName == null ) {
                probeName = id;
                if ( log.isDebugEnabled() )
                    log.debug( "Probe retaining original name: " + probeName );
                platform.getProbeNamesInGemma().put( id, id ); // must make sure this is populated.
            } else {
                if ( log.isDebugEnabled() )
                    log.debug( "Found probe: " + probeName );
            }

            cs.setName( probeName );
            cs.setDescription( description );
            cs.setArrayDesign( arrayDesign );

            // LMD:1647- If There is a Organism Column given for the probe then set taxon from that overwriting platform
            // if probeOrganismColumn is set but for this probe no taxon do not set probeTaxon and thus create no
            // biosequence
            Taxon probeTaxon = Taxon.Factory.newInstance();
            if ( probeOrganism != null && StringUtils.isNotBlank( probeOrganism.get( i ) ) ) {
                probeTaxon = convertProbeOrganism( probeOrganism.get( i ) );
            }
            // if there are no probe taxons then all the probes should take the taxon from the primary taxon
            if ( probeOrganismColumn == null ) {
                probeTaxon = primaryTaxon;
            }

            BioSequence bs = createMinimalBioSequence( probeTaxon );

            boolean isRefseq = false;

            // ExternalDB will be null if it's IMAGE (this is really pretty messy, sorry)
            if ( StringUtils.isNotBlank( externalAccession ) && isGenbank( externalDb ) ) {
                Matcher refSeqAccessionMatcher = refSeqAccessionPattern.matcher( externalAccession );
                isRefseq = refSeqAccessionMatcher.matches();
                bs.setName( externalAccession );
            } else if ( StringUtils.isNotBlank( cloneIdentifier ) ) {
                bs.setName( cloneIdentifier );
            } else {
                bs.setName( id );
            }

            /*
             * If we are given a sequence (as in, AGTC), we don't need the genbank identifier, which is probably not
             * correct anyway.
             */
            if ( sequences != null && StringUtils.isNotBlank( sequences.get( i ) ) ) {
                bs.setSequence( sequences.get( i ) );
                bs.setIsApproximateLength( false );
                bs.setLength( ( long ) bs.getSequence().length() );
                bs.setType( SequenceType.DNA );
                bs.setName( id );
                bs.setDescription(
                        "Sequence from platform " + platform.getGeoAccession() + " provided by manufacturer. "
                                + ( externalAccession != null ? "Used in leiu of " + externalAccession : "No external accession provided" ) );
            } else if ( externalAccession != null && !isRefseq && externalDb != null ) {

                /*
                 * We also don't store them if they are refseq ids, because refseq ids are generally not the actual
                 * sequences put on arrays.
                 */

                DatabaseEntry dbe = createDatabaseEntry( externalDb, externalAccession, bs );
                bs.setSequenceDatabaseEntry( dbe );
            }

            /*
             * If we have no basis for describing the sequence, we have to skip it.
             */
            if ( StringUtils.isBlank( externalAccession ) && StringUtils.isBlank( cloneIdentifier ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Blank external reference and clone id for " + cs + " on " + arrayDesign
                            + ", no biological characteristic can be added." );
                }
            } else if ( probeTaxon == null ) {
                /*
                 * FIXME we might want to just skip the probe entirely.
                 */
                if ( log.isDebugEnabled() ) {
                    log.debug( "No valid taxon identified for " + cs + " on " + arrayDesign
                            + ", no biological characteristic can be added." );
                }
            } else if ( probeTaxon.getId() != null ) {
                // IF there is no taxon given for probe do not create a biosequence otherwise bombs as there is no taxon
                // to persist
                cs.setBiologicalCharacteristic( bs );
            }

            compositeSequences.add( cs );
            platformDesignElementMap.get( arrayDesign.getShortName() ).put( probeName, cs );

            i++;
        }
        arrayDesign.setCompositeSequences( new HashSet<>( compositeSequences ) );
        arrayDesign.setAdvertisedNumberOfDesignElements( compositeSequences.size() );

        if ( !skipped.isEmpty() ) {
            log.info( "Skipped " + skipped.size() + " elements due to strict selection; last was " + skipped
                    .get( skipped.size() - 1 ) );
        }

        if ( arrayDesign.getCompositeSequences().size() > tooManyElements ) {
            // this is just a safeguard; perhaps temporary.
            throw new IllegalStateException(
                    "Platform has too many elements to be loaded. " + arrayDesign.getCompositeSequences().size() );
        }

        log.info( arrayDesign.getCompositeSequences().size() + " elements on the platform" );
    }

    /**
     * Retrieve full taxon details for a platform given the organism's scientific name in GEO. If multiple organisms are
     * recorded against an array only first taxon details are returned. Warning is given when no column is found to give
     * the taxa for the probes
     *
     * @param platform GEO platform details
     * @param probeTaxonColumnName Column name of probe taxa
     * @return List of taxa on platform
     */
    private Collection<Taxon> convertPlatformOrganisms( GeoPlatform platform, String probeTaxonColumnName ) {
        Collection<String> organisms = platform.getOrganisms();
        Collection<Taxon> platformTaxa = new HashSet<>();
        StringBuilder taxaOnPlatform = new StringBuilder();

        if ( organisms.isEmpty() ) {
            return platformTaxa;
        }

        for ( String taxonScientificName : organisms ) {
            if ( taxonScientificName == null )
                continue;
            taxaOnPlatform.append( ": " ).append( taxonScientificName );
            // make sure add scientific name to map for platform
            if ( taxonScientificNameMap.containsKey( taxonScientificName ) ) {
                platformTaxa.add( taxonScientificNameMap.get( taxonScientificName ) );
            } else {
                platformTaxa.add( convertOrganismToTaxon( taxonScientificName ) );
            }
        }

        // multiple organisms are found on the platform yet there is no column defined to represent taxon for the
        // probes.
        if ( platformTaxa.size() > 1 && probeTaxonColumnName == null ) {
            /*
             * This is okay if all the platformTaxa have the same parent. Here we're just doing a check.
             */
            Taxon parentTaxon = null;
            for ( Taxon taxon : platformTaxa ) {
                this.taxonService.thaw( taxon );
                if ( taxon.getParentTaxon() != null ) {
                    if ( parentTaxon != null && !parentTaxon.equals( taxon.getParentTaxon() ) ) {
                        throw new IllegalArgumentException(
                                platformTaxa.size() + " taxon found on platform" + taxaOnPlatform
                                        + " but there is no probe specific taxon Column found for platform " + platform
                                        + " and the parentTaxon is not the same for the taxa." );
                    }
                    parentTaxon = taxon.getParentTaxon();

                }
            }

        }
        // no platform organism given
        if ( platformTaxa.size() == 0 ) {
            throw new IllegalArgumentException( "No organisms found on platform  " + platform );
        }
        return platformTaxa;

    }

    /**
     * Retrieve taxon details for a probe given an abbreviation or scientific name. All scientific names should be in
     * the map as they were set there by the convertPlatform method. If the abbreviation is not found in the database
     * then stop processing as the organism name is likely to be an unknown abbreviation.
     *
     * @param probeOrganism scientific name, common name or abbreviation of organism associated to a biosequence.
     * @return Taxon of biosequence.
     * @throws IllegalArgumentException taxon supplied has not been processed before, it does not match the scientific
     *         names used in platform definition and does not match a known abbreviation in the database.
     */
    private Taxon convertProbeOrganism( String probeOrganism ) {
        Taxon taxon = Taxon.Factory.newInstance();
        // Check if we have processed this organism before as defined by scientific or abbreviation definition.
        assert probeOrganism != null;

        /*
         * Detect blank taxon. We support 'n/a' here .... a little kludgy but shows up in some files.
         */
        if ( StringUtils.isBlank( probeOrganism ) || probeOrganism.equals( "n/a" ) ) {
            return null;
        }
        if ( taxonScientificNameMap.containsKey( probeOrganism ) ) {
            return taxonScientificNameMap.get( probeOrganism );
        }
        if ( taxonAbbreviationMap.containsKey( probeOrganism ) ) {
            return taxonAbbreviationMap.get( probeOrganism );
        }

        taxon.setAbbreviation( probeOrganism );
        // taxon not processed before check database.
        if ( taxonService != null ) {
            Taxon t = taxonService.findByAbbreviation( probeOrganism.toLowerCase() );

            if ( t != null ) {
                taxon = t;
                taxonAbbreviationMap.put( taxon.getAbbreviation(), t );
            } else {

                t = taxonService.findByCommonName( probeOrganism.toLowerCase() );

                if ( t != null ) {
                    taxon = t;
                    taxonAbbreviationMap.put( taxon.getAbbreviation(), t );
                } else {

                    // if probe organism can not be found i.e it is not a known abbreviation or scientific name
                    // and it was not already created during platform organism processing then warn user. Examples would
                    // be "taxa" like "ILMN Controls". See bug 3207 (we used to throw an exception)
                    log.warn( "'" + probeOrganism + "' is not recognized as a taxon in Gemma" );
                    return null;
                }
            }
        }
        return taxon;

    }

    private void convertPubMedIds( GeoSeries series, ExpressionExperiment expExp ) {
        Collection<String> ids = series.getPubmedIds();
        if ( ids == null || ids.size() == 0 )
            return;
        for ( String string : ids ) {
            BibliographicReference bibRef = BibliographicReference.Factory.newInstance();
            DatabaseEntry pubAccession = DatabaseEntry.Factory.newInstance();
            pubAccession.setAccession( string );
            ExternalDatabase ed = ExternalDatabase.Factory.newInstance();
            ed.setName( "PubMed" );
            pubAccession.setExternalDatabase( ed );
            bibRef.setPubAccession( pubAccession );
            expExp.setPrimaryPublication( bibRef );
            break; // usually just one...
        }
    }

    /**
     * Note that this is apparently never actually used?
     */
    private VocabCharacteristic convertReplicatationType( ReplicationType repType ) {
        VocabCharacteristic result = VocabCharacteristic.Factory.newInstance();
        result.setCategory( "replicate" );
        result.setCategoryUri( "http://www.ebi.ac.uk/efo/EFO_0000683" /* replicate */ );
        result.setEvidenceCode( GOEvidenceCode.IIA );
        ExternalDatabase mged = ExternalDatabase.Factory.newInstance();
        mged.setName( "MGED Ontology" );
        mged.setType( DatabaseType.ONTOLOGY );

        if ( repType.equals( ReplicationType.biologicalReplicate ) ) {
            result.setValue( "biological replicate" );
            result.setValueUri( "http://www.ebi.ac.uk/efo/EFO_0002091" /* biological replicate */ );
        } else if ( repType.equals( ReplicationType.technicalReplicateExtract ) ) {
            result.setValue( "technical replicate" );
            result.setValueUri( "http://www.ebi.ac.uk/efo/EFO_0002090" /* technical replicate */ );
        } else if ( repType.equals( ReplicationType.technicalReplicateLabeledExtract ) ) {
            result.setValue( "technical replicate" );
            result.setValueUri( "http://www.ebi.ac.uk/efo/EFO_0002090" /* technical replicate */ );
        } else {
            throw new IllegalStateException( "Unhandled replication type: " + repType );
        }

        return result;

    }

    /**
     * Convert a variable into a ExperimentalFactor
     */
    private ExperimentalFactor convertReplicationToFactor( GeoReplication replication ) {
        log.debug( "Converting replication " + replication.getType() );
        ExperimentalFactor result = ExperimentalFactor.Factory.newInstance();
        result.setName( replication.getType().toString() );
        result.setDescription( replication.getDescription() );
        result.setType( FactorType.CATEGORICAL );
        VocabCharacteristic term = convertReplicatationType( replication.getType() );

        result.setCategory( term );
        return result;

    }

    private FactorValue convertReplicationToFactorValue( GeoReplication replication ) {
        FactorValue factorValue = FactorValue.Factory.newInstance();
        VocabCharacteristic term = convertReplicatationType( replication.getType() );
        factorValue.setValue( term.getValue() );
        factorValue.getCharacteristics().add( term );
        return factorValue;
    }

    private void convertReplicationToFactorValue( GeoReplication replication, ExperimentalFactor factor ) {
        FactorValue factorValue = convertReplicationToFactorValue( replication );
        factor.getFactorValues().add( factorValue );
    }

    /**
     * A Sample corresponds to a BioAssay; the channels correspond to BioMaterials.
     */
    private BioAssay convertSample( GeoSample sample, BioMaterial bioMaterial, ExperimentalDesign experimentalDesign ) {
        if ( sample == null ) {
            log.warn( "Null sample" );
            return null;
        }

        if ( sample.getGeoAccession() == null || sample.getGeoAccession().length() == 0 ) {
            log.error( "No GEO accession for sample" );
            return null;
        }

        log.debug( "Converting sample: " + sample.getGeoAccession() );

        BioAssay bioAssay = BioAssay.Factory.newInstance();
        String title = sample.getTitle();
        if ( StringUtils.isBlank( title ) ) {
            // throw new IllegalArgumentException( "Title cannot be blank for sample " + sample );
            log.warn( "Blank title for sample " + sample + ", using accession number instead." );
            sample.setTitle( sample.getGeoAccession() );
        }
        bioAssay.setName( sample.getTitle() );
        bioAssay.setDescription( sample.getDescription() );
        bioAssay.setAccession( convertDatabaseEntry( sample ) );
        bioAssay.setIsOutlier( false );
        bioAssay.setSequencePairedReads( false );

        /*
         * NOTE - according to GEO (http://www.ncbi.nlm.nih.gov/projects/geo/info/soft2.html) "variable information is
         * optional and does not appear in Series records or downloads, but will be used to assemble corresponding GEO
         * DataSet records" If we would get that information we would pass it into this method as
         * expExp.getExperimentalDesign().getExperimentalFactors().
         */

        // : use the ones from the ExperimentalFactor. In other words, these factor values should correspond to
        // experimentalfactors
        Collection<ExperimentalFactor> experimentalFactors = experimentalDesign.getExperimentalFactors();
        for ( GeoReplication replication : sample.getReplicates() ) {
            matchSampleReplicationToExperimentalFactorValue( bioMaterial, experimentalFactors, replication );
        }

        // : use the ones from the ExperimentalFactor.
        for ( GeoVariable variable : sample.getVariables() ) {
            matchSampleVariableToExperimentalFactorValue( bioMaterial, experimentalFactors, variable );
        }

        for ( GeoChannel channel : sample.getChannels() ) {
            /*
             * In reality GEO does not have information about the samples run on each channel. We're just making it up.
             * So we need to just add the channel information to the biomaterials we have already. Note taxon is now
             * taken from sample FIXME this is no longer accurate; GEO has species information for each channel.
             *
             * Actually this has changed. GEO does store channel information. However, we don't use it (see bug 2902).
             */
            if ( bioAssay.getSampleUsed() != null ) {
                bioMaterial = bioAssay.getSampleUsed();
                log.info( "Multi-sample information stored in biomaterial " + bioMaterial );
            }
            convertChannel( sample, channel, bioMaterial );
            bioAssay.setSampleUsed( bioMaterial );
        }

        // Taxon lastTaxon = null;

        for ( GeoPlatform platform : sample.getPlatforms() ) {
            ArrayDesign arrayDesign;
            if ( seenPlatforms.containsKey( platform.getGeoAccession() ) ) {
                arrayDesign = seenPlatforms.get( platform.getGeoAccession() );
            } else {
                // platform not exist yet
                arrayDesign = convertPlatform( platform );
            }

            bioAssay.setArrayDesignUsed( arrayDesign );

        }

        return bioAssay;
    }

    /**
     * Convert a GEO series into one or more ExpressionExperiments. The "more than one" case comes up if the are
     * platforms from more than one organism represented in the series, or if 'split by platform' is set, or if multiple
     * species were run on a single platform. If the series is split into two or more ExpressionExperiments, each refers
     * to a modified GEO accession such as GSE2393.1, GSE2393.2 etc for each organism/platform
     * Similarly, because there is no concept of "biomaterial" in GEO, samples that are inferred to have been run using
     * the same biomaterial. The biomaterials are given names after the GSE and the bioAssays (GSMs) such as
     * GSE2939_biomaterial_1|GSM12393|GSN12394.
     */
    private Collection<ExpressionExperiment> convertSeries( GeoSeries series ) {

        Collection<ExpressionExperiment> converted = new HashSet<>();

        // figure out if there are multiple species involved here.

        Map<String, Collection<GeoData>> organismDatasetMap = getOrganismDatasetMap( series );
        Map<GeoPlatform, Collection<GeoData>> platformDatasetMap = getPlatformDatasetMap( series );
        Map<String, Collection<GeoSample>> organismSampleMap = getOrganismSampleMap( series );
        // get map of platform to dataset.

        if ( organismDatasetMap.size() > 1 ) {
            log.warn( "**** Multiple-species series, with multiple datasets. This series will be split into "
                    + organismDatasetMap.size() + " experiments. ****" );
            int i = 1;
            for ( String organism : organismDatasetMap.keySet() ) {
                convertSpeciesSpecific( series, converted, organismDatasetMap, i, organism );
                i++;
            }
        } else if ( organismSampleMap.size() > 1 ) {
            log.warn( "**** Multiple-species series. This series will be split into " + organismSampleMap.size()
                    + " experiments. ****" );
            int i = 1;
            for ( String organism : organismSampleMap.keySet() ) {
                convertSpeciesSpecificSamples( series, converted, organismSampleMap, i, organism );
                i++;
            }
        } else if ( platformDatasetMap.size() > 1 && this.splitByPlatform ) {
            int i = 1;
            for ( GeoPlatform platform : platformDatasetMap.keySet() ) {
                convertByPlatform( series, converted, platformDatasetMap, i, platform );
                i++;
            }
        } else {
            ExpressionExperiment ee = this.convertSeriesSingle( series );
            if ( ee != null )
                converted.add( ee );
        }

        return converted;
    }

    /**
     * Use this when we don't have a GDS for a GSE.
     */
    private void convertSeriesDataVectors( GeoSeries geoSeries, ExpressionExperiment expExp ) {
        /*
         * Tricky thing is that series contains data from multiple platforms.
         */
        Map<GeoPlatform, List<GeoSample>> platformSamples = DatasetCombiner.getPlatformSampleMap( geoSeries );

        for ( GeoPlatform platform : platformSamples.keySet() ) {
            List<GeoSample> samples = platformSamples.get( platform );
            log.debug( samples.size() + " samples on " + platform );
            convertVectorsForPlatform( geoSeries.getValues(), expExp, samples, platform );
            geoSeries.getValues().clear( platform );
        }

    }

    private ExpressionExperiment convertSeriesSingle( GeoSeries series ) {
        if ( series == null )
            return null;
        log.info( "Converting series: " + series.getGeoAccession() );

        Collection<GeoDataset> dataSets = series.getDatasets();
        Collection<String> dataSetsToSkip = new HashSet<>();
        Collection<GeoSample> samplesToSkip = new HashSet<>();
        checkForDataToSkip( series, dataSetsToSkip, samplesToSkip );
        if ( dataSets.size() > 0 && dataSetsToSkip.size() == dataSets.size() ) {
            return null;
        }

        if ( !isUsable( series ) ) {
            log.warn( "Series was not usable: types=" + StringUtils.join( series.getSeriesTypes(), " " ) );
            return null;
        }

        ExpressionExperiment expExp = ExpressionExperiment.Factory.newInstance();
        expExp.setDescription( "" );

        expExp.setDescription( series.getSummaries() + ( series.getSummaries().endsWith( "\n" ) ? "" : "\n" ) );
        if ( series.getLastUpdateDate() != null ) {
            expExp.setDescription(
                    expExp.getDescription() + "Last Updated (by provider): " + series.getLastUpdateDate() + "\n" );
        }

        expExp.setName( series.getTitle() );
        expExp.setShortName( series.getGeoAccession() );

        convertContacts( series, expExp );

        convertPubMedIds( series, expExp );

        expExp.setAccession( convertDatabaseEntry( series ) );

        LocalFile expExpRawDataFile = convertSupplementaryFileToLocalFile( series );
        expExp.setRawDataFile( expExpRawDataFile );

        ExperimentalDesign design = ExperimentalDesign.Factory.newInstance();
        design.setDescription( "" );
        design.setName( "" );
        Collection<GeoVariable> variables = series.getVariables().values();
        for ( GeoVariable variable : variables ) {
            log.debug( "Adding variable " + variable );
            ExperimentalFactor ef = convertVariableToFactor( variable );
            convertVariableToFactorValue( variable, ef );
            design.getExperimentalFactors().add( ef );
            design.setName( variable.getDescription() + " " + design.getName() );
        }

        if ( series.getKeyWords().size() > 0 ) {
            for ( String keyWord : series.getKeyWords() ) {
                // design.setDescription( design.getDescription() + " Keyword: " + keyWord );
                Characteristic o = Characteristic.Factory.newInstance();
                o.setDescription( "GEO Keyword" );
                o.setValue( keyWord );
                o.setEvidenceCode( GOEvidenceCode.IIA );
                o.setDescription( "Keyword from GEO series definition file." );
            }
        }

        if ( series.getOverallDesign() != null ) {
            design.setDescription( design.getDescription() + " Overall design: " + series.getOverallDesign() );
        }

        Collection<GeoReplication> replication = series.getReplicates().values();
        for ( GeoReplication replicate : replication ) {
            log.debug( "Adding replication " + replicate );
            ExperimentalFactor ef = convertReplicationToFactor( replicate );
            convertReplicationToFactorValue( replicate, ef );
            design.getExperimentalFactors().add( ef );
        }

        expExp.setExperimentalDesign( design );

        // GEO does not have the concept of a biomaterial.
        Collection<GeoSample> allSeriesSamples = series.getSamples();
        log.info( "Series has " + series.getSamples().size() + " samples" );
        if ( samplesToSkip.size() > 0 ) {
            log.info( samplesToSkip.size() + " samples will be skipped" );
        }
        expExp.setBioAssays( new HashSet<BioAssay>() );

        if ( series.getSampleCorrespondence().size() == 0 ) {
            throw new IllegalArgumentException( "No sample correspondence!" );
        }

        // spits out a big summary of the correspondence.
        if ( log.isDebugEnabled() )
            log.debug( series.getSampleCorrespondence() );
        int numBioMaterials = 0;

        /*
         * For each _set_ of "corresponding" samples (from the same RNA, or so we think) we make up a new BioMaterial.
         */

        Collection<String> seen = new HashSet<>();
        for ( Iterator<Set<String>> iter = series.getSampleCorrespondence().iterator(); iter.hasNext(); ) {

            Set<String> correspondingSamples = iter.next();
            if ( correspondingSamples.isEmpty() )
                continue; // can happen after removing samples (multitaxon)

            BioMaterial bioMaterial = BioMaterial.Factory.newInstance();
            String bioMaterialName = getBiomaterialPrefix( series, ++numBioMaterials );
            StringBuilder bioMaterialDescription = new StringBuilder(
                    BIOMATERIAL_DESCRIPTION_PREFIX + series.getGeoAccession() );

            // From the series samples, find the sample that corresponds and convert it.
            for ( String cSample : correspondingSamples ) {
                boolean found = false;
                for ( GeoSample sample : allSeriesSamples ) {
                    if ( sample == null || sample.getGeoAccession() == null ) {
                        log.warn( "Null sample or no accession for " + sample );
                        continue;
                    }

                    if ( samplesToSkip.contains( sample ) ) {
                        continue;
                    }

                    String accession = sample.getGeoAccession();

                    if ( accession.equals( cSample ) ) {

                        if ( seen.contains( accession ) ) {
                            log.error( "Got " + accession + " twice, this time in set " + correspondingSamples );
                        }
                        seen.add( accession );

                        BioAssay ba = convertSample( sample, bioMaterial, expExp.getExperimentalDesign() );

                        assert ( ba != null );
                        LocalFile rawDataFile = convertSupplementaryFileToLocalFile( sample );
                        ba.setRawDataFile( rawDataFile );// deal with null at UI

                        // TODO these custom string prefixes should be made into constants, need to make public for use
                        // by ExpressionExperimentAnnotator

                        ba.setDescription( ba.getDescription() + "\nSource GEO sample is " + sample.getGeoAccession()
                                + "\nLast updated (according to GEO): " + sample.getLastUpdateDate() );

                        assert ba.getSampleUsed() != null;
                        bioMaterial.getBioAssaysUsedIn().add( ba );
                        bioMaterialDescription.append( "," ).append( sample );
                        expExp.getBioAssays().add( ba );
                        found = true;
                        break;
                    }
                }
                if ( !found ) {
                    if ( log.isDebugEnabled() )
                        log.debug( "No sample found in " + series + " to match " + cSample
                                + "; this can happen if some samples were not run on all platforms." );

                }
            }
            bioMaterial.setName( bioMaterialName );
            bioMaterial.setDescription( bioMaterialDescription.toString() );
        }

        log.info( "Expression Experiment from " + series + " has " + expExp.getBioAssays().size() + " bioassays and "
                + numBioMaterials + " biomaterials." );

        int expectedNumSamples = series.getSamples().size() - samplesToSkip.size();
        int actualNumSamples = expExp.getBioAssays().size();
        if ( expectedNumSamples > actualNumSamples ) {
            log.warn( ( expectedNumSamples - actualNumSamples ) + " samples were not in the 'sample correspondence'"
                    + " and have been omitted. Possibly they were in the Series (GSE) but not in the corresponding Dataset (GDS)?" );
        }

        // Dataset has additional information about the samples.

        if ( dataSets.size() == 0 ) {
            // we miss extra description and the subset information.
            if ( series.getValues().hasData() )
                convertSeriesDataVectors( series, expExp );
        } else {
            for ( GeoDataset dataset : dataSets ) {
                if ( dataSetsToSkip.contains( dataset.getGeoAccession() ) )
                    continue;
                convertDataset( dataset, expExp );
            }
        }

        return expExp;
    }

    private void convertSpeciesSpecific( GeoSeries series, Collection<ExpressionExperiment> converted,
            Map<String, Collection<GeoData>> organismDatasetMap, int i, String organism ) {
        GeoSeries speciesSpecific = new GeoSeries();

        Collection<GeoData> datasets = organismDatasetMap.get( organism );
        assert datasets.size() > 0;

        for ( GeoSample sample : series.getSamples() ) {
            // ugly, we have to assume there is only one platform and one organism...
            if ( sample.getPlatforms().iterator().next().getOrganisms().iterator().next().equals( organism ) ) {
                speciesSpecific.addSample( sample );
            }
        }

        // strip out samples that aren't from this organism.

        for ( GeoData dataset : datasets ) {
            if ( dataset instanceof GeoDataset ) {
                ( ( GeoDataset ) dataset ).dissociateFromSeries( series );
                speciesSpecific.addDataSet( ( GeoDataset ) dataset );
            }
        }

        /*
         * Basically copy over most of the information
         */
        speciesSpecific.setContact( series.getContact() );
        speciesSpecific.setContributers( series.getContributers() );
        speciesSpecific.setGeoAccession( series.getGeoAccession() + "." + i );
        speciesSpecific.setKeyWords( series.getKeyWords() );
        speciesSpecific.setOverallDesign( series.getOverallDesign() );
        speciesSpecific.setPubmedIds( series.getPubmedIds() );
        speciesSpecific.setReplicates( series.getReplicates() );
        speciesSpecific.setSampleCorrespondence( series.getSampleCorrespondence() );
        speciesSpecific.setSummaries( series.getSummaries() );
        speciesSpecific.setTitle( series.getTitle() + " - " + organism );
        speciesSpecific.setWebLinks( series.getWebLinks() );
        speciesSpecific.setValues( series.getValues() );
        speciesSpecific.getSeriesTypes().addAll( series.getSeriesTypes() ); // even though this might apply to samples left behind in other part.

        converted.add( convertSeriesSingle( speciesSpecific ) );
    }

    /**
     * Handle the case where a single series has samples from more than one species.
     *
     * @param organismSampleMap the samples divvied up by organism
     */
    private void convertSpeciesSpecificSamples( GeoSeries series, Collection<ExpressionExperiment> converted,
            Map<String, Collection<GeoSample>> organismSampleMap, int i, String organism ) {

        GeoSeries speciesSpecific = new GeoSeries();

        Collection<GeoSample> samples = organismSampleMap.get( organism );

        for ( GeoSample s : samples ) {
            speciesSpecific.addSample( s );
        }

        /*
         * Strip out sample correspondence for samples not for this organism.
         */
        GeoSampleCorrespondence sampleCorrespondence = series.getSampleCorrespondence().copy();

        for ( String o : organismSampleMap.keySet() ) {
            if ( o.equals( organism ) ) {
                continue;
            }
            for ( GeoSample s : organismSampleMap.get( o ) ) {
                sampleCorrespondence.removeSample( s.getGeoAccession() );
            }
        }

        /*
         * Basically copy over most of the information
         */
        speciesSpecific.setContact( series.getContact() );
        speciesSpecific.setContributers( series.getContributers() );
        speciesSpecific.setGeoAccession( series.getGeoAccession() + "." + i );
        speciesSpecific.setKeyWords( series.getKeyWords() );
        speciesSpecific.setOverallDesign( series.getOverallDesign() );
        speciesSpecific.setPubmedIds( series.getPubmedIds() );
        speciesSpecific.setReplicates( series.getReplicates() );
        speciesSpecific.setSampleCorrespondence( sampleCorrespondence );
        speciesSpecific.setSummaries( series.getSummaries() );
        speciesSpecific.setTitle( series.getTitle() + " - " + organism );
        speciesSpecific.setWebLinks( series.getWebLinks() );
        speciesSpecific.setValues( series.getValues( speciesSpecific.getSamples() ) );
        speciesSpecific.getSeriesTypes().addAll( series.getSeriesTypes() );

        converted.add( convertSeriesSingle( speciesSpecific ) );

    }

    private void convertSubsetAssociations( ExpressionExperiment result, GeoDataset geoDataset ) {
        for ( GeoSubset subset : geoDataset.getSubsets() ) {
            if ( log.isDebugEnabled() )
                log.debug( "Converting subset to experimentalFactor" + subset.getType() );
            convertSubsetToExperimentalFactor( result, subset );
        }
    }

    /**
     * Creates a new factorValue, or identifies an existing one, matching the subset. If it is a new one it adds it to
     * the given experimentalFactor.
     */
    private FactorValue convertSubsetDescriptionToFactorValue( GeoSubset geoSubSet,
            ExperimentalFactor experimentalFactor ) {
        // By definition each subset defines a new factor value.
        FactorValue factorValue = FactorValue.Factory.newInstance();

        Characteristic term = Characteristic.Factory.newInstance();
        convertVariableType( term, geoSubSet.getType() );
        if ( term.getCategory() != null ) {
            term.setValue( geoSubSet.getDescription() );
            term.setDescription( "Converted from GEO subset " + geoSubSet.getGeoAccession() );
            factorValue.getCharacteristics().add( term );
        }

        factorValue.setExperimentalFactor( experimentalFactor );
        factorValue.setValue( geoSubSet.getDescription() );

        /* Check that there isn't already a factor value for this in the factor */

        for ( FactorValue fv : experimentalFactor.getFactorValues() ) {
            if ( fv.equals( factorValue ) ) {
                log.debug( factorValue + " is matched by existing factorValue for " + experimentalFactor );
                return fv;
            }
        }
        experimentalFactor.getFactorValues().add( factorValue );
        return factorValue;
    }

    private FactorValue convertTypeToFactorValue( VariableType type, String value ) {
        FactorValue factorValue = FactorValue.Factory.newInstance();
        Characteristic term = Characteristic.Factory.newInstance();
        convertVariableType( term, type );
        if ( term.getCategory() != null ) {
            factorValue.setValue( value );
            return factorValue;
        }
        term.setValue( value ); // TODO map onto an ontology.
        factorValue.setValue( term.getValue() );
        factorValue.getCharacteristics().add( term );
        return factorValue;
    }

    /**
     * Convert a variable into a ExperimentalFactor
     */
    private ExperimentalFactor convertVariableToFactor( GeoVariable variable ) {
        log.debug( "Converting variable " + variable.getType() );
        ExperimentalFactor result = ExperimentalFactor.Factory.newInstance();
        result.setName( variable.getType().toString() );
        result.setType( FactorType.CATEGORICAL );
        result.setDescription( variable.getDescription() );
        Characteristic term = Characteristic.Factory.newInstance();
        convertVariableType( term, variable.getType() );

        if ( term.getCategory() != null )
            result.setCategory( term );
        return result;
    }

    /**
     * @return Category will be filled in with a URI but value will just be plain text.
     */
    private FactorValue convertVariableToFactorValue( GeoVariable variable ) {
        log.info( "Converting variable " + variable );
        VariableType type = variable.getType();
        return convertTypeToFactorValue( type, variable.getDescription() );
    }

    private void convertVariableToFactorValue( GeoVariable variable, ExperimentalFactor factor ) {
        FactorValue factorValue = convertVariableToFactorValue( variable );
        factor.getFactorValues().add( factorValue );
    }

    /**
     * Convert a variable, category URI and category filled in. Will not be filled in (null) the case of "Other" or
     * "Organism"
     *
     * @param c to be modified
     * @throws IllegalStateException if it's a variable type we don't know how to handle.
     */
    private void convertVariableType( Characteristic c, VariableType varType ) {
        c.setCategory( null );
        String term = null;
        String uri = null;
        if ( varType.equals( VariableType.age ) ) {
            term = "age";
            uri = "http://www.ebi.ac.uk/efo/EFO_0000246";
        } else if ( varType.equals( VariableType.agent ) ) {
            uri = "http://purl.obolibrary.org/obo/CHEBI_23367";
            term = "molecular entity";
        } else if ( varType.equals( VariableType.cellLine ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000322";
            term = "CellLine";
        } else if ( varType.equals( VariableType.cellType ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000324";
            term = "CellType";
        } else if ( varType.equals( VariableType.developmentStage ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000399";
            term = "developmental stage";
        } else if ( varType.equals( VariableType.diseaseState ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000408";
            term = "disease";
        } else if ( varType.equals( VariableType.dose ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000428";
            term = "dose";
        } else if ( varType.equals( VariableType.gender ) ) {
            // see bug 4317
            uri = "http://purl.obolibrary.org/obo/PATO_0000047";
            term = "sex";
        } else if ( varType.equals( VariableType.genotypeOrVariation ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000513";
            term = "genotype";
        } else if ( varType.equals( VariableType.growthProtocol ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000523";
            term = "grwoth condition";
        } else if ( varType.equals( VariableType.individual ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000542";
            term = "individual";
        } else if ( varType.equals( VariableType.infection ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000651";
            term = "phenotype";
        } else if ( varType.equals( VariableType.isolate ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000246";
            term = "age";
        } else if ( varType.equals( VariableType.metabolism ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000651";
            term = "phenotype";
        } else if ( varType.equals( VariableType.other ) ) {
        } else if ( varType.equals( VariableType.protocol ) ) {
            uri = "http://purl.obolibrary.org/obo/OBI_0000272";
            term = "protocol";
        } else if ( varType.equals( VariableType.shock ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000470";
            term = "environmental stress";
        } else if ( varType.equals( VariableType.species ) ) {
        } else if ( varType.equals( VariableType.specimen ) ) {
            uri = "http://purl.obolibrary.org/obo/OBI_0100051";
            term = "specimen";
        } else if ( varType.equals( VariableType.strain ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0005135";
            term = "strain";
        } else if ( varType.equals( VariableType.stress ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000470";
            term = "environmental stress";
        } else if ( varType.equals( VariableType.temperature ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0001702";
            term = "Temperature";
        } else if ( varType.equals( VariableType.time ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000724";
            term = "timepoint";
        } else if ( varType.equals( VariableType.tissue ) ) {
            uri = "http://www.ebi.ac.uk/efo/EFO_0000635";
            term = "organism part";
        } else {
            throw new IllegalStateException();
        }

        if ( log.isDebugEnabled() )
            log.debug( "Category term: " + term + " " );
        c.setCategory( term );
        c.setCategoryUri( uri );
        c.setEvidenceCode( GOEvidenceCode.IIA );

    }

    /**
     * For data coming from a single platform, create vectors.
     *
     * @param values A GeoValues object holding the parsed results.
     */
    private void convertVectorsForPlatform( GeoValues values, ExpressionExperiment expExp,
            List<GeoSample> datasetSamples, GeoPlatform geoPlatform ) {

        assert datasetSamples.size() > 0 : "No samples in dataset";

        if ( !geoPlatform.useDataFromGeo() ) {
            // see bug 4181
            log.warn(
                    "Platform characteristics indicate data from GEO should be ignored or will not be present anyway ("
                            + geoPlatform + ")" );
            return;
        }

        log.info( "Converting vectors for " + geoPlatform.getGeoAccession() + ", " + datasetSamples.size()
                + " samples." );

        BioAssayDimension bioAssayDimension = convertGeoSampleList( datasetSamples, expExp );

        if ( bioAssayDimension.getBioAssays().size() == 0 )
            throw new IllegalStateException( "No bioAssays in the BioAssayDimension" );

        sanityCheckQuantitationTypes( datasetSamples );

        List<String> quantitationTypes = datasetSamples.iterator().next().getColumnNames();
        List<String> quantitationTypeDescriptions = datasetSamples.iterator().next().getColumnDescriptions();
        boolean first = true;

        /*
         * For the data that are put in 'datasets' (GDS), we know the type of data, but it can be misleading (e.g., Affy
         * data is 'counts'). For others we just have free text in the column descriptions
         */

        for ( String quantitationType : quantitationTypes ) {

            // skip the first quantitationType, it's the ID or ID_REF.
            if ( first ) {
                first = false;
                continue;
            }

            int columnAccordingToSample = quantitationTypes.indexOf( quantitationType );

            int quantitationTypeIndex = values.getQuantitationTypeIndex( geoPlatform, quantitationType );
            log.debug( "Processing " + quantitationType + " (column=" + quantitationTypeIndex
                    + " - according to sample, it's " + columnAccordingToSample + ")" );

            Map<String, List<Object>> dataVectors = makeDataVectors( values, datasetSamples, quantitationTypeIndex );

            if ( dataVectors == null || dataVectors.size() == 0 ) {
                log.debug( "No data for " + quantitationType + " (column=" + quantitationTypeIndex + ")" );
                continue;
            }
            log.info( dataVectors.size() + " data vectors for " + quantitationType );

            Object exampleValue = dataVectors.values().iterator().next().iterator().next();

            QuantitationType qt = QuantitationType.Factory.newInstance();
            qt.setName( quantitationType );
            String description = quantitationTypeDescriptions.get( columnAccordingToSample );
            qt.setDescription( description );
            QuantitationTypeParameterGuesser
                    .guessQuantitationTypeParameters( qt, quantitationType, description, exampleValue );

            int count = 0;
            int skipped = 0;
            for ( String designElementName : dataVectors.keySet() ) {
                List<Object> dataVector = dataVectors.get( designElementName );
                if ( dataVector == null || dataVector.size() == 0 )
                    continue;

                RawExpressionDataVector vector = convertDesignElementDataVector( geoPlatform, expExp, bioAssayDimension,
                        designElementName, dataVector, qt );

                if ( vector == null ) {
                    skipped++;
                    if ( log.isDebugEnabled() )
                        log.debug( "Null vector for DE=" + designElementName + " QT=" + quantitationType );
                    continue;
                }

                if ( log.isTraceEnabled() ) {
                    log.trace( designElementName + " " + qt.getName() + " " + qt.getRepresentation() + " " + dataVector
                            .size() + " elements in vector" );
                }

                expExp.getRawExpressionDataVectors().add( vector );

                if ( ++count % LOGGING_VECTOR_COUNT_UPDATE == 0 && log.isDebugEnabled() ) {
                    log.debug( count + " Data vectors added" );
                }
            }

            if ( count > 0 ) {
                expExp.getQuantitationTypes().add( qt );
                if ( log.isDebugEnabled() && count > 1000 ) {
                    log.debug( count + " Data vectors added for '" + quantitationType + "'" );
                }
            } else {
                log.info( "No vectors were retained for " + quantitationType
                        + " -- usually this is due to all values being missing." );
            }

            if ( skipped > 0 ) {
                log.info( "Skipped " + skipped + " vectors" );
            }
        }
        log.info(
                "Total of " + expExp.getRawExpressionDataVectors().size() + " vectors on platform " + geoPlatform + ", "
                        + expExp.getQuantitationTypes().size() + " quantitation types." );
    }

    private DatabaseEntry createDatabaseEntry( ExternalDatabase externalDb, String externalRef, BioSequence bs ) {
        DatabaseEntry dbe;
        if ( isGenbank( externalDb ) ) {
            // deal with accessions in the form XXXXX.N
            dbe = ExternalDatabaseUtils.getGenbankAccession( externalRef );
            dbe.setExternalDatabase( externalDb ); // make sure it matches the one used here.
            bs.setName( dbe.getAccession() ); // trimmed version.
        } else {
            bs.setName( externalRef );
            dbe = DatabaseEntry.Factory.newInstance();
            dbe.setAccession( externalRef );
            dbe.setExternalDatabase( externalDb );
        }
        return dbe;
    }

    private ArrayDesign createMinimalArrayDesign( GeoPlatform platform ) {
        ArrayDesign arrayDesign = ArrayDesign.Factory.newInstance();
        arrayDesign.setName( platform.getTitle() );
        arrayDesign.setShortName( platform.getGeoAccession() );
        arrayDesign.setDescription( platform.getDescriptions() );
        PlatformType technology = platform.getTechnology();
        if ( technology == PlatformType.dualChannel || technology == PlatformType.dualChannelGenomic
                || technology == PlatformType.spottedOligonucleotide || technology == PlatformType.spottedDNAOrcDNA ) {
            arrayDesign.setTechnologyType( TechnologyType.TWOCOLOR );
        } else if ( technology == PlatformType.singleChannel || technology == PlatformType.oligonucleotideBeads
                || technology == PlatformType.inSituOligonucleotide ) {
            arrayDesign.setTechnologyType( TechnologyType.ONECOLOR );
        } else if ( technology == null ) {
            log.warn( "No technology type available for " + platform + ", provisionally setting to 'dual mode'" );
            arrayDesign.setTechnologyType( TechnologyType.DUALMODE );
        } else if ( technology.equals( PlatformType.MPSS ) ) {
            // we don't support this directly
            arrayDesign.setTechnologyType( TechnologyType.NONE );
        } else if ( technology.equals( PlatformType.SAGE ) || technology.equals( PlatformType.SAGENlaIII ) || technology
                .equals( PlatformType.SAGERsaI ) || technology.equals( PlatformType.SAGESau3A ) || technology
                        .equals( PlatformType.other ) ) {
            // we don't support this directly
            arrayDesign.setTechnologyType( TechnologyType.NONE );
        } else {
            throw new IllegalArgumentException( "Don't know how to interpret technology type " + technology );
        }
        return arrayDesign;
    }

    /**
     * @param taxon Can be null, we will discard this
     */
    private BioSequence createMinimalBioSequence( Taxon taxon ) {
        BioSequence bs = BioSequence.Factory.newInstance();
        bs.setTaxon( taxon );
        bs.setPolymerType( PolymerType.DNA );
        bs.setType( SequenceType.DNA );
        return bs;
    }

    private String determinePlatformDescriptionColumn( GeoPlatform platform ) {
        Collection<String> columnNames = platform.getColumnNames();
        int index = 0;
        for ( String string : columnNames ) {
            if ( GeoConstants.likelyProbeDescription( string ) ) {
                log.debug( string + " appears to indicate the  probe descriptions in column " + index + " for platform "
                        + platform );
                return string;
            }
            index++;
        }
        log.debug( "No platform element description column found for " + platform );
        return null;
    }

    private ExternalDatabase determinePlatformExternalDatabase( GeoPlatform platform ) {
        ExternalDatabase result = ExternalDatabase.Factory.newInstance();

        Collection<String> likelyExternalDatabaseIdentifiers = determinePlatformExternalReferenceIdentifier( platform );
        String dbIdentifierDescription = getDbIdentifierDescription( platform );

        String url;
        if ( dbIdentifierDescription == null ) {
            return null;
        } else if ( dbIdentifierDescription.contains( "LINK_PRE:" ) ) {
            // example: #ORF = ORF reference LINK_PRE:"http://genome-www4.stanford.edu/cgi-bin/SGD/locus.pl?locus="
            url = dbIdentifierDescription.substring( dbIdentifierDescription.indexOf( "LINK_PRE:" ) );
            result.setWebUri( url );
        }

        if ( likelyExternalDatabaseIdentifiers == null || likelyExternalDatabaseIdentifiers.size() == 0 ) {
            throw new IllegalStateException( "No external database identifier column was identified" );
        }

        String likelyExternalDatabaseIdentifier = likelyExternalDatabaseIdentifiers.iterator().next();
        if ( likelyExternalDatabaseIdentifier.equals( "GB_ACC" ) || likelyExternalDatabaseIdentifier.equals( "GB_LIST" )
                || likelyExternalDatabaseIdentifier.toLowerCase().equals( "genbank" ) ) {
            if ( genbank == null ) {
                if ( externalDatabaseService != null ) {
                    genbank = externalDatabaseService.find( "Genbank" );
                } else {
                    result.setName( "Genbank" );
                    result.setType( DatabaseType.SEQUENCE );
                    genbank = result;
                }
            }
            result = genbank;
        } else if ( likelyExternalDatabaseIdentifier.equals( "ORF" ) ) {
            String organism = platform.getOrganisms().iterator().next();

            result.setType( DatabaseType.GENOME );

            if ( organismDatabases.containsKey( organism ) ) {
                result.setName( organismDatabases.get( organism ) );
            } else {
                // Placeholder
                result.setName( organism + " ORFs" );
                log.warn( "External database is " + result );
            }
        }
        if ( result == null || result.getName() == null ) {
            throw new IllegalStateException( "No external database was identified" );
        }
        return result;
    }

    private Collection<String> determinePlatformExternalReferenceIdentifier( GeoPlatform platform ) {
        Collection<String> columnNames = platform.getColumnNames();
        int index = 0;
        Collection<String> matches = new HashSet<>();
        for ( String string : columnNames ) {
            if ( GeoConstants.likelyExternalReference( string ) ) {
                log.debug( string + " appears to indicate a possible external reference identifier in column " + index
                        + " for platform " + platform );
                matches.add( string );

            }
            index++;
        }

        if ( matches.size() == 0 ) {
            return null;
        }
        return matches;

    }

    /**
     * Allow multiple taxa for a platform. Method retrieves from parsed GEO file the header column name which contains
     * the species/organism used to create probe.
     *
     * @param platform Parsed GEO platform details.
     * @return Column name in GEO used to identify column containing species/organism used to create probe
     */
    private String determinePlatformProbeOrganismColumn( GeoPlatform platform ) {
        Collection<String> columnNames = platform.getColumnNames();
        int index = 0;
        for ( String columnName : columnNames ) {
            if ( GeoConstants.likelyProbeOrganism( columnName ) ) {
                log.debug(
                        "'" + columnName + "' appears to indicate the sequences in column " + index + " for platform "
                                + platform );
                return columnName;
            }
            index++;
        }
        log.debug( "No platform organism description column found for " + platform );
        return null;
    }

    private String determinePlatformSequenceColumn( GeoPlatform platform ) {
        Collection<String> columnNames = platform.getColumnNames();
        int index = 0;
        for ( String columnName : columnNames ) {
            if ( GeoConstants.likelySequence( columnName ) ) {
                log.debug(
                        "'" + columnName + "' appears to indicate the sequences in column " + index + " for platform "
                                + platform );
                return columnName;
            }
            index++;
        }
        log.debug( "No platform sequence description column found for " + platform );
        return null;
    }

    private void doFallback( BioMaterial bioMaterial, String characteristic, String defaultDescription ) {
        Characteristic gemmaChar = Characteristic.Factory.newInstance();
        gemmaChar.setValue( characteristic );
        gemmaChar.setDescription( defaultDescription );
        gemmaChar.setEvidenceCode( GOEvidenceCode.IIA );
        bioMaterial.getCharacteristics().add( gemmaChar );
    }

    private FactorValue findMatchingExperimentalFactorValue( Collection<ExperimentalFactor> experimentalFactors,
            FactorValue convertVariableToFactorValue ) {
        Collection<Characteristic> characteristics = convertVariableToFactorValue.getCharacteristics();
        if ( characteristics.size() > 1 )
            throw new UnsupportedOperationException(
                    "Can't handle factor values with multiple characteristics in GEO conversion" );
        Characteristic c = characteristics.iterator().next();

        FactorValue matchingFactorValue = null;
        factors: for ( ExperimentalFactor factor : experimentalFactors ) {
            for ( FactorValue fv : factor.getFactorValues() ) {
                for ( Characteristic m : fv.getCharacteristics() ) {
                    if ( m.getCategory().equals( c.getCategory() ) && m.getValue().equals( c.getValue() ) ) {
                        matchingFactorValue = fv;
                        break factors;
                    }

                }
            }
        }
        return matchingFactorValue;
    }

    /**
     * Turn a rough-cut dimension name into something of reasonable length.
     */
    private String formatName( StringBuilder dimensionName ) {
        return StringUtils.abbreviate( dimensionName.toString(), 100 );
    }

    private String getBiomaterialPrefix( GeoSeries series, int i ) {
        return series.getGeoAccession() + BIOMATERIAL_NAME_TAG + i;
    }

    private Collection<GeoSample> getDatasetSamples( GeoDataset geoDataset ) {
        Collection<GeoSample> seriesSamples = getSeriesSamplesForDataset( geoDataset );

        // get just the samples used in this dataset
        Collection<GeoSample> datasetSamples = new ArrayList<>();

        for ( GeoSample sample : seriesSamples ) {
            if ( geoDataset.getColumnNames().contains( sample.getGeoAccession() ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Dataset " + geoDataset + " includes sample " + sample + " on platform " + sample
                            .getPlatforms().iterator().next() );
                }
                datasetSamples.add( sample );
            }

            if ( log.isDebugEnabled() ) {
                log.debug( "Dataset " + geoDataset + " DOES NOT include sample " + sample + " on platform " + sample
                        .getPlatforms().iterator().next() );
            }
        }

        return datasetSamples;
    }

    private String getDbIdentifierDescription( GeoPlatform platform ) {
        Collection<String> columnNames = platform.getColumnNames();
        int index = 0;
        for ( String string : columnNames ) {
            if ( GeoConstants.likelyExternalReference( string ) ) {
                return platform.getColumnDescriptions().get( index );
            }
            index++;
        }
        return null;
    }

    private String getExternalAccession( List<List<String>> externalRefs, int i ) {
        for ( List<String> refs : externalRefs ) {
            if ( StringUtils.isNotBlank( refs.get( i ) ) ) {
                return refs.get( i );
            }
        }
        return null;
    }

    /**
     * @return map of organisms to a collection of either datasets or platforms.
     */
    private Map<String, Collection<GeoData>> getOrganismDatasetMap( GeoSeries series ) {
        Map<String, Collection<GeoData>> organisms = new HashMap<>();

        if ( series.getDatasets() == null || series.getDatasets().size() == 0 ) {
            for ( GeoSample sample : series.getSamples() ) {

                assert sample.getPlatforms().size() > 0 : sample + " has no platform";
                assert sample.getPlatforms().size() == 1 : sample + " has multiple platforms: "
                        + StringUtils.join( sample.getPlatforms().toArray(), "," );
                String organism = sample.getPlatforms().iterator().next().getOrganisms().iterator().next();

                if ( !organisms.containsKey( organism ) ) {
                    organisms.put( organism, new HashSet<GeoData>() );
                }
                organisms.get( organism ).add( sample.getPlatforms().iterator().next() );
            }
        } else {
            for ( GeoDataset dataset : series.getDatasets() ) {
                String organism = dataset.getOrganism();
                if ( organisms.get( organism ) == null ) {
                    organisms.put( organism, new HashSet<GeoData>() );
                }
                organisms.get( organism ).add( dataset );
            }
        }
        return organisms;
    }

    /**
     * Based on the sample organisms, not the platforms. For rare cases where more than one species is run on a platform
     * (e.g., chimp and human run on a human platform)
     */
    private Map<String, Collection<GeoSample>> getOrganismSampleMap( GeoSeries series ) {
        Map<String, Collection<GeoSample>> result = new HashMap<>();
        for ( GeoSample sample : series.getSamples() ) {
            String organism = sample.getOrganism();
            if ( !result.containsKey( organism ) ) {
                result.put( organism, new HashSet<GeoSample>() );
            }
            result.get( organism ).add( sample );
        }
        return result;
    }

    private Map<GeoPlatform, Collection<GeoData>> getPlatformDatasetMap( GeoSeries series ) {
        Map<GeoPlatform, Collection<GeoData>> platforms = new HashMap<>();

        if ( series.getDatasets() == null || series.getDatasets().size() == 0 ) {
            for ( GeoSample sample : series.getSamples() ) {
                assert sample.getPlatforms().size() > 0 : sample + " has no platform";
                assert sample.getPlatforms().size() == 1 : sample + " has multiple platforms: "
                        + StringUtils.join( sample.getPlatforms().toArray(), "," );
                GeoPlatform platform = sample.getPlatforms().iterator().next();

                if ( platforms.get( platform ) == null ) {
                    platforms.put( platform, new HashSet<GeoData>() );
                }
                // This is a bit silly, but made coding this easier.
                platforms.get( platform ).add( sample.getPlatforms().iterator().next() );
            }
        } else {
            for ( GeoDataset dataset : series.getDatasets() ) {
                GeoPlatform platform = dataset.getPlatform();
                if ( platforms.get( platform ) == null ) {
                    platforms.put( platform, new HashSet<GeoData>() );
                }
                platforms.get( platform ).add( dataset );
            }
        }
        return platforms;
    }

    /**
     * Assumes that all samples have the same platform. If not, throws an exception.
     */
    private GeoPlatform getPlatformForSamples( List<GeoSample> datasetSamples ) {
        GeoPlatform platform = null;
        for ( GeoSample sample : datasetSamples ) {
            Collection<GeoPlatform> platforms = sample.getPlatforms();
            assert platforms.size() != 0;
            if ( platforms.size() > 1 ) {
                throw new UnsupportedOperationException(
                        "Can't handle GEO sample ids associated with multiple platforms just yet" );
            }
            GeoPlatform nextPlatform = platforms.iterator().next();
            if ( platform == null )
                platform = nextPlatform;
            else if ( !platform.equals( nextPlatform ) )
                throw new IllegalArgumentException( "All samples here must use the same platform" );
        }
        return platform;
    }

    private Collection<GeoSample> getSeriesSamplesForDataset( GeoDataset geoDataset ) {
        Collection<GeoSample> seriesSamples = null;
        Collection<GeoSeries> series = geoDataset.getSeries();

        // this is highly defensive programming prompted by a bug that caused the same series to be listed more than
        // once, but empty in one case.

        if ( series == null || series.size() == 0 ) {
            throw new IllegalStateException( "No series for " + geoDataset );
        }

        if ( series.size() > 1 ) {
            log.warn( "More than one series for a data set, probably some kind of parsing bug!" );
        }

        boolean found = false;
        for ( GeoSeries series2 : series ) {
            if ( series2.getSamples() != null && series2.getSamples().size() > 0 ) {
                if ( found ) {
                    throw new IllegalStateException(
                            "More than one of the series for " + geoDataset + " has samples: " + series2 );
                }
                seriesSamples = series2.getSamples();
                found = true;
            }
        }

        if ( seriesSamples == null || seriesSamples.size() == 0 ) {
            throw new IllegalStateException( "No series had samples for " + geoDataset );
        }

        return seriesSamples;
    }

    /**
     * Deal with missing values, identified by nulls or number format exceptions.
     */
    private void handleMissing( List<Object> toConvert, PrimitiveType pt ) {
        if ( pt.equals( PrimitiveType.DOUBLE ) ) {
            toConvert.add( Double.NaN );
        } else if ( pt.equals( PrimitiveType.STRING ) ) {
            toConvert.add( "" );
        } else if ( pt.equals( PrimitiveType.INT ) ) {
            toConvert.add( 0 );
        } else if ( pt.equals( PrimitiveType.BOOLEAN ) ) {
            toConvert.add( false );
        } else {
            throw new UnsupportedOperationException(
                    "Missing values in data vectors of type " + pt + " not supported" );
        }
    }

    private void initGeoExternalDatabase() {
        if ( geoDatabase == null ) {
            if ( externalDatabaseService != null ) {
                ExternalDatabase ed = externalDatabaseService.find( "GEO" );
                if ( ed != null ) {
                    geoDatabase = ed;
                }
            } else {
                geoDatabase = ExternalDatabase.Factory.newInstance();
                geoDatabase.setName( "GEO" );
                geoDatabase.setType( DatabaseType.EXPRESSION );
            }
        }
    }

    private boolean isGenbank( ExternalDatabase externalDb ) {
        return externalDb != null && externalDb.getName().equalsIgnoreCase( "Genbank" );
    }

    /**
     * Check to see if we got any data. If not, we should return null. This can happen if the quantitation type was
     * filtered during parsing.
     */
    private boolean isPopulated( Map<String, List<Object>> dataVectors ) {
        boolean filledIn = false;
        for ( List<Object> vector : dataVectors.values() ) {
            for ( Object object : vector ) {
                if ( object != null ) {
                    filledIn = true;
                    break;
                }
            }
            if ( filledIn ) {
                break;
            }
        }
        return filledIn;
    }

    /**
     * Note that series can have more than one type, if it has mixed samples; if at least on type matches one we can
     * use, we keep it.
     *
     * @param series
     * @return
     */
    private boolean isUsable( GeoSeries series ) {

        return series.getSeriesTypes().contains( SeriesType.geneExpressionByArray )
                || series.getSeriesTypes().contains( SeriesType.geneExpressionBySequencing );

    }

    /**
     * Convert the by-sample data for a given quantitation type to by-designElement data vectors.
     *
     * @param datasetSamples The samples we want to get data for. These should all have been run on the same platform.
     * @param quantitationTypeIndex - first index is 0
     * @return A map of Strings (design element names) to Lists of Strings containing the data.
     * @throws IllegalArgumentException if the columnNumber is not valid
     */
    private Map<String, List<Object>> makeDataVectors( GeoValues values, List<GeoSample> datasetSamples,
            Integer quantitationTypeIndex ) {
        Map<String, List<Object>> dataVectors = new HashMap<>( INITIAL_VECTOR_CAPACITY );
        Collections.sort( datasetSamples );
        GeoPlatform platform = getPlatformForSamples( datasetSamples );

        // the locations of the data we need in the target vectors (mostly reordering)
        Integer[] indices = values.getIndices( platform, datasetSamples, quantitationTypeIndex );

        if ( indices == null || indices.length == 0 )
            return null; // can happen if quantitation type was filtered out.

        assert indices.length == datasetSamples.size();

        String identifier = platform.getIdColumnName();
        List<String> designElements = platform.getColumnData( identifier );

        if ( designElements == null ) {
            return dataVectors;
        }

        for ( String designElementName : designElements ) {
            /*
             * Note: null data can happen if the platform has probes that aren't in the data, or if this is a
             * quantitation type that was filtered out during parsing, or absent from some samples.
             */
            List<Object> ob = values.getValues( platform, quantitationTypeIndex, designElementName, indices );
            if ( ob == null || ob.size() == 0 )
                continue;
            assert ob.size() == datasetSamples.size();
            dataVectors.put( designElementName, ob );
        }

        boolean filledIn = isPopulated( dataVectors );

        values.clear( platform, datasetSamples, quantitationTypeIndex );

        if ( !filledIn )
            return null;

        return dataVectors;
    }

    private void matchSampleReplicationToExperimentalFactorValue( BioMaterial bioMaterial,
            Collection<ExperimentalFactor> experimentalFactors, GeoReplication replication ) {
        // find the experimentalFactor that matches this.
        FactorValue convertVariableToFactorValue = convertReplicationToFactorValue( replication );
        FactorValue matchingFactorValue = findMatchingExperimentalFactorValue( experimentalFactors,
                convertVariableToFactorValue );
        if ( matchingFactorValue != null ) {
            bioMaterial.getFactorValues().add( matchingFactorValue );
        } else {
            throw new IllegalStateException(
                    "Could not find matching factor value for " + replication + " in experimental design for sample "
                            + bioMaterial );
        }
    }

    /**
     * @param expExp ExpressionExperiment to be searched for matching BioAssays
     * @param bioAssayDimension BioAssayDimension to be added to
     * @param sampleAcc The GEO accession id for the sample. This is compared to the external accession recorded for the
     *        BioAssay
     */
    private boolean matchSampleToBioAssay( ExpressionExperiment expExp, BioAssayDimension bioAssayDimension,
            String sampleAcc ) {

        for ( BioAssay bioAssay : expExp.getBioAssays() ) {
            if ( sampleAcc.equals( bioAssay.getAccession().getAccession() ) ) {
                bioAssayDimension.getBioAssays().add( bioAssay );
                log.debug( "Found sample match for bioAssay " + bioAssay.getAccession().getAccession() );
                return true;
            }
        }
        return false;
    }

    private void matchSampleVariableToExperimentalFactorValue( BioMaterial bioMaterial,
            Collection<ExperimentalFactor> experimentalFactors, GeoVariable variable ) {

        // find the experimentalFactor that matches this.
        FactorValue convertVariableToFactorValue = convertVariableToFactorValue( variable );
        FactorValue matchingFactorValue = findMatchingExperimentalFactorValue( experimentalFactors,
                convertVariableToFactorValue );

        if ( matchingFactorValue == null ) {
            throw new IllegalStateException(
                    "Could not find matching factor value for " + variable + " in experimental design for sample "
                            + bioMaterial );
        }

        // make sure we don't put the factor value on more than once.
        if ( alreadyHasFactorValueForFactor( bioMaterial, matchingFactorValue.getExperimentalFactor() ) ) {
            return;
        }

        bioMaterial.getFactorValues().add( matchingFactorValue );

    }

    /**
     * Sanity check.
     */
    private void sanityCheckQuantitationTypes( List<GeoSample> datasetSamples ) {
        List<String> reference = new ArrayList<>();

        // Choose a reference that is populated ...
        boolean expectingData = true;
        for ( GeoSample sample : datasetSamples ) {
            if ( sample.hasUsableData() ) {
                reference = sample.getColumnNames();
                if ( !reference.isEmpty() )
                    break;
            } else {
                expectingData = false;
            }
        }

        if ( !expectingData ) {
            log.warn( "Not expecting any data, so quantitation type checking is skipped." );
            return;
        }

        if ( reference.isEmpty() ) {
            throw new IllegalStateException( "None of the samples have any quantitation type names" );
        }

        boolean someDidntMatch = false;
        String lastError = "";
        for ( GeoSample sample : datasetSamples ) {
            List<String> columnNames = sample.getColumnNames();

            assert !columnNames.isEmpty();

            if ( !reference.equals( columnNames ) ) {

                StringBuilder buf = new StringBuilder();
                buf.append( "\nSample " ).append( sample.getGeoAccession() ).append( ":" );
                for ( String string : columnNames ) {
                    buf.append( " " ).append( string );
                }
                buf.append( "\nReference " ).append( datasetSamples.iterator().next().getGeoAccession() ).append( ":" );
                for ( String string : reference ) {
                    buf.append( " " ).append( string );
                }
                someDidntMatch = true;

                lastError = "*** Sample quantitation type names do not match: " + buf.toString();
                log.debug( lastError );
            }
        }
        if ( someDidntMatch ) {
            log.warn( "Samples do not have consistent quantification type names. Last error was: " + lastError );
        }
    }

    private String trimString( String characteristic ) {
        if ( characteristic.length() > 255 ) {
            log.warn( "** Characteristic too long: " + characteristic + " - will truncate - ****" );
            characteristic = characteristic.substring( 0, 199 ) + " (truncated at 200 characters)";
        }
        return characteristic;
    }

    private URL tryGetRemoteFileUrl( String file ) {
        URL remoteFileUrl = null;
        try {
            remoteFileUrl = new URL( file );
        } catch ( MalformedURLException e ) {
            log.error(
                    "Problems with url: " + file + ".  Will not store the url of the raw data file.  Full error is: " );
            e.printStackTrace();
        }
        return remoteFileUrl;
    }

}