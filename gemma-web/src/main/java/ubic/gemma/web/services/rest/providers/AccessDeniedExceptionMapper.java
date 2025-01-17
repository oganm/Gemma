package ubic.gemma.web.services.rest.providers;

import org.springframework.security.access.AccessDeniedException;
import ubic.gemma.web.services.rest.util.OpenApiUtils;
import ubic.gemma.web.services.rest.util.ResponseErrorObject;
import ubic.gemma.web.services.rest.util.ServletUtils;
import ubic.gemma.web.services.rest.util.WellComposedErrorBody;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Map Spring Security's {@link AccessDeniedException} to a 403 Forbidden response.
 */
@Provider
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {

    @Context
    private ServletConfig servletConfig;

    @Override
    public Response toResponse( AccessDeniedException e ) {
        // for security reasons, we don't include the error object in the response entity
        WellComposedErrorBody errorBody = new WellComposedErrorBody( Response.Status.FORBIDDEN, e.getMessage() );
        return Response.status( Response.Status.FORBIDDEN )
                .type( MediaType.APPLICATION_JSON_TYPE )
                .entity( new ResponseErrorObject( errorBody, OpenApiUtils.getOpenApi( servletConfig ) ) )
                .build();
    }
}
