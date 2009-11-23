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
package ubic.gemma.model.genome;

import java.util.Collection;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.genome.BehavioralQtl</code>.
 * </p>
 * 
 * @see ubic.gemma.model.genome.BehavioralQtl
 */
public abstract class BehavioralQtlDaoBase extends BaseQtlDaoImpl<BehavioralQtl> implements
        ubic.gemma.model.genome.BaseQtlDao<BehavioralQtl> {

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#create(int, java.util.Collection)
     */

    public java.util.Collection create( final int transform, final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                            create( transform, ( ubic.gemma.model.genome.BehavioralQtl ) entityIterator.next() );
                        }
                        return null;
                    }
                } );
        return entities;
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#create(int transform, ubic.gemma.model.genome.BehavioralQtl)
     */
    public Object create( final int transform, final ubic.gemma.model.genome.BehavioralQtl behavioralQtl ) {
        if ( behavioralQtl == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.create - 'behavioralQtl' can not be null" );
        }
        this.getHibernateTemplate().save( behavioralQtl );
        return this.transformEntity( transform, behavioralQtl );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#create(java.util.Collection)
     */

    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#create(ubic.gemma.model.genome.BehavioralQtl)
     */
    public BehavioralQtl create( ubic.gemma.model.genome.BehavioralQtl behavioralQtl ) {
        return ( ubic.gemma.model.genome.BehavioralQtl ) this.create( TRANSFORM_NONE, behavioralQtl );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#findByPhysicalMarkers(int, java.lang.String,
     *      ubic.gemma.model.genome.PhysicalMarker, ubic.gemma.model.genome.PhysicalMarker)
     */

    @Override
    public java.util.Collection findByPhysicalMarkers( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.genome.PhysicalMarker startMarker,
            final ubic.gemma.model.genome.PhysicalMarker endMarker ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( startMarker );
        argNames.add( "startMarker" );
        args.add( endMarker );
        argNames.add( "endMarker" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#findByPhysicalMarkers(int, ubic.gemma.model.genome.PhysicalMarker,
     *      ubic.gemma.model.genome.PhysicalMarker)
     */

    @Override
    public java.util.Collection findByPhysicalMarkers( final int transform,
            final ubic.gemma.model.genome.PhysicalMarker startMarker,
            final ubic.gemma.model.genome.PhysicalMarker endMarker ) {
        return this
                .findByPhysicalMarkers(
                        transform,
                        "from QtlImpl qtl where (qtl.startMaker.physicalLocation.chromosome = :n.physicalLocation.chromosome and qtl.startMaker.physicalLocation.nucleotide > :n.physicalLocation.nucleotide and qtl.endMarker.physicalLocation.nucleotide < (:n.physicalLocation.nucleotide + :n.physicalLocation.nucleotide.nucleotideLength)",
                        startMarker, endMarker );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#findByPhysicalMarkers(java.lang.String,
     *      ubic.gemma.model.genome.PhysicalMarker, ubic.gemma.model.genome.PhysicalMarker)
     */

    @Override
    public java.util.Collection findByPhysicalMarkers( final java.lang.String queryString,
            final ubic.gemma.model.genome.PhysicalMarker startMarker,
            final ubic.gemma.model.genome.PhysicalMarker endMarker ) {
        return this.findByPhysicalMarkers( TRANSFORM_NONE, queryString, startMarker, endMarker );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#findByPhysicalMarkers(ubic.gemma.model.genome.PhysicalMarker,
     *      ubic.gemma.model.genome.PhysicalMarker)
     */

    @Override
    public java.util.Collection findByPhysicalMarkers( ubic.gemma.model.genome.PhysicalMarker startMarker,
            ubic.gemma.model.genome.PhysicalMarker endMarker ) {
        return this.findByPhysicalMarkers( TRANSFORM_NONE, startMarker, endMarker );
    }

    public Collection<? extends BehavioralQtl> load( Collection<Long> ids ) {
        return this.getHibernateTemplate().findByNamedParam( "from BehavioralQtlImpl where id in (:ids)", "ids", ids );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#load(int, java.lang.Long)
     */

    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get( ubic.gemma.model.genome.BehavioralQtlImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.genome.BehavioralQtl ) entity );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#load(java.lang.Long)
     */

    public BehavioralQtl load( java.lang.Long id ) {
        return ( ubic.gemma.model.genome.BehavioralQtl ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#loadAll()
     */

    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#loadAll(int)
     */

    public java.util.Collection loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.genome.BehavioralQtlImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#remove(java.lang.Long)
     */

    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.remove - 'id' can not be null" );
        }
        ubic.gemma.model.genome.BehavioralQtl entity = this.load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#remove(java.util.Collection)
     */

    public void remove( java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#remove(ubic.gemma.model.genome.BehavioralQtl)
     */
    public void remove( ubic.gemma.model.genome.BehavioralQtl behavioralQtl ) {
        if ( behavioralQtl == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.remove - 'behavioralQtl' can not be null" );
        }
        this.getHibernateTemplate().delete( behavioralQtl );
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */

    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                            update( ( ubic.gemma.model.genome.BehavioralQtl ) entityIterator.next() );
                        }
                        return null;
                    }
                } );
    }

    /**
     * @see ubic.gemma.model.genome.BehavioralQtlDao#update(ubic.gemma.model.genome.BehavioralQtl)
     */
    public void update( ubic.gemma.model.genome.BehavioralQtl behavioralQtl ) {
        if ( behavioralQtl == null ) {
            throw new IllegalArgumentException( "BehavioralQtl.update - 'behavioralQtl' can not be null" );
        }
        this.getHibernateTemplate().update( behavioralQtl );
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,ubic.gemma.model.genome.BehavioralQtl)}
     * method. This method does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform one of the constants declared in <code>ubic.gemma.model.genome.BehavioralQtlDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.genome.BehavioralQtl)
     */

    @Override
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
     * <code>ubic.gemma.model.genome.BehavioralQtlDao</code>, please note that the {@link #TRANSFORM_NONE} constant
     * denotes no transformation, so the entity itself will be returned. If the integer argument value is unknown
     * {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in {@link ubic.gemma.model.genome.BehavioralQtlDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform, final ubic.gemma.model.genome.BehavioralQtl entity ) {
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