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
package ubic.gemma.model.common.auditAndSecurity;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.common.auditAndSecurity.UserGroup</code>.
 * </p>
 * 
 * @see ubic.gemma.model.common.auditAndSecurity.UserGroup
 */
public abstract class UserGroupDaoBase extends ubic.gemma.model.common.AuditableDaoImpl implements
        ubic.gemma.model.common.auditAndSecurity.UserGroupDao {

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#load(int, java.lang.Long)
     */
    @Override
    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "UserGroup.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.common.auditAndSecurity.UserGroupImpl.class, id );
        return transformEntity( transform, ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) entity );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#load(java.lang.Long)
     */
    @Override
    public ubic.gemma.model.common.Securable load( java.lang.Long id ) {
        return ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#loadAll()
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#loadAll(int)
     */
    @Override
    public java.util.Collection loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.common.auditAndSecurity.UserGroupImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#create(ubic.gemma.model.common.auditAndSecurity.UserGroup)
     */
    public ubic.gemma.model.common.Securable create( ubic.gemma.model.common.auditAndSecurity.UserGroup userGroup ) {
        return ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) this.create( TRANSFORM_NONE, userGroup );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#create(int transform,
     *      ubic.gemma.model.common.auditAndSecurity.UserGroup)
     */
    public Object create( final int transform, final ubic.gemma.model.common.auditAndSecurity.UserGroup userGroup ) {
        if ( userGroup == null ) {
            throw new IllegalArgumentException( "UserGroup.create - 'userGroup' can not be null" );
        }
        this.getHibernateTemplate().save( userGroup );
        return this.transformEntity( transform, userGroup );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#create(java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#create(int, java.util.Collection)
     */
    public java.util.Collection create( final int transform, final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "UserGroup.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    create( transform, ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) entityIterator.next() );
                }
                return null;
            }
        }, true );
        return entities;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#update(ubic.gemma.model.common.auditAndSecurity.UserGroup)
     */
    public void update( ubic.gemma.model.common.auditAndSecurity.UserGroup userGroup ) {
        if ( userGroup == null ) {
            throw new IllegalArgumentException( "UserGroup.update - 'userGroup' can not be null" );
        }
        this.getHibernateTemplate().update( userGroup );
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */
    @Override
    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "UserGroup.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    update( ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) entityIterator.next() );
                }
                return null;
            }
        }, true );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#remove(ubic.gemma.model.common.auditAndSecurity.UserGroup)
     */
    public void remove( ubic.gemma.model.common.auditAndSecurity.UserGroup userGroup ) {
        if ( userGroup == null ) {
            throw new IllegalArgumentException( "UserGroup.remove - 'userGroup' can not be null" );
        }
        this.getHibernateTemplate().delete( userGroup );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#remove(java.lang.Long)
     */
    @Override
    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "UserGroup.remove - 'id' can not be null" );
        }
        ubic.gemma.model.common.auditAndSecurity.UserGroup entity = ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) this
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
            throw new IllegalArgumentException( "UserGroup.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#findByUserGroupName(java.lang.String)
     */
    public ubic.gemma.model.common.auditAndSecurity.UserGroup findByUserGroupName( java.lang.String name ) {
        return ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) this.findByUserGroupName( TRANSFORM_NONE, name );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#findByUserGroupName(java.lang.String,
     *      java.lang.String)
     */
    @SuppressWarnings( { "unchecked" })
    public ubic.gemma.model.common.auditAndSecurity.UserGroup findByUserGroupName( final java.lang.String queryString,
            final java.lang.String name ) {
        return ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) this.findByUserGroupName( TRANSFORM_NONE,
                queryString, name );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#findByUserGroupName(int, java.lang.String)
     */
    @SuppressWarnings( { "unchecked" })
    public Object findByUserGroupName( final int transform, final java.lang.String name ) {
        return this.findByUserGroupName( transform,
                "from ubic.gemma.model.common.auditAndSecurity.UserGroup as userGroup where userGroup.name = :name",
                name );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#findByUserGroupName(int, java.lang.String,
     *      java.lang.String)
     */
    @SuppressWarnings( { "unchecked" })
    public Object findByUserGroupName( final int transform, final java.lang.String queryString,
            final java.lang.String name ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( name );
        argNames.add( "name" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'ubic.gemma.model.common.auditAndSecurity.UserGroup"
                                + "' was found when executing query --> '" + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getRecipient(java.lang.Long)
     */
    @Override
    public java.lang.String getRecipient( java.lang.Long id ) {
        return ( java.lang.String ) this.getRecipient( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getRecipient(java.lang.String, java.lang.Long)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.lang.String getRecipient( final java.lang.String queryString, final java.lang.Long id ) {
        return ( java.lang.String ) this.getRecipient( TRANSFORM_NONE, queryString, id );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getRecipient(int, java.lang.Long)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getRecipient( final int transform, final java.lang.Long id ) {
        return this.getRecipient( transform,
                "from ubic.gemma.model.common.auditAndSecurity.UserGroup as userGroup where userGroup.id = :id", id );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getRecipient(int, java.lang.String, java.lang.Long)
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
        result = transformEntity( transform, ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getAclObjectIdentityId(ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Long getAclObjectIdentityId( ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Long ) this.getAclObjectIdentityId( TRANSFORM_NONE, securable );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getAclObjectIdentityId(java.lang.String,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.lang.Long getAclObjectIdentityId( final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Long ) this.getAclObjectIdentityId( TRANSFORM_NONE, queryString, securable );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getAclObjectIdentityId(int,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getAclObjectIdentityId( final int transform, final ubic.gemma.model.common.Securable securable ) {
        return this
                .getAclObjectIdentityId(
                        transform,
                        "from ubic.gemma.model.common.auditAndSecurity.UserGroup as userGroup where userGroup.securable = :securable",
                        securable );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getAclObjectIdentityId(int, java.lang.String,
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
        result = transformEntity( transform, ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMask(ubic.gemma.model.common.Securable)
     */
    @Override
    public java.lang.Integer getMask( ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Integer ) this.getMask( TRANSFORM_NONE, securable );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMask(java.lang.String,
     *      ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.lang.Integer getMask( final java.lang.String queryString,
            final ubic.gemma.model.common.Securable securable ) {
        return ( java.lang.Integer ) this.getMask( TRANSFORM_NONE, queryString, securable );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMask(int, ubic.gemma.model.common.Securable)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getMask( final int transform, final ubic.gemma.model.common.Securable securable ) {
        return this
                .getMask(
                        transform,
                        "from ubic.gemma.model.common.auditAndSecurity.UserGroup as userGroup where userGroup.securable = :securable",
                        securable );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMask(int, java.lang.String,
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
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'java.lang.Integer" + "' was found when executing query --> '"
                                + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMasks(java.util.Collection)
     */
    @Override
    public java.util.Map getMasks( java.util.Collection securables ) {
        return ( java.util.Map ) this.getMasks( TRANSFORM_NONE, securables );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMasks(java.lang.String, java.util.Collection)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Map getMasks( final java.lang.String queryString, final java.util.Collection securables ) {
        return ( java.util.Map ) this.getMasks( TRANSFORM_NONE, queryString, securables );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMasks(int, java.util.Collection)
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public Object getMasks( final int transform, final java.util.Collection securables ) {
        return this
                .getMasks(
                        transform,
                        "from ubic.gemma.model.common.auditAndSecurity.UserGroup as userGroup where userGroup.securables = :securables",
                        securables );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.UserGroupDao#getMasks(int, java.lang.String, java.util.Collection)
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
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of 'java.util.Map" + "' was found when executing query --> '"
                                + queryString + "'" );
            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        result = transformEntity( transform, ( ubic.gemma.model.common.auditAndSecurity.UserGroup ) result );
        return result;
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.common.auditAndSecurity.UserGroupDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If the integer
     * argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in {@link ubic.gemma.model.common.auditAndSecurity.UserGroupDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.common.auditAndSecurity.UserGroup entity ) {
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
     * {@link #transformEntity(int,ubic.gemma.model.common.auditAndSecurity.UserGroup)} method. This method does not
     * instantiate a new collection. <p/> This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.common.auditAndSecurity.UserGroupDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.common.auditAndSecurity.UserGroup)
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