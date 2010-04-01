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
package ubic.gemma.analysis.expression.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.model.analysis.expression.ExpressionAnalysis;
import ubic.gemma.model.analysis.expression.ExpressionAnalysisResultSet;
import ubic.gemma.model.analysis.expression.ExpressionExperimentSet;
import ubic.gemma.model.analysis.expression.ProbeAnalysisResult;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysisResult;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.BioAssaySet;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * A two way anova base class as described by P. Pavlidis, Methods 31 (2003) 282-289.
 * <p>
 * See http://www.bioinformatics.ubc.ca/pavlidis/lab/docs/reprints/anova-methods.pdf.
 * <p>
 * For specific implementations with and without interactions, see the {@link TwoWayAnovaWithInteractionsAnalyzer} and
 * {@link TwoWayAnovaWithoutInteractionsAnalyzer} respectively.
 * 
 * @author keshav
 * @version $Id$
 */
public abstract class AbstractTwoWayAnovaAnalyzer extends AbstractDifferentialExpressionAnalyzer {

    protected final int mainEffectAIndex = 0;
    protected final int mainEffectBIndex = 1;
    protected final int mainEffectInteractionIndex = 2;
    protected final int maxResults = 3;

    /**
     * Creates and returns an {@link ExpressionAnalysis} and fills in the expression analysis results.
     * 
     * @param dmatrix
     * @param mainEffectAPvalues
     * @param mainEffectBPvalues
     * @param interactionEffectPvalues - null if no interactions.
     * @param fStatistics
     * @param experimentalFactorA
     * @param experimentalFactorB
     * @param quantitationType
     * @param expressionExperiment
     * @return
     */
    protected DifferentialExpressionAnalysis createExpressionAnalysis( ExpressionDataDoubleMatrix dmatrix,
            double[] mainEffectAPvalues, double[] mainEffectBPvalues, double[] interactionEffectPvalues,
            double[] fStatistics, ExperimentalFactor experimentalFactorA, ExperimentalFactor experimentalFactorB,
            QuantitationType quantitationType, ExpressionExperiment expressionExperiment ) {

        assert mainEffectAPvalues.length == mainEffectBPvalues.length;

        int pvaluesPerExample = 2;

        boolean withInteractions = interactionEffectPvalues != null;

        if ( withInteractions ) {
            assert interactionEffectPvalues != null && interactionEffectPvalues.length == mainEffectAPvalues.length;
            pvaluesPerExample = 3;
        }

        assert mainEffectBPvalues.length == fStatistics.length / pvaluesPerExample;

        Collection<ExpressionAnalysisResultSet> resultSets = new HashSet<ExpressionAnalysisResultSet>();

        DifferentialExpressionAnalysis expressionAnalysis = configureAnalysisEntity( dmatrix );

        /* All results for the first main effect */
        List<DifferentialExpressionAnalysisResult> analysisResultsMainEffectA = new ArrayList<DifferentialExpressionAnalysisResult>();

        /* All results for the second main effect */
        List<DifferentialExpressionAnalysisResult> analysisResultsMainEffectB = new ArrayList<DifferentialExpressionAnalysisResult>();

        /* Interaction effect */
        List<DifferentialExpressionAnalysisResult> analysisResultsInteractionEffect = new ArrayList<DifferentialExpressionAnalysisResult>();

        /* q-values */
        double[] mainEffectAQvalues = super.getQValues( mainEffectAPvalues );
        double[] mainEffectBQvalues = super.getQValues( mainEffectBPvalues );

        double[] ranksA = computeRanks( mainEffectAPvalues );
        double[] ranksB = computeRanks( mainEffectBPvalues );
        double[] ranksI = new double[mainEffectAPvalues.length]; // temporary.

        double[] interactionEffectQvalues = null;
        if ( interactionEffectPvalues != null ) {
            ranksI = computeRanks( interactionEffectPvalues );
            interactionEffectQvalues = super.getQValues( interactionEffectPvalues );
        }

        int k = 0;// statistics
        int l = 0;// main effect A
        int m = 0;// main effect B
        int n = 0;// interaction effect
        for ( int i = 0; i < dmatrix.rows(); i++ ) {

            CompositeSequence cs = ( CompositeSequence ) dmatrix.getDesignElementForRow( i );

            for ( int j = 0; j < pvaluesPerExample; j++ ) {

                ProbeAnalysisResult probeAnalysisResult = ProbeAnalysisResult.Factory.newInstance();
                probeAnalysisResult.setProbe( cs );
                probeAnalysisResult.setQuantitationType( quantitationType );

                probeAnalysisResult.setScore( Double.isNaN( fStatistics[k] ) ? null : fStatistics[k] );
                // probeAnalysisResult.setParameters( parameters );

                if ( j % pvaluesPerExample == mainEffectAIndex ) {
                    probeAnalysisResult
                            .setPvalue( Double.isNaN( mainEffectAPvalues[l] ) ? null : mainEffectAPvalues[l] );
                    probeAnalysisResult.setCorrectedPvalue( Double.isNaN( mainEffectAQvalues[l] ) ? null
                            : mainEffectAQvalues[l] );
                    probeAnalysisResult.setRank( Double.isNaN( ranksA[l] ) ? null : ranksA[l] );
                    analysisResultsMainEffectA.add( probeAnalysisResult );
                    l++;
                } else if ( j % pvaluesPerExample == mainEffectBIndex ) {
                    probeAnalysisResult
                            .setPvalue( Double.isNaN( mainEffectBPvalues[m] ) ? null : mainEffectBPvalues[m] );
                    probeAnalysisResult.setCorrectedPvalue( Double.isNaN( mainEffectBQvalues[m] ) ? null
                            : mainEffectBQvalues[m] );
                    probeAnalysisResult.setRank( Double.isNaN( ranksB[m] ) ? null : ranksB[m] );
                    analysisResultsMainEffectB.add( probeAnalysisResult );
                    m++;
                } else if ( j % pvaluesPerExample == mainEffectInteractionIndex ) {
                    if ( interactionEffectPvalues != null ) {
                        probeAnalysisResult.setPvalue( Double.isNaN( interactionEffectPvalues[n] ) ? null
                                : interactionEffectPvalues[n] );
                        probeAnalysisResult.setRank( Double.isNaN( ranksI[n] ) ? null : ranksI[n] );
                    }

                    if ( interactionEffectQvalues != null ) {
                        probeAnalysisResult.setCorrectedPvalue( Double.isNaN( interactionEffectQvalues[n] ) ? null
                                : interactionEffectQvalues[n] );
                    }
                    analysisResultsInteractionEffect.add( probeAnalysisResult );
                    n++;
                }

                k++;
            }
        }

        /* main effects */
        Collection<ExperimentalFactor> mainA = new HashSet<ExperimentalFactor>();
        mainA.add( experimentalFactorA );
        ExpressionAnalysisResultSet mainEffectResultSetA = ExpressionAnalysisResultSet.Factory.newInstance(
                expressionAnalysis, analysisResultsMainEffectA, mainA );
        resultSets.add( mainEffectResultSetA );

        Collection<ExperimentalFactor> mainB = new HashSet<ExperimentalFactor>();
        mainB.add( experimentalFactorB );
        ExpressionAnalysisResultSet mainEffectResultSetB = ExpressionAnalysisResultSet.Factory.newInstance(
                expressionAnalysis, analysisResultsMainEffectB, mainB );
        resultSets.add( mainEffectResultSetB );

        /* interaction effect */
        if ( withInteractions ) {
            Collection<ExperimentalFactor> interAB = new HashSet<ExperimentalFactor>();
            interAB.add( experimentalFactorA );
            interAB.add( experimentalFactorB );
            ExpressionAnalysisResultSet interactionEffectResultSet = ExpressionAnalysisResultSet.Factory.newInstance(
                    expressionAnalysis, analysisResultsInteractionEffect, interAB );
            resultSets.add( interactionEffectResultSet );
        }

        expressionAnalysis.setResultSets( resultSets );

        expressionAnalysis.setName( this.getClass().getSimpleName() );

        expressionAnalysis.setDescription( "Two-way ANOVA for " + experimentalFactorA + " and " + experimentalFactorB
                + ( withInteractions ? " with " : " without " ) + "interactions" );

        return expressionAnalysis;
    }

    /**
     * @param dmatrix
     * @return
     */
    private DifferentialExpressionAnalysis configureAnalysisEntity( ExpressionDataDoubleMatrix dmatrix ) {
        // TODO pass the DifferentialExpressionAnalysisConfig in (see LinkAnalysisService)
        /* Create the expression analysis and pack the results. */
        DifferentialExpressionAnalysisConfig config = new DifferentialExpressionAnalysisConfig();
        DifferentialExpressionAnalysis expressionAnalysis = config.toAnalysis();

        ExpressionExperimentSet eeSet = ExpressionExperimentSet.Factory.newInstance();
        Collection<BioAssaySet> experimentsAnalyzed = new HashSet<BioAssaySet>();
        experimentsAnalyzed.add( dmatrix.getExpressionExperiment() );
        eeSet.setExperiments( experimentsAnalyzed );
        expressionAnalysis.setExpressionExperimentSetAnalyzed( eeSet );
        return expressionAnalysis;
    }

    /*
     * (non-Javadoc)
     * @seeubic.gemma.analysis.diff.AbstractAnalyzer#getExpressionAnalysis(ubic.gemma.model.expression.experiment.
     * ExpressionExperiment)
     */
    @Override
    public DifferentialExpressionAnalysis run( ExpressionExperiment expressionExperiment ) {

        Collection<ExperimentalFactor> experimentalFactors = expressionExperiment.getExperimentalDesign()
                .getExperimentalFactors();

        return run( expressionExperiment, experimentalFactors );

    }

    /*
     * (non-Javadoc)
     * @see
     * ubic.gemma.analysis.expression.diff.AbstractDifferentialExpressionAnalyzer#run(ubic.gemma.model.expression.experiment
     * .ExpressionExperiment, java.util.Collection)
     */
    @Override
    public DifferentialExpressionAnalysis run( ExpressionExperiment expressionExperiment,
            Collection<ExperimentalFactor> experimentalFactors ) {
        if ( experimentalFactors.size() != mainEffectInteractionIndex )
            throw new RuntimeException( "Two way anova supports 2 experimental factors.  Received "
                    + experimentalFactors.size() + "." );

        Iterator<ExperimentalFactor> iter = experimentalFactors.iterator();
        ExperimentalFactor experimentalFactorA = iter.next();
        ExperimentalFactor experimentalFactorB = iter.next();

        return twoWayAnova( expressionExperiment, experimentalFactorA, experimentalFactorB );
    }

    /**
     * To be implemented by the two way anova analyzer.
     * <p>
     * See class level javadoc of two way anova anlayzer for R Call.
     * 
     * @param expressionExperiment
     * @param experimentalFactorA
     * @param experimentalFactorB
     * @return
     */
    protected abstract DifferentialExpressionAnalysis twoWayAnova( ExpressionExperiment expressionExperiment,
            ExperimentalFactor experimentalFactorA, ExperimentalFactor experimentalFactorB );

}
