package ubic.gemma.persistence.service.common.auditAndSecurity.curation;

import gemma.gsec.util.SecurityUtil;
import lombok.SneakyThrows;
import org.hibernate.SessionFactory;
import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.common.auditAndSecurity.curation.AbstractCuratableValueObject;
import ubic.gemma.model.common.auditAndSecurity.curation.Curatable;
import ubic.gemma.persistence.service.AbstractFilteringVoEnabledDao;
import ubic.gemma.persistence.service.ObjectFilterException;
import ubic.gemma.persistence.service.common.auditAndSecurity.CurationDetailsDao;
import ubic.gemma.persistence.service.common.auditAndSecurity.CurationDetailsDaoImpl;
import ubic.gemma.persistence.util.ObjectFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by tesarst on 07/03/17.
 * DAO covering methods common to all Curatable objects.
 *
 * @author tesarst
 */
public abstract class AbstractCuratableDao<C extends Curatable, VO extends AbstractCuratableValueObject<C>>
        extends AbstractFilteringVoEnabledDao<C, VO> implements CuratableDao<C, VO> {

    protected AbstractCuratableDao( Class<C> elementClass, SessionFactory sessionFactory ) {
        super( elementClass, sessionFactory );
    }

    @Override
    public Collection<C> create( final Collection<C> entities ) {
        this.getSessionFactory().getCurrentSession().doWork( connection -> {
            for ( C entity : entities ) {
                AbstractCuratableDao.this.create( entity );
            }
        } );
        return entities;
    }

    @Override
    public C create( final C entity ) {
        super.create( entity );
        if ( entity.getCurationDetails() == null ) {
            CurationDetailsDao curationDetailsDao = new CurationDetailsDaoImpl( this.getSessionFactory() );
            entity.setCurationDetails( curationDetailsDao.create() );
        }
        return entity;
    }

    /**
     * Finds an entity by given name.
     *
     * @param name name of the entity to be found.
     * @return entity with given name, or null if such entity does not exist.
     */
    @Override
    public Collection<C> findByName( String name ) {
        return this.findByProperty( "name", name );
    }

    /**
     * Finds an entity by given short name.
     *
     * @param name short name of the entity to be found.
     * @return entity with given short name, or null if such entity does not exist.
     */
    @Override
    public C findByShortName( String name ) {
        return this.findOneByProperty( "shortName", name );
    }

    protected void addEventsToMap( Map<Long, Collection<AuditEvent>> eventMap, Long id, AuditEvent event ) {
        if ( eventMap.containsKey( id ) ) {

            Collection<AuditEvent> events = eventMap.get( id );
            events.add( event );
        } else {
            Collection<AuditEvent> events = new ArrayList<>();
            events.add( event );
            eventMap.put( id, events );
        }
    }

    /**
     * Restrict results to non-troubled EEs for non-administrators
     * @param filters set of {@link ObjectFilter} to which the non-troubled EEs filter will be added.
     */
    @SneakyThrows(ObjectFilterException.class)
    protected void addNonTroubledFilter( List<ObjectFilter[]> filters, String objectAlias ) {
        if ( !SecurityUtil.isUserAdmin() ) {
            filters.add( new ObjectFilter[] { new ObjectFilter( objectAlias, "curationDetails.troubled", Boolean.class, ObjectFilter.Operator.is, "false" ) } );
        }
    }
}
