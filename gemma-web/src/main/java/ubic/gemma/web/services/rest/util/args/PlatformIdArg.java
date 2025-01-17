package ubic.gemma.web.services.rest.util.args;

import io.swagger.v3.oas.annotations.media.Schema;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;

/**
 * Long argument type for platform API, referencing the platform ID.
 *
 * @author tesarst
 */
@Schema(type = "integer", format = "int64", description = "A platform numerical identifier.")
public class PlatformIdArg extends PlatformArg<Long> {

    /**
     * @param l intentionally primitive type, so the value property can never be null.
     */
    PlatformIdArg( long l ) {
        super( l );
    }

    @Override
    public ArrayDesign getEntity( ArrayDesignService service ) {
        return checkEntity( service.load( this.getValue() ) );
    }

    @Override
    public String getPropertyName() {
        return "id";
    }

}
