package ubic.gemma.web.services.rest.util.args;

import com.google.common.base.Strings;
import org.apache.commons.lang3.NotImplementedException;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.persistence.service.FilteringService;
import ubic.gemma.persistence.util.ObjectFilter;
import ubic.gemma.web.services.rest.util.MalformedArgException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Array of identifiers of an Identifiable entity
 * @param <A> the type of the value used to retrieve the entity, which is typically a {@link String}
 * @param <O> the type of the resulting entity
 * @param <S> the type of the filtering service providing the entity
 */
public abstract class AbstractEntityArrayArg<A, O extends Identifiable, S extends FilteringService<O>> extends AbstractArrayArg<A> {

    private final Class<? extends AbstractEntityArg> entityArgClass;
    private String argValueName = null;

    protected AbstractEntityArrayArg( Class<? extends AbstractEntityArg> entityArgClass, List<A> values ) {
        super( values );
        this.entityArgClass = entityArgClass;
    }

    /**
     * Obtain an {@link ObjectFilter} disjunction clause for this entity.
     *
     * By applying this to a query, only the entities defined in this argument will be retrieved.
     *
     * By default, this is constructing a single,
     *
     * @param service a service which provide the
     * @return
     */
    public ObjectFilter[] getObjectFilters( S service ) throws MalformedArgException {
        try {
            //noinspection unchecked
            return new ObjectFilter[] { service.getObjectFilter( this.getPropertyName( service ), ObjectFilter.Operator.in, ( Collection<String> ) this.getValue() ) };
        } catch ( ClassCastException e ) {
            throw new NotImplementedException( "Filtering with non-string values is not supported.", e );
        }
    }

    /**
     * Retrieves the persistent objects for all the identifiers in this array arg.
     * Note that if any of the values in the array do not map to an object (i.e. an object with such identifier does not
     * exist),
     * a 404 error will be thrown.
     *
     * @param  service the service that will be used to retrieve the persistent objects.
     * @return a collection of persistent objects matching the identifiers on this array arg.
     */
    public Collection<O> getEntities( S service ) throws NotFoundException {
        Collection<O> objects = new ArrayList<>( this.getValue().size() );
        for ( A s : this.getValue() ) {
            AbstractEntityArg<?, O, S> arg;
            if ( s instanceof String ) {
                arg = this.entityArgValueOf( ( String ) s );
            } else {
                throw new NotImplementedException( "Obtaining entities is only supported for string values." );
            }
            objects.add( arg.checkEntity( arg.getEntity( service ) ) );
        }
        return objects;
    }

    /**
     * Reads the given MutableArgs property name and checks whether it is null or empty.
     *
     * @param  arg     the MutableArg to retrieve the property name from.
     * @param  value   one of the values of the property that has been passed into this array arg.
     * @param  service service that may be used to retrieve the property from the MutableArg.
     * @param          <T> type of the given MutableArg.
     * @return the name of the property that the values in this arrayArg refer to.
     */
    private <T extends AbstractEntityArg<?, O, S>> String checkPropertyNameString( T arg, A value, S service ) {
        String identifier = arg.getPropertyName();
        if ( Strings.isNullOrEmpty( identifier ) ) {
            throw new BadRequestException( "Identifier " + value + " not recognized." );
        }
        return identifier;
    }

    /**
     * Guess the property name for this array of entities by testing the valueOf of the first entity.
     *
     * If no entity is specified, defaults on 'id'.
     *
     * This routine only works if the type of this array is {@link String}.
     *
     * @param  service the service used to guess the type and name of the property that this arrayEntityArg represents.
     * @return the name of the property that the values in this array represent.
     */
    protected String getPropertyName( S service ) {
        if ( this.argValueName == null ) {
            Optional<A> value = this.getValue().stream().findFirst();
            if ( value.isPresent() ) {
                AbstractEntityArg<?, O, S> arg;
                if ( value.get() instanceof String ) {
                    arg = this.entityArgValueOf( ( String ) value.get() );
                } else {
                    throw new NotImplementedException( "Inferring the property name is only supported for string values." );
                }
                this.argValueName = this.checkPropertyNameString( arg, value.get(), service );
            } else {
                /* assumed since {@link O} is identifiable */
                this.argValueName = "id";
            }
        }
        return this.argValueName;
    }

    /**
     * Call the valueOf method of the entity arg that consititute the elements of this array.
     */
    private AbstractEntityArg<?, O, S> entityArgValueOf( String s ) {
        try {
            // noinspection unchecked // Could not avoid using reflection, because java does not allow abstract static methods.
            return ( AbstractEntityArg<?, O, S> ) entityArgClass.getMethod( "valueOf", String.class ).invoke( null, s );
        } catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            throw new NotImplementedException( "Could not call 'valueOf' for " + entityArgClass.getName(), e );
        }
    }

}
