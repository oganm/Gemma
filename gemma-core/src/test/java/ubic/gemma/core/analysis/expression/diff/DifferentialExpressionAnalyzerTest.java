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
package ubic.gemma.core.analysis.expression.diff;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author keshav
 */
public class DifferentialExpressionAnalyzerTest extends BaseAnalyzerConfigurationTest {

    @Autowired
    AnalysisSelectionAndExecutionService analysis = null;
    private DiffExAnalyzer analyzer;

    /*
     * Tests determineAnalysis.
     * 2 experimental factors
     * 2 factor value / experimental factor
     * complete block design and biological replicates
     * Expected analyzer: TwoWayAnovaWithInteractionsAnalyzerImpl
     */
    @Test
    public void testDetermineAnalysisA() {
        this.configureMocks();
        analyzer = this.getBean( DiffExAnalyzer.class );
        analyzer.setExpressionDataMatrixService( expressionDataMatrixService );
    }

    /*
     * Tests determineAnalysis.
     * 2 experimental factors
     * 2 factor value / experimental factor
     * no replicates
     * Expected analyzer: TwoWayAnovaWithoutInteractionsAnalyzerImpl
     */
    @Test
    public void testDetermineAnalysisB() throws Exception {
        super.configureTestDataForTwoWayAnovaWithoutInteractions();
        this.configureMocks();
        analyzer = this.getBean( DiffExAnalyzer.class );
        analyzer.setExpressionDataMatrixService( expressionDataMatrixService );
    }

    private void configureMocks() {
        this.configureMockAnalysisServiceHelper( 2 );
    }

}
