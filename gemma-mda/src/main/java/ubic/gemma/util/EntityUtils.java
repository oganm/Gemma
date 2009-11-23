/*
 * The Gemma project
 * 
 * Copyright (c) 2007 University of British Columbia
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
package ubic.gemma.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author paul
 * @version $Id$
 */
public class EntityUtils {

    /**
     * @param entity
     * @return
     */
    public static Long getId( Object entity ) {
        try {
            Method m = entity.getClass().getMethod( "getId", new Class[] {} );
            return ( Long ) m.invoke( entity, new Object[] {} );
        } catch ( SecurityException e ) {
            throw new RuntimeException( e );
        } catch ( NoSuchMethodException e ) {
            throw new RuntimeException( e );
        } catch ( IllegalArgumentException e ) {
            throw new RuntimeException( e );
        } catch ( IllegalAccessException e ) {
            throw new RuntimeException( e );
        } catch ( InvocationTargetException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * @param entities
     * @return
     */
    public static Collection<Long> getIds( Collection<? extends Object> entities ) {

        Collection<Long> r;

        if ( List.class.isAssignableFrom( entities.getClass() ) ) {
            r = new ArrayList<Long>();
        } else {
            r = new HashSet<Long>();
        }

        for ( Object object : entities ) {
            r.add( getId( object ) );
        }
        return r;
    }

    /**
     * @param entities
     * @return
     */
    public static Map<Long, Object> getIdMap( Collection<? extends Object> entities ) {
        Map<Long, Object> result = new HashMap<Long, Object>();

        for ( Object object : entities ) {
            result.put( getId( object ), object );
        }

        return result;

    }

    /**
     * Obtain the implementation for a proxy. If target is not an instanceof HibernateProxy, target is returned.
     * 
     * @param target The object to be unproxied.
     * @return the underlying implementation.
     */
    public static Object getImplementationForProxy( Object target ) {
        if ( target instanceof HibernateProxy ) {
            HibernateProxy proxy = ( HibernateProxy ) target;
            return proxy.getHibernateLazyInitializer().getImplementation();
        }
        return target;
    }

    /**
     * Replaces a proxy with an implementation. If target is not an instanceof HibernateProxy, target is unchanged.
     * 
     * @param target The object to be unproxied.
     */
    public static void unProxy( Object target ) {
        target = getImplementationForProxy( target );
    }

    /**
     * Put the given entity into the Session, with LockMode.NONE
     * <p>
     * Based on idea from {@link https://forum.hibernate.org/viewtopic.php?p=2284826#p2284826}
     * 
     * @param session Hibernate Session (use factory.getCurrentSession())
     * @param obj the entity
     * @param clazz the class type of the persisted entity. Don't use obj.getClass() as this might return a proxy type.
     * @param id identifier of the obj
     */
    public static void attach( Session session, Object obj, Class<?> clazz, Long id ) {
        if ( obj == null || id == null ) return;
        if ( !session.isOpen() ) throw new IllegalArgumentException( "Illegal attempt to use a closed session" );
        if ( !session.contains( obj ) ) {

            Object oldObj = session.get( clazz, id );

            if ( oldObj != null ) {
                session.evict( oldObj );
            }
        }
        session.lock( obj, LockMode.NONE );
    }

}
