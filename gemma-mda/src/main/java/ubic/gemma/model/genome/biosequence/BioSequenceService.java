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
package ubic.gemma.model.genome.biosequence;

/**
 * 
 */
public interface BioSequenceService {

    /**
     * 
     */
    public ubic.gemma.model.genome.biosequence.BioSequence find(
            ubic.gemma.model.genome.biosequence.BioSequence bioSequence );

    /**
     * 
     */
    public void remove( ubic.gemma.model.genome.biosequence.BioSequence bioSequence );

    /**
     * 
     */
    public ubic.gemma.model.genome.biosequence.BioSequence findOrCreate(
            ubic.gemma.model.genome.biosequence.BioSequence bioSequence );

    /**
     * 
     */
    public void update( ubic.gemma.model.genome.biosequence.BioSequence bioSequence );

    /**
     * 
     */
    public ubic.gemma.model.genome.biosequence.BioSequence create(
            ubic.gemma.model.genome.biosequence.BioSequence bioSequence );

    /**
     * 
     */
    public java.util.Collection create( java.util.Collection bioSequences );

    /**
     * 
     */
    public java.util.Collection findOrCreate( java.util.Collection bioSequences );

    /**
     * 
     */
    public void update( java.util.Collection bioSequences );

    /**
     * 
     */
    public java.util.Collection getGenesByName( java.lang.String search );

    /**
     * 
     */
    public java.util.Collection getGenesByAccession( java.lang.String search );

    /**
     * 
     */
    public ubic.gemma.model.genome.biosequence.BioSequence load( long id );

    /**
     * 
     */
    public java.lang.Integer countAll();

    /**
     * <p>
     * Retrieve all biosequences with names matching the given string. This matches only the name field, not the
     * accession.
     * </p>
     */
    public java.util.Collection findByName( java.lang.String name );

    /**
     * 
     */
    public ubic.gemma.model.genome.biosequence.BioSequence findByAccession(
            ubic.gemma.model.common.description.DatabaseEntry accession );

    /**
     * <p>
     * loads all biosequences specified by the provided ids.
     * </p>
     */
    public java.util.Collection loadMultiple( java.util.Collection ids );

    /**
     * 
     */
    public void thaw( java.util.Collection bioSequences );

    /**
     * 
     */
    public void thaw( ubic.gemma.model.genome.biosequence.BioSequence bioSequence );

    /**
     * <p>
     * Thaw but do not initialize as many associations as regular thaw.
     * </p>
     */
    public void thawLite( java.util.Collection bioSequences );

    /**
     * <p>
     * Returns matching biosequences for the given genes in a Map (gene to biosequences). Genes which had no associated
     * sequences are not included in the result.
     * </p>
     */
    public java.util.Map findByGenes( java.util.Collection genes );

}
