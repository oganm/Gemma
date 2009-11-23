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
package ubic.gemma.loader.expression.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubic.gemma.loader.expression.simple.model.SimpleExpressionExperimentMetaData;
import ubic.gemma.model.common.quantitationtype.GeneralType;
import ubic.gemma.model.common.quantitationtype.ScaleType;
import ubic.gemma.model.common.quantitationtype.StandardQuantitationType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.testing.BaseSpringContextTest;

/**
 * @author pavlidis
 * @version $Id$
 */
public class SimpleExpressionDataLoaderServiceTest extends BaseSpringContextTest {

    ExpressionExperiment ee;

    @Autowired
    ExpressionExperimentService eeService;

    @Autowired
    SimpleExpressionDataLoaderService service;

    @After
    public void after() {
        if ( ee != null ) {
            ee = eeService.load( ee.getId() );
            eeService.delete( ee );
        }

    }

    @Test
    public final void testLoad() throws Exception {

        SimpleExpressionExperimentMetaData metaData = new SimpleExpressionExperimentMetaData();
        ArrayDesign ad = ArrayDesign.Factory.newInstance();
        ad.setName( RandomStringUtils.randomAlphabetic( 5 ) );
        Collection<ArrayDesign> ads = new HashSet<ArrayDesign>();
        ads.add( ad );
        metaData.setArrayDesigns( ads );

        Taxon taxon = Taxon.Factory.newInstance();
        taxon.setCommonName( "mouse" );
        taxon.setIsGenesUsable( true );
        taxon.setIsSpecies( true );
        metaData.setTaxon( taxon );
        metaData.setName( RandomStringUtils.randomAlphabetic( 5 ) );
        metaData.setQuantitationTypeName( "testing" );
        metaData.setGeneralType( GeneralType.QUANTITATIVE );
        metaData.setScale( ScaleType.LOG2 );
        metaData.setType( StandardQuantitationType.AMOUNT );
        metaData.setIsRatio( true );

        InputStream data = this.getClass().getResourceAsStream( "/data/testdata.txt" );

        ee = service.load( metaData, data );

        eeService.thaw( ee );

        assertNotNull( ee );
        assertEquals( 30, ee.getRawExpressionDataVectors().size() );
        assertEquals( 12, ee.getBioAssays().size() );
    }

    /**
     * @throws Exception
     *         {@link ubic.gemma.loader.expression.simple.SimpleExpressionDataLoaderService#loadPersistentModel(ubic.gemma.loader.expression.simple.model.ExpressionExperimentMetaData, java.io.InputStream)}
     *         .
     */
    @Test
    public final void testLoadB() throws Exception {

        SimpleExpressionExperimentMetaData metaData = new SimpleExpressionExperimentMetaData();
        ArrayDesign ad = ArrayDesign.Factory.newInstance();
        ad.setName( RandomStringUtils.randomAlphabetic( 5 ) );
        Collection<ArrayDesign> ads = new HashSet<ArrayDesign>();
        ads.add( ad );
        metaData.setArrayDesigns( ads );

        Taxon taxon = Taxon.Factory.newInstance();
        taxon.setCommonName( "mouse" );
        metaData.setTaxon( taxon );
        metaData.setName( RandomStringUtils.randomAlphabetic( 5 ) );
        metaData.setQuantitationTypeName( "testing" );
        metaData.setGeneralType( GeneralType.QUANTITATIVE );
        metaData.setScale( ScaleType.LOG2 );
        metaData.setType( StandardQuantitationType.AMOUNT );
        metaData.setIsRatio( true );

        InputStream data = this.getClass().getResourceAsStream(
                "/data/loader/aov.results-2-monocyte-data-bytime.bypat.data.sort" );

        ee = service.load( metaData, data );

        eeService.thaw( ee );

        assertNotNull( ee );
        assertEquals( 200, ee.getRawExpressionDataVectors().size() );
        assertEquals( 59, ee.getBioAssays().size() );
        // 
    }

}
