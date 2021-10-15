package ubic.gemma.persistence.util;

import com.google.common.base.Strings;
import gemma.gsec.acl.domain.AclObjectIdentity;
import gemma.gsec.model.Securable;
import gemma.gsec.util.SecurityUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Query;
import org.hibernate.QueryParameterException;
import org.springframework.security.acls.domain.BasePermission;

/**
 * Utilities for integrating ACL into {@link Query}.
 */
public class AclQueryUtils {

    public static final String AOI_ALIAS = "aoi", SID_ALIAS = "sid";

    /**
     * Select all the SIDs that belong to a given user (specified by a :userName parameter).
     */
    //language=HQL
    static final String CURRENT_USER_SIDS_SQL =
            "select sid.id from UserGroup as ug join ug.authorities as ga, AclGrantedAuthoritySid sid "
                    + "where sid.grantedAuthority = CONCAT('GROUP_', ga.authority) "
                    + "and ug.name in (select ug.name from UserGroup ug inner join ug.groupMembers memb where memb.userName = :userName)";

    /**
     * Create an HQL select clause for all the alias mentioned in {@link #formAclJoinClause(String)}.
     * <p>
     * Use this if you need to retrieve the ACL OI and SID in the result set.
     */
    public static String formAclSelectClause() {
        return String.join( ", ", AOI_ALIAS, SID_ALIAS );
    }

    /**
     * Create an HQL join clause from ACL OI -{@literal >} ACL entries and ACL OI -{@literal >} ACL SID.
     * <p>
     * Ensure that you use {@link #addAclJoinParameters(Query, Class)} afterward to bind the query parameters.
     *
     * @param alias   placeholder for the identifier e.g. "ee.id"
     * @return clause to add to the query
     */
    public static String formAclJoinClause( String alias ) {
        if ( Strings.isNullOrEmpty( alias ) )
            throw new IllegalArgumentException( "Alias cannot be null or empty." );
        if ( SecurityUtil.isUserAdmin() ) {
            return ", AclObjectIdentity as aoi inner join aoi.ownerSid sid " + "where aoi.identifier = " + alias
                    + ".id and aoi.type = :aoiType";
        } else {
            return ", AclObjectIdentity as aoi inner join aoi.entries ace inner join aoi.ownerSid sid "
                    + "where aoi.identifier = " + alias + ".id and aoi.type = :aoiType";
        }
    }

    /**
     * Bind {@link Query} parameters to a join clause generated with {@link #formAclJoinClause(String)}.
     *
     * @param query   a {@link Query} object that contains the join clause
     * @param aoiType the AOI type to be bound in the query
     */
    public static void addAclJoinParameters( Query query, Class<? extends Securable> aoiType ) {
        query.setParameter( "aoiType", aoiType.getCanonicalName() );
    }

    /**
     * Creates a restriction clause to limit the result only to objects the currently logged user can access.
     * Do not forget to populate the :userName parameter for non-admin logged users before using the string
     * to create a Query object.
     *
     * If you use this, you must also bind its parameters with {@link #addAclJoinParameters(Query, Class)}.
     *
     * @return a string that can be appended to a query string that was created using {@link #formAclJoinClause(String)}.
     */
    public static String formAclRestrictionClause() {
        // add ACL restrictions
        if ( SecurityUtil.isUserAnonymous() ) {
            // For anonymous users, only pick publicly readable data
            //language=HQL
            return " and ace.mask = :readMask and ace.sid.id = 4"; // sid 4 = IS_AUTHENTICATED_ANONYMOUSLY
        } else if ( SecurityUtil.isUserAdmin() ) {
            // For administrators, no filtering is needed, so the ACE is completely skipped from the where clause.
            return "";
        } else {
            // For non-admin users, pick non-troubled, publicly readable data and data that are readable by them or a group they belong to
            //language=HQL
            return " and ("
                    // user own the object
                    + "sid.principal = :userName "
                    // specific rights to the object
                    + "or (ace.sid.id in (" + CURRENT_USER_SIDS_SQL + ") and (ace.mask = :readMask or ace.mask = :writeMask)) "
                    // publicly available
                    + "or (ace.sid.id = 4 and ace.mask = :readMask))";
        }
    }

    /**
     * Add ACL restriction parameters defined in {@link #formAclRestrictionClause()}.
     * @param query
     * @throws QueryParameterException if any defined parameters are missing, which is typically due to a missing {@link #formAclRestrictionClause()}.
     */
    public static void addAclRestrictionParameters( Query query ) throws QueryParameterException {
        if ( SecurityUtil.isUserAnonymous() ) {
            query.setParameter( "readMask", BasePermission.READ.getMask() );
        } else if ( SecurityUtil.isUserAdmin() ) {
            // For administrators, no filtering is needed, so the ACE is completely skipped from the where clause.
        } else {
            query.setParameter( "userName", SecurityUtil.getCurrentUsername() );
            query.setParameter( "readMask", BasePermission.READ.getMask() );
            query.setParameter( "writeMask", BasePermission.WRITE.getMask() );
        }
    }
}
