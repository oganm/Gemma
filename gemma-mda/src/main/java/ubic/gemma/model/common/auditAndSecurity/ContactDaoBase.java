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
package ubic.gemma.model.common.auditAndSecurity;

import java.util.Collection;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.common.auditAndSecurity.Contact</code>.
 * </p>
 * 
 * @see ubic.gemma.model.common.auditAndSecurity.Contact
 */
public abstract class ContactDaoBase extends HibernateDaoSupport implements
        ubic.gemma.model.common.auditAndSecurity.ContactDao {

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#create(int, java.util.Collection)
     */
    public java.util.Collection<? extends Contact> create( final java.util.Collection<? extends Contact> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "Contact.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<? extends Contact> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            create( entityIterator.next() );
                        }
                        return null;
                    }
                } );
        return entities;
    }

    
    public Collection<? extends Contact> load( Collection<Long> ids ) {
        return this.getHibernateTemplate().findByNamedParam( "from ContactImpl where id in (:ids)", "ids", ids );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#create(int transform,
     *      ubic.gemma.model.common.auditAndSecurity.Contact)
     */
    public Contact create( final ubic.gemma.model.common.auditAndSecurity.Contact contact ) {
        if ( contact == null ) {
            throw new IllegalArgumentException( "Contact.create - 'contact' can not be null" );
        }
        this.getHibernateTemplate().save( contact );
        return contact;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#findByEmail(int, java.lang.String)
     */
    public Contact findByEmail( final java.lang.String email ) {
        return this.findByEmail( "from ContactImpl c where c.email = :email", email );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#findByEmail(int, java.lang.String, java.lang.String)
     */
    
    public Contact findByEmail( final java.lang.String queryString, final java.lang.String email ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( email );
        argNames.add( "email" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.common.auditAndSecurity.Contact"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        return ( Contact ) result;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#load(int, java.lang.Long)
     */

    public Contact load( final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "Contact.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.common.auditAndSecurity.ContactImpl.class, id );
        return ( ubic.gemma.model.common.auditAndSecurity.Contact ) entity;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#loadAll(int)
     */
    public java.util.Collection<? extends Contact> loadAll() {
        final java.util.Collection<? extends Contact> results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.common.auditAndSecurity.ContactImpl.class );
        return results;
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#remove(java.lang.Long)
     */

    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "Contact.remove - 'id' can not be null" );
        }
        ubic.gemma.model.common.auditAndSecurity.Contact entity = this.load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#remove(java.util.Collection)
     */

    public void remove( java.util.Collection<? extends Contact> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "Contact.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#remove(ubic.gemma.model.common.auditAndSecurity.Contact)
     */
    public void remove( ubic.gemma.model.common.auditAndSecurity.Contact contact ) {
        if ( contact == null ) {
            throw new IllegalArgumentException( "Contact.remove - 'contact' can not be null" );
        }
        this.getHibernateTemplate().delete( contact );
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */

    public void update( final java.util.Collection<? extends Contact> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "Contact.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<? extends Contact> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            update( entityIterator.next() );
                        }
                        return null;
                    }
                } );
    }

    /**
     * @see ubic.gemma.model.common.auditAndSecurity.ContactDao#update(ubic.gemma.model.common.auditAndSecurity.Contact)
     */
    public void update( ubic.gemma.model.common.auditAndSecurity.Contact contact ) {
        if ( contact == null ) {
            throw new IllegalArgumentException( "Contact.update - 'contact' can not be null" );
        }
        this.getHibernateTemplate().update( contact );
    }

}