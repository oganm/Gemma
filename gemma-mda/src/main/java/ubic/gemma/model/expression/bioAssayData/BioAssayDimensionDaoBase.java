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
package ubic.gemma.model.expression.bioAssayData;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.expression.bioAssayData.BioAssayDimension</code>.
 * </p>
 * 
 * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimension
 */
public abstract class BioAssayDimensionDaoBase extends HibernateDaoSupport implements
        ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao {

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#create(int, java.util.Collection)
     */
    public java.util.Collection<? extends BioAssayDimension> create( final int transform,
            final java.util.Collection<? extends BioAssayDimension> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<? extends BioAssayDimension> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            create( transform, entityIterator.next() );
                        }
                        return null;
                    }
                } );
        return entities;
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#create(int transform,
     *      ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public BioAssayDimension create( final int transform,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        if ( bioAssayDimension == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.create - 'bioAssayDimension' can not be null" );
        }
        this.getHibernateTemplate().save( bioAssayDimension );
        return ( BioAssayDimension ) this.transformEntity( transform, bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#create(java.util.Collection)
     */
    public java.util.Collection<? extends BioAssayDimension> create(
            final java.util.Collection<? extends BioAssayDimension> entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#create(ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public BioAssayDimension create( ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        return this.create( TRANSFORM_NONE, bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#find(int, java.lang.String,
     *      ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    
    public BioAssayDimension find( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( bioAssayDimension );
        argNames.add( "bioAssayDimension" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.bioAssayData.BioAssayDimension"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.bioAssayData.BioAssayDimension ) result );
        return ( BioAssayDimension ) result;
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#find(int,
     *      ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public BioAssayDimension find( final int transform,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        return this
                .find(
                        transform,
                        "from ubic.gemma.model.expression.bioAssayData.BioAssayDimension as bioAssayDimension where bioAssayDimension.bioAssayDimension = :bioAssayDimension",
                        bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#find(java.lang.String,
     *      ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public ubic.gemma.model.expression.bioAssayData.BioAssayDimension find( final java.lang.String queryString,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        return this.find( TRANSFORM_NONE, queryString, bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#find(ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public ubic.gemma.model.expression.bioAssayData.BioAssayDimension find(
            ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        return this.find( TRANSFORM_NONE, bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#findOrCreate(int, java.lang.String,
     *      ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    
    public BioAssayDimension findOrCreate( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( bioAssayDimension );
        argNames.add( "bioAssayDimension" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.bioAssayData.BioAssayDimension"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.bioAssayData.BioAssayDimension ) result );
        return ( BioAssayDimension ) result;
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#findOrCreate(int,
     *      ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public BioAssayDimension findOrCreate( final int transform,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        return this
                .findOrCreate(
                        transform,
                        "from ubic.gemma.model.expression.bioAssayData.BioAssayDimension as bioAssayDimension where bioAssayDimension.bioAssayDimension = :bioAssayDimension",
                        bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#findOrCreate(java.lang.String,
     *      ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public ubic.gemma.model.expression.bioAssayData.BioAssayDimension findOrCreate( final java.lang.String queryString,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        return this.findOrCreate( TRANSFORM_NONE, queryString, bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#findOrCreate(ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public ubic.gemma.model.expression.bioAssayData.BioAssayDimension findOrCreate(
            ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        return this.findOrCreate( TRANSFORM_NONE, bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#load(int, java.lang.Long)
     */

    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.expression.bioAssayData.BioAssayDimensionImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.expression.bioAssayData.BioAssayDimension ) entity );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#load(java.lang.Long)
     */

    public BioAssayDimension load( java.lang.Long id ) {
        return ( ubic.gemma.model.expression.bioAssayData.BioAssayDimension ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#loadAll()
     */
    public java.util.Collection<? extends BioAssayDimension> loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#loadAll(int)
     */

    
    public java.util.Collection<? extends BioAssayDimension> loadAll( final int transform ) {
        final java.util.Collection<? extends BioAssayDimension> results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.expression.bioAssayData.BioAssayDimensionImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#remove(java.lang.Long)
     */

    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.remove - 'id' can not be null" );
        }
        ubic.gemma.model.expression.bioAssayData.BioAssayDimension entity = this.load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#remove(java.util.Collection)
     */

    public void remove( java.util.Collection<? extends BioAssayDimension> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#remove(ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public void remove( ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        if ( bioAssayDimension == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.remove - 'bioAssayDimension' can not be null" );
        }
        this.getHibernateTemplate().delete( bioAssayDimension );
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */

    public void update( final java.util.Collection<? extends BioAssayDimension> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<? extends BioAssayDimension> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            update( entityIterator.next() );
                        }
                        return null;
                    }
                } );
    }

    /**
     * @see ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao#update(ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */
    public void update( ubic.gemma.model.expression.bioAssayData.BioAssayDimension bioAssayDimension ) {
        if ( bioAssayDimension == null ) {
            throw new IllegalArgumentException( "BioAssayDimension.update - 'bioAssayDimension' can not be null" );
        }
        this.getHibernateTemplate().update( bioAssayDimension );
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,ubic.gemma.model.expression.bioAssayData.BioAssayDimension)} method. This method does
     * not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.expression.bioAssayData.BioAssayDimension)
     */

    protected void transformEntities( final int transform,
            final java.util.Collection<? extends BioAssayDimension> entities ) {
        switch ( transform ) {
            case TRANSFORM_NONE: // fall-through
            default:
                // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If the integer
     * argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.expression.bioAssayData.BioAssayDimensionDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.expression.bioAssayData.BioAssayDimension entity ) {
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