/*
 * The Gemma project
 *
 * Copyright (c) 2011 University of British Columbia
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
package ubic.gemma.core.analysis.preprocess.svd;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.math.CorrelationStats;
import ubic.basecode.math.Distance;
import ubic.basecode.math.KruskalWallis;
import ubic.gemma.core.analysis.util.ExperimentalDesignUtils;
import ubic.gemma.core.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.model.analysis.expression.pca.PrincipalComponentAnalysis;
import ubic.gemma.model.analysis.expression.pca.ProbeLoading;
import ubic.gemma.model.common.auditAndSecurity.eventType.PCAAnalysisEvent;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimension;
import ubic.gemma.model.expression.bioAssayData.DoubleVectorValueObject;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.persistence.service.analysis.expression.pca.PrincipalComponentAnalysisService;
import ubic.gemma.persistence.service.common.auditAndSecurity.AuditTrailService;
import ubic.gemma.persistence.service.expression.bioAssayData.ProcessedExpressionDataVectorService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;
import ubic.gemma.persistence.util.EntityUtils;

import java.util.*;

/**
 * Perform SVD on expression data and store the results.
 *
 * @author paul
 * @see PrincipalComponentAnalysisService
 */
@Component
public class SVDServiceHelperImpl implements SVDServiceHelper {

    /**
     * How many probe (gene) loadings to store, at most.
     */
    private static final int MAX_LOADINGS_TO_PERSIST = 1000;

    /**
     * How many components we should store probe (gene) loadings for.
     */
    private static final int MAX_NUM_COMPONENTS_TO_PERSIST = 5;

    private static final int MINIMUM_POINTS_TO_COMPARE_TO_EIGEN_GENE = 3;

    private static final int MAX_EIGEN_GENES_TO_TEST = 5;

    private static final Log log = LogFactory.getLog( SVDServiceHelperImpl.class );

    @Autowired
    private ProcessedExpressionDataVectorService processedExpressionDataVectorService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private PrincipalComponentAnalysisService principalComponentAnalysisService;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    public static void populateBMFMap( Map<ExperimentalFactor, Map<Long, Double>> bioMaterialFactorMap,
            BioMaterial bm ) {
        for ( FactorValue fv : bm.getFactorValues() ) {

            ExperimentalFactor experimentalFactor = fv.getExperimentalFactor();

            if ( !bioMaterialFactorMap.containsKey( experimentalFactor ) ) {
                bioMaterialFactorMap.put( experimentalFactor, new HashMap<Long, Double>() );
            }

            double valueToStore;
            if ( fv.getMeasurement() != null ) {
                try {
                    valueToStore = Double.parseDouble( fv.getMeasurement().getValue() );
                } catch ( NumberFormatException e ) {
                    SVDServiceHelperImpl.log.warn( "Measurement wasn't a number for " + fv );
                    valueToStore = Double.NaN;
                }

            } else {
                /*
                 * This is a hack. We're storing the ID but as a double.
                 */
                valueToStore = fv.getId().doubleValue();
            }
            bioMaterialFactorMap.get( experimentalFactor ).put( bm.getId(), valueToStore );
        }
    }

    /**
     * Get the SVD information for experiment with id given.
     *
     * @return value or null if there isn't one.
     */
    @Override
    public SVDValueObject retrieveSvd( ExpressionExperiment ee ) {
        PrincipalComponentAnalysis pca = this.principalComponentAnalysisService.loadForExperiment( ee );
        if ( pca == null )
            return null;
        // pca.setBioAssayDimension( bioAssayDimensionService.thawRawAndProcessed( pca.getBioAssayDimension() ) );
        try {
            return new SVDValueObject( pca );
        } catch ( Exception e ) {
            SVDServiceHelperImpl.log.error( e.getLocalizedMessage() );
            return null;
        }
    }

    @Override
    public void svd( Collection<ExpressionExperiment> ees ) {
        for ( ExpressionExperiment ee : ees ) {
            this.svd( ee );
        }
    }

    /**
     * This is run in a within a nested transaction since to account for the exception raised when the matrix is not
     * invertible.
     */
    @Override
    @Transactional(propagation = Propagation.NESTED)
    public SVDValueObject svd( ExpressionExperiment ee ) {
        assert ee != null;

        Collection<ProcessedExpressionDataVector> vectors = processedExpressionDataVectorService
                .getProcessedDataVectors( ee );

        if ( vectors.isEmpty() ) {
            throw new IllegalArgumentException( "Experiment must have processed data already to do SVD" );
        }
        expressionExperimentService.update( ee );

        processedExpressionDataVectorService.thaw( vectors );
        ExpressionDataDoubleMatrix mat = new ExpressionDataDoubleMatrix( vectors );

        SVDServiceHelperImpl.log.info( "Starting SVD" );
        ExpressionDataSVD svd = new ExpressionDataSVD( mat );

        SVDServiceHelperImpl.log.info( "SVD done, postprocessing and storing results." );

        /*
         * Save the results
         */
        DoubleMatrix<Integer, BioMaterial> v = svd.getV();

        BioAssayDimension b = mat.getBestBioAssayDimension();

        PrincipalComponentAnalysis pca = this.updatePca( ee, svd, v, b );
        expressionExperimentService.update( ee );

        SVDValueObject factorAnalysis = this.svdFactorAnalysis( pca );
        expressionExperimentService.update( ee );
        expressionExperimentService.update( ee );
        return factorAnalysis;
    }

    @Override
    public Map<ProbeLoading, DoubleVectorValueObject> getTopLoadedVectors( ExpressionExperiment ee, int component,
            int count ) {
        PrincipalComponentAnalysis pca = principalComponentAnalysisService.loadForExperiment( ee );
        Map<ProbeLoading, DoubleVectorValueObject> result = new HashMap<>();
        if ( pca == null ) {
            return result;
        }

        List<ProbeLoading> topLoadedProbes = principalComponentAnalysisService
                .getTopLoadedProbes( ee, component, count );

        if ( topLoadedProbes == null ) {
            SVDServiceHelperImpl.log.warn( "No probes?" );
            return result;
        }

        Map<Long, ProbeLoading> probes = new LinkedHashMap<>();
        Set<CompositeSequence> p = new HashSet<>();
        for ( ProbeLoading probeLoading : topLoadedProbes ) {
            CompositeSequence probe = probeLoading.getProbe();
            probes.put( probe.getId(), probeLoading );
            p.add( probe );
        }

        if ( probes.isEmpty() )
            return result;

        assert probes.size() <= count;

        Collection<ExpressionExperiment> ees = new HashSet<>();
        ees.add( ee );
        Collection<DoubleVectorValueObject> dvVos = processedExpressionDataVectorService
                .getProcessedDataArraysByProbe( ees, p );

        if ( dvVos.isEmpty() ) {
            SVDServiceHelperImpl.log.warn( "No vectors came back from the call; check the Gene2CS table?" );
            return result;
        }

        // note that this might have come from a cache.

        /*
         * This is actually expected, because we go through the genes.
         */

        BioAssayDimension bioAssayDimension = pca.getBioAssayDimension();
        assert bioAssayDimension != null;
        assert !bioAssayDimension.getBioAssays().isEmpty();

        for ( DoubleVectorValueObject vct : dvVos ) {
            ProbeLoading probeLoading = probes.get( vct.getDesignElement().getId() );

            if ( probeLoading == null ) {
                /*
                 * This is okay, we will skip this probe. It was another probe for a gene that _was_ highly loaded.
                 */
                continue;
            }

            assert bioAssayDimension.getBioAssays().size() == vct.getData().length;

            vct.setRank( probeLoading.getLoadingRank().doubleValue() );
            vct.setExpressionExperiment( new ExpressionExperimentValueObject( ee ) );
            result.put( probeLoading, vct );
        }

        if ( result.isEmpty() ) {
            SVDServiceHelperImpl.log.warn( "No results, something went wrong; there were " + dvVos.size()
                    + " vectors to start but they all got filtered out." );
        }

        return result;

    }

    @Override
    public boolean hasPca( ExpressionExperiment ee ) {
        return this.retrieveSvd( ee ) != null;
    }

    @Override
    public Set<ExperimentalFactor> getImportantFactors( ExpressionExperiment ee,
            Collection<ExperimentalFactor> experimentalFactors, Double importanceThreshold ) {
        Set<ExperimentalFactor> importantFactors = new HashSet<>();

        if ( experimentalFactors.isEmpty() ) {
            return importantFactors;
        }
        Map<Long, ExperimentalFactor> factors = EntityUtils.getIdMap( experimentalFactors );
        SVDValueObject svdFactorAnalysis = this.svdFactorAnalysis( ee );
        if ( svdFactorAnalysis == null ) {
            return importantFactors;
        }
        Map<Integer, Map<Long, Double>> factorPVals = svdFactorAnalysis.getFactorPvals();
        for ( Integer cmp : factorPVals.keySet() ) {
            Map<Long, Double> factorPv = factorPVals.get( cmp );
            for ( Long efId : factorPv.keySet() ) {
                Double pvalue = factorPv.get( efId );
                ExperimentalFactor ef = factors.get( efId );

                if ( pvalue < importanceThreshold ) {
                    assert factors.containsKey( efId );
                    SVDServiceHelperImpl.log
                            .info( ef + " retained at p=" + String.format( "%.2g", pvalue ) + " for PC" + cmp );
                    importantFactors.add( ef );
                } else {
                    SVDServiceHelperImpl.log
                            .info( ef + " not retained at p=" + String.format( "%.2g", pvalue ) + " for PC" + cmp );
                }
            }
        }
        return importantFactors;
    }

    @Override
    public SVDValueObject svdFactorAnalysis( PrincipalComponentAnalysis pca ) {

        BioAssayDimension bad = pca.getBioAssayDimension();
        List<BioAssay> bioAssays = bad.getBioAssays();

        SVDValueObject svo;
        try {
            svo = new SVDValueObject( pca );
        } catch ( Exception e ) {
            SVDServiceHelperImpl.log.error( e.getLocalizedMessage() );
            return null;
        }

        Map<Long, Date> bioMaterialDates = new HashMap<>();
        Map<ExperimentalFactor, Map<Long, Double>> bioMaterialFactorMap = new HashMap<>();

        this.prepareForFactorComparisons( svo, bioAssays, bioMaterialDates, bioMaterialFactorMap );

        if ( bioMaterialDates.isEmpty() && bioMaterialFactorMap.isEmpty() ) {
            SVDServiceHelperImpl.log.warn( "No factor or date information to compare to the eigenGenes" );
            return svo;
        }

        Long[] svdBioMaterials = svo.getBioMaterialIds();

        svo.getDateCorrelations().clear();
        svo.getFactorCorrelations().clear();
        svo.getDates().clear();
        svo.getFactors().clear();

        for ( int componentNumber = 0; componentNumber < Math
                .min( svo.getvMatrix().columns(), SVDServiceHelperImpl.MAX_EIGEN_GENES_TO_TEST ); componentNumber++ ) {
            this.analyzeComponent( svo, componentNumber, svo.getvMatrix(), bioMaterialDates, bioMaterialFactorMap,
                    svdBioMaterials );
        }

        return svo;
    }

    @Override
    public SVDValueObject svdFactorAnalysis( ExpressionExperiment ee ) {
        PrincipalComponentAnalysis pca = principalComponentAnalysisService.loadForExperiment( ee );
        if ( pca == null ) {
            SVDServiceHelperImpl.log.warn( "PCA not available for this experiment" );
            return null;
        }
        return this.svdFactorAnalysis( pca );
    }

    /**
     * Do the factor comparisons for one component.
     *
     * @param bioMaterialFactorMap Map of factors to biomaterials to the value we're going to use. Even for
     *                             non-continuous factors the value is a double.
     */
    private void analyzeComponent( SVDValueObject svo, int componentNumber, DoubleMatrix<Long, Integer> vMatrix,
            Map<Long, Date> bioMaterialDates, Map<ExperimentalFactor, Map<Long, Double>> bioMaterialFactorMap,
            Long[] svdBioMaterials ) {
        DoubleArrayList eigenGene = new DoubleArrayList( vMatrix.getColumn( componentNumber ) );
        // since we use rank correlation/anova, we just use the casted ids (two-groups) or dates as the covariate

        int numWithDates = 0;
        for ( Long id : bioMaterialDates.keySet() ) {
            if ( bioMaterialDates.get( id ) != null ) {
                numWithDates++;
            }
        }

        if ( numWithDates > 2 ) {
            /*
             * Get the dates in order, - no rounding.
             */
            boolean initializingDates = svo.getDates().isEmpty();
            double[] dates = new double[svdBioMaterials.length];

            /*
             * If dates are all the same, skip.
             */
            Set<Date> uniqueDate = new HashSet<>();

            for ( int j = 0; j < svdBioMaterials.length; j++ ) {

                Date date = bioMaterialDates.get( svdBioMaterials[j] );
                if ( date == null ) {
                    SVDServiceHelperImpl.log
                            .warn( "Incomplete date information, missing for biomaterial " + svdBioMaterials[j] );
                    dates[j] = Double.NaN;
                } else {
                    Date roundDate = DateUtils.round( date, Calendar.MINUTE );
                    uniqueDate.add( roundDate );
                    dates[j] = roundDate.getTime(); // round to minute; make int, cast to
                    // double
                }

                if ( initializingDates ) {
                    svo.getDates().add( date );
                }
            }

            if ( uniqueDate.size() == 1 ) {
                SVDServiceHelperImpl.log.warn( "All scan dates the same, skipping data analysis" );
                svo.getDates().clear();
            }

            if ( eigenGene.size() != dates.length ) {
                SVDServiceHelperImpl.log
                        .warn( "Could not compute correlation, dates and eigenGene had different lengths." );
                return;
            }

            double dateCorrelation = Distance.spearmanRankCorrelation( eigenGene, new DoubleArrayList( dates ) );

            svo.setPCDateCorrelation( componentNumber, dateCorrelation );
            svo.setPCDateCorrelationPval( componentNumber,
                    CorrelationStats.spearmanPvalue( dateCorrelation, eigenGene.size() ) );
        }

        /*
         * Compare each factor (including batch information that is somewhat redundant with the dates) to the
         * eigen-genes. Using rank statistics.
         */
        for ( ExperimentalFactor ef : bioMaterialFactorMap.keySet() ) {
            Map<Long, Double> bmToFv = bioMaterialFactorMap.get( ef );

            double[] fvs = new double[svdBioMaterials.length];
            assert fvs.length > 0;

            int numNotMissing = 0;

            boolean initializing = false;
            if ( !svo.getFactors().containsKey( ef.getId() ) ) {
                svo.getFactors().put( ef.getId(), new ArrayList<Double>() );
                initializing = true;
            }

            for ( int j = 0; j < svdBioMaterials.length; j++ ) {
                fvs[j] = bmToFv.get( svdBioMaterials[j] );
                if ( !Double.isNaN( fvs[j] ) ) {
                    numNotMissing++;
                }
                // note that this is a double. In the case of categorical factors, it's the Double-fied ID of the factor
                // value.
                if ( initializing ) {
                    if ( SVDServiceHelperImpl.log.isDebugEnabled() )
                        SVDServiceHelperImpl.log
                                .debug( "EF:" + ef.getId() + " fv=" + bmToFv.get( svdBioMaterials[j] ) );
                    svo.getFactors().get( ef.getId() ).add( bmToFv.get( svdBioMaterials[j] ) );
                }
            }

            if ( fvs.length != eigenGene.size() ) {
                SVDServiceHelperImpl.log.debug( fvs.length + " factor values (biomaterials) but " + eigenGene.size()
                        + " values in the eigenGene" );
                continue;
            }

            if ( numNotMissing < SVDServiceHelperImpl.MINIMUM_POINTS_TO_COMPARE_TO_EIGEN_GENE ) {
                SVDServiceHelperImpl.log.debug( "Insufficient values to compare " + ef + " to eigenGenes" );
                continue;
            }

            if ( ExperimentalDesignUtils.isContinuous( ef ) ) {
                double factorCorrelation = Distance.spearmanRankCorrelation( eigenGene, new DoubleArrayList( fvs ) );
                svo.setPCFactorCorrelation( componentNumber, ef, factorCorrelation );
                svo.setPCFactorCorrelationPval( componentNumber, ef,
                        CorrelationStats.spearmanPvalue( factorCorrelation, eigenGene.size() ) );
            } else {

                Collection<Integer> groups = new HashSet<>();
                IntArrayList groupings = new IntArrayList( fvs.length );
                int k = 0;
                DoubleArrayList eigenGeneWithoutMissing = new DoubleArrayList();
                for ( double d : fvs ) {
                    if ( Double.isNaN( d ) ) {
                        k++;
                        continue;
                    }
                    groupings.add( ( int ) d );
                    groups.add( ( int ) d );
                    eigenGeneWithoutMissing.add( eigenGene.get( k ) );
                    k++;
                }

                if ( groups.size() < 2 ) {
                    SVDServiceHelperImpl.log
                            .debug( "Factor had less than two groups: " + ef + ", SVD comparison can't be done." );
                    continue;
                }

                if ( eigenGeneWithoutMissing.size() < SVDServiceHelperImpl.MINIMUM_POINTS_TO_COMPARE_TO_EIGEN_GENE ) {
                    SVDServiceHelperImpl.log
                            .debug( "Too few non-missing values for factor to compare to eigenGenes: " + ef );
                    continue;
                }

                if ( groups.size() == 2 ) {
                    // use the one that still has missing values.
                    double factorCorrelation = Distance
                            .spearmanRankCorrelation( eigenGene, new DoubleArrayList( fvs ) );
                    svo.setPCFactorCorrelation( componentNumber, ef, factorCorrelation );
                    svo.setPCFactorCorrelationPval( componentNumber, ef,
                            CorrelationStats.spearmanPvalue( factorCorrelation, eigenGeneWithoutMissing.size() ) );
                } else {
                    // one-way ANOVA on ranks.
                    double kwPVal = KruskalWallis.test( eigenGeneWithoutMissing, groupings );

                    svo.setPCFactorCorrelationPval( componentNumber, ef, kwPVal );

                    double factorCorrelation = Distance
                            .spearmanRankCorrelation( eigenGene, new DoubleArrayList( fvs ) );
                    double corrPvalue = CorrelationStats
                            .spearmanPvalue( factorCorrelation, eigenGeneWithoutMissing.size() );
                    assert Math.abs( factorCorrelation ) < 1.0 + 1e-2; // sanity.
                    /*
                     * Avoid storing a pvalue, as it's hard to compare. If the regular linear correlation is strong,
                     * then we should just use that -- basically, it means the order we have the groups happens to be a
                     * good one. Of course we could just store pvalues, but that's not easy to use either.
                     */
                    if ( corrPvalue <= kwPVal ) {
                        svo.setPCFactorCorrelation( componentNumber, ef, factorCorrelation );
                    } else {
                        // hack. A bit like turning pvalues into prob it
                        double approxCorr = CorrelationStats
                                .correlationForPvalue( kwPVal, eigenGeneWithoutMissing.size() );
                        svo.setPCFactorCorrelation( componentNumber, ef, approxCorr );

                    }
                }

            }
        }
    }

    /**
     * Fill in NaN for any missing biomaterial factorValues (dates were already done)
     */
    private void fillInMissingValues( Map<ExperimentalFactor, Map<Long, Double>> bioMaterialFactorMap,
            Long[] svdBioMaterials ) {

        for ( Long id : svdBioMaterials ) {
            for ( ExperimentalFactor ef : bioMaterialFactorMap.keySet() ) {
                if ( !bioMaterialFactorMap.get( ef ).containsKey( id ) ) {
                    /*
                     * Missing values in factors, not fatal but not great either.
                     */
                    if ( SVDServiceHelperImpl.log.isDebugEnabled() )
                        SVDServiceHelperImpl.log
                                .debug( "Incomplete factorvalue information for " + ef + " (biomaterial id=" + id
                                        + " missing a value)" );
                    bioMaterialFactorMap.get( ef ).put( id, Double.NaN );
                }
            }
        }
    }

    private void getFactorsForAnalysis( Collection<BioAssay> bioAssays, Map<Long, Date> bioMaterialDates,
            Map<ExperimentalFactor, Map<Long, Double>> bioMaterialFactorMap ) {
        for ( BioAssay bioAssay : bioAssays ) {
            Date processingDate = bioAssay.getProcessingDate();
            BioMaterial bm = bioAssay.getSampleUsed();
            bioMaterialDates.put( bm.getId(), processingDate ); // can be null

            SVDServiceHelperImpl.populateBMFMap( bioMaterialFactorMap, bm );
        }
    }

    /**
     * Gather the information we need for comparing PCs to factors.
     */
    private void prepareForFactorComparisons( SVDValueObject svo, Collection<BioAssay> bioAssays,
            Map<Long, Date> bioMaterialDates, Map<ExperimentalFactor, Map<Long, Double>> bioMaterialFactorMap ) {
        /*
         * Note that dates or batch information can be missing for some bioassays.
         */

        this.getFactorsForAnalysis( bioAssays, bioMaterialDates, bioMaterialFactorMap );
        Long[] svdBioMaterials = svo.getBioMaterialIds();

        if ( svdBioMaterials == null || svdBioMaterials.length == 0 ) {
            throw new IllegalStateException( "SVD did not have biomaterial information" );
        }

        this.fillInMissingValues( bioMaterialFactorMap, svdBioMaterials );

    }

    private PrincipalComponentAnalysis updatePca( ExpressionExperiment ee, ExpressionDataSVD svd,
            DoubleMatrix<Integer, BioMaterial> v, BioAssayDimension b ) {
        principalComponentAnalysisService.removeForExperiment( ee );
        PrincipalComponentAnalysis pca = principalComponentAnalysisService
                .create( ee, svd.getU(), svd.getEigenvalues(), v, b, SVDServiceHelperImpl.MAX_NUM_COMPONENTS_TO_PERSIST,
                        SVDServiceHelperImpl.MAX_LOADINGS_TO_PERSIST );

        ee = expressionExperimentService.thawLite( ee ); // I wish this wasn't needed.
        auditTrailService.addUpdateEvent( ee, PCAAnalysisEvent.class, "SVD computation", null );
        return pca;
    }

}
