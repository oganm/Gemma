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
package ubic.gemma.web.controller.expression.bioAssay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssay.BioAssayService; 
import ubic.gemma.testing.BaseSpringWebTest;

/**
 * @author keshav
 * @version $Id$
 */
public class BioAssayFormControllerTest extends BaseSpringWebTest {

    private Log log = LogFactory.getLog( this.getClass() );
    private MockHttpServletRequest request = null;

    BioAssay ba = BioAssay.Factory.newInstance();

    /**
     * setUp
     * 
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {

        ArrayDesign ad = this.getTestPersistentArrayDesign( 10, true, false, true ); // readonly.
        ba = this.getTestPersistentBioAssay( ad );
        BioAssayService bas = ( BioAssayService ) getBean( "bioAssayService" );
        ba = bas.findOrCreate( ba );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testEdit() throws Exception {
        log.debug( "testing edit" );

        BioAssayFormController c = ( BioAssayFormController ) getBean( "bioAssayFormController" );

        request = new MockHttpServletRequest( "GET", "/bioAssay/editBioAssay.html" );
        request.addParameter( "id", ba.getId().toString() );

        ModelAndView mav = c.handleRequest( request, ( new MockHttpServletResponse() ) );

        assertEquals( "bioAssay.edit", mav.getViewName() );

        // 
    }

    /**
     * @throws Exception
     */
    @Test
    public void testFormBackingObject() throws Exception {
        log.debug( "testing formBackingObject" );
        BioAssayFormController c = ( BioAssayFormController ) getBean( "bioAssayFormController" );

        request = new MockHttpServletRequest( "GET", "/bioAssay/editBioAssay.html" );
        request.addParameter( "id", ba.getId().toString() );

        BioAssay b = ( BioAssay ) c.formBackingObject( request );

        assertNotNull( b );

    }
}
