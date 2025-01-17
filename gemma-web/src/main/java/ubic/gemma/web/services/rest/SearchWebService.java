package ubic.gemma.web.services.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ubic.gemma.core.search.SearchException;
import ubic.gemma.core.search.SearchResult;
import ubic.gemma.core.search.SearchService;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.model.common.search.SearchSettings;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;
import ubic.gemma.model.genome.TaxonValueObject;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;
import ubic.gemma.web.services.rest.swagger.resolvers.SearchResultTypeAllowableValuesModelResolver;
import ubic.gemma.web.services.rest.util.ResponseDataObject;
import ubic.gemma.web.services.rest.util.args.LimitArg;
import ubic.gemma.web.services.rest.util.args.PlatformArg;
import ubic.gemma.web.services.rest.util.args.TaxonArg;

import javax.ws.rs.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Provides search capabilities to the RESTful API.
 *
 * @author poirigui
 */
@Service
@Path("/search")
@CommonsLog
public class SearchWebService {

    /**
     * Name used in the OpenAPI schema to identify result types as per {@link #search(String, TaxonArg, PlatformArg, List, LimitArg)}'s
     * fourth argument.
     */
    public static final String RESULT_TYPES_SCHEMA_NAME = "SearchResultType";

    @Autowired
    private SearchService searchService;
    @Autowired
    private TaxonService taxonService;
    @Autowired
    private ArrayDesignService arrayDesignService;

    /**
     * Search everything subject to taxon and platform constraints.
     *
     * Naming the schema in for the result types is necessary so that it can be resolved in {@link SearchResultTypeAllowableValuesModelResolver}.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search everything in Gemma.")
    public SearchResultsResponseDataObject search( @QueryParam("query") String query,
            @QueryParam("taxon") TaxonArg<?> taxonArg,
            @QueryParam("platform") PlatformArg<?> platformArg,
            @Parameter(array = @ArraySchema(schema = @Schema(name = RESULT_TYPES_SCHEMA_NAME, hidden = true))) @QueryParam("resultTypes") List<String> resultTypes,
            @QueryParam("limit") @DefaultValue("20") LimitArg limit ) {
        if ( StringUtils.isBlank( query ) ) {
            throw new BadRequestException( "A non-empty query must be supplied." );
        }
        Map<String, Class<? extends Identifiable>> supportedResultTypesByName = searchService.getSupportedResultTypes().stream()
                .collect( Collectors.toMap( Class::getName, identity() ) );
        Collection<Class<? extends Identifiable>> resultTypesCls;
        if ( resultTypes == null || resultTypes.isEmpty() ) {
            // include everything
            resultTypesCls = supportedResultTypesByName.values();
        } else if ( supportedResultTypesByName.keySet().containsAll( resultTypes ) ) {
            // only include what the user asks for
            resultTypesCls = resultTypes.stream().map( supportedResultTypesByName::get ).collect( Collectors.toSet() );
        } else {
            throw new BadRequestException( String.format( "Unsupported result type(s). Ensure that your results are among: %s.",
                    String.join( ", ", supportedResultTypesByName.keySet() ) ) );
        }

        SearchSettings searchSettings = SearchSettings.builder()
                .query( query )
                .taxon( taxonArg != null ? taxonArg.getEntity( taxonService ) : null )
                .platformConstraint( platformArg != null ? platformArg.getEntity( arrayDesignService ) : null )
                .resultTypes( resultTypesCls )
                .maxResults( limit.getValue( 100 ) )
                .build();

        List<SearchResult<? extends Identifiable>> searchResults;
        try {
            searchResults = searchService.search( searchSettings ).values().stream()
                    .flatMap( List::stream )
                    .collect( Collectors.toList() );
        } catch ( SearchException e ) {
            throw new BadRequestException( "Invalid search settings: " + searchSettings + ".", e );
        }

        // convert the response to search results of VOs
        return new SearchResultsResponseDataObject( searchResults.stream()
                .map( searchService::loadValueObject )
                .sorted() // SearchResults are sorted by descending score order
                .limit( limit.getValue( 100 ) ) // results are limited by class, so there might be more results than expected when unraveling everything
                .map( SearchResultValueObject::new )
                .collect( Collectors.toList() ), new SearchSettingsValueObject( searchSettings ) );
    }

    /**
     * Represents search settings for the RESTful API.
     *
     * Note that we will only expose back what the {@link SearchWebService} accepts to take as parameters for searching.
     */
    @Data
    public class SearchSettingsValueObject {

        private final String query;
        private final Set<String> resultTypes;

        /* constraints */
        private final TaxonValueObject taxon;
        private final ArrayDesignValueObject platform;

        private final Integer maxResults;

        public SearchSettingsValueObject( SearchSettings searchSettings ) {
            this.query = searchSettings.getQuery();
            this.resultTypes = searchSettings.getResultTypes().stream().map( Class::getName ).collect( Collectors.toSet() );
            this.taxon = taxonService.loadValueObject( searchSettings.getTaxon() );
            this.platform = arrayDesignService.loadValueObject( searchSettings.getPlatformConstraint() );
            this.maxResults = searchSettings.getMaxResults();
        }
    }

    /**
     * Representation of {@link SearchResult} for the RESTful API.
     */
    @Data
    public static class SearchResultValueObject<T extends IdentifiableValueObject<? extends Identifiable>> {

        private final Long resultId;

        private final String resultType;

        private final Double score;

        private final T resultObject;

        public SearchResultValueObject( SearchResult<T> searchResult ) {
            this.resultId = searchResult.getResultId();
            this.resultType = searchResult.getResultClass().getName();
            this.resultObject = searchResult.getResultObject();
            this.score = searchResult.getScore();
        }
    }

    public static class SearchResultsResponseDataObject extends ResponseDataObject<List<SearchResultValueObject>> {

        private final SearchSettingsValueObject searchSettings;

        /**
         * @param payload the data to be serialised and returned as the response payload.
         */
        public SearchResultsResponseDataObject( List<SearchResultValueObject> payload, SearchSettingsValueObject searchSettings ) {
            super( payload );
            this.searchSettings = searchSettings;
        }

        public SearchSettingsValueObject getSearchSettings() {
            return searchSettings;
        }
    }
}
