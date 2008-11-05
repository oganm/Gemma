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
package ubic.gemma.model.association.coexpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.analysis.preprocess.ProcessedExpressionDataVectorCreateService;
import ubic.gemma.model.analysis.expression.ExpressionExperimentSet;
import ubic.gemma.model.analysis.expression.coexpression.ProbeCoexpressionAnalysis;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.testing.BaseSpringContextTest;

/**
 * @author pavlidis
 * @version $Id$
 */
public class Probe2ProbeCoexpressionDaoImplTest extends BaseSpringContextTest {

    private static Log log = LogFactory.getLog( Probe2ProbeCoexpressionDaoImplTest.class.getName() );

    ExpressionExperiment ee;
    ExpressionExperimentService ees;
    Long firstProbeId;
    Long secondProbeId;

    Probe2ProbeCoexpressionService ppcs;

    @SuppressWarnings("unchecked")
    @Override
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        this.endTransaction();
        ee = this.getTestPersistentCompleteExpressionExperiment( false );

        ees = ( ExpressionExperimentService ) this.getBean( "expressionExperimentService" );

        Collection<QuantitationType> qts = ees.getQuantitationTypes( ee );

        // this is bogus, it should represent "pearson correlation" for example, but doesn't matter for this test.
        QuantitationType qt = qts.iterator().next();

        ppcs = ( Probe2ProbeCoexpressionService ) this.getBean( "probe2ProbeCoexpressionService" );

        ProcessedExpressionDataVectorCreateService processedExpressionDataVectorCreateService = ( ProcessedExpressionDataVectorCreateService ) this
                .getBean( "processedExpressionDataVectorCreateService" );

        Collection<ProcessedExpressionDataVector> dvs = processedExpressionDataVectorCreateService
                .computeProcessedExpressionData( ee );

        List<ProcessedExpressionDataVector> dvl = new ArrayList<ProcessedExpressionDataVector>( dvs );

        int j = dvs.size();

        assert j > 0;

        ProbeCoexpressionAnalysis analysis = ProbeCoexpressionAnalysis.Factory.newInstance();
        analysis.setName( "foo" );
        Taxon mouse = this.getTaxon( "mouse" );
        ExpressionExperimentSet se = ExpressionExperimentSet.Factory.newInstance();
        se.getExperiments().add( ee );
        se.setTaxon( mouse );
        se.setName( "bar" );
        analysis.setExpressionExperimentSetAnalyzed( se );

        analysis = ( ProbeCoexpressionAnalysis ) this.persisterHelper.persist( analysis );

        this.firstProbeId = dvl.get( 0 ).getDesignElement().getId();
        this.secondProbeId = dvl.get( 1 ).getDesignElement().getId();
        
        for ( int i = 0; i < j - 1; i += 2 ) {
            Probe2ProbeCoexpression ppc = MouseProbeCoExpression.Factory.newInstance();
            ppc.setFirstVector( dvl.get( i ) );
            ppc.setSecondVector( dvl.get( i + 1 ) );
            ppc.setMetric( qt );
            ppc.setScore( 0.0 );
            ppc.setPvalue( 0.2 );
            ppc.setExpressionBioAssaySet( ee );
            ppc.setSourceAnalysis( analysis );
            ppcs.create( ppc );
        }

        this.flushAndClearSession();

    }

    @Override
    protected void onTearDownInTransaction() throws Exception {
        super.onTearDownInTransaction();
        if ( ee != null ) {
            ees.delete( ee );
        }
    }

    public void testCountLinks() {
        Integer countLinks = ppcs.countLinks( ee );
        /*
         * This would be 6 but we divide the count by 2 as it is assumed we save each link twice.
         */
        assertEquals( 3, countLinks.intValue() );
    }

    /**
     * 
     *
     */
    public void testHandleDeleteLinksExpressionExperiment() {
        ppcs.deleteLinks( ee );
        Integer countLinks = ppcs.countLinks( ee );
        assertEquals( 0, countLinks.intValue() );
    }
    
    public void testValidateProbesInCoexpression(){
        Collection<Long> queryProbeIds = new ArrayList<Long>();
        queryProbeIds.add( new Long( this.firstProbeId) );
        queryProbeIds.add( new Long( 3));
        queryProbeIds.add( new Long( 100 ));
        
        Collection<Long> coexpressedProbeIds = new ArrayList<Long>();
        coexpressedProbeIds.add( new Long( this.secondProbeId));
        coexpressedProbeIds.add( new Long( 4));
        coexpressedProbeIds.add( new Long( 101 ));
        
        Collection<Long> results = null;
        try{
           results = ppcs.validateProbesInCoexpression(queryProbeIds,coexpressedProbeIds, ee,"mouse" );
        }catch(Exception e){
            log.info( "Boom!  " + e );
        }
        
        log.info( "ee id: " + ee.getId() );
        
        assertFalse( results.contains(  new Long(100) ));
        assertTrue( results.contains( new Long(this.firstProbeId) ));
    }

}
