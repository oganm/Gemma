package ubic.gemma.web.services.rest.swagger.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ubic.gemma.core.search.SearchService;
import ubic.gemma.web.services.rest.util.args.LimitArg;
import ubic.gemma.web.services.rest.util.args.PlatformArg;
import ubic.gemma.web.services.rest.util.args.TaxonArg;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves allowed values for the {@link ubic.gemma.web.services.rest.SearchWebService#search(String, TaxonArg, PlatformArg, List, LimitArg)}
 * resultTypes argument.
 *
 * This ensures that the OpenAPI specification exposes all supported search result types in the {@link SearchService} as
 * allowable values.
 *
 * @author poirigui
 */
@Component
@CommonsLog
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SearchResultTypeAllowableValuesModelResolver extends ModelResolver {

    private final SearchService searchService;

    @Autowired
    public SearchResultTypeAllowableValuesModelResolver( @Qualifier("swaggerObjectMapper") ObjectMapper objectMapper, SearchService searchService ) {
        super( objectMapper );
        this.searchService = searchService;
    }

    @Override
    protected List<String> resolveAllowableValues( Annotated a, Annotation[] annotations, Schema schema ) {
        // FIXME: use a more stringent way of matching this parameter
        if ( schema != null ) {
            log.info( schema.name() );
            if ( schema.name().equals( "resultTypes" ) ) {
                return searchService.getSupportedResultTypes().stream().map( Class::getName ).collect( Collectors.toList() );
            }
        }
        return super.resolveAllowableValues( a, annotations, schema );
    }
}
