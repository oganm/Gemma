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
package ubic.gemma.security;

import java.util.Collection;
import java.util.HashSet;

import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.RandomStringUtils;

import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.designElement.CompositeSequenceService;
import ubic.gemma.testing.BaseSpringContextTest;

/**
 * Use this to test acegi functionality.
 * 
 * @author pavlidis
 * @author keshav
 * @version $Id$
 */
public class SecurityIntegrationTest extends BaseSpringContextTest {

    private ArrayDesignService arrayDesignService;

    ArrayDesign arrayDesign;
    String username = "test";
    String aDifferentUser = "aDifferentUsername";

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.BaseDependencyInjectionSpringContextTest#onSetUpInTransaction()
     */
    @Override
    protected void onSetUpInTransaction() throws Exception {

        // super.onSetUpInTransaction(); //admin
        super.onSetUpInTransactionGrantingUserAuthority( username ); // user

        arrayDesign = ArrayDesign.Factory.newInstance();
        arrayDesign.setName( "Array Design Foo" );
        arrayDesign.setDescription( "A test ArrayDesign from " + this.getClass().getName() );

        CompositeSequence cs1 = CompositeSequence.Factory.newInstance();
        cs1.setName( "Design Element Bar1" );

        CompositeSequence cs2 = CompositeSequence.Factory.newInstance();
        cs2.setName( "Design Element Bar2" );

        Collection<CompositeSequence> col = new HashSet<CompositeSequence>();
        col.add( cs1 );
        col.add( cs2 );

        /*
         * Note this sequence. Remember, inverse="true" if using this. If you do not make an explicit call to
         * cs1(2).setArrayDesign(arrayDesign), then inverse="false" must be set.
         */
        cs1.setArrayDesign( arrayDesign );
        cs2.setArrayDesign( arrayDesign );
        arrayDesign.setCompositeSequences( col );

        // arrayDesign = arrayDesignService.findOrCreate( arrayDesign );
        arrayDesign = ( ArrayDesign ) persisterHelper.persist( arrayDesign );

    }

    /**
     * Test removing an arrayDesign with the correct authorization privileges. The security interceptor should be called
     * on this method, as should the AddOrRemoveFromACLInterceptor.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testRemoveArrayDesign() throws Exception {
        ArrayDesign ad = ArrayDesign.Factory.newInstance();
        ad.setName( RandomStringUtils.randomAlphabetic( 10 ) + "_array" );
        ad = ( ArrayDesign ) persisterHelper.persist( ad );
        arrayDesignService.remove( ad );
    }

    /**
     * Test removing an arrayDesign without the correct authorization. The security interceptor should be called on this
     * method, as should the AddOrRemoveFromACLInterceptor. You should get an AccessDeniedException.
     * 
     * @throws Exception
     */
    public void testRemoveArrayDesignNotAuthorized() throws Exception {

        this.onSetUpInTransactionGrantingUserAuthority( aDifferentUser );// use a non-admin user

        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info( "user is " + obj.toString() );

        try {
            arrayDesignService.remove( arrayDesign );
            fail( "Should have gotten an AccessDeniedException" );
        } catch ( AccessDeniedException okay ) {
            log.info( "Access successfully denied." );
        }
    }

    /**
     * Tests getting composite sequences (target objects) with correct privileges on domain object (array design).
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testGetCompositeSequencesForArrayDesign() throws Exception {

        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info( "user is: " + obj.toString() );

        assertNotNull( arrayDesign.getId() );

        CompositeSequenceService compositeSequenceService = ( CompositeSequenceService ) this
                .getBean( "compositeSequenceService" );
        Collection col = compositeSequenceService.findByName( "Design Element Bar1" );
        if ( col.size() == 0 ) {
            fail( "User not authorized to access at least one of the objects in the graph" );
        }
    }

    /**
     * Tests getting composite sequences (target objects) without correct privileges on domain object (array design).
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testGetCompositeSequencesForArrayDesignWithoutAuthorization() throws Exception {

        this.onSetUpInTransactionGrantingUserAuthority( aDifferentUser );// a different user

        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info( "user is: " + obj.toString() );

        assertNotNull( arrayDesign.getId() );

        CompositeSequenceService compositeSequenceService = ( CompositeSequenceService ) this
                .getBean( "compositeSequenceService" );
        Collection col = compositeSequenceService.findByName( "Design Element Bar1" );

        /*
         * expection is to not have access to the composite sequences for this array design when authenticated as
         * 'aDifferentUser'
         */
        assertTrue(
                "User should not be authorized to access target objects (composite sequences) in the graph for this domain object (array design).",
                col.isEmpty() );

    }

    /**
     * @param arrayDesignService The arrayDesignService to set.
     */
    public void setArrayDesignService( ArrayDesignService arrayDesignService ) {
        this.arrayDesignService = arrayDesignService;
    }

}
