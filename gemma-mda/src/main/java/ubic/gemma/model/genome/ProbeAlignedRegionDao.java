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
package ubic.gemma.model.genome;

import ubic.gemma.persistence.BaseDao;

/**
 * @see ubic.gemma.model.genome.ProbeAlignedRegion
 */
public interface ProbeAlignedRegionDao extends BaseDao<ProbeAlignedRegion> {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into value objects or
     * other types, different methods in a class implementing this interface support this feature: look for an
     * <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes entities must be transformed into objects of type
     * {@link ubic.gemma.model.genome.gene.GeneValueObject}.
     */
    public final static int TRANSFORM_GENEVALUEOBJECT = 1;

    /**
     * <p>
     * Does the same thing as {@link #find(boolean, ubic.gemma.model.genome.sequenceAnalysis.BlatResult)} with an
     * additional argument called <code>queryString</code>. This <code>queryString</code> argument allows you to
     * override the query string defined in {@link #find(int, ubic.gemma.model.genome.sequenceAnalysis.BlatResult
     * blatResult)}.
     * </p>
     */
    public java.util.Collection<ProbeAlignedRegion> find( int transform, String queryString,
            ubic.gemma.model.genome.sequenceAnalysis.BlatResult blatResult );

    /**
     * <p>
     * Does the same thing as {@link #find(ubic.gemma.model.genome.sequenceAnalysis.BlatResult)} with an additional flag
     * called <code>transform</code>. If this flag is set to <code>TRANSFORM_NONE</code> then finder results will
     * <strong>NOT</strong> be transformed during retrieval. If this flag is any of the other constants defined here
     * then finder results <strong>WILL BE</strong> passed through an operation which can optionally transform the
     * entities (into value objects for example). By default, transformation does not occur.
     * </p>
     */
    public java.util.Collection<ProbeAlignedRegion> find( int transform,
            ubic.gemma.model.genome.sequenceAnalysis.BlatResult blatResult );

    /**
     * <p>
     * Does the same thing as {@link #find(ubic.gemma.model.genome.sequenceAnalysis.BlatResult)} with an additional
     * argument called <code>queryString</code>. This <code>queryString</code> argument allows you to override the query
     * string defined in {@link #find(ubic.gemma.model.genome.sequenceAnalysis.BlatResult)}.
     * </p>
     */
    public java.util.Collection<ProbeAlignedRegion> find( String queryString,
            ubic.gemma.model.genome.sequenceAnalysis.BlatResult blatResult );

    /**
     * 
     */
    public java.util.Collection<ProbeAlignedRegion> find( ubic.gemma.model.genome.sequenceAnalysis.BlatResult blatResult );

    /**
     * <p>
     * Find chromosome features that fall within the physical location.
     * </p>
     */
    public java.util.Collection<ProbeAlignedRegion> findByPhysicalLocation(
            ubic.gemma.model.genome.PhysicalLocation location );

    /**
     * Copies the fields of {@link ubic.gemma.model.genome.gene.GeneValueObject} to the specified entity.
     * 
     * @param copyIfNull If FALSE, the value object's field will not be copied to the entity if the value is NULL. If
     *        TRUE, it will be copied regardless of its value.
     */
    public void geneValueObjectToEntity( ubic.gemma.model.genome.gene.GeneValueObject sourceVO,
            ubic.gemma.model.genome.ProbeAlignedRegion targetEntity, boolean copyIfNull );

    /**
     * Converts a Collection of instances of type {@link ubic.gemma.model.genome.gene.GeneValueObject} to this DAO's
     * entity.
     */
    public void geneValueObjectToEntityCollection( java.util.Collection<? extends ProbeAlignedRegion> instances );

    public void thaw( ProbeAlignedRegion par );
}
