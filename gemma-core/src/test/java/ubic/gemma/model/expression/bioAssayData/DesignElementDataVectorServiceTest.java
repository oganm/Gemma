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
package ubic.gemma.model.expression.bioAssayData;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ubic.gemma.core.genome.gene.service.GeneService;
import ubic.gemma.core.loader.expression.geo.AbstractGeoServiceTest;
import ubic.gemma.core.loader.expression.geo.GeoDomainObjectGeneratorLocal;
import ubic.gemma.core.loader.expression.geo.service.GeoService;
import ubic.gemma.core.loader.util.AlreadyExistsInSystemException;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.expression.bioAssayData.RawExpressionDataVectorService;
import ubic.gemma.persistence.service.expression.designElement.CompositeSequenceService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author joseph
 */
public class DesignElementDataVectorServiceTest extends AbstractGeoServiceTest {

    @Autowired
    protected GeoService geoService;
    @Autowired
    ExpressionExperimentService expressionExperimentService;
    @Autowired
    ArrayDesignService arrayDesignService;
    @Autowired
    CompositeSequenceService compositeSequenceService;
    @Autowired
    GeneService geneService;
    @Autowired
    RawExpressionDataVectorService rawService;
    private ExpressionExperiment newee = null;

    @After
    public void tearDown() {
        try {
            if ( newee != null && newee.getId() != null ) {
                expressionExperimentService.remove( newee );
            }
        } catch ( Exception ignored ) {

        }

    }

    @Test
    public void testFindByQt() throws Exception {

        try {

            geoService.setGeoDomainObjectGenerator(
                    new GeoDomainObjectGeneratorLocal( this.getTestFileBasePath( "gse432Short" ) ) );
            Collection<?> results = geoService.fetchAndLoad( "GSE432", false, true, false );
            newee = ( ExpressionExperiment ) results.iterator().next();

        } catch ( AlreadyExistsInSystemException e ) {
            newee = ( ExpressionExperiment ) e.getData();
        }

        newee.setShortName( RandomStringUtils.randomAlphabetic( 12 ) );
        expressionExperimentService.update( newee );

        newee = this.expressionExperimentService.thawLite( newee );

        QuantitationType qt = null;
        for ( QuantitationType q : newee.getQuantitationTypes() ) {
            if ( q.getIsPreferred() ) {
                qt = q;
                break;
            }
        }

        assertNotNull( "QT is null", qt );

        Collection<? extends DesignElementDataVector> preferredVectors = rawService.findRawAndProcessed( qt );

        assertNotNull( preferredVectors );
        assertEquals( 40, preferredVectors.size() );
    }

}