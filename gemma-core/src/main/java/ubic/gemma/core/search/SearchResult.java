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
package ubic.gemma.core.search;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import ubic.gemma.model.common.Identifiable;

import java.util.Objects;

/**
 * @author paul
 */
@EqualsAndHashCode(of = { "resultClass", "resultId", "resultObject" })
public class SearchResult<T extends Identifiable> implements Comparable<SearchResult<? extends Identifiable>> {

    private final Class<? extends Identifiable> resultClass;

    private final long resultId;

    private T resultObject;

    private double score = 0.0;

    private String highlightedText;

    /**
     * This constructor allows you to create an initialized search result, inferring the result type and identifier from
     * the passed result object.
     */
    public SearchResult( @NonNull T resultObject ) {
        if ( resultObject.getId() == null ) {
            throw new IllegalArgumentException( "Result object id must not be null." );
        }
        this.resultClass = resultObject.getClass();
        this.resultId = resultObject.getId();
        this.resultObject = resultObject;
    }

    /**
     * This constructor allows you to create an uninitialized search result. It must however have a result type and
     * identifier.
     */
    public SearchResult( @NonNull Class<? extends Identifiable> resultClass, @NonNull Long entityId ) {
        this.resultClass = resultClass;
        this.resultId = entityId;
    }

    /**
     * @see SearchResult#SearchResult(Class, Long)
     */
    public SearchResult( @NonNull Class<? extends Identifiable> resultClass, @NonNull Long entityId, double score, String highlightedText ) {
        this( resultClass, entityId );
        this.score = score;
        this.highlightedText = highlightedText;
    }

    /**
     * This constructor allows you to create a transformed search result that preserves the original entity class.
     */
    public SearchResult( @NonNull Class<? extends Identifiable> resultClass, @NonNull T entity ) {
        this( resultClass, entity.getId() );
        this.resultObject = entity;
    }

    /**
     * @see SearchResult#SearchResult(Class, Identifiable)
     */
    public SearchResult( @NonNull Class<? extends Identifiable> resultClass, @NonNull T entity, double score, String highlightedText ) {
        this( resultClass, entity.getId(), score, highlightedText );
        this.resultObject = entity;
    }

    @Override
    public int compareTo( SearchResult<?> o ) {
        return -Double.compare( this.score, o.score );
    }

    public String getHighlightedText() {
        return highlightedText;
    }

    public void setHighlightedText( String highlightedText ) {
        this.highlightedText = highlightedText;
    }

    /**
     * Obtain the result identifier.
     * @return the id for the underlying result entity.
     */
    public Long getResultId() {
        return resultId;
    }

    /**
     * Obtain the result type.
     *
     * This type might differ from the type of {@link #getResultObject()} if the result is uninitialized or has been
     * transformed. This is typically the case when we convert the result to an {@link ubic.gemma.model.IdentifiableValueObject}.
     */
    public Class<? extends Identifiable> getResultClass() {
        return resultClass;
    }

    /**
     * This can be null, at least initially, if the {@link #getResultClass()} and {@link #getResultId()} are provided.
     */
    public T getResultObject() {
        return this.resultObject;
    }

    /**
     * Set the result object this search result is referring to.
     */
    public void setResultObject( @NonNull T resultObject ) {
        if ( !Objects.equals( resultObject.getId(), resultId ) ) {
            throw new IllegalArgumentException( "The result object " + resultObject + " ID does not match this result." );
        }
        this.resultObject = resultObject;
    }

    public double getScore() {
        return score;
    }

    public void setScore( double score ) {
        this.score = score;
    }

    @Override
    public String toString() {
        return resultClass.getSimpleName() + "[ID=" + this.resultId + "] matched in: "
                + ( this.highlightedText != null ? "'" + this.highlightedText + "'" : "(?)" );
    }
}
