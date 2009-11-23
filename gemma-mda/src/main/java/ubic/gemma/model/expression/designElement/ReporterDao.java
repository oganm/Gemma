/*
 * The Gemma project.
 * 
 * Copyright (c) 2006-2007 University of British Columbia
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
package ubic.gemma.model.expression.designElement;


/**
 * @see ubic.gemma.model.expression.designElement.Reporter
 */
public interface ReporterDao extends ubic.gemma.model.expression.designElement.DesignElementDao<Reporter> {
    /**
     * <p>
     * Does the same thing as {@link #find(boolean, ubic.gemma.model.expression.designElement.Reporter)} with an
     * additional argument called <code>queryString</code>. This <code>queryString</code> argument allows you to
     * override the query string defined in {@link #find(int, ubic.gemma.model.expression.designElement.Reporter
     * reporter)}.
     * </p>
     */
    public Object find( int transform, String queryString, ubic.gemma.model.expression.designElement.Reporter reporter );

    /**
     * <p>
     * Does the same thing as {@link #find(ubic.gemma.model.expression.designElement.Reporter)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder results will
     * <strong>NOT</strong> be transformed during retrieval. If this flag is any of the other constants defined here
     * then finder results <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public Object find( int transform, ubic.gemma.model.expression.designElement.Reporter reporter );

    /**
     * <p>
     * Does the same thing as {@link #find(ubic.gemma.model.expression.designElement.Reporter)} with an additional
     * argument called <code>queryString</code>. This <code>queryString</code> argument allows you to override the query
     * string defined in {@link #find(ubic.gemma.model.expression.designElement.Reporter)}.
     * </p>
     */
    public ubic.gemma.model.expression.designElement.Reporter find( String queryString,
            ubic.gemma.model.expression.designElement.Reporter reporter );

    /**
     * 
     */
    public ubic.gemma.model.expression.designElement.Reporter find(
            ubic.gemma.model.expression.designElement.Reporter reporter );

    /**
     * <p>
     * Does the same thing as {@link #findOrCreate(boolean, ubic.gemma.model.expression.designElement.Reporter)} with an
     * additional argument called <code>queryString</code>. This <code>queryString</code> argument allows you to
     * override the query string defined in {@link #findOrCreate(int, ubic.gemma.model.expression.designElement.Reporter
     * reporter)}.
     * </p>
     */
    public Object findOrCreate( int transform, String queryString,
            ubic.gemma.model.expression.designElement.Reporter reporter );

    /**
     * <p>
     * Does the same thing as {@link #findOrCreate(ubic.gemma.model.expression.designElement.Reporter)} with an
     * additional flag called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder
     * results will <strong>NOT</strong> be transformed during retrieval. If this flag is any of the other constants
     * defined here then finder results <strong>WILL BE</strong> passed through an operation which can optionally
     * transform the entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public Object findOrCreate( int transform, ubic.gemma.model.expression.designElement.Reporter reporter );

    /**
     * <p>
     * Does the same thing as {@link #findOrCreate(ubic.gemma.model.expression.designElement.Reporter)} with an
     * additional argument called <code>queryString</code>. This <code>queryString</code> argument allows you to
     * override the query string defined in {@link #findOrCreate(ubic.gemma.model.expression.designElement.Reporter)}.
     * </p>
     */
    public ubic.gemma.model.expression.designElement.Reporter findOrCreate( String queryString,
            ubic.gemma.model.expression.designElement.Reporter reporter );

    /**
     * 
     */
    public ubic.gemma.model.expression.designElement.Reporter findOrCreate(
            ubic.gemma.model.expression.designElement.Reporter reporter );

}
