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
package ubic.gemma.model.common.auditAndSecurity;


/**
 * @see ubic.gemma.model.common.auditAndSecurity.JobInfo
 */
public interface JobInfoDao {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into value objects or
     * other types, different methods in a class implementing this interface support this feature: look for an
     * <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes no transformation will occur.
     */
    public final static int TRANSFORM_NONE = 0;

    /**
     * <p>
     * Does the same thing as {@link #create(ubic.gemma.model.common.auditAndSecurity.JobInfo)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned entity will
     * <strong>NOT</strong> be transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the entities (into value
     * objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection<JobInfo> create( int transform, java.util.Collection<JobInfo> entities );

    /**
     * <p>
     * Does the same thing as {@link #create(ubic.gemma.model.common.auditAndSecurity.JobInfo)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then the returned entity will
     * <strong>NOT</strong> be transformed. If this flag is any of the other constants defined here then the result
     * <strong>WILL BE</strong> passed through an operation which can optionally transform the entity (into a value
     * object for example). By default, transformation does not occur.
     * </p>
     */
    public JobInfo create( int transform, ubic.gemma.model.common.auditAndSecurity.JobInfo jobInfo );

    /**
     * Creates a new instance of ubic.gemma.model.common.auditAndSecurity.JobInfo and adds from the passed in
     * <code>entities</code> collection
     * 
     * @param entities the collection of ubic.gemma.model.common.auditAndSecurity.JobInfo instances to create.
     * @return the created instances.
     */
    public java.util.Collection<JobInfo> create( java.util.Collection<JobInfo> entities );

    /**
     * Creates an instance of ubic.gemma.model.common.auditAndSecurity.JobInfo and adds it to the persistent store.
     */
    public ubic.gemma.model.common.auditAndSecurity.JobInfo create(
            ubic.gemma.model.common.auditAndSecurity.JobInfo jobInfo );

    /**
     * <p>
     * Does the same thing as {@link #load(java.lang.Long)} with an additional flag called <code>transform</code>. If
     * this flag is set to <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined in this class then the result <strong>WILL
     * BE</strong> passed through an operation which can optionally transform the entity (into a value object for
     * example). By default, transformation does not occur.
     * </p>
     * 
     * @param id the identifier of the entity to load.
     * @return either the entity or the object transformed from the entity.
     */
    public JobInfo load( int transform, java.lang.Long id );

    /**
     * Loads an instance of ubic.gemma.model.common.auditAndSecurity.JobInfo from the persistent store.
     */
    public ubic.gemma.model.common.auditAndSecurity.JobInfo load( java.lang.Long id );

    /**
     * Loads all entities of type {@link ubic.gemma.model.common.auditAndSecurity.JobInfo}.
     * 
     * @return the loaded entities.
     */
    public java.util.Collection<JobInfo> loadAll();

    /**
     * <p>
     * Does the same thing as {@link #loadAll()} with an additional flag called <code>transform</code>. If this flag is
     * set to <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be transformed. If this
     * flag is any of the other constants defined here then the result <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     * 
     * @param transform the flag indicating what transformation to use.
     * @return the loaded entities.
     */
    public java.util.Collection<JobInfo> loadAll( final int transform );

    /**
     * Removes the instance of ubic.gemma.model.common.auditAndSecurity.JobInfo having the given <code>identifier</code>
     * from the persistent store.
     */
    public void remove( java.lang.Long id );

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove( java.util.Collection<JobInfo> entities );

    /**
     * Removes the instance of ubic.gemma.model.common.auditAndSecurity.JobInfo from the persistent store.
     */
    public void remove( ubic.gemma.model.common.auditAndSecurity.JobInfo jobInfo );

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update( java.util.Collection<JobInfo> entities );

    /**
     * Updates the <code>jobInfo</code> instance in the persistent store.
     */
    public void update( ubic.gemma.model.common.auditAndSecurity.JobInfo jobInfo );

}
