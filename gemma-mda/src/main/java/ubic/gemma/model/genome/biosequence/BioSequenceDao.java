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
package ubic.gemma.model.genome.biosequence;

import java.util.Collection;

import ubic.gemma.model.genome.Gene;
import ubic.gemma.persistence.BaseDao;

/**
 * @see ubic.gemma.model.genome.biosequence.BioSequence
 */
public interface BioSequenceDao extends BaseDao<BioSequence> {
    /**
     * 
     */
    public java.lang.Integer countAll();

    /**
     * 
     */
    public BioSequence find( BioSequence bioSequence );

    /**
     * 
     */
    public BioSequence findByAccession( ubic.gemma.model.common.description.DatabaseEntry accession );

    /**
     * <p>
     * Returns matching biosequences for the given genes in a Map (gene to biosequences). Genes which had no associated
     * sequences are not included in the result.
     * </p>
     */
    public java.util.Map<Gene, Collection<BioSequence>> findByGenes( java.util.Collection<Gene> genes );

    /**
     * 
     */
    public java.util.Collection<BioSequence> findByName( java.lang.String name );

    /**
     * 
     */
    public BioSequence findOrCreate( BioSequence bioSequence );

    /**
     * 
     */
    public java.util.Collection<Gene> getGenesByAccession( java.lang.String search );

    /**
     * For a biosequence name, get the genes
     */
    public java.util.Collection<Gene> getGenesByName( java.lang.String search );

    public Collection<BioSequence> load( Collection<Long> ids );

    /**
     * 
     */
    public Collection<BioSequence> thaw( java.util.Collection<BioSequence> bioSequences );

    /**
     * 
     */
    public BioSequence thaw( BioSequence bioSequence );

}
