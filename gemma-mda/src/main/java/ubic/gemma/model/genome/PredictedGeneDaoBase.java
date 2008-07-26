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
package ubic.gemma.model.genome;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.genome.PredictedGene</code>.
 * </p>
 * 
 * @see ubic.gemma.model.genome.PredictedGene
 */
public abstract class PredictedGeneDaoBase extends ubic.gemma.model.genome.GeneDaoImpl implements
        ubic.gemma.model.genome.PredictedGeneDao {

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#load(int, java.lang.Long)
     */
    @Override
    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "PredictedGene.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get( ubic.gemma.model.genome.PredictedGeneImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.genome.PredictedGene ) entity );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#load(java.lang.Long)
     */
    @Override
    public ubic.gemma.model.common.Securable load( java.lang.Long id ) {
        return ( ubic.gemma.model.genome.PredictedGene ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#loadAll()
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#loadAll(int)
     */
    @Override
    public java.util.Collection loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.genome.PredictedGeneImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#create(ubic.gemma.model.genome.PredictedGene)
     */
    public ubic.gemma.model.common.Securable create( ubic.gemma.model.genome.PredictedGene predictedGene ) {
        return ( ubic.gemma.model.genome.PredictedGene ) this.create( TRANSFORM_NONE, predictedGene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#create(int transform, ubic.gemma.model.genome.PredictedGene)
     */
    public Object create( final int transform, final ubic.gemma.model.genome.PredictedGene predictedGene ) {
        if ( predictedGene == null ) {
            throw new IllegalArgumentException( "PredictedGene.create - 'predictedGene' can not be null" );
        }
        this.getHibernateTemplate().save( predictedGene );
        return this.transformEntity( transform, predictedGene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#create(java.util.Collection)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#create(int, java.util.Collection)
     */
    @Override
    public java.util.Collection create( final int transform, final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "PredictedGene.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    create( transform, ( ubic.gemma.model.genome.PredictedGene ) entityIterator.next() );
                }
                return null;
            }
        }, true );
        return entities;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#update(ubic.gemma.model.genome.PredictedGene)
     */
    public void update( ubic.gemma.model.genome.PredictedGene predictedGene ) {
        if ( predictedGene == null ) {
            throw new IllegalArgumentException( "PredictedGene.update - 'predictedGene' can not be null" );
        }
        this.getHibernateTemplate().update( predictedGene );
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */
    @Override
    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "PredictedGene.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    update( ( ubic.gemma.model.genome.PredictedGene ) entityIterator.next() );
                }
                return null;
            }
        }, true );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#remove(ubic.gemma.model.genome.PredictedGene)
     */
    public void remove( ubic.gemma.model.genome.PredictedGene predictedGene ) {
        if ( predictedGene == null ) {
            throw new IllegalArgumentException( "PredictedGene.remove - 'predictedGene' can not be null" );
        }
        this.getHibernateTemplate().delete( predictedGene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#remove(java.lang.Long)
     */
    @Override
    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "PredictedGene.remove - 'id' can not be null" );
        }
        ubic.gemma.model.genome.PredictedGene entity = ( ubic.gemma.model.genome.PredictedGene ) this.load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#remove(java.util.Collection)
     */
    @Override
    public void remove( java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "PredictedGene.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficalSymbol(java.lang.String)
     */
    @Override
    public java.util.Collection findByOfficalSymbol( java.lang.String officialSymbol ) {
        return this.findByOfficalSymbol( TRANSFORM_NONE, officialSymbol );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficalSymbol(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficalSymbol( final java.lang.String queryString,
            final java.lang.String officialSymbol ) {
        return this.findByOfficalSymbol( TRANSFORM_NONE, queryString, officialSymbol );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficalSymbol(int, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficalSymbol( final int transform, final java.lang.String officialSymbol ) {
        return this.findByOfficalSymbol( transform,
                "from GeneImpl g where g.officialSymbol=:officialSymbol order by g.officialName", officialSymbol );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficalSymbol(int, java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficalSymbol( final int transform, final java.lang.String queryString,
            final java.lang.String officialSymbol ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( officialSymbol );
        argNames.add( "officialSymbol" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialSymbolInexact(java.lang.String)
     */
    @Override
    public java.util.Collection findByOfficialSymbolInexact( java.lang.String officialSymbol ) {
        return this.findByOfficialSymbolInexact( TRANSFORM_NONE, officialSymbol );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialSymbolInexact(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficialSymbolInexact( final java.lang.String queryString,
            final java.lang.String officialSymbol ) {
        return this.findByOfficialSymbolInexact( TRANSFORM_NONE, queryString, officialSymbol );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialSymbolInexact(int, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficialSymbolInexact( final int transform, final java.lang.String officialSymbol ) {
        return this
                .findByOfficialSymbolInexact( transform,
                        "from GeneImpl g where g.officialSymbol like :officialSymbol order by g.officialSymbol",
                        officialSymbol );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialSymbolInexact(int, java.lang.String,
     *      java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficialSymbolInexact( final int transform, final java.lang.String queryString,
            final java.lang.String officialSymbol ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( officialSymbol );
        argNames.add( "officialSymbol" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialName(java.lang.String)
     */
    @Override
    public java.util.Collection findByOfficialName( java.lang.String officialName ) {
        return this.findByOfficialName( TRANSFORM_NONE, officialName );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialName(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficialName( final java.lang.String queryString,
            final java.lang.String officialName ) {
        return this.findByOfficialName( TRANSFORM_NONE, queryString, officialName );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialName(int, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficialName( final int transform, final java.lang.String officialName ) {
        return this.findByOfficialName( transform,
                "from GeneImpl g where g.officialName=:officialName order by g.officialName", officialName );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByOfficialName(int, java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByOfficialName( final int transform, final java.lang.String queryString,
            final java.lang.String officialName ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( officialName );
        argNames.add( "officialName" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#find(ubic.gemma.model.genome.Gene)
     */
    @Override
    public ubic.gemma.model.genome.Gene find( ubic.gemma.model.genome.Gene gene ) {
        return ( ubic.gemma.model.genome.Gene ) this.find( TRANSFORM_NONE, gene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#find(java.lang.String, ubic.gemma.model.genome.Gene)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public ubic.gemma.model.genome.Gene find( final java.lang.String queryString,
            final ubic.gemma.model.genome.Gene gene ) {
        return ( ubic.gemma.model.genome.Gene ) this.find( TRANSFORM_NONE, queryString, gene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#find(int, ubic.gemma.model.genome.Gene)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object find( final int transform, final ubic.gemma.model.genome.Gene gene ) {
        return this.find( transform,
                "from ubic.gemma.model.genome.PredictedGene as predictedGene where predictedGene.gene = :gene", gene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#find(int, java.lang.String, ubic.gemma.model.genome.Gene)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object find( final int transform, final java.lang.String queryString, final ubic.gemma.model.genome.Gene gene ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( gene );
        argNames.add( "gene" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'ubic.gemma.model.genome.Gene"
                                + "' was found when executing query --> '" + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.genome.PredictedGene ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findOrCreate(ubic.gemma.model.genome.Gene)
     */
    @Override
    public ubic.gemma.model.genome.Gene findOrCreate( ubic.gemma.model.genome.Gene gene ) {
        return ( ubic.gemma.model.genome.Gene ) this.findOrCreate( TRANSFORM_NONE, gene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findOrCreate(java.lang.String, ubic.gemma.model.genome.Gene)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public ubic.gemma.model.genome.Gene findOrCreate( final java.lang.String queryString,
            final ubic.gemma.model.genome.Gene gene ) {
        return ( ubic.gemma.model.genome.Gene ) this.findOrCreate( TRANSFORM_NONE, queryString, gene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findOrCreate(int, ubic.gemma.model.genome.Gene)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object findOrCreate( final int transform, final ubic.gemma.model.genome.Gene gene ) {
        return this.findOrCreate( transform,
                "from ubic.gemma.model.genome.PredictedGene as predictedGene where predictedGene.gene = :gene", gene );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findOrCreate(int, java.lang.String, ubic.gemma.model.genome.Gene)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object findOrCreate( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.genome.Gene gene ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( gene );
        argNames.add( "gene" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'ubic.gemma.model.genome.Gene"
                                + "' was found when executing query --> '" + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.genome.PredictedGene ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByPhysicalLocation(ubic.gemma.model.genome.PhysicalLocation)
     */
    @Override
    public java.util.Collection findByPhysicalLocation( ubic.gemma.model.genome.PhysicalLocation location ) {
        return this.findByPhysicalLocation( TRANSFORM_NONE, location );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByPhysicalLocation(java.lang.String,
     *      ubic.gemma.model.genome.PhysicalLocation)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByPhysicalLocation( final java.lang.String queryString,
            final ubic.gemma.model.genome.PhysicalLocation location ) {
        return this.findByPhysicalLocation( TRANSFORM_NONE, queryString, location );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByPhysicalLocation(int,
     *      ubic.gemma.model.genome.PhysicalLocation)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByPhysicalLocation( final int transform,
            final ubic.gemma.model.genome.PhysicalLocation location ) {
        return this.findByPhysicalLocation( transform,
                "from ubic.gemma.model.genome.PredictedGene as predictedGene where predictedGene.location = :location",
                location );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByPhysicalLocation(int, java.lang.String,
     *      ubic.gemma.model.genome.PhysicalLocation)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByPhysicalLocation( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.genome.PhysicalLocation location ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( location );
        argNames.add( "location" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByNcbiId(java.lang.String)
     */
    @Override
    public java.util.Collection findByNcbiId( java.lang.String ncbiId ) {
        return this.findByNcbiId( TRANSFORM_NONE, ncbiId );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByNcbiId(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByNcbiId( final java.lang.String queryString, final java.lang.String ncbiId ) {
        return this.findByNcbiId( TRANSFORM_NONE, queryString, ncbiId );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByNcbiId(int, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByNcbiId( final int transform, final java.lang.String ncbiId ) {
        return this.findByNcbiId( transform, "from GeneImpl g where g.ncbiId = :ncbiId", ncbiId );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#findByNcbiId(int, java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection findByNcbiId( final int transform, final java.lang.String queryString,
            final java.lang.String ncbiId ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( ncbiId );
        argNames.add( "ncbiId" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getRecipient(java.lang.Long)
     */
    @Override
    public java.lang.String getRecipient( java.lang.Long id ) {
        return ( java.lang.String ) this.getRecipient( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getRecipient(java.lang.String, java.lang.Long)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.lang.String getRecipient( final java.lang.String queryString, final java.lang.Long id ) {
        return ( java.lang.String ) this.getRecipient( TRANSFORM_NONE, queryString, id );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getRecipient(int, java.lang.Long)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getRecipient( final int transform, final java.lang.Long id ) {
        return this.getRecipient( transform,
                "from ubic.gemma.model.genome.PredictedGene as predictedGene where predictedGene.id = :id", id );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getRecipient(int, java.lang.String, java.lang.Long)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getRecipient( final int transform, final java.lang.String queryString, final java.lang.Long id ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( id );
        argNames.add( "id" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'java.lang.String" + "' was found when executing query --> '"
                                + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.genome.PredictedGene ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getAclObjectIdentityId(ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Long getAclObjectIdentityId( ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Long ) this.getAclObjectIdentityId( TRANSFORM_NONE, securable );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getAclObjectIdentityId(java.lang.String,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.lang.Long getAclObjectIdentityId( final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Long ) this.getAclObjectIdentityId( TRANSFORM_NONE, queryString, securable );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getAclObjectIdentityId(int, ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getAclObjectIdentityId( final int transform, final ubic.gemma.model.common.Securable securable ) {
        return this
                .getAclObjectIdentityId(
                        transform,
                        "from ubic.gemma.model.genome.PredictedGene as predictedGene where predictedGene.securable = :securable",
                        securable );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getAclObjectIdentityId(int, java.lang.String,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getAclObjectIdentityId( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( securable );
        argNames.add( "securable" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'java.lang.Long" + "' was found when executing query --> '"
                                + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.genome.PredictedGene ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMask(ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Integer getMask( ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Integer ) this.getMask( TRANSFORM_NONE, securable );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMask(java.lang.String, ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.lang.Integer getMask( final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Integer ) this.getMask( TRANSFORM_NONE, queryString, securable );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMask(int, ubic.gemma.model.common.Securable)
     */
    @SuppressWarnings( { "unchecked" })
    public Object getMask( final int transform, final ubic.gemma.model.common.Securable securable ) {
        return this
                .getMask(
                        transform,
                        "from ubic.gemma.model.genome.PredictedGene as predictedGene where predictedGene.securable = :securable",
                        securable );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMask(int, java.lang.String, ubic.gemma.model.common.Securable)
     */
    @SuppressWarnings( { "unchecked" })
    public Object getMask( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( securable );
        argNames.add( "securable" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'java.lang.Integer" + "' was found when executing query --> '"
                                + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = ( ubic.gemma.model.genome.PredictedGene ) results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.genome.PredictedGene ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMasks(java.util.Collection)
     */
    public java.util.Map getMasks( java.util.Collection securables ) {
        return ( java.util.Map ) this.getMasks( TRANSFORM_NONE, securables );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMasks(java.lang.String, java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Map getMasks( final java.lang.String queryString, final java.util.Collection securables ) {
        return ( java.util.Map ) this.getMasks( TRANSFORM_NONE, queryString, securables );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMasks(int, java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public Object getMasks( final int transform, final java.util.Collection securables ) {
        return this
                .getMasks(
                        transform,
                        "from ubic.gemma.model.genome.PredictedGene as predictedGene where predictedGene.securables = :securables",
                        securables );
    }

    /**
     * @see ubic.gemma.model.genome.PredictedGeneDao#getMasks(int, java.lang.String, java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public Object getMasks( final int transform, final java.lang.String queryString,
            final java.util.Collection securables ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( securables );
        argNames.add( "securables" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'java.util.Map" + "' was found when executing query --> '"
                                + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = ( ubic.gemma.model.genome.PredictedGene ) results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.genome.PredictedGene ) result );
        return result;
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.genome.PredictedGeneDao</code>, please note that the {@link #TRANSFORM_NONE} constant
     * denotes no transformation, so the entity itself will be returned. If the integer argument value is unknown
     * {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in {@link ubic.gemma.model.genome.PredictedGeneDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform, final ubic.gemma.model.genome.PredictedGene entity ) {
        Object target = null;
        if ( entity != null ) {
            switch ( transform ) {
                case ubic.gemma.model.genome.GeneDao.TRANSFORM_GENEVALUEOBJECT:
                    target = toGeneValueObject( entity );
                    break;
                case TRANSFORM_NONE: // fall-through
                default:
                    target = entity;
            }
        }
        return target;
    }

    /**
     * Transforms a collection of entities using the {@link #transformEntity(int,ubic.gemma.model.genome.PredictedGene)}
     * method. This method does not instantiate a new collection. <p/> This method is to be used internally only.
     * 
     * @param transform one of the constants declared in <code>ubic.gemma.model.genome.PredictedGeneDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.genome.PredictedGene)
     */
    protected void transformEntities( final int transform, final java.util.Collection entities ) {
        switch ( transform ) {
            case ubic.gemma.model.genome.GeneDao.TRANSFORM_GENEVALUEOBJECT:
                toGeneValueObjectCollection( entities );
                break;
            case TRANSFORM_NONE: // fall-through
            default:
                // do nothing;
        }
    }

}