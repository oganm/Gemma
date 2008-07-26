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
package ubic.gemma.model.common.auditAndSecurity.eventType;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent</code>.
 * </p>
 * 
 * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent
 */
public abstract class OKStatusFlagEventDaoBase extends
        ubic.gemma.model.common.auditAndSecurity.eventType.StatusFlagEventDaoImpl implements
        ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao {

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#load(int, java.lang.Long)
     */
    @Override
    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventImpl.class, id );
        return transformEntity( transform,
                ( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent ) entity );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#load(java.lang.Long)
     */
    @Override
    public ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType load( java.lang.Long id ) {
        return ( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent ) this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#loadAll()
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#loadAll(int)
     */
    @Override
    public java.util.Collection loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#create(ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent)
     */
    public ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType create(
            ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent oKStatusFlagEvent ) {
        return ( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent ) this.create( TRANSFORM_NONE,
                oKStatusFlagEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#create(int transform,
     *      ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent)
     */
    public Object create( final int transform,
            final ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent oKStatusFlagEvent ) {
        if ( oKStatusFlagEvent == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.create - 'oKStatusFlagEvent' can not be null" );
        }
        this.getHibernateTemplate().save( oKStatusFlagEvent );
        return this.transformEntity( transform, oKStatusFlagEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#create(java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#create(int, java.util.Collection)
     */
    public java.util.Collection create( final int transform, final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    create( transform,
                            ( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent ) entityIterator
                                    .next() );
                }
                return null;
            }
        }, true );
        return entities;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#update(ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent)
     */
    public void update( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent oKStatusFlagEvent ) {
        if ( oKStatusFlagEvent == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.update - 'oKStatusFlagEvent' can not be null" );
        }
        this.getHibernateTemplate().update( oKStatusFlagEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventTypeDao#update(java.util.Collection)
     */
    @Override
    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    update( ( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent ) entityIterator
                            .next() );
                }
                return null;
            }
        }, true );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#remove(ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent)
     */
    public void remove( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent oKStatusFlagEvent ) {
        if ( oKStatusFlagEvent == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.remove - 'oKStatusFlagEvent' can not be null" );
        }
        this.getHibernateTemplate().delete( oKStatusFlagEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao#remove(java.lang.Long)
     */
    @Override
    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.remove - 'id' can not be null" );
        }
        ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent entity = ( ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent ) this
                .load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventTypeDao#remove(java.util.Collection)
     */
    @Override
    public void remove( java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "OKStatusFlagEvent.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If the integer
     * argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent entity ) {
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
     * {@link #transformEntity(int,ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent)} method. This
     * method does not instantiate a new collection. <p/> This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEventDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.common.auditAndSecurity.eventType.OKStatusFlagEvent)
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