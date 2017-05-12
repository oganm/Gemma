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

package ubic.gemma.model.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubic.gemma.core.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.persistence.service.analysis.expression.diff.DifferentialExpressionAnalysisService;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.core.testing.BaseSpringContextTest;
import ubic.gemma.persistence.util.EntityUtils;

/**
 * @author klc
 * @version $Id$
 */
public class DifferentialExpressionAnalysisServiceTest extends BaseSpringContextTest {

    @Autowired
    private DifferentialExpressionAnalysisService analysisService;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    // Test Data
    DifferentialExpressionAnalysis eAnalysis1;
    DifferentialExpressionAnalysis eAnalysis2;
    DifferentialExpressionAnalysis eAnalysis3;
    DifferentialExpressionAnalysis eAnalysis4;

    ExpressionExperiment e1;
    ExpressionExperiment e2;
    ExpressionExperiment e3;
    ExpressionExperiment e4;

    String dea1_name;
    String dea2_name;

    private String testEESetName;

    private String testAnalysisName;

    @Before
    public void setup() {

        e1 = ExpressionExperiment.Factory.newInstance();
        e1.setShortName( RandomStringUtils.randomAlphabetic( 6 ) );
        e1 = expressionExperimentService.create( e1 );

        e2 = ExpressionExperiment.Factory.newInstance();
        e2.setShortName( RandomStringUtils.randomAlphabetic( 6 ) );
        e2 = expressionExperimentService.create( e2 );

        e3 = ExpressionExperiment.Factory.newInstance();
        e3.setShortName( RandomStringUtils.randomAlphabetic( 6 ) );
        e3 = expressionExperimentService.create( e3 );

        e4 = ExpressionExperiment.Factory.newInstance();
        e4.setShortName( RandomStringUtils.randomAlphabetic( 6 ) );
        e4 = expressionExperimentService.create( e4 );

        // //////////////////
        eAnalysis1 = DifferentialExpressionAnalysis.Factory.newInstance();
        eAnalysis1.setExperimentAnalyzed( e1 );
        dea1_name = RandomStringUtils.randomAlphabetic( 6 );
        eAnalysis1.setName( dea1_name );
        eAnalysis1.setDescription( "An analysis Test 1" );
        eAnalysis1 = analysisService.create( eAnalysis1 );

        // ///////////////
        eAnalysis2 = DifferentialExpressionAnalysis.Factory.newInstance();

        eAnalysis2.setExperimentAnalyzed( e2 );
        dea2_name = RandomStringUtils.randomAlphabetic( 6 );
        eAnalysis2.setName( dea2_name );
        eAnalysis2.setDescription( "An analysis Test 2" );
        eAnalysis2 = analysisService.create( eAnalysis2 );

        // /////////////
        eAnalysis3 = DifferentialExpressionAnalysis.Factory.newInstance();

        eAnalysis3.setExperimentAnalyzed( e3 );
        this.testAnalysisName = RandomStringUtils.randomAlphabetic( 6 );
        eAnalysis3.setName( testAnalysisName );
        eAnalysis3.setDescription( "An analysis Test 3" );
        eAnalysis3 = analysisService.create( eAnalysis3 );

        // ////
        eAnalysis4 = DifferentialExpressionAnalysis.Factory.newInstance();
        eAnalysis4.setExperimentAnalyzed( e3 );
        testEESetName = RandomStringUtils.randomAlphabetic( 6 );
        eAnalysis4.setName( testEESetName );
        eAnalysis4.setDescription( "An analysis Test 4" );
        eAnalysis4 = analysisService.create( eAnalysis4 );

    }

    /**
     * 
     */
    @Test
    public void testFindByInvestigation() {

        Collection<DifferentialExpressionAnalysis> results = analysisService.findByInvestigation( e1 );
        assertEquals( 1, results.size() );
        DifferentialExpressionAnalysis dea = results.iterator().next();
        assertEquals( dea1_name, dea.getName() );

        results = analysisService.findByInvestigation( e2 );
        assertEquals( 1, results.size() );
        dea = results.iterator().next();
        assertEquals( dea2_name, dea.getName() );

        results = analysisService.findByInvestigation( e3 );
        assertEquals( 2, results.size() );
    }

    /**
     * 
     */
    @Test
    public void testFindByInvestigations() {
        Collection<ExpressionExperiment> investigations = new ArrayList<ExpressionExperiment>();
        investigations.add( e1 );
        investigations.add( e3 );

        Map<Investigation, Collection<DifferentialExpressionAnalysis>> results = analysisService
                .findByInvestigations( investigations );
        assertEquals( 2, results.keySet().size() );

        assertEquals( 1, results.get( e1 ).size() );
        assertEquals( 2, results.get( e3 ).size() );

        // also by ID
        Map<Long, Collection<DifferentialExpressionAnalysis>> ees = analysisService.findByInvestigationIds( EntityUtils
                .getIds( investigations ) );
        assertEquals( 2, ees.size() );

        assertEquals( 1, ees.get( e1.getId() ).size() );
        assertEquals( 2, ees.get( e3.getId() ).size() );
    }

    @Test
    public void testFindByNameExact() {

        Collection<DifferentialExpressionAnalysis> result = analysisService.findByName( this.testEESetName );
        assertNotNull( result );
        assertEquals( 1, result.size() );
        assertEquals( this.testEESetName, result.iterator().next().getName() );
    }

    @Test
    public void testFindByNameRecent() {
        Collection<DifferentialExpressionAnalysis> result = analysisService.findByName( this.testAnalysisName );
        assertNotNull( result );
        assertEquals( this.testAnalysisName, result.iterator().next().getName() );
    }

    /**
     *  
     */
    // @Test
    // public void testFindByUniqueInvestigations() {
    // Collection<Investigation> investigations = new ArrayList<Investigation>();
    // investigations.add( e1 );
    // investigations.add( e2 );
    //
    // DifferentialExpressionAnalysis results = analysisService.findByUniqueInvestigations( investigations );
    // assertNotNull( results );
    // assertEquals( eAnalysis2.getId(), results.getId() );
    //
    // }

}
