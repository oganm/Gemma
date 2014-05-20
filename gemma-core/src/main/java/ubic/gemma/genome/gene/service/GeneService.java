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
package ubic.gemma.genome.gene.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.access.annotation.Secured;

import ubic.gemma.model.genome.gene.GeneValueObject;
import ubic.gemma.model.common.description.AnnotationValueObject;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.PhysicalLocation;
import ubic.gemma.model.genome.RelativeLocationData;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.GeneProductValueObject;

/**
 * @author kelsey
 * @version $Id$
 */
public interface GeneService {

    /**
     * @return
     */
    public Integer countAll();

    /**
     * @param genes
     * @return
     */
    @Secured({ "GROUP_ADMIN" })
    public Collection<Gene> create( Collection<Gene> genes );

    /**
     * @param gene
     * @return
     */
    @Secured({ "GROUP_ADMIN" })
    public Gene create( Gene gene );

    /**
     * @param gene
     * @return
     */
    public Gene find( Gene gene );

    /**
     * Find all genes at a physical location. All overlapping genes are returned. The location can be a point or a
     * region. If strand is non-null, only genes on the same strand are returned.
     * 
     * @param physicalLocation
     * @return
     */
    public Collection<Gene> find( PhysicalLocation physicalLocation );

    /**
     * @param accession
     * @param source
     * @return
     */
    public Gene findByAccession( String accession, ubic.gemma.model.common.description.ExternalDatabase source );

    /**
     * @param search
     * @return
     */
    public Collection<Gene> findByAlias( String search );

    public Collection<? extends Gene> findByEnsemblId( String exactString );

    /**
     * @param accession
     * @return
     */
    public Gene findByNCBIId( Integer accession );

    /**
     * @param accession
     * @return
     */
    public GeneValueObject findByNCBIIdValueObject( Integer accession );

    /**
     * @param officialName
     * @return
     */
    public Collection<Gene> findByOfficialName( String officialName );

    /**
     * @param officialName
     * @return
     */
    public Collection<Gene> findByOfficialNameInexact( String officialName );

    /**
     * @param officialSymbol
     * @return
     */
    public Collection<Gene> findByOfficialSymbol( String officialSymbol );

    /**
     * @param symbol
     * @param taxon
     * @return
     */
    public Gene findByOfficialSymbol( String symbol, Taxon taxon );

    /**
     * @param officialSymbol
     * @return
     */
    public Collection<Gene> findByOfficialSymbolInexact( String officialSymbol );

    /**
     * Quickly load exact matches.
     * 
     * @param query
     * @param taxonId
     * @return
     */
    public Map<String, GeneValueObject> findByOfficialSymbols( Collection<String> query, Long taxonId );

    public Collection<AnnotationValueObject> findGOTerms( Long geneId );

    /**
     * Find the gene(s) nearest to the location.
     * 
     * @param physicalLocation
     * @param useStrand if true, the nearest Gene on the same strand will be found. Otherwise the nearest gene on either
     *        strand will be returned.
     * @return RelativeLocationData - a value object for containing the gene that is nearest the given physical location
     */
    public RelativeLocationData findNearest( PhysicalLocation physicalLocation, boolean useStrand );

    /**
     * @param id
     * @return
     */
    public long getCompositeSequenceCountById( Long id );

    /**
     * Returns a list of compositeSequences associated with the given gene and array design
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_ARRAYDESIGN_COLLECTION_READ" })
    public Collection<CompositeSequence> getCompositeSequences( Gene gene, ArrayDesign arrayDesign );

    /**
     * @param id Gemma gene id
     * @return Return probes for a given gene id.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_ARRAYDESIGN_COLLECTION_READ" })
    public Collection<CompositeSequence> getCompositeSequencesById( Long id );

    /**
     * Gets all the genes for a given taxon
     */
    public Collection<Gene> getGenesByTaxon( Taxon taxon );

    /**
     * Given the gemma id of a valid gemma gene will try to calculate the maximum extend of the transcript length. Does
     * this by using the gene products to find the largest max and min nucliotide positions
     * 
     * @param geneId
     * @return
     */
    public PhysicalLocation getMaxPhysicalLength( Gene gene );

    /**
     * @param geneId
     * @return empty collection if no products
     */
    public Collection<GeneProductValueObject> getProducts( Long geneId );

    /**
     * @param id
     * @return
     */
    public Gene load( Long id );

    /**
     * @return
     */
    public Collection<Gene> loadAll();

    /**
     * Returns a collection of genes for the specified taxon
     */
    public Collection<Gene> loadAll( Taxon taxon );

    /**
     * @param ids
     * @return
     */
    public GeneValueObject loadFullyPopulatedValueObject( Long id );

    public GeneValueObject loadGenePhenotypes( Long geneId );

    /**
     * Gets all the microRNAs for a given taxon. Note query could be slow or inexact due to use of wild card searching
     * of the genes description
     */
    public Collection<Gene> loadMicroRNAs( Taxon taxon );

    /**
     * load all genes specified by the given ids.
     * 
     * @return A collection containing up to ids.size() genes.
     */
    public Collection<Gene> loadMultiple( Collection<Long> ids );

    /**
     * Load with objects already thawed.
     * 
     * @param ids
     * @return
     */
    public Collection<Gene> loadThawed( Collection<Long> ids );

    public Collection<Gene> loadThawedLiter( Collection<Long> ids );

    /**
     * @param ids
     * @return
     */
    public GeneValueObject loadValueObject( Long id );

    /**
     * @param ids
     * @return
     */
    public Collection<GeneValueObject> loadValueObjects( Collection<Long> ids );

    public Collection<GeneValueObject> loadValueObjectsLiter( Collection<Long> ids );

    /**
     * @param genes
     */
    @Secured({ "GROUP_ADMIN" })
    public void remove( Collection<Gene> genes );

    /**
     * @param gene
     */
    @Secured({ "GROUP_ADMIN" })
    public void remove( Gene gene );

    /**
     * @param gene
     */
    public Gene thaw( Gene gene );

    /**
     * Only thaw the Aliases, very light version
     * 
     * @param gene
     */
    public Gene thawAliases( Gene gene );

    /**
     * @param genes
     * @see loadThawed as a way to avoid the load..thaw pattern.
     */
    public Collection<Gene> thawLite( Collection<Gene> genes );

    /**
     * @param gene
     */
    public Gene thawLite( Gene gene );

    public Gene thawLiter( Gene gene );

    @Secured({ "GROUP_ADMIN" })
    public void update( Collection<Gene> genes );

    /**
     * @param gene
     */
    @Secured({ "GROUP_ADMIN" })
    /* we would need to relax this to allow phenotype associations to be added, but I think we should avoid doing that */
    public void update( Gene gene );

}
