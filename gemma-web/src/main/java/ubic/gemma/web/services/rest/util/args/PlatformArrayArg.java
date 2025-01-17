package ubic.gemma.web.services.rest.util.args;

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.web.services.rest.util.MalformedArgException;
import ubic.gemma.web.services.rest.util.StringUtils;

import java.util.List;

@ArraySchema(schema = @Schema(implementation = PlatformArg.class))
public class PlatformArrayArg extends AbstractEntityArrayArg<String, ArrayDesign, ArrayDesignService> {
    private static final String ERROR_MSG_DETAIL = "Provide a string that contains at least one ID or short name, or multiple, separated by (',') character. All identifiers must be same type, i.e. do not combine IDs and short names.";
    private static final String ERROR_MSG = AbstractArrayArg.ERROR_MSG + " Platform identifiers";

    private PlatformArrayArg( List<String> values ) {
        super( PlatformArg.class, values );
    }

    /**
     * Used by RS to parse value of request parameters.
     *
     * @param s the request arrayPlatform argument
     * @return an instance of ArrayPlatformArg representing an array of Platform identifiers from the input string, or a
     * malformed ArrayPlatformArg that will throw an {@link javax.ws.rs.BadRequestException} when accessing its value,
     * if the input String can not be converted into an array of Platform identifiers.
     */
    @SuppressWarnings("unused")
    public static PlatformArrayArg valueOf( final String s ) throws MalformedArgException {
        if ( Strings.isNullOrEmpty( s ) ) {
            throw new MalformedArgException( String.format( PlatformArrayArg.ERROR_MSG, s ),
                    new IllegalArgumentException( PlatformArrayArg.ERROR_MSG_DETAIL ) );
        }
        return new PlatformArrayArg( StringUtils.splitAndTrim( s ) );
    }

}
