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
package ubic.gemma.persistence.util;

import gemma.gsec.AuthorityConstants;
import gemma.gsec.acl.domain.AclEntry;
import gemma.gsec.acl.domain.AclGrantedAuthoritySid;
import gemma.gsec.acl.domain.AclObjectIdentity;
import gemma.gsec.acl.domain.AclPrincipalSid;
import gemma.gsec.model.Securable;
import gemma.gsec.util.SecurityUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Sid;
import ubic.gemma.model.common.Identifiable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author paul
 */
public class EntityUtils {

    /**
     * Checks if the given map already contains a Set for the given key, and if it does, adds the given has code to it.
     * If a key does not exist in the map, it creates a new set, and adds the has into it.
     *
     * @param map  the map to be checked
     * @param key  the key
     * @param hash the hash to be added to the set at the given key of the map
     * @param <T>  the key type parameter of the map
     * @param <S>  the type parameter of the set
     */
    public static <T, S> void populateMapSet( Map<T, Set<S>> map, T key, S hash ) {
        if ( map.containsKey( key ) ) {
            map.get( key ).add( hash );
        } else {
            Set<S> set = new HashSet<>();
            if ( hash != null ) {
                set.add( hash );
            }
            map.put( key, set );
        }
    }

    /**
     * @param entities entities
     * @return returns a collection of IDs. Avoids using reflection by requiring that the given entities all
     * implement the Identifiable interface.
     */
    public static <T extends Identifiable> List<Long> getIds( Collection<T> entities ) {
        return entities.stream().map( Identifiable::getId ).collect( Collectors.toList() );
    }

    /**
     * Given a set of entities, create a map of their ids to the entities.
     *
     * Note: If more than one entity share the same ID, there is no guarantee on which will be kept in the final
     * mapping.
     *
     * @param entities where id is called "id"
     * @param <T>      the type
     * @return the created map
     */
    public static <T extends Identifiable> Map<Long, T> getIdMap( Collection<T> entities ) {
        Map<Long, T> result = new HashMap<>();
        for ( T entity : entities ) {
            result.put( entity.getId(), entity );
        }
        return result;
    }

    /**
     * Get a property of an entity object via its getter.
     *
     * @param entity       the entity
     * @param propertyName name of the property
     * @param <T>          type of the property
     * @return the return value of the getter for the property
     * @see PropertyUtils#getProperty(Object, String)
     */
    public static <T> T getProperty( Object entity, String propertyName ) {
        try {
            //noinspection unchecked
            return ( T ) PropertyUtils.getProperty( entity, propertyName );
        } catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Group entities by their property.
     *
     * @param entities     entities
     * @param propertyName property name (e.g. "id")
     * @param <T>        the type
     * @return the created map
     * @see #getProperty(Object, String)
     */
    public static <S, T> Map<S, T> getPropertyMap( Collection<? extends T> entities, String propertyName ) {
        Map<S, T> result = new HashMap<>();
        for ( T object : entities ) {
            result.put( EntityUtils.getProperty( object, propertyName ), object );
        }
        return result;
    }

    /**
     * Group entities by their property using a nested property.
     *
     * @param entities       entities
     * @param nestedProperty nested property
     * @param propertyName   property name (e.g. "id")
     * @param <T>            the type
     * @return the created map
     * @see #getProperty(Object, String)
     */
    public static <S, T> Map<S, T> getNestedPropertyMap( Collection<T> entities, String nestedProperty,
            String propertyName ) {
        Map<S, T> result = new HashMap<>();
        for ( T object : entities ) {
            result.put( EntityUtils.getProperty( EntityUtils.getProperty( object, nestedProperty ), propertyName ), object );
        }
        return result;
    }

    public static Class<?> getImplClass( Class<?> type ) {
        String canonicalName = type.getName();
        try {
            return Class.forName( canonicalName.endsWith( "Impl" ) ? type.getName() : type.getName() + "Impl" );
        } catch ( ClassNotFoundException e ) {
            // OLDCODE throw new RuntimeException( "No 'impl' class for " + type.getCanonicalName() + " found" );
            // not all our mapped classes end with Impl any more.
            return type;
        }
    }

    /**
     * Expert only. Must be called within a session? Not sure why this is necessary. Obtain the implementation for a
     * proxy. If target is not an instanceof HibernateProxy, target is returned.
     *
     * @param target The object to be un-proxied.
     * @return the underlying implementation.
     */
    public static Object getImplementationForProxy( Object target ) {
        if ( EntityUtils.isProxy( target ) ) {
            HibernateProxy proxy = ( HibernateProxy ) target;
            return proxy.getHibernateLazyInitializer().getImplementation();
        }
        return target;
    }

    /**
     * @param target target
     * @return true if the target is a hibernate proxy.
     */
    public static boolean isProxy( Object target ) {
        return target instanceof HibernateProxy;
    }

    /**
     * Expert use only. Used to expose some ACL information to the DAO layer (normally this happens in an interceptor).
     *
     * @param sess             session
     * @param securedClass     Securable type
     * @param ids              to be filtered
     * @param showPublic       also show public items (won't work if showOnlyEditable is true)
     * @param showOnlyEditable show only editable
     * @return filtered IDs, at the very least limited to those that are readable by the current user
     */
    public static Collection<Long> securityFilterIds( Class<? extends Securable> securedClass, Collection<Long> ids,
            boolean showOnlyEditable, boolean showPublic, Session sess ) {

        if ( ids.isEmpty() )
            return ids;
        if ( SecurityUtil.isUserAdmin() ) {
            return ids;
        }

        /*
         * Find groups user is a member of
         */

        String userName = SecurityUtil.getCurrentUsername();

        boolean isAnonymous = SecurityUtil.isUserAnonymous();

        if ( isAnonymous && ( showOnlyEditable || !showPublic ) ) {
            return new HashSet<>();
        }

        String queryString = "select aoi.OBJECT_ID";
        queryString += " from ACLOBJECTIDENTITY aoi";
        queryString += " join ACLENTRY ace on ace.OBJECTIDENTITY_FK = aoi.ID ";
        queryString += " join ACLSID sid on sid.ID = aoi.OWNER_SID_FK ";
        queryString += " where aoi.OBJECT_ID in (:ids)";
        queryString += " and aoi.OBJECT_CLASS = :clazz and ";
        queryString += EntityUtils.addGroupAndUserNameRestriction( showOnlyEditable, showPublic );

        // will be empty if anonymous
        //noinspection unchecked
        Collection<String> groups = sess.createQuery(
                        "select ug.name from UserGroup ug inner join ug.groupMembers memb where memb.userName = :user" )
                .setParameter( "user", userName ).list();

        Query query = sess.createSQLQuery( queryString ).setParameter( "clazz", securedClass.getName() )
                .setParameterList( "ids", ids );

        if ( queryString.contains( ":groups" ) ) {
            query.setParameterList( "groups", groups );
        }

        if ( queryString.contains( ":userName" ) ) {
            query.setParameter( "userName", userName );
        }

        //noinspection unchecked
        List<BigInteger> r = query.list();
        Set<Long> rl = new HashSet<>();
        for ( BigInteger bi : r ) {
            rl.add( bi.longValue() );
        }

        if ( !ids.containsAll( rl ) ) {
            // really an assertion, but being extra-careful
            throw new SecurityException( "Security filter failure" );
        }

        return rl;
    }

    /**
     * Have to add 'and' to start of this if it's a later clause
     * Author: nicolas with fixes to generalize by paul, same code appears in the PhenotypeAssociationDaoImpl
     *
     * @param showOnlyEditable only show those the user has access to edit
     * @param showPublic       also show public items (wont work if showOnlyEditable is true)
     * @return clause
     */
    public static String addGroupAndUserNameRestriction( boolean showOnlyEditable, boolean showPublic ) {

        String sqlQuery = "";

        if ( !SecurityUtil.isUserAnonymous() ) {

            if ( showPublic && !showOnlyEditable ) {
                sqlQuery += "  ((sid.PRINCIPAL = :userName";
            } else {
                sqlQuery += "  (sid.PRINCIPAL = :userName";
            }

            // if ( !groups.isEmpty() ) { // is it possible to be empty? If they are non-anonymous it will not be, there
            // // will at least be GROUP_USER? Or that doesn't count?
            sqlQuery += " or (ace.SID_FK in (";
            // SUBSELECT
            sqlQuery += " select sid.ID from USER_GROUP ug ";
            sqlQuery += " join GROUP_AUTHORITY ga on ug.ID = ga.GROUP_FK ";
            sqlQuery += " join ACLSID sid on sid.GRANTED_AUTHORITY=CONCAT('GROUP_', ga.AUTHORITY) ";
            sqlQuery += " where ug.name in (:groups) ";
            if ( showOnlyEditable ) {
                sqlQuery += ") and ace.MASK = 2) "; // 2 = read-writable
            } else {
                sqlQuery += ") and (ace.MASK = 1 or ace.MASK = 2)) "; // 1 = read only
            }
            // }
            sqlQuery += ") ";

            if ( showPublic && !showOnlyEditable ) {
                // publicly readable data.
                sqlQuery += "or (ace.SID_FK = 4 and ace.MASK = 1)) "; // 4 =IS_AUTHENTICATED_ANONYMOUSLY
            }

        } else if ( showPublic && !showOnlyEditable ) {
            // publicly readable data
            sqlQuery += " (ace.SID_FK = 4 and ace.MASK = 1) "; // 4 = IS_AUTHENTICATED_ANONYMOUSLY
        }

        return sqlQuery;
    }

    /**
     * Populates parameters in query created using addGroupAndUserNameRestriction(boolean, boolean).
     *
     * @param queryObject    the query object created using the sql query with group and username restrictions.
     * @param sessionFactory session factory from the DAO that is using this method.
     */
    public static void addUserAndGroupParameters( SQLQuery queryObject, SessionFactory sessionFactory ) {
        if ( SecurityUtil.isUserAnonymous() ) {
            return;
        }
        String sqlQuery = queryObject.getQueryString();
        String userName = SecurityUtil.getCurrentUsername();

        // if user is member of any groups.
        if ( sqlQuery.contains( ":groups" ) ) {
            //noinspection unchecked
            Collection<String> groups = sessionFactory.getCurrentSession().createQuery(
                            "select ug.name from UserGroup ug inner join ug.groupMembers memb where memb.userName = :user" )
                    .setParameter( "user", userName ).list();
            queryObject.setParameterList( "groups", groups );
        }

        if ( sqlQuery.contains( ":userName" ) ) {
            queryObject.setParameter( "userName", userName );
        }

    }

    /**
     * Checks ACL related properties from the AclObjectIdentity.
     * Some of the code is adapted from {@link gemma.gsec.util.SecurityUtil}, but allows usage without an Acl object.
     *
     * @param aoi the acl object identity of an object whose permissions are to be checked.
     * @return an array of booleans that represent permissions of currently logged in user as follows:
     * <ol>
     * <li>is object public</li>
     * <li>can user write to object</li>
     * <li>is object shared</li>
     * </ol>
     * (note that actual indexing in the array starts at 0).
     */
    public static boolean[] getPermissions( AclObjectIdentity aoi ) {
        boolean isPublic = false;
        boolean canWrite = false;
        boolean isShared = false;

        for ( AclEntry ace : aoi.getEntries() ) {
            if ( SecurityUtil.isUserAdmin() ) {
                canWrite = true;
            } else if ( SecurityUtil.isUserAnonymous() ) {
                canWrite = false;
            } else {
                if ( ace.getMask() == BasePermission.WRITE.getMask() || ace.getMask() == BasePermission.ADMINISTRATION
                        .getMask() ) {
                    Sid sid = ace.getSid();
                    if ( sid instanceof AclGrantedAuthoritySid ) {
                        //noinspection unused //FIXME if user is in granted group then he can write probably
                        String grantedAuthority = ( ( AclGrantedAuthoritySid ) sid ).getGrantedAuthority();
                    } else if ( sid instanceof AclPrincipalSid ) {
                        if ( ( ( AclPrincipalSid ) sid ).getPrincipal().equals( SecurityUtil.getCurrentUsername() ) ) {
                            canWrite = true;
                        }
                    }
                }
            }

            // Check public and shared - code adapted from SecurityUtils, only we do not hold an ACL object.
            if ( ace.getPermission().equals( BasePermission.READ ) ) {
                Sid sid = ace.getSid();
                if ( sid instanceof AclGrantedAuthoritySid ) {
                    String grantedAuthority = ( ( AclGrantedAuthoritySid ) sid ).getGrantedAuthority();

                    if ( grantedAuthority.equals( AuthorityConstants.IS_AUTHENTICATED_ANONYMOUSLY ) && ace
                            .isGranting() ) {
                        isPublic = true;
                    }
                    if ( grantedAuthority.startsWith( "GROUP_" ) && ace.isGranting() ) {
                        if ( !grantedAuthority.equals( AuthorityConstants.AGENT_GROUP_AUTHORITY ) && !grantedAuthority
                                .equals( AuthorityConstants.ADMIN_GROUP_AUTHORITY ) ) {
                            isShared = true;
                        }
                    }
                }
            }
        }

        return new boolean[] { isPublic, canWrite, isShared };
    }

    public static void createFile( File file ) throws IOException {
        if ( !file.createNewFile() ) {
            Exception e = new RuntimeException( "Could not create file " + file.getPath() );
            e.printStackTrace();
        }
    }

    public static void deleteFile( File file ) {
        if ( !file.delete() ) {
            Exception e = new RuntimeException( "Could not delete file " + file.getPath() );
            e.printStackTrace();
        }
    }

    public static void renameFile( File file, File newFile ) {
        if ( !file.renameTo( newFile ) ) {
            Exception e = new RuntimeException( "Could not rename file " + file.getPath() );
            e.printStackTrace();
        }
    }

    /**
     * Checks if property of given name exists in the given class. If the given string specifies
     * nested properties (E.g. curationDetails.troubled), only the substring before the first dot is evaluated and the
     * rest of the string is processed in a new recursive iteration.
     *
     * @param property the property to check for. If the string contains dot characters ('.'), only the part
     *                 before the first dot will be evaluated. Substring after the dot will be checked against the
     *                 type of the field retrieved from the substring before the dot.
     * @param cls      the class to check the property on.
     * @return the class of the property last in the line of nesting.
     */
    public static Class getDeclaredFieldType( String property, Class cls ) throws NoSuchFieldException {
        String[] parts = property.split( "\\.", 2 );
        Field field = getDeclaredField( cls, parts[0] );
        Class<?> subCls = field.getType();

        if ( Collection.class.isAssignableFrom( subCls ) ) {
            ParameterizedType pt = ( ParameterizedType ) field.getGenericType();
            for ( Type type : pt.getActualTypeArguments() ) {
                if ( type instanceof Class ) {
                    subCls = ( Class<?> ) type;
                    break;
                }
            }
        }

        if ( parts.length > 1 ) {
            return getDeclaredFieldType( parts[1], subCls );
        } else {
            return subCls;
        }
    }

    /**
     * Recursive version of {@link Class#getDeclaredField(String)} that also checks in the superclass hierarchy.
     * @see Class#getDeclaredField(String)
     */
    public static Field getDeclaredField( Class<?> cls, String field ) throws NoSuchFieldException {
        try {
            return cls.getDeclaredField( field );
        } catch ( NoSuchFieldException e ) {
            if ( cls.getSuperclass() != null ) {
                return getDeclaredField( cls.getSuperclass(), field );
            } else {
                throw e;
            }
        }
    }
}

