package ubic.gemma.web.services.rest.util.args;

import com.google.common.base.Strings;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.persistence.service.FilteringService;
import ubic.gemma.persistence.service.ObjectFilterException;
import ubic.gemma.persistence.util.ObjectFilter;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Array of identifiers of an Identifiable entity
 */
public abstract class AbstractEntityArrayArg<O extends Identifiable, S extends FilteringService<O>> extends AbstractArrayArg<String> {

    private static Log log = LogFactory.getLog( AbstractEntityArrayArg.class.getClass() );

    protected String argValueName = null;

    AbstractEntityArrayArg( List<String> values ) {
        super( values );
    }

    AbstractEntityArrayArg( String errorMessage, Exception exception ) {
        super( errorMessage, exception );
    }

    /**
     * Obtain the class for the argument used to wrap individual entities.
     */
    protected abstract Class<? extends AbstractEntityArg> getEntityArgClass();

    /**
     * Combines the given filters with the properties in this array to create a final filter to be used for VO
     * retrieval.
     * Note that this does not check whether objects with identifiers in this array arg do actually exist. This merely
     * creates
     * a set of filters that should be used to impose restrictions in the database query.
     * You can call this#getPersistentObjects which does try to retrieve the corresponding objects, and consequently
     * does yield a 404 error if an object for any of the identifiers in this array arg does not exist.
     *
     * @param  service the service used to guess the type and name of the property that this arrayEntityArg represents.
     * @param  filters the filters list to add the new filter to. Can be null.
     * @return the same array list as given, with a new added element, or a new ArrayList, in case the given
     *                 filters
     *                 was null.
     */
    public List<ObjectFilter[]> combineFilters( List<ObjectFilter[]> filters, S service ) {
        if ( filters == null ) {
            filters = new ArrayList<>();
        }

        try {
            filters.add( new ObjectFilter[] { service.getObjectFilter( this.getPropertyName( service ), ObjectFilter.Operator.in, this.getValue() ) } );
        } catch ( ObjectFilterException e ) {
            throw new RuntimeException( e.getMessage(), e );
        }

        return filters;
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
        for ( String s : this.getValue() ) {
            AbstractEntityArg<?, O, S> arg;
            arg = this.entityArgValueOf( s );
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
    protected <T extends AbstractEntityArg<?, O, S>> String checkPropertyNameString( T arg, String value, S service ) {
        String identifier = arg.getPropertyName();
        if ( Strings.isNullOrEmpty( identifier ) ) {
            throw new BadRequestException( "Identifier " + value + " not recognized." );
        }
        return identifier;
    }

    /**
     * @param  service the service used to guess the type and name of the property that this arrayEntityArg represents.
     * @return the name of the property that the values in this array represent.
     */
    protected String getPropertyName( S service ) {
        if ( this.argValueName == null ) {
            Optional<String> value = this.getValue().stream().findFirst();
            if ( value.isPresent() ) {
                AbstractEntityArg<?, O, S> arg = this.entityArgValueOf( value.get() );
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
            return ( AbstractEntityArg<?, O, S> ) getEntityArgClass().getMethod( "valueOf", String.class ).invoke( null, s );
        } catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            throw new NotImplementedException( "Could not call 'valueOf' for " + getEntityArgClass().getName(), e );
        }
    }

}
