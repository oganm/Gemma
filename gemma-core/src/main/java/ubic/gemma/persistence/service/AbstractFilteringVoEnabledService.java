package ubic.gemma.persistence.service;

import org.springframework.transaction.annotation.Transactional;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.persistence.util.*;

import java.util.Collection;
import java.util.List;

/**
 * Base implementation for {@link FilteringVoEnabledService}.
 */
public abstract class AbstractFilteringVoEnabledService<O extends Identifiable, VO extends IdentifiableValueObject<O>>
        extends AbstractVoEnabledService<O, VO> implements FilteringVoEnabledService<O, VO> {

    private final FilteringVoEnabledDao<O, VO> voDao;

    protected AbstractFilteringVoEnabledService( FilteringVoEnabledDao<O, VO> voDao ) {
        super( voDao );
        this.voDao = voDao;
    }

    @Override
    public ObjectFilter getObjectFilter( String property, ObjectFilter.Operator operator, String value ) {
        try {
            return ObjectFilter.parseObjectFilter( getPropertyAlias( property ), getPropertyName( property ), getPropertyType( property ), operator, value );
        } catch ( NoSuchFieldException e ) {
            throw new IllegalArgumentException( "Could not create object filter for " + property + " on " + voDao.getElementClass().getName() + ".", e );
        }
    }

    @Override
    public ObjectFilter getObjectFilter( String property, ObjectFilter.Operator operator, Collection<String> values ) {
        try {
            return ObjectFilter.parseObjectFilter( getPropertyAlias( property ), getPropertyName( property ), getPropertyType( property ), operator, values );
        } catch ( NoSuchFieldException e ) {
            throw new IllegalArgumentException( "Could not create object filter for " + property + " on " + voDao.getElementClass().getName() + ".", e );
        }
    }

    @Override
    public Sort getSort( String property, Sort.Direction direction ) {
        // this only serves as a pre-condition to ensure that the propertyName exists
        try {
            return Sort.by( getPropertyAlias( property ), getPropertyName( property ), direction );
        } catch ( NoSuchFieldException e ) {
            throw new IllegalArgumentException( "Could not resolve propertyName " + property + " on " + voDao.getElementClass().getName() + ".", e );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<VO> loadValueObjectsPreFilter( Filters filters, Sort sort, int offset, int limit ) {
        return voDao.loadValueObjectsPreFilter( filters, sort, offset, limit );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VO> loadValueObjectsPreFilter( Filters filters, Sort sort ) {
        return voDao.loadValueObjectsPreFilter( filters, sort );
    }

    /**
     * Obtain the alias that refers to the entity.
     *
     * Defaults to {@link FilteringVoEnabledDao#getElementClass()}.
     *
     * @throws NoSuchFieldException if no such propertyName exists in {@link O}
     */
    protected String getPropertyAlias( String propertyName ) throws NoSuchFieldException {
        EntityUtils.getDeclaredField( voDao.getElementClass(), propertyName );
        return voDao.getObjectAlias();
    }

    /**
     * Obtain the propertyName on {@link O} that correspond to the passed propertyName name.
     *
     * Defaults to the propertyName name itself.
     *
     * @throws NoSuchFieldException if no such propertyName exists in {@link O}
     */
    protected String getPropertyName( String propertyName ) throws NoSuchFieldException {
        EntityUtils.getDeclaredField( voDao.getElementClass(), propertyName );
        return propertyName;
    }

    /**
     * Obtain the propertyName type on {@link O} that correspond to the passed propertyName name.
     *
     * Defaults to {@link EntityUtils#getDeclaredFieldType(String, Class)} invoked with {@link #getPropertyName(String)}
     * and {@link FilteringVoEnabledDao#getElementClass()}.
     *
     * @throws NoSuchFieldException if no such propertyName exists in {@link O}
     */
    protected Class<?> getPropertyType( String propertyName ) throws NoSuchFieldException {
        return EntityUtils.getDeclaredFieldType( getPropertyName( propertyName ), voDao.getElementClass() );
    }
}
