package ubic.gemma.core.security.authorization.acl;

import gemma.gsec.acl.domain.AclObjectIdentity;
import gemma.gsec.acl.domain.AclService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.stereotype.Component;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.experiment.*;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;

import static org.junit.Assert.*;

/**
 * Methods for checking ACLs.
 *
 * @author paul
 */
@Component
public class AclTestUtils {

    private static final Log log = LogFactory.getLog( AclTestUtils.class );

    @Autowired
    private AclService aclService;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    /**
     * Make sure object f has no ACLs
     *
     * @param f f
     */
    public void checkDeletedAcl( Object f ) {

        Acl acl = this.getAcl( f );
        if ( acl != null ) {
            fail( "Failed to  remove ACL for " + f + ", got " + acl );
        }
    }

    /**
     * CHeck the entire entity graph of an ee for ACL deletion.
     *
     * @param ee ee
     */
    public void checkDeleteEEAcls( ExpressionExperiment ee ) {
        this.checkDeletedAcl( ee );

        this.checkDeletedAcl( ee.getExperimentalDesign() );

        for ( ExperimentalFactor f : ee.getExperimentalDesign().getExperimentalFactors() ) {
            this.checkDeletedAcl( f );

            for ( FactorValue fv : f.getFactorValues() ) {
                this.checkDeletedAcl( fv );
            }
        }

        assertTrue( ee.getBioAssays().size() > 0 );
        for ( BioAssay ba : ee.getBioAssays() ) {
            this.checkDeletedAcl( ba );

            BioMaterial bm = ba.getSampleUsed();
            this.checkDeletedAcl( bm );
        }

    }

    /**
     * Validate ACLs on EE
     *
     * @param ee ee
     */
    public void checkEEAcls( ExpressionExperiment ee ) {
        ee = expressionExperimentService.thawLite( ee );
        this.checkHasAcl( ee );
        this.checkHasAces( ee );

        ExperimentalDesign experimentalDesign = ee.getExperimentalDesign();
        this.checkHasAcl( experimentalDesign );
        this.checkHasAclParent( experimentalDesign, ee );
        this.checkLacksAces( experimentalDesign );

        for ( ExperimentalFactor f : experimentalDesign.getExperimentalFactors() ) {
            this.checkHasAcl( f );
            this.checkHasAclParent( f, ee );
            this.checkLacksAces( f );

            for ( FactorValue fv : f.getFactorValues() ) {
                this.checkHasAcl( fv );
                this.checkHasAclParent( fv, ee );
                this.checkLacksAces( fv );
            }
        }

        // make sure ACLs for the child objects are there
        assertTrue( ee.getBioAssays().size() > 0 );
        for ( BioAssay ba : ee.getBioAssays() ) {
            this.checkHasAcl( ba );
            this.checkHasAclParent( ba, ee );
            this.checkLacksAces( ba );

            BioMaterial bm = ba.getSampleUsed();
            this.checkHasAcl( bm );
            this.checkHasAclParent( bm, ee );
            this.checkLacksAces( bm );

            ArrayDesign arrayDesign = ba.getArrayDesignUsed();
            this.checkHasAcl( arrayDesign );
            assertTrue( this.getParentAcl( arrayDesign ) == null );

        }
    }

    public void checkEESubSetAcls( ExpressionExperimentSubSet eeset ) {
        this.checkEEAcls( eeset.getSourceExperiment() );
        this.checkHasAcl( eeset );
        this.checkLacksAces( eeset );
        this.checkHasAclParent( eeset, eeset.getSourceExperiment() );
    }

    public void checkHasAces( Object f ) {
        Acl a = this.getAcl( f );
        assertTrue( "For object " + f + " with ACL " + a + ":doesn't have ACEs, it should", a.getEntries().size() > 0 );
    }

    public void checkHasAcl( Object f ) {
        if ( null == aclService.readAclById( new AclObjectIdentity( f ) ) )
            fail( "Failed to create ACL for " + f );
    }

    public void checkHasAclParent( Object f, Object parent ) {
        Acl parentAcl = this.getParentAcl( f );
        assertNotNull( "No ACL for parent of " + f + "; the parent is " + parent, parentAcl );

        if ( parent != null ) {
            Acl b = this.getAcl( parent );
            assertEquals( b, parentAcl );
        }

        assertNotNull( parentAcl );

        AclTestUtils.log.debug( "ACL has correct parent for " + f + " <----- " + parentAcl.getObjectIdentity() );
    }

    public void checkLacksAces( Object f ) {
        Acl a = this.getAcl( f );
        assertTrue( f + " has ACEs, it shouldn't: " + a, a.getEntries().size() == 0 );
    }

    public void checkLacksAcl( Object f ) {
        if ( null != aclService.readAclById( new AclObjectIdentity( f ) ) )
            fail( "Should not have found an ACL" );
    }

    public void update( MutableAcl acl ) {
        this.aclService.updateAcl( acl );
    }

    public MutableAcl getAcl( Object f ) {
        Acl a = aclService.readAclById( new AclObjectIdentity( f ) );
        return ( MutableAcl ) a;
    }

    private Acl getParentAcl( Object f ) {
        Acl a = this.getAcl( f );
        return a.getParentAcl();
    }

}
