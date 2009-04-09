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
package ubic.gemma.model.genome.sequenceAnalysis;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation</code>.
 * </p>
 * 
 * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation
 */
public abstract class BlatAssociationDaoBase extends HibernateDaoSupport implements
        ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao {

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#create(int, java.util.Collection)
     */
    public java.util.Collection<BlatAssociation> create( final int transform,
            final java.util.Collection<BlatAssociation> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BlatAssociation.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<BlatAssociation> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            create( transform, entityIterator.next() );
                        }
                        return null;
                    }
                } );
        return entities;
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#create(int transform,
     *      ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)
     */
    public Object create( final int transform,
            final ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation blatAssociation ) {
        if ( blatAssociation == null ) {
            throw new IllegalArgumentException( "BlatAssociation.create - 'blatAssociation' can not be null" );
        }
        this.getHibernateTemplate().save( blatAssociation );
        return this.transformEntity( transform, blatAssociation );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#create(java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#create(ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)
     */
    public ubic.gemma.model.association.Relationship create(
            ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation blatAssociation ) {
        return ( ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation ) this.create( TRANSFORM_NONE,
                blatAssociation );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(int, java.lang.String,
     *      ubic.gemma.model.genome.biosequence.BioSequence)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection find( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.genome.biosequence.BioSequence bioSequence ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( bioSequence );
        argNames.add( "bioSequence" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(int, java.lang.String,
     *      ubic.gemma.model.genome.Gene)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection find( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.genome.Gene gene ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( gene );
        argNames.add( "gene" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(int,
     *      ubic.gemma.model.genome.biosequence.BioSequence)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection find( final int transform,
            final ubic.gemma.model.genome.biosequence.BioSequence bioSequence ) {
        return this
                .find(
                        transform,
                        "from ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation as blatAssociation where blatAssociation.bioSequence = :bioSequence",
                        bioSequence );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(int, ubic.gemma.model.genome.Gene)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection find( final int transform, final ubic.gemma.model.genome.Gene gene ) {
        return this
                .find(
                        transform,
                        "from ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation as blatAssociation where blatAssociation.gene = :gene",
                        gene );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(java.lang.String,
     *      ubic.gemma.model.genome.biosequence.BioSequence)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection find( final java.lang.String queryString,
            final ubic.gemma.model.genome.biosequence.BioSequence bioSequence ) {
        return this.find( TRANSFORM_NONE, queryString, bioSequence );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(java.lang.String,
     *      ubic.gemma.model.genome.Gene)
     */
    public java.util.Collection<BlatAssociation> find( final java.lang.String queryString,
            final ubic.gemma.model.genome.Gene gene ) {
        return this.find( TRANSFORM_NONE, queryString, gene );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(ubic.gemma.model.genome.biosequence.BioSequence)
     */
    public java.util.Collection<BlatAssociation> find( ubic.gemma.model.genome.biosequence.BioSequence bioSequence ) {
        return this.find( TRANSFORM_NONE, bioSequence );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#find(ubic.gemma.model.genome.Gene)
     */
    public java.util.Collection<BlatAssociation> find( ubic.gemma.model.genome.Gene gene ) {
        return this.find( TRANSFORM_NONE, gene );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#load(int, java.lang.Long)
     */
    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BlatAssociation.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation ) entity );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#load(java.lang.Long)
     */
    public ubic.gemma.model.association.Relationship load( java.lang.Long id ) {
        return ( ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#loadAll()
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#loadAll(int)
     */
    public java.util.Collection<BlatAssociation> loadAll( final int transform ) {
        final java.util.Collection<BlatAssociation> results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#remove(java.lang.Long)
     */
    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "BlatAssociation.remove - 'id' can not be null" );
        }
        ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation entity = ( ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation ) this
                .load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.association.RelationshipDao#remove(java.util.Collection)
     */
    public void remove( java.util.Collection<BlatAssociation> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BlatAssociation.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#remove(ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)
     */
    public void remove( ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation blatAssociation ) {
        if ( blatAssociation == null ) {
            throw new IllegalArgumentException( "BlatAssociation.remove - 'blatAssociation' can not be null" );
        }
        this.getHibernateTemplate().delete( blatAssociation );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#thaw(java.util.Collection)
     */
    public void thaw( final java.util.Collection<BlatAssociation> blatAssociations ) {
        try {
            this.handleThaw( blatAssociations );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao.thaw(java.util.Collection blatAssociations)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#thaw(ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)
     */
    public void thaw( final ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation blatAssociation ) {
        try {
            this.handleThaw( blatAssociation );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao.thaw(ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation blatAssociation)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.association.RelationshipDao#update(java.util.Collection)
     */
    public void update( final java.util.Collection<BlatAssociation> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "BlatAssociation.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<BlatAssociation> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            update( entityIterator.next() );
                        }
                        return null;
                    }
                } );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao#update(ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)
     */
    public void update( ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation blatAssociation ) {
        if ( blatAssociation == null ) {
            throw new IllegalArgumentException( "BlatAssociation.update - 'blatAssociation' can not be null" );
        }
        this.getHibernateTemplate().update( blatAssociation );
    }

    /**
     * Performs the core logic for {@link #thaw(java.util.Collection)}
     */
    protected abstract void handleThaw( java.util.Collection<BlatAssociation> blatAssociations )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #thaw(ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)}
     */
    protected abstract void handleThaw( ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation blatAssociation )
            throws java.lang.Exception;

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)} method. This method does
     * not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation)
     */
    protected void transformEntities( final int transform, final java.util.Collection<BlatAssociation> entities ) {
        switch ( transform ) {
            case TRANSFORM_NONE: // fall-through
            default:
                // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If the integer
     * argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.genome.sequenceAnalysis.BlatAssociationDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation entity ) {
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