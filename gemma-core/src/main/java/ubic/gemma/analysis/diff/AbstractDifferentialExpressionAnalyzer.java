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
package ubic.gemma.analysis.diff;

import ubic.gemma.model.expression.analysis.ExpressionAnalysis;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * An abstract differential expression analyzer to be extended by analyzers which will make use of R. For example, see
 * {@link OneWayAnovaAnalyzer}.
 * 
 * @author keshav
 * @version $Id$
 */
public abstract class AbstractDifferentialExpressionAnalyzer extends AbstractAnalyzer {

    public static final String DIFFERENTIAL_EXPRESSION = "differential expression";

    /**
     * @param expressionExperiment
     * @return ExpressionAnalysis
     */
    public abstract ExpressionAnalysis getExpressionAnalysis( ExpressionExperiment expressionExperiment );

    /**
     * Sets the name and description on the analysis. Typically, this involves terms like "differential", "one", "two",
     * etc.
     * 
     * @param expressionAnalysis
     * @param name
     * @param description
     */
    public void setAnalysisMetadata( ExpressionAnalysis expressionAnalysis, String name, String description ) {
        expressionAnalysis.setName( name );
        expressionAnalysis.setDescription( description );
    }

}
