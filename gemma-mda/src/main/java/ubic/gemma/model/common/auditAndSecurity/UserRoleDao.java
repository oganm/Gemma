/*
 * The Gemma project.
 * 
 * Copyright (c) 2006-2007 University of British Columbia
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
//
// Attention: Generated code! Do not modify by hand!
// Generated by: SpringDao.vsl in andromda-spring-cartridge.
//
package ubic.gemma.model.common.auditAndSecurity;

/**
 * @see ubic.gemma.model.common.auditAndSecurity.UserRole
 */
public interface UserRoleDao extends ubic.gemma.model.common.DescribableDao {
    /**
     * Loads an instance of ubic.gemma.model.common.auditAndSecurity.UserRole from the persistent store.
     */
    public ubic.gemma.model.common.Securable load( java.lang.Long id );

    /**
     * <p>
     * Does the same thing as {@link #load(java.lang.Long)} with an additional flag called <code>transform</code>. If
     * this flag is set to <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined in this class then the result <strong>WILL BE</strong>
     * passed through an operation which can optionally transform the entity (into a value object for example). By
     * default, transformation does not occur.
     * </p>
     * 
     * @param id the identifier of the entity to load.
     * @return either the entity or the object transformed from the entity.
     */
    public Object load( int transform, java.lang.Long id );

    /**
     * Loads all entities of type {@link ubic.gemma.model.common.auditAndSecurity.UserRole}.
     * 
     * @return the loaded entities.
     */
    public java.util.Collection loadAll();

    /**
     * <p>
     * Does the same thing as {@link #loadAll()} with an additional flag called <code>transform</code>. If this flag
     * is set to <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be transformed. If
     * this flag is any of the other constants defined here then the result <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     * 
     * @param transform the flag indicating what transformation to use.
     * @return the loaded entities.
     */
    public java.util.Collection loadAll( final int transform );

    /**
     * Creates an instance of ubic.gemma.model.common.auditAndSecurity.UserRole and adds it to the persistent store.
     */
    public ubic.gemma.model.common.Securable create( ubic.gemma.model.common.auditAndSecurity.UserRole userRole );

    /**
     * <p>
     * Does the same thing as {@link #create(ubic.gemma.model.common.auditAndSecurity.UserRole)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned entity
     * will <strong>NOT</strong> be transformed. If this flag is any of the other constants defined here then the
     * result <strong>WILL BE</strong> passed through an operation which can optionally transform the entity (into a
     * value object for example). By default, transformation does not occur.
     * </p>
     */
    public Object create( int transform, ubic.gemma.model.common.auditAndSecurity.UserRole userRole );

    /**
     * Creates a new instance of ubic.gemma.model.common.auditAndSecurity.UserRole and adds from the passed in
     * <code>entities</code> collection
     * 
     * @param entities the collection of ubic.gemma.model.common.auditAndSecurity.UserRole instances to create.
     * @return the created instances.
     */
    public java.util.Collection create( java.util.Collection entities );

    /**
     * <p>
     * Does the same thing as {@link #create(ubic.gemma.model.common.auditAndSecurity.UserRole)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned entity
     * will <strong>NOT</strong> be transformed. If this flag is any of the other constants defined here then the
     * result <strong>WILL BE</strong> passed through an operation which can optionally transform the entities (into
     * value objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection create( int transform, java.util.Collection entities );

    /**
     * Updates the <code>userRole</code> instance in the persistent store.
     */
    public void update( ubic.gemma.model.common.auditAndSecurity.UserRole userRole );

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update( java.util.Collection entities );

    /**
     * Removes the instance of ubic.gemma.model.common.auditAndSecurity.UserRole from the persistent store.
     */
    public void remove( ubic.gemma.model.common.auditAndSecurity.UserRole userRole );

    /**
     * Removes the instance of ubic.gemma.model.common.auditAndSecurity.UserRole having the given
     * <code>identifier</code> from the persistent store.
     */
    public void remove( java.lang.Long id );

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove( java.util.Collection entities );

    /**
     * <p>
     * Return all roles
     * </p>
     */
    public java.util.Collection findRolesByUserName( java.lang.String userName );

    /**
     * <p>
     * Does the same thing as {@link #findRolesByUserName(java.lang.String)} with an additional argument called
     * <code>queryString</code>. This <code>queryString</code> argument allows you to override the query string
     * defined in {@link #findRolesByUserName(java.lang.String)}.
     * </p>
     */
    public java.util.Collection findRolesByUserName( String queryString, java.lang.String userName );

    /**
     * <p>
     * Does the same thing as {@link #findRolesByUserName(java.lang.String)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder results will
     * <strong>NOT</strong> be transformed during retrieval. If this flag is any of the other constants defined here
     * then finder results <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection findRolesByUserName( int transform, java.lang.String userName );

    /**
     * <p>
     * Does the same thing as {@link #findRolesByUserName(boolean, java.lang.String)} with an additional argument called
     * <code>queryString</code>. This <code>queryString</code> argument allows you to override the query string
     * defined in {@link #findRolesByUserName(int, java.lang.String userName)}.
     * </p>
     */
    public java.util.Collection findRolesByUserName( int transform, String queryString, java.lang.String userName );

    /**
     * 
     */
    public java.util.Collection findRolesByRoleName( java.lang.String name );

    /**
     * <p>
     * Does the same thing as {@link #findRolesByRoleName(java.lang.String)} with an additional argument called
     * <code>queryString</code>. This <code>queryString</code> argument allows you to override the query string
     * defined in {@link #findRolesByRoleName(java.lang.String)}.
     * </p>
     */
    public java.util.Collection findRolesByRoleName( String queryString, java.lang.String name );

    /**
     * <p>
     * Does the same thing as {@link #findRolesByRoleName(java.lang.String)} with an additional flag called
     * <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder results will
     * <strong>NOT</strong> be transformed during retrieval. If this flag is any of the other constants defined here
     * then finder results <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection findRolesByRoleName( int transform, java.lang.String name );

    /**
     * <p>
     * Does the same thing as {@link #findRolesByRoleName(boolean, java.lang.String)} with an additional argument called
     * <code>queryString</code>. This <code>queryString</code> argument allows you to override the query string
     * defined in {@link #findRolesByRoleName(int, java.lang.String name)}.
     * </p>
     */
    public java.util.Collection findRolesByRoleName( int transform, String queryString, java.lang.String name );

}
