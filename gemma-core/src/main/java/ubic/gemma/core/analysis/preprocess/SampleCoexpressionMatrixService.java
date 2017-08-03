/*
 * The Gemma project
 * 
 * Copyright (c) 2012 University of British Columbia
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
package ubic.gemma.core.analysis.preprocess;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

import java.util.Collection;

/**
 * @author Paul
 */
public interface SampleCoexpressionMatrixService {

    /**
     * Creates the matrix, or loads it if it already exists.
     */
    DoubleMatrix<BioAssay, BioAssay> findOrCreate( ExpressionExperiment expressionExperiment );

    /**
     * Retrieve (and if necessary compute) the correlation matrix for the samples.
     *
     * @return Matrix, sorted by experimental design
     */
    DoubleMatrix<BioAssay, BioAssay> create( ExpressionExperiment ee, boolean forceRecompute );

    boolean hasMatrix( ExpressionExperiment ee );

    void delete( ExpressionExperiment ee );

    /**
     * @return correlation matrix. The matrix is NOT sorted by the experimental design.
     */
    DoubleMatrix<BioAssay, BioAssay> create( ExpressionExperiment ee,
            Collection<ProcessedExpressionDataVector> processedVectors );

}