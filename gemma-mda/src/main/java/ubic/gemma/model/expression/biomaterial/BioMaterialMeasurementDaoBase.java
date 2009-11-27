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
package ubic.gemma.model.expression.biomaterial;

import java.util.Collection;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement</code>.
 * </p>
 * 
 * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement
 */
public abstract class BioMaterialMeasurementDaoBase extends HibernateDaoSupport implements
        ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao {

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#create(int, java.util.Collection)
     */
    public java.util.Collection create( final int transform, final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BioMaterialMeasurement.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                            create( transform,
                                    ( ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement ) entityIterator
                                            .next() );
                        }
                        return null;
                    }
                } );
        return entities;
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#create(int transform,
     *      ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement)
     */
    public Object create( final int transform,
            final ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement bioMaterialMeasurement ) {
        if ( bioMaterialMeasurement == null ) {
            throw new IllegalArgumentException(
                    "BioMaterialMeasurement.create - 'bioMaterialMeasurement' can not be null" );
        }
        this.getHibernateTemplate().save( bioMaterialMeasurement );
        return this.transformEntity( transform, bioMaterialMeasurement );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#create(java.util.Collection)
     */

    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#create(ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement)
     */
    public BioMaterialMeasurement create(
            ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement bioMaterialMeasurement ) {
        return ( ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement ) this.create( TRANSFORM_NONE,
                bioMaterialMeasurement );
    }

    public Collection<? extends BioMaterialMeasurement> load( Collection<Long> ids ) {
        return this.getHibernateTemplate().findByNamedParam( "from BioMaterialMeasurementImpl where id in (:ids)",
                "ids", ids );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#load(int, java.lang.Long)
     */

    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BioMaterialMeasurement.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement ) entity );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#load(java.lang.Long)
     */

    public BioMaterialMeasurement load( java.lang.Long id ) {
        return ( ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#loadAll()
     */

    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#loadAll(int)
     */

    public java.util.Collection loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#remove(java.lang.Long)
     */

    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BioMaterialMeasurement.remove - 'id' can not be null" );
        }
        ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement entity = this.load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#remove(java.util.Collection)
     */

    public void remove( java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BioMaterialMeasurement.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#remove(ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement)
     */
    public void remove( ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement bioMaterialMeasurement ) {
        if ( bioMaterialMeasurement == null ) {
            throw new IllegalArgumentException(
                    "BioMaterialMeasurement.remove - 'bioMaterialMeasurement' can not be null" );
        }
        this.getHibernateTemplate().delete( bioMaterialMeasurement );
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */

    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BioMaterialMeasurement.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                            update( ( ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement ) entityIterator
                                    .next() );
                        }
                        return null;
                    }
                } );
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao#update(ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement)
     */
    public void update( ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement bioMaterialMeasurement ) {
        if ( bioMaterialMeasurement == null ) {
            throw new IllegalArgumentException(
                    "BioMaterialMeasurement.update - 'bioMaterialMeasurement' can not be null" );
        }
        this.getHibernateTemplate().update( bioMaterialMeasurement );
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement)
     */

    protected void transformEntities( final int transform, final java.util.Collection entities ) {
        switch ( transform ) {
            case TRANSFORM_NONE: // fall-through
            default:
                // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If the integer
     * argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.expression.biomaterial.BioMaterialMeasurementDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.expression.biomaterial.BioMaterialMeasurement entity ) {
        Object target = null;
        if ( entity != null ) {
            switch ( transform ) {
                case TRANSFORM_NONE: // fall-through
                default:
                    target = entity;
            }
        }
        return target;
    }

}