/*
 * The Gemma project
 * 
 * Copyright (c) 2011 University of British Columbia
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

package ubic.gemma.model.genome;

import org.junit.Test;

import ubic.gemma.core.util.test.BaseSpringContextTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author paul
 */
public class TaxonServiceImplTest extends BaseSpringContextTest {

    /**
     * Test method for {@link ubic.gemma.persistence.service.genome.taxon.TaxonService#findOrCreate(Taxon)} )}.
     * Situation where the secondary id is treated as the primary, we must not make a new taxon!
     */
    @Test
    public void testFindOrCreate() {
        Taxon t = Taxon.Factory.newInstance();
        t.setNcbiId( 559292 );

        Taxon yeast = taxonService.findByCommonName( "yeast" );
        assertNotNull( yeast ); // this should be loaded automatically.

        Taxon found = taxonService.findOrCreate( t );

        assertEquals( new Integer( 4932 ), found.getNcbiId() );
    }

    @Test
    public void testLoadValueObject() {
        Taxon yeast = taxonService.findByCommonName( "yeast" );
        assertNotNull( yeast ); // this should be loaded automatically.
        taxonService.loadValueObject( yeast );
    }

}
