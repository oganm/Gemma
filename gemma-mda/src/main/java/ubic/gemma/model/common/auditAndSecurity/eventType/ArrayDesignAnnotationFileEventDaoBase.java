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
 * <code>ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent</code>.
 * </p>
 * 
 * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent
 */
public abstract class ArrayDesignAnnotationFileEventDaoBase extends
        ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnalysisEventDaoImpl implements
        ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao {

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#load(int,
     *      java.lang.Long)
     */
    @Override
    public Object load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "ArrayDesignAnnotationFileEvent.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventImpl.class, id );
        return transformEntity( transform,
                ( ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent ) entity );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#load(java.lang.Long)
     */
    @Override
    public ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType load( java.lang.Long id ) {
        return ( ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent ) this.load(
                TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#loadAll()
     */
    @Override
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#loadAll(int)
     */
    @Override
    public java.util.Collection loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#create(ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent)
     */
    public ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType create(
            ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent arrayDesignAnnotationFileEvent ) {
        return ( ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent ) this.create(
                TRANSFORM_NONE, arrayDesignAnnotationFileEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#create(int transform,
     *      ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent)
     */
    public Object create(
            final int transform,
            final ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent arrayDesignAnnotationFileEvent ) {
        if ( arrayDesignAnnotationFileEvent == null ) {
            throw new IllegalArgumentException(
                    "ArrayDesignAnnotationFileEvent.create - 'arrayDesignAnnotationFileEvent' can not be null" );
        }
        this.getHibernateTemplate().save( arrayDesignAnnotationFileEvent );
        return this.transformEntity( transform, arrayDesignAnnotationFileEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#create(java.util.Collection)
     */
    @SuppressWarnings( { "unchecked" })
    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#create(int,
     *      java.util.Collection)
     */
    public java.util.Collection create( final int transform, final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "ArrayDesignAnnotationFileEvent.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    create(
                            transform,
                            ( ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent ) entityIterator
                                    .next() );
                }
                return null;
            }
        }, true );
        return entities;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#update(ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent)
     */
    public void update(
            ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent arrayDesignAnnotationFileEvent ) {
        if ( arrayDesignAnnotationFileEvent == null ) {
            throw new IllegalArgumentException(
                    "ArrayDesignAnnotationFileEvent.update - 'arrayDesignAnnotationFileEvent' can not be null" );
        }
        this.getHibernateTemplate().update( arrayDesignAnnotationFileEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventTypeDao#update(java.util.Collection)
     */
    @Override
    public void update( final java.util.Collection entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "ArrayDesignAnnotationFileEvent.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().execute( new org.springframework.orm.hibernate3.HibernateCallback() {
            public Object doInHibernate( org.hibernate.Session session ) throws org.hibernate.HibernateException {
                for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                    update( ( ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent ) entityIterator
                            .next() );
                }
                return null;
            }
        }, true );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#remove(ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent)
     */
    public void remove(
            ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent arrayDesignAnnotationFileEvent ) {
        if ( arrayDesignAnnotationFileEvent == null ) {
            throw new IllegalArgumentException(
                    "ArrayDesignAnnotationFileEvent.remove - 'arrayDesignAnnotationFileEvent' can not be null" );
        }
        this.getHibernateTemplate().delete( arrayDesignAnnotationFileEvent );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao#remove(java.lang.Long)
     */
    @Override
    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "ArrayDesignAnnotationFileEvent.remove - 'id' can not be null" );
        }
        ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent entity = ( ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent ) this
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
            throw new IllegalArgumentException( "ArrayDesignAnnotationFileEvent.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao</code>, please note
     * that the {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned. If
     * the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent entity ) {
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
     * {@link #transformEntity(int,ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent)}
     * method. This method does not instantiate a new collection. <p/> This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEventDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.common.auditAndSecurity.eventType.ArrayDesignAnnotationFileEvent)
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