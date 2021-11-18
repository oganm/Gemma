/*
 * The Gemma project
 *
 * Copyright (c) 2007 University of British Columbia
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
package ubic.gemma.core.analysis.preprocess;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubic.basecode.io.ByteArrayConverter;
import ubic.gemma.core.analysis.expression.AnalysisUtilService;
import ubic.gemma.core.analysis.service.ExpressionExperimentVectorManipulatingService;
import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;
import ubic.gemma.model.common.auditAndSecurity.eventType.ExpressionExperimentVectorMergeEvent;
import ubic.gemma.model.common.quantitationtype.PrimitiveType;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimension;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.model.expression.bioAssayData.RawExpressionDataVector;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.persistence.service.common.auditAndSecurity.AuditTrailService;
import ubic.gemma.persistence.service.expression.bioAssay.BioAssayService;
import ubic.gemma.persistence.service.expression.bioAssayData.BioAssayDimensionService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;

import java.util.*;

/**
 * Tackles the problem of concatenating DesignElementDataVectors for a single experiment. This is necessary When a study
 * uses two or more similar array designs without 'replication'. Typical of the genre is GSE60 ("Diffuse large B-cell
 * lymphoma"), with 31 BioAssays on GPL174, 35 BioAssays on GPL175, and 66 biomaterials. A more complex one: GSE3500,
 * with 13 ArrayDesigns. In that (and others) case, there are quantitation types which do not appear on all array
 * designs, leaving gaps in the vectors that have to be filled in.
 * The algorithm for dealing with this is a preprocessing step:
 * <ol>
 * <li>Generate a merged set of vectors for each of the (important) quantitation types.</li>
 * <li>Create a merged BioAssayDimension</li>
 * <li>Persist the new vectors, which are now tied to a <em>single DesignElement</em>. This is, strictly speaking,
 * incorrect, but because the design elements used in the vector all point to the same sequence, there is no major
 * problem in analyzing this. However, there is a potential loss of information.
 * <li>Cleanup: remove old vectors, analyses, and BioAssayDimensions.
 * <li>Postprocess: Recreate the processed datavectors, including masking missing values if necesssary.
 * </ol>
 * Vectors which are empty (all missing values) are not persisted. If problems are found during merging, an exception
 * will be thrown, though this may leave things in a bad state requiring a reload of the data.
 *
 * @author pavlidis
 * @see ExpressionDataMatrixBuilder
 */
@Component
public class VectorMergingServiceImpl extends ExpressionExperimentVectorManipulatingService
        implements VectorMergingService {

    private static final String MERGED_DIM_DESC_PREFIX = "Generated by the merger of";
    private static final Log log = LogFactory.getLog( VectorMergingServiceImpl.class.getName() );
    @Autowired
    private AnalysisUtilService analysisUtilService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private BioAssayDimensionService bioAssayDimensionService;

    @Autowired
    private BioAssayService bioAssayService;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    @Autowired
    private PreprocessorService preprocessorService;

    @Autowired
    private VectorMergingHelperService vectorMergingHelperService;

    @Override
    public ExpressionExperiment mergeVectors( ExpressionExperiment ee ) {

        Collection<ArrayDesign> arrayDesigns = expressionExperimentService.getArrayDesignsUsed( ee );

        if ( arrayDesigns.size() > 1 ) {
            throw new IllegalArgumentException(
                    "Cannot cope with more than one platform; switch experiment to use a (merged) platform first" );
        }

        ee = expressionExperimentService.thaw( ee );
        Collection<QuantitationType> qts = expressionExperimentService.getQuantitationTypes( ee );
        VectorMergingServiceImpl.log.info( qts.size() + " quantitation types for potential merge" );

        /*
         * Load all the bioassay dimensions, which will be merged.
         */
        Set<BioAssayDimension> allOldBioAssayDims = new HashSet<>();
        for ( BioAssay ba : ee.getBioAssays() ) {
            Collection<BioAssayDimension> oldBioAssayDims = bioAssayService.findBioAssayDimensions( ba );
            for ( BioAssayDimension bioAssayDim : oldBioAssayDims ) {
                if ( bioAssayDim.getDescription().startsWith( VectorMergingServiceImpl.MERGED_DIM_DESC_PREFIX ) ) {
                    // not foolproof, but avoids some artifacts - e.g. if there were previous failed attempts at this.
                    continue;
                }
                allOldBioAssayDims.add( bioAssayDim );
            }
        }

        if ( allOldBioAssayDims.size() == 0 ) {
            throw new IllegalStateException(
                    "No bioAssayDimensions found to merge (previously merged ones are filtered, data may be corrupt?" );
        }

        if ( allOldBioAssayDims.size() == 1 ) {
            VectorMergingServiceImpl.log
                    .warn( "Experiment already has only a single bioAssayDimension, nothing seems to need merging. Bailing" );
            return ee;
        }

        VectorMergingServiceImpl.log.info( allOldBioAssayDims.size() + " bioAssayDimensions to merge" );
        List<BioAssayDimension> sortedOldDims = this.sortedBioAssayDimensions( allOldBioAssayDims );

        BioAssayDimension newBioAd = this.getNewBioAssayDimension( sortedOldDims );
        int totalBioAssays = newBioAd.getBioAssays().size();
        assert totalBioAssays == ee.getBioAssays().size() :
                "experiment has " + ee.getBioAssays().size() + " but new bioAssayDimension has " + totalBioAssays;

        Map<QuantitationType, Collection<RawExpressionDataVector>> qt2Vec = this
                .getVectors( ee, qts, allOldBioAssayDims );

        /*
         * This will run into problems if there are excess quantitation types
         */
        int numSuccessfulMergers = 0;
        Collection<RawExpressionDataVector> newVectors = new HashSet<>();

        for ( QuantitationType type : qt2Vec.keySet() ) {

            Collection<RawExpressionDataVector> oldVecs = qt2Vec.get( type );

            if ( oldVecs.isEmpty() ) {
                VectorMergingServiceImpl.log.warn( "No vectors for " + type );
                continue;
            }

            Map<CompositeSequence, Collection<RawExpressionDataVector>> deVMap = this.getDevMap( oldVecs );

            if ( deVMap == null ) {
                VectorMergingServiceImpl.log.info( "Vector merging will not be done for " + type
                        + " as there is only one vector per element already" );
                continue;
            }

            VectorMergingServiceImpl.log.info( "Processing " + oldVecs.size() + " vectors  for " + type );

            int numAllMissing = 0;
            int missingValuesForQt = 0;
            for ( CompositeSequence de : deVMap.keySet() ) {

                RawExpressionDataVector vector = this.initializeNewVector( ee, newBioAd, type, de );
                Collection<RawExpressionDataVector> dedvs = deVMap.get( de );

                /*
                 * these ugly nested loops are to ENSURE that we get the vector reconstructed properly. For each of the
                 * old bioassayDimensions, find the designElementDataVector that uses it. If there isn't one, fill in
                 * the values for that dimension with missing data. We go through the dimensions in the same order that
                 * we joined them up.
                 */

                List<Object> data = new ArrayList<>();
                int totalMissingInVector = this.makeMergedData( sortedOldDims, newBioAd, type, de, dedvs, data );
                missingValuesForQt += totalMissingInVector;
                if ( totalMissingInVector == totalBioAssays ) {
                    numAllMissing++;
                    continue; // we don't save data that is all missing.
                }

                if ( data.size() != totalBioAssays ) {
                    throw new IllegalStateException(
                            "Wrong number of values for " + de + " / " + type + ", expected " + totalBioAssays
                                    + ", got " + data.size() );
                }

                byte[] newDataAr = converter.toBytes( data.toArray() );

                vector.setData( newDataAr );

                newVectors.add( vector );

            }

            if ( numAllMissing > 0 ) {
                VectorMergingServiceImpl.log
                        .info( numAllMissing + " vectors had all missing values and were junked for " + type );
            }

            if ( missingValuesForQt > 0 ) {
                VectorMergingServiceImpl.log.info( missingValuesForQt + " total missing values: " + type );
            }

            numSuccessfulMergers++;
        } // for each quantitation type

        // Up to now, nothing has been changed in the database except we made a new bioassaydimension.

        if ( numSuccessfulMergers == 0 ) {
            /*
             * Try to clean up before bailing
             */
            this.bioAssayDimensionService.remove( newBioAd );
            throw new IllegalStateException(
                    "Nothing was merged. Maybe all the vectors are effectively merged already" );
        }

        ee.setNumberOfSamples( ee.getBioAssays().size() );

        // TRANSACTION
        log.info( ee.getRawExpressionDataVectors().size() );
        vectorMergingHelperService.persist( ee, newVectors );

        ee = expressionExperimentService.load( ee.getId() );
        ee = expressionExperimentService.thaw( ee );
        log.info( ee.getRawExpressionDataVectors().size() );

        // Several transactions
        this.cleanUp( ee, allOldBioAssayDims, newBioAd );

        // transaction
        this.audit( ee,
                "Vector merging performed, merged " + allOldBioAssayDims + " old bioassay dimensions for " + qts.size()
                        + " quantitation types." );

        // several transactions
        try {
            preprocessorService.process( ee );
        } catch ( PreprocessingException e ) {
            VectorMergingServiceImpl.log.error( "Error during postprocessing: " + e.getMessage(), e );
        }

        return ee;
    }

    private void audit( ExpressionExperiment ee, String note ) {
        AuditEventType eventType = ExpressionExperimentVectorMergeEvent.Factory.newInstance();
        auditTrailService.addUpdateEvent( ee, eventType, note );
    }

    private void cleanUp( ExpressionExperiment expExp, Collection<BioAssayDimension> allOldBioAssayDims,
            BioAssayDimension newBioAd ) {

        analysisUtilService.deleteOldAnalyses( expExp );

        /*
         * Delete the experimental design? Actually it _should_ be okay, since the association is with biomaterials.
         */

        // remove the old BioAssayDimensions
        for ( BioAssayDimension oldDim : allOldBioAssayDims ) {
            // careful, the 'new' bioAssayDimension might be one of the old ones that we're reusing.
            if ( oldDim.equals( newBioAd ) )
                continue;
            try {
                bioAssayDimensionService.remove( oldDim );
            } catch ( Exception e ) {
                VectorMergingServiceImpl.log
                        .warn( "Could not remove an old bioAssayDimension with ID=" + oldDim.getId() );
            }
        }
    }

    /**
     * Create a new one or use an existing one. (an existing one might be found if this process was started once before
     * and aborted partway through).
     *
     * @param oldDims in the sort order to be used.
     * @return BA dim
     */
    private BioAssayDimension combineBioAssayDimensions( List<BioAssayDimension> oldDims ) {

        List<BioAssay> bioAssays = new ArrayList<>();
        for ( BioAssayDimension bioAd : oldDims ) {
            for ( BioAssay bioAssay : bioAd.getBioAssays() ) {
                if ( bioAssays.contains( bioAssay ) ) {
                    throw new IllegalStateException(
                            "Duplicate bioassay for biodimension: " + bioAssay + "; inspecting " + oldDims.size()
                                    + " BioAssayDimensions" );
                }
                bioAssays.add( bioAssay );

            }
        }

        // first see if we already have an equivalent one.
        boolean found = true;
        for ( BioAssayDimension newDim : oldDims ) {
            // size should be the same.
            List<BioAssay> assaysInExisting = newDim.getBioAssays();
            if ( assaysInExisting.size() != bioAssays.size() ) {
                continue;
            }

            for ( int i = 0; i < bioAssays.size(); i++ ) {
                if ( !assaysInExisting.get( i ).equals( bioAssays.get( i ) ) ) {
                    found = false;
                    break;
                }
            }
            if ( !found )
                continue;
            VectorMergingServiceImpl.log
                    .info( "Already have a dimension created that fits the bill - removing it from the 'old' list." );
            oldDims.remove( newDim );
            return newDim;
        }

        BioAssayDimension newBioAd = BioAssayDimension.Factory.newInstance();
        newBioAd.setName( "" );
        newBioAd.setDescription(
                VectorMergingServiceImpl.MERGED_DIM_DESC_PREFIX + " " + oldDims.size() + " dimensions: " );

        for ( BioAssayDimension bioAd : oldDims ) {
            newBioAd.setName( newBioAd.getName() + bioAd.getName() + " " );
            newBioAd.setDescription( newBioAd.getDescription() + bioAd.getName() + " " );
        }

        newBioAd.setName( StringUtils.abbreviate( newBioAd.getName(), 255 ) );
        newBioAd.setBioAssays( bioAssays );

        newBioAd = bioAssayDimensionService.create( newBioAd );
        VectorMergingServiceImpl.log
                .info( "Created new bioAssayDimension with " + newBioAd.getBioAssays().size() + " bioassays." );
        return newBioAd;
    }

    /**
     * @param de             de
     * @param data           data
     * @param oldDim         old dim
     * @param representation representation
     * @return The number of missing values which were added.
     */
    private int fillMissingValues( CompositeSequence de, List<Object> data, BioAssayDimension oldDim,
            PrimitiveType representation ) {
        int nullsNeeded = oldDim.getBioAssays().size();
        for ( int i = 0; i < nullsNeeded; i++ ) {
            // this code taken from GeoConverter
            if ( representation.equals( PrimitiveType.DOUBLE ) ) {
                data.add( Double.NaN );
            } else if ( representation.equals( PrimitiveType.STRING ) ) {
                data.add( "" );
            } else if ( representation.equals( PrimitiveType.INT ) ) {
                data.add( 0 );
            } else if ( representation.equals( PrimitiveType.BOOLEAN ) ) {
                data.add( false );
            } else {
                throw new UnsupportedOperationException(
                        "Missing values in data vectors of type " + representation + " not supported (when processing "
                                + de );
            }
        }
        return nullsNeeded;
    }

    /**
     * @param oldVectors old vectors
     * @return map of design element to vectors.
     */
    private Map<CompositeSequence, Collection<RawExpressionDataVector>> getDevMap(
            Collection<RawExpressionDataVector> oldVectors ) {
        Map<CompositeSequence, Collection<RawExpressionDataVector>> deVMap = new HashMap<>();
        boolean atLeastOneMatch = false;
        assert !oldVectors.isEmpty();

        for ( RawExpressionDataVector vector : oldVectors ) {
            CompositeSequence designElement = vector.getDesignElement();
            if ( !deVMap.containsKey( designElement ) ) {
                if ( VectorMergingServiceImpl.log.isDebugEnabled() )
                    VectorMergingServiceImpl.log
                            .debug( "adding " + designElement + " " + designElement.getBiologicalCharacteristic() );
                deVMap.put( designElement, new HashSet<RawExpressionDataVector>() );
            }
            deVMap.get( designElement ).add( vector );

            if ( !atLeastOneMatch && deVMap.get( designElement ).size() > 1 ) {
                atLeastOneMatch = true;
            }
        }

        if ( !atLeastOneMatch ) {
            return null;
        }
        return deVMap;
    }

    /**
     * @param sortedOldDims
     * @return persistent bioassaydimension (either re-used or a new one)
     */
    private BioAssayDimension getNewBioAssayDimension( List<BioAssayDimension> sortedOldDims ) {
        return this.combineBioAssayDimensions( sortedOldDims );
    }

    /**
     * Get the current set of vectors that need to be updated.
     *
     * @param expExp             ee
     * @param qts                - only used to check for problems.
     * @param allOldBioAssayDims old BA dims
     * @return map
     */
    private Map<QuantitationType, Collection<RawExpressionDataVector>> getVectors( ExpressionExperiment expExp,
            Collection<QuantitationType> qts, Collection<BioAssayDimension> allOldBioAssayDims ) {
        Collection<RawExpressionDataVector> oldVectors = new HashSet<>();

        for ( BioAssayDimension dim : allOldBioAssayDims ) {
            oldVectors.addAll( rawExpressionDataVectorService.find( dim ) );
        }

        if ( oldVectors.isEmpty() ) {
            throw new IllegalStateException( "No vectors" );
        }

        oldVectors = rawExpressionDataVectorService.thaw( oldVectors );
        Map<QuantitationType, Collection<RawExpressionDataVector>> qt2Vec = new HashMap<>();
        Collection<QuantitationType> qtsToAdd = new HashSet<>();
        for ( RawExpressionDataVector v : oldVectors ) {

            QuantitationType qt = v.getQuantitationType();
            if ( !qts.contains( qt ) ) {
                /*
                 * Guard against QTs that are broken. Sometimes the QTs for the EE don't include the ones that the DEDVs
                 * have, due to corruption.
                 */
                qtsToAdd.add( qt );
            }
            if ( !qt2Vec.containsKey( qt ) ) {
                qt2Vec.put( qt, new HashSet<RawExpressionDataVector>() );
            }

            qt2Vec.get( qt ).add( v );
        }

        if ( !qtsToAdd.isEmpty() ) {
            expExp.getQuantitationTypes().addAll( qtsToAdd );
            VectorMergingServiceImpl.log
                    .info( "Adding " + qtsToAdd.size() + " missing quantitation types to experiment" );
            expressionExperimentService.update( expExp );
        }

        return qt2Vec;
    }

    /**
     * Make a (non-persistent) vector that has the right bioAssayDimension, designelement and quantitationtype.
     *
     * @param expExp   ee
     * @param newBioAd new BA dim
     * @param type     type
     * @param de       de
     * @return raw data vector
     */
    private RawExpressionDataVector initializeNewVector( ExpressionExperiment expExp, BioAssayDimension newBioAd,
            QuantitationType type, CompositeSequence de ) {
        RawExpressionDataVector vector = RawExpressionDataVector.Factory.newInstance();
        vector.setBioAssayDimension( newBioAd );
        vector.setDesignElement( de );
        vector.setQuantitationType( type );
        vector.setExpressionExperiment( expExp );
        return vector;
    }

    /**
     * @param sortedOldDims sorted old dims
     * @param newBioAd      new BA dims
     * @param type          type
     * @param de            de
     * @param dedvs         dedvs
     * @param mergedData    starts out empty, is initalized to the new data.
     * @return number of values missing
     */
    private int makeMergedData( List<BioAssayDimension> sortedOldDims, BioAssayDimension newBioAd,
            QuantitationType type, CompositeSequence de, Collection<RawExpressionDataVector> dedvs,
            List<Object> mergedData ) {
        int totalMissingInVector = 0;
        PrimitiveType representation = type.getRepresentation();

        for ( BioAssayDimension oldDim : sortedOldDims ) {
            // careful, the 'new' bioAssayDimension might be one of the old ones that we're reusing.
            if ( oldDim.equals( newBioAd ) )
                continue;
            boolean found = false;

            for ( RawExpressionDataVector oldV : dedvs ) {
                assert oldV.getDesignElement().equals( de );
                assert oldV.getQuantitationType().equals( type );

                if ( oldV.getBioAssayDimension().equals( oldDim ) ) {
                    found = true;
                    this.convertFromBytes( mergedData, representation, oldV );
                    break;
                }
            }
            if ( !found ) {
                int missing = this.fillMissingValues( de, mergedData, oldDim, representation );
                totalMissingInVector += missing;
            }
        }
        return totalMissingInVector;
    }

    @SuppressWarnings("unused")
    private void print( Collection<DesignElementDataVector> newVectors ) {
        StringBuilder buf = new StringBuilder();
        ByteArrayConverter conv = new ByteArrayConverter();
        for ( DesignElementDataVector vector : newVectors ) {
            buf.append( vector.getDesignElement() );
            QuantitationType qtype = vector.getQuantitationType();
            if ( qtype.getRepresentation().equals( PrimitiveType.DOUBLE ) ) {
                double[] vals = conv.byteArrayToDoubles( vector.getData() );
                for ( double d : vals ) {
                    buf.append( "\t" ).append( d );
                }
            } else if ( qtype.getRepresentation().equals( PrimitiveType.INT ) ) {
                int[] vals = conv.byteArrayToInts( vector.getData() );
                for ( int i : vals ) {
                    buf.append( "\t" ).append( i );
                }
            } else if ( qtype.getRepresentation().equals( PrimitiveType.BOOLEAN ) ) {
                boolean[] vals = conv.byteArrayToBooleans( vector.getData() );
                for ( boolean d : vals ) {
                    buf.append( "\t" ).append( d );
                }
            } else if ( qtype.getRepresentation().equals( PrimitiveType.STRING ) ) {
                String[] vals = conv.byteArrayToStrings( vector.getData() );
                for ( String d : vals ) {
                    buf.append( "\t" ).append( d );
                }

            }
            buf.append( "\n" );
        }

        VectorMergingServiceImpl.log.info( "\n" + buf );
    }

    /**
     * Provide a sorted list of bioAssayDimensions for merging. The actual order doesn't matter, just so long as we are
     * consistent further on.
     *
     * @param oldBioAssayDims old dims
     * @return BA dims
     */
    private List<BioAssayDimension> sortedBioAssayDimensions( Collection<BioAssayDimension> oldBioAssayDims ) {
        List<BioAssayDimension> sortedOldDims = new ArrayList<>( oldBioAssayDims );
        Collections.sort( sortedOldDims, new Comparator<BioAssayDimension>() {
            @Override
            public int compare( BioAssayDimension o1, BioAssayDimension o2 ) {
                return o1.getId().compareTo( o2.getId() );
            }
        } );
        return sortedOldDims;
    }
}
