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
package ubic.gemma.model.expression.experiment;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.expression.experiment.ExperimentalFactor</code>.
 * </p>
 * 
 * @see ubic.gemma.model.expression.experiment.ExperimentalFactor
 */
public abstract class ExperimentalFactorDaoBase extends ubic.gemma.model.common.AuditableDaoImpl implements
        ubic.gemma.model.expression.experiment.ExperimentalFactorDao {

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#load(int, java.lang.Long)
     */
    @Override
    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "ExperimentalFactor.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.expression.experiment.ExperimentalFactorImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) entity );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#load(java.lang.Long)
     */
    @Override
    public ubic.gemma.model.common.Securable load( java.lang.Long id ) {
        return ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#loadAll()
     */
    @Override
    public java.util.Collection<ExperimentalFactor> loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#loadAll(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public java.util.Collection<ExperimentalFactor> loadAll( final int transform ) {
        final java.util.Collection<ExperimentalFactor> results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.expression.experiment.ExperimentalFactorImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#create(ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public ubic.gemma.model.common.Securable create(
            ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        return ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) this.create( TRANSFORM_NONE,
                experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#create(int transform,
     *      ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public Object create( final int transform,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        if ( experimentalFactor == null ) {
            throw new IllegalArgumentException( "ExperimentalFactor.create - 'experimentalFactor' can not be null" );
        }
        this.getHibernateTemplate().save( experimentalFactor );
        return this.transformEntity( transform, experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#create(java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#create(int, java.util.Collection)
     */
    public java.util.Collection<ExperimentalFactor> create( final int transform,
            final java.util.Collection<ExperimentalFactor> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "ExperimentalFactor.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<ExperimentalFactor> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            create( transform, entityIterator.next() );
                        }
                        return null;
                    }
                } );
        return entities;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#update(ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public void update( ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        if ( experimentalFactor == null ) {
            throw new IllegalArgumentException( "ExperimentalFactor.update - 'experimentalFactor' can not be null" );
        }
        this.getHibernateTemplate().update( experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */
    @Override
    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "ExperimentalFactor.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<ExperimentalFactor> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            update( ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) entityIterator.next() );
                        }
                        return null;
                    }
                } );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#remove(ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public void remove( ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        if ( experimentalFactor == null ) {
            throw new IllegalArgumentException( "ExperimentalFactor.remove - 'experimentalFactor' can not be null" );
        }
        this.getHibernateTemplate().delete( experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#remove(java.lang.Long)
     */
    @Override
    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "ExperimentalFactor.remove - 'id' can not be null" );
        }
        ubic.gemma.model.expression.experiment.ExperimentalFactor entity = ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) this
                .load( id );
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
            throw new IllegalArgumentException( "ExperimentalFactor.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#find(ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public ubic.gemma.model.expression.experiment.ExperimentalFactor find(
            ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        return ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) this.find( TRANSFORM_NONE,
                experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#find(java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public ubic.gemma.model.expression.experiment.ExperimentalFactor find( final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        return ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) this.find( TRANSFORM_NONE, queryString,
                experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#find(int,
     *      ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public Object find( final int transform,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        return this
                .find(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExperimentalFactor as experimentalFactor where experimentalFactor.experimentalFactor = :experimentalFactor",
                        experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#find(int, java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    @SuppressWarnings( { "unchecked" })
    public Object find( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( experimentalFactor );
        argNames.add( "experimentalFactor" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.experiment.ExperimentalFactor"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#findOrCreate(ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public ubic.gemma.model.expression.experiment.ExperimentalFactor findOrCreate(
            ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        return ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) this.findOrCreate( TRANSFORM_NONE,
                experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#findOrCreate(java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public ubic.gemma.model.expression.experiment.ExperimentalFactor findOrCreate( final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        return ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) this.findOrCreate( TRANSFORM_NONE,
                queryString, experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#findOrCreate(int,
     *      ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    public Object findOrCreate( final int transform,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        return this
                .findOrCreate(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExperimentalFactor as experimentalFactor where experimentalFactor.experimentalFactor = :experimentalFactor",
                        experimentalFactor );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#findOrCreate(int, java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExperimentalFactor)
     */
    @SuppressWarnings( { "unchecked" })
    public Object findOrCreate( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor experimentalFactor ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( experimentalFactor );
        argNames.add( "experimentalFactor" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.experiment.ExperimentalFactor"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getRecipient(java.lang.Long)
     */
    @Override
    public java.lang.String getRecipient( java.lang.Long id ) {
        return ( java.lang.String ) this.getRecipient( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getRecipient(java.lang.String, java.lang.Long)
     */
    @Override
    public java.lang.String getRecipient( final java.lang.String queryString, final java.lang.Long id ) {
        return ( java.lang.String ) this.getRecipient( TRANSFORM_NONE, queryString, id );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getRecipient(int, java.lang.Long)
     */
    @Override
    public Object getRecipient( final int transform, final java.lang.Long id ) {
        return this
                .getRecipient(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExperimentalFactor as experimentalFactor where experimentalFactor.id = :id",
                        id );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getRecipient(int, java.lang.String,
     *      java.lang.Long)
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
        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'java.lang.String" + "' was found when executing query --> '"
                            + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getAclObjectIdentityId(ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Long getAclObjectIdentityId( ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Long ) this.getAclObjectIdentityId( TRANSFORM_NONE, securable );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getAclObjectIdentityId(java.lang.String,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Long getAclObjectIdentityId( final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Long ) this.getAclObjectIdentityId( TRANSFORM_NONE, queryString, securable );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getAclObjectIdentityId(int,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    public Object getAclObjectIdentityId( final int transform, final ubic.gemma.model.common.Securable securable ) {
        return this
                .getAclObjectIdentityId(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExperimentalFactor as experimentalFactor where experimentalFactor.securable = :securable",
                        securable );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getAclObjectIdentityId(int, java.lang.String,
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

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'java.lang.Long" + "' was found when executing query --> '"
                            + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMask(ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Integer getMask( ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Integer ) this.getMask( TRANSFORM_NONE, securable );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMask(java.lang.String,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Integer getMask( final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Integer ) this.getMask( TRANSFORM_NONE, queryString, securable );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMask(int, ubic.gemma.model.common.Securable)
     */
    @Override
    public Object getMask( final int transform, final ubic.gemma.model.common.Securable securable ) {
        return this
                .getMask(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExperimentalFactor as experimentalFactor where experimentalFactor.securable = :securable",
                        securable );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMask(int, java.lang.String,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
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

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'java.lang.Integer" + "' was found when executing query --> '"
                            + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMasks(java.util.Collection)
     */
    @Override
    public java.util.Map getMasks( java.util.Collection securables ) {
        return ( java.util.Map ) this.getMasks( TRANSFORM_NONE, securables );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMasks(java.lang.String,
     *      java.util.Collection)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Map getMasks( final java.lang.String queryString, final java.util.Collection securables ) {
        return ( java.util.Map ) this.getMasks( TRANSFORM_NONE, queryString, securables );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMasks(int, java.util.Collection)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getMasks( final int transform, final java.util.Collection securables ) {
        return this
                .getMasks(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExperimentalFactor as experimentalFactor where experimentalFactor.securables = :securables",
                        securables );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorDao#getMasks(int, java.lang.String,
     *      java.util.Collection)
     */
    @Override
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

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'java.util.Map" + "' was found when executing query --> '" + queryString
                            + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExperimentalFactor ) result );
        return result;
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.expression.experiment.ExperimentalFactorDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If the integer
     * argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.expression.experiment.ExperimentalFactorDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.expression.experiment.ExperimentalFactor entity ) {
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

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,ubic.gemma.model.expression.experiment.ExperimentalFactor)} method. This method does
     * not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.expression.experiment.ExperimentalFactorDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.expression.experiment.ExperimentalFactor)
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