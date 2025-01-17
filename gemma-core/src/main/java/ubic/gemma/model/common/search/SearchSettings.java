/*
 * The Gemma project
 *
 * Copyright (c) 2007 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.gemma.model.common.search;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import ubic.gemma.core.search.SearchResult;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;

import java.io.Serializable;
import java.util.Set;

/**
 * Configuration options for searching.
 *
 * @author paul
 */
@Data
@Builder
@With
@ToString(of = { "query", "taxon", "platformConstraint", "resultTypes" })
public class SearchSettings implements Serializable {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -982243911532743661L;

    /**
     * How many results per result type are allowed. This implies that if you search for multiple types of things, you
     * can get more than this.
     */
    static final int DEFAULT_MAX_RESULTS_PER_RESULT_TYPE = 5000;

    /**
     * Convenience method to get pre-configured settings.
     *
     * @param  query query
     * @return search settings
     */
    public static SearchSettings arrayDesignSearch( String query ) {
        return builder().query( query ).resultType( ArrayDesign.class ).build();
    }

    /**
     * Convenience method to get pre-configured settings.
     *
     * @param  query query
     * @return search settings
     */
    public static SearchSettings bibliographicReferenceSearch( String query ) {
        return builder().query( query ).resultType( BibliographicReference.class ).build();
    }

    /**
     * Convenience method to get pre-configured settings.
     *
     * @param  query       query
     * @param  arrayDesign the array design to limit the search to
     * @return search settings
     */
    public static SearchSettings compositeSequenceSearch( String query, ArrayDesign arrayDesign ) {
        return builder().query( query )
                .resultType( CompositeSequence.class )
                .resultType( ArrayDesign.class )
                .platformConstraint( arrayDesign ) // TODO: check if this was specified in the original code
                .build();
    }

    /**
     * Convenience method to get pre-configured settings.
     *
     * @param  query query
     * @return search settings
     */
    public static SearchSettings expressionExperimentSearch( String query ) {
        return builder()
                .query( query )
                .resultType( ExpressionExperiment.class )
                .build();
    }

    /**
     * Convenience method to get pre-configured settings.
     *
     * @param  query query
     * @param taxon if you want to filter by taxon (can be null)
     * @return search settings
     */
    public static SearchSettings expressionExperimentSearch( String query, Taxon taxon ) {
        return builder()
                .query( query )
                .resultType( ExpressionExperiment.class )
                .taxon( taxon )
                .build();
    }

    /**
     * Convenience method to get pre-configured settings.
     *
     * @param  query query
     * @param  taxon the taxon to limit the search to (can be null)
     * @return search settings
     */
    public static SearchSettings geneSearch( String query, Taxon taxon ) {
        return builder().query( query ).resultType( Gene.class ).taxon( taxon ).build();
    }

    /**
     * Processed query for performing a search.
     */
    private String query;

    /**
     * Entities to retrieve.
     */
    @Singular
    private Set<Class<? extends Identifiable>> resultTypes;

    /* optional search constraints */
    private ArrayDesign platformConstraint;
    private Taxon taxon;

    /* sources */
    @Builder.Default
    private Boolean useCharacteristics = Boolean.TRUE;
    @Builder.Default
    private Boolean useDatabase = Boolean.TRUE;
    @Builder.Default
    private Boolean useGo = Boolean.TRUE;
    @Builder.Default
    private Boolean useIndices = Boolean.TRUE;

    /**
     * Highlight part of the search result as per {@link SearchResult#getHighlightedText()}.
     *
     * Overhead can be reduced by disabling highlighting if not needed.
     */
    private boolean doHighlighting;

    @Builder.Default
    private Integer maxResults = SearchSettings.DEFAULT_MAX_RESULTS_PER_RESULT_TYPE;

    /**
     * Get this query, trimmed.
     */
    public String getQuery() {
        return query == null ? null : query.trim();
    }

    /**
     * Get the original query that was set by {@link #setQuery(String)}, untrimmed.
     */
    @SuppressWarnings("unused")
    public String getRawQuery() {
        return this.query;
    }

    /**
     * Indicate if the query refers to an ontology term.
     *
     * This is done by checking if this query starts with 'http://' for now, but there could be fancier checks performed
     * in the future.
     */
    public boolean isTermQuery() {
        return getQuery() != null && getQuery().startsWith( "http://" );
    }

    /**
     * Obtain the term URI.
     *
     * @deprecated use {@link #getQuery()} and {@link #isTermQuery()} instead.
     *
     * @return the term URI if this is a term query, otherwise null
     */
    @Deprecated
    public String getTermUri() {
        return isTermQuery() ? getQuery() : null;
    }

    /**
     * Set this term URI.
     *
     * @deprecated URI can be set with {@link #setQuery(String)} instead.
     *
     * @param termUri a valid term URI, or null or a blank string
     */
    @Deprecated
    public void setTermUri( String termUri ) {
        if ( StringUtils.isNotBlank( termUri ) && !termUri.startsWith( "http://" ) ) {
            throw new IllegalArgumentException( "The term URI must be a valid URI." );
        }
        setQuery( termUri );
    }

    /**
     * Check if this is configured to search a given result type.
     */
    public boolean hasResultType( Class<?> cls ) {
        return resultTypes.contains( cls );
    }
}