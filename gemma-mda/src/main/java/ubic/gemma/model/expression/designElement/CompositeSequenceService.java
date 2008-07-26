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
// Generated by: SpringService.vsl in andromda-spring-cartridge.
//
package ubic.gemma.model.expression.designElement;

/**
 * 
 */
public interface CompositeSequenceService {

    /**
     * 
     */
    public ubic.gemma.model.expression.designElement.CompositeSequence findOrCreate(
            ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence );

    /**
     * 
     */
    public void remove( ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence );

    /**
     * 
     */
    public ubic.gemma.model.expression.designElement.CompositeSequence find(
            ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence );

    /**
     * 
     */
    public ubic.gemma.model.expression.designElement.CompositeSequence create(
            ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence );

    /**
     * 
     */
    public java.util.Collection create( java.util.Collection compositeSequences );

    /**
     * 
     */
    public java.util.Collection findByName( java.lang.String name );

    /**
     * 
     */
    public ubic.gemma.model.expression.designElement.CompositeSequence findByName(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign, java.lang.String name );

    /**
     * 
     */
    public void remove( java.util.Collection sequencesToDelete );

    /**
     * 
     */
    public java.lang.Integer countAll();

    /**
     * 
     */
    public void update( ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence );

    /**
     * 
     */
    public java.util.Collection findByNamesInArrayDesigns( java.util.Collection compositeSequenceNames,
            java.util.Collection arrayDesigns );

    /**
     * <p>
     * Load all compositeSequences specified by the given ids.
     * </p>
     */
    public java.util.Collection loadMultiple( java.util.Collection ids );

    /**
     * 
     */
    public java.util.Collection getRawSummary(
            ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence, java.lang.Integer numResults );

    /**
     * 
     */
    public java.util.Collection getRawSummary( java.util.Collection compositeSequences, java.lang.Integer numResults );

    /**
     * 
     */
    public java.util.Collection findByBioSequenceName( java.lang.String name );

    /**
     * 
     */
    public java.util.Collection findByBioSequence( ubic.gemma.model.genome.biosequence.BioSequence bioSequence );

    /**
     * 
     */
    public void thaw( java.util.Collection compositeSequences );

    /**
     * 
     */
    public java.util.Collection getRawSummary( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign,
            java.lang.Integer numResults );

    /**
     * 
     */
    public java.util.Collection findByGene( ubic.gemma.model.genome.Gene gene );

    /**
     * 
     */
    public java.util.Collection findByGene( ubic.gemma.model.genome.Gene gene,
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.util.Collection getGenes( ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence );

    /**
     * <p>
     * Given a Collection of composite sequences returns of map of a compositesequence to a collection of genes
     * </p>
     */
    public java.util.Map getGenes( java.util.Collection sequences );

    /**
     * 
     */
    public ubic.gemma.model.expression.designElement.CompositeSequence load( java.lang.Long id );

    /**
     * <p>
     * Returns a map of CompositeSequences to PhysicalLocation to BlatAssociations at each location.
     * </p>
     */
    public java.util.Map getGenesWithSpecificity( java.util.Collection compositeSequences );

}
