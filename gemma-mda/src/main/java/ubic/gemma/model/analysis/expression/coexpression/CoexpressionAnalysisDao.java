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
//
// Attention: Generated code! Do not modify by hand!
// Generated by: SpringDao.vsl in andromda-spring-cartridge.
//
package ubic.gemma.model.analysis.expression.coexpression;

/**
 * @see ubic.gemma.model.analysis.expression.coexpression.CoexpressionAnalysis
 */
public interface CoexpressionAnalysisDao extends ubic.gemma.model.analysis.expression.ExpressionAnalysisDao {
    /**
     * Loads an instance of ubic.gemma.model.analysis.expression.coexpression.CoexpressionAnalysis from the persistent
     * store.
     */
    public ubic.gemma.model.common.Securable load( java.lang.Long id );

    /**
     * <p>
     * Does the same thing as {@link #load(java.lang.Long)} with an additional flag called <code>transform</code>. If
     * this flag is set to <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be
     * transformed. If this flag is any of the other constants defined in this class then the result <strong>WILL BE</strong>
     * passed through an operation which can optionally transform the entity (into a value object for example). By
     * default, transformation does not occur.
     * </p>
     * 
     * @param id the identifier of the entity to load.
     * @return either the entity or the object transformed from the entity.
     */
    public Object load( int transform, java.lang.Long id );

    /**
     * Loads all entities of type {@link ubic.gemma.model.analysis.expression.coexpression.CoexpressionAnalysis}.
     * 
     * @return the loaded entities.
     */
    public java.util.Collection loadAll();

    /**
     * <p>
     * Does the same thing as {@link #loadAll()} with an additional flag called <code>transform</code>. If this flag
     * is set to <code>TRANSFORM_NONE</code> then the returned entity will <strong>NOT</strong> be transformed. If
     * this flag is any of the other constants defined here then the result <strong>WILL BE</strong> passed through an
     * operation which can optionally transform the entity (into a value object for example). By default, transformation
     * does not occur.
     * </p>
     * 
     * @param transform the flag indicating what transformation to use.
     * @return the loaded entities.
     */
    public java.util.Collection loadAll( final int transform );

    /**
     * Updates the <code>coexpressionAnalysis</code> instance in the persistent store.
     */
    public void update( ubic.gemma.model.analysis.expression.coexpression.CoexpressionAnalysis coexpressionAnalysis );

    /**
     * Updates all instances in the <code>entities</code> collection in the persistent store.
     */
    public void update( java.util.Collection entities );

    /**
     * Removes the instance of ubic.gemma.model.analysis.expression.coexpression.CoexpressionAnalysis from the
     * persistent store.
     */
    public void remove( ubic.gemma.model.analysis.expression.coexpression.CoexpressionAnalysis coexpressionAnalysis );

    /**
     * Removes the instance of ubic.gemma.model.analysis.expression.coexpression.CoexpressionAnalysis having the given
     * <code>identifier</code> from the persistent store.
     */
    public void remove( java.lang.Long id );

    /**
     * Removes all entities in the given <code>entities<code> collection.
     */
    public void remove( java.util.Collection entities );

}
