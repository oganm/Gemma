package ubic.gemma.web.services.rest.util.args;

import io.swagger.v3.oas.annotations.media.Schema;
import ubic.gemma.web.services.rest.util.MalformedArgException;

/**
 * Class representing an API argument that should be a long.
 *
 * @author tesarst
 */
@Schema(type = "integer", format = "int64")
public class LongArg extends AbstractArg<Long> {

    private LongArg( long value ) {
        super( value );
    }

    /**
     * Used by RS to parse value of request parameters.
     *
     * @param s the request long number argument
     * @return an instance of LongArg representing long number value of the input string, or a malformed LongArg that
     * will throw a {@link javax.ws.rs.BadRequestException} when accessing its value, if the input String can not be
     * converted into a long.
     */
    @SuppressWarnings("unused")
    public static LongArg valueOf( final String s ) throws MalformedArgException {
        try {
            return new LongArg( Long.parseLong( s ) );
        } catch ( NumberFormatException e ) {
            throw new MalformedArgException( String.format( "Value '%s' can not converted to a long number", s ), e );
        }
    }

}
