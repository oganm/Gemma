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
// Generated by: SpringHibernateDaoBase.vsl in andromda-spring-cartridge.
//
package ubic.gemma.model.association.coexpression;

import ubic.gemma.model.genome.Gene;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.association.coexpression.Gene2GeneCoexpression</code>.
 * </p>
 * 
 * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpression
 */
public abstract class Gene2GeneCoexpressionDaoBase extends ubic.gemma.model.association.Gene2GeneAssociationDaoImpl
        implements ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao {

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#load(int, java.lang.Long)
     */
    @Override
    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "Gene2GeneCoexpression.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.association.coexpression.Gene2GeneCoexpression ) entity );
    }

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#load(java.lang.Long)
     */
    @Override
    public Gene2GeneCoexpression load( java.lang.Long id ) {
        return ( ubic.gemma.model.association.coexpression.Gene2GeneCoexpression ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#loadAll()
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection<Gene2GeneCoexpression> loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#loadAll(int)
     */
    @Override
    public java.util.Collection<Gene2GeneCoexpression> loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#update(ubic.gemma.model.association.coexpression.Gene2GeneCoexpression)
     */
    public void update( ubic.gemma.model.association.coexpression.Gene2GeneCoexpression gene2GeneCoexpression ) {
        if ( gene2GeneCoexpression == null ) {
            throw new IllegalArgumentException(
                    "Gene2GeneCoexpression.update - 'gene2GeneCoexpression' can not be null" );
        }
        this.getHibernateTemplate().update( gene2GeneCoexpression );
    }

    /**
     * @see ubic.gemma.model.association.RelationshipDao#update(java.util.Collection)
     */
    @Override
    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "Gene2GeneCoexpression.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    update( ( ubic.gemma.model.association.coexpression.Gene2GeneCoexpression ) entityIterator.next() );
                }
                return null;
            }
        }, true );
    }

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#remove(ubic.gemma.model.association.coexpression.Gene2GeneCoexpression)
     */
    public void remove( ubic.gemma.model.association.coexpression.Gene2GeneCoexpression gene2GeneCoexpression ) {
        if ( gene2GeneCoexpression == null ) {
            throw new IllegalArgumentException(
                    "Gene2GeneCoexpression.remove - 'gene2GeneCoexpression' can not be null" );
        }
        this.getHibernateTemplate().delete( gene2GeneCoexpression );
    }

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#remove(java.lang.Long)
     */
    @Override
    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "Gene2GeneCoexpression.remove - 'id' can not be null" );
        }
        ubic.gemma.model.association.coexpression.Gene2GeneCoexpression entity = this.load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.association.RelationshipDao#remove(java.util.Collection)
     */
    @Override
    public void remove( java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "Gene2GeneCoexpression.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#findCoexpressionRelationships(ubic.gemma.model.genome.Gene,
     *      int, int)
     */
    public java.util.Collection<Gene2GeneCoexpression> findCoexpressionRelationships(
            final ubic.gemma.model.genome.Gene gene, final int stringency, final int maxResults ) {
        try {
            return this.handleFindCoexpressionRelationships( gene, stringency, maxResults );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao.findCoexpressionRelationships(ubic.gemma.model.genome.Gene gene, int stringency, int maxResults)' --> "
                            + th, th );
        }
    }

    /**
     * Performs the core logic for {@link #findCoexpressionRelationships(ubic.gemma.model.genome.Gene, int, int)}
     */
    protected abstract java.util.Collection<Gene2GeneCoexpression> handleFindCoexpressionRelationships(
            ubic.gemma.model.genome.Gene gene, int stringency, int maxResults ) throws java.lang.Exception;

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#findCoexpressionRelationships(java.util.Collection,
     *      int, int)
     */
    public java.util.Map findCoexpressionRelationships( final java.util.Collection<Gene> genes, final int stringency,
            final int maxResults ) {
        try {
            return this.handleFindCoexpressionRelationships( genes, stringency, maxResults );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao.findCoexpressionRelationships(java.util.Collection genes, int stringency, int maxResults)' --> "
                            + th, th );
        }
    }

    /**
     * Performs the core logic for {@link #findCoexpressionRelationships(java.util.Collection, int, int)}
     */
    protected abstract java.util.Map handleFindCoexpressionRelationships( java.util.Collection<Gene> genes,
            int stringency, int maxResults ) throws java.lang.Exception;

    /**
     * @see ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao#findInterCoexpressionRelationships(java.util.Collection,
     *      int)
     */
    public java.util.Map findInterCoexpressionRelationships( final java.util.Collection<Gene> genes,
            final int stringency ) {
        try {
            return this.handleFindInterCoexpressionRelationships( genes, stringency );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao.findInterCoexpressionRelationships(java.util.Collection genes, int stringency)' --> "
                            + th, th );
        }
    }

    /**
     * Performs the core logic for {@link #findInterCoexpressionRelationships(java.util.Collection, int)}
     */
    protected abstract java.util.Map handleFindInterCoexpressionRelationships( java.util.Collection<Gene> genes,
            int stringency ) throws java.lang.Exception;

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If the integer
     * argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Gene2GeneCoexpression transformEntity( final int transform,
            final ubic.gemma.model.association.coexpression.Gene2GeneCoexpression entity ) {
        Gene2GeneCoexpression target = null;
        if ( entity != null ) {
            switch ( transform ) {
                case TRANSFORM_NONE: // fall-through
                default:
                    target = entity;
            }
        }
        return target;
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,ubic.gemma.model.association.coexpression.Gene2GeneCoexpression)} method. This method
     * does not instantiate a new collection. <p/> This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.association.coexpression.Gene2GeneCoexpressionDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.association.coexpression.Gene2GeneCoexpression)
     */
    @Override
    protected void transformEntities( final int transform, final java.util.Collection entities ) {
        switch ( transform ) {
            case TRANSFORM_NONE: // fall-through
            default:
                // do nothing;
        }
    }

}