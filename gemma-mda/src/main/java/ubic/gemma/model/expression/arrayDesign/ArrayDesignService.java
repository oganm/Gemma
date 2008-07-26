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
package ubic.gemma.model.expression.arrayDesign;

/**
 * 
 */
public interface ArrayDesignService extends ubic.gemma.model.common.AuditableService {

    /**
     * 
     */
    public java.util.Collection loadAll();

    /**
     * 
     */
    public ubic.gemma.model.expression.arrayDesign.ArrayDesign findOrCreate(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public void remove( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public ubic.gemma.model.expression.arrayDesign.ArrayDesign findByName( java.lang.String name );

    /**
     * 
     */
    public void update( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public ubic.gemma.model.expression.arrayDesign.ArrayDesign find(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.lang.Integer getCompositeSequenceCount( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.lang.Integer getReporterCount( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public ubic.gemma.model.expression.arrayDesign.ArrayDesign create(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.util.Collection loadCompositeSequences( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public ubic.gemma.model.expression.arrayDesign.ArrayDesign load( long id );

    /**
     * 
     */
    public ubic.gemma.model.genome.Taxon getTaxon( java.lang.Long id );

    /**
     * 
     */
    public java.util.Collection getAllAssociatedBioAssays( java.lang.Long id );

    /**
     * 
     */
    public ubic.gemma.model.expression.arrayDesign.ArrayDesign findByShortName( java.lang.String shortName );

    /**
     * 
     */
    public void thaw( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.lang.Integer countAll();

    /**
     * <p>
     * returns the number of bioSequences associated with this ArrayDesign id
     * </p>
     */
    public long numBioSequences( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * returns the number of BlatResults (BioSequence2GeneProduct) entries associated with this ArrayDesign id.
     * </p>
     */
    public long numBlatResults( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * Returns the number of unique Genes associated with this ArrayDesign id
     * </p>
     */
    public long numGenes( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.util.Collection getExpressionExperiments(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public long numCompositeSequenceWithBioSequences( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public long numCompositeSequenceWithBlatResults( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public long numCompositeSequenceWithGenes( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * deletes the gene product associations on the specified array design
     * </p>
     */
    public void deleteGeneProductAssociations( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * delete sequence alignment results associated with the bioSequences for this array design.
     * </p>
     */
    public void deleteAlignmentData( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * loads the Value Objects for the Array Designs specified by the input ids.
     * </p>
     */
    public java.util.Collection loadValueObjects( java.util.Collection ids );

    /**
     * <p>
     * loads all Array designs as value objects.
     * </p>
     */
    public java.util.Collection loadAllValueObjects();

    /**
     * <p>
     * Function to return a count of all compositeSequences with bioSequence associations
     * </p>
     */
    public long numAllCompositeSequenceWithBioSequences();

    /**
     * <p>
     * Function to return all composite sequences with blat results
     * </p>
     */
    public long numAllCompositeSequenceWithBlatResults();

    /**
     * <p>
     * Function to return a count of all composite sequences with associated genes.
     * </p>
     */
    public long numAllCompositeSequenceWithGenes();

    /**
     * <p>
     * Returns a count of the number of genes associated with all arrayDesigns
     * </p>
     */
    public long numAllGenes();

    /**
     * <p>
     * Function to return the count of all composite sequences with biosequences, given a list of array design Ids
     * </p>
     */
    public long numAllCompositeSequenceWithBioSequences( java.util.Collection ids );

    /**
     * <p>
     * Function to return the count of all composite sequences with blat results, given a list of array design Ids
     * </p>
     */
    public long numAllCompositeSequenceWithBlatResults( java.util.Collection ids );

    /**
     * <p>
     * Function to return the count of all composite sequences with genes, given a list of array design Ids
     * </p>
     */
    public long numAllCompositeSequenceWithGenes( java.util.Collection ids );

    /**
     * <p>
     * Returns the number of unique Genes associated with the collection of ArrayDesign ids.
     * </p>
     */
    public long numAllGenes( java.util.Collection ids );

    /**
     * <p>
     * returns all compositeSequences for the given arrayDesign that do not have any bioSequence associations.
     * </p>
     */
    public java.util.Collection compositeSequenceWithoutBioSequences(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * returns all compositeSequences for the given arrayDesign that do not have BLAT result associations.
     * </p>
     */
    public java.util.Collection compositeSequenceWithoutBlatResults(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * returns all compositeSequences for the given arrayDesign that do not have gene associations.
     * </p>
     */
    public java.util.Collection compositeSequenceWithoutGenes(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * function to get the number of composite sequences that are aligned to a probe-mapped region.
     * </p>
     */
    public long numCompositeSequenceWithProbeAlignedRegion(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * function to get the number of composite sequences that are aligned to a predicted gene
     * </p>
     */
    public long numCompositeSequenceWithPredictedGenes( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * <p>
     * Gets the AuditEvents of the latest sequence analyses for the specified array design ids. This returns a map of id ->
     * AuditEvent. If the events do not exist, the map entry will point to null.
     * </p>
     */
    public java.util.Map getLastSequenceAnalysis( java.util.Collection ids );

    /**
     * <p>
     * Gets the AuditEvents of the latest gene mapping for the specified array design ids. This returns a map of id ->
     * AuditEvent. If the events do not exist, the map entry will point to null.
     * </p>
     */
    public java.util.Map getLastGeneMapping( java.util.Collection ids );

    /**
     * <p>
     * Gets the AuditEvents of the latest annotation file event for the specified array design ids. This returns a map
     * of id -> AuditEvent. If the events do not exist, the map entry will point to null.
     * </p>
     */
    public java.util.Map getLastAnnotationFile( java.util.Collection ids );

    /**
     * <p>
     * Gets the AuditEvents of the latest sequence update for the specified array design ids. This returns a map of id ->
     * AuditEvent. If the events do not exist, the map entry will point to null.
     * </p>
     */
    public java.util.Map getLastSequenceUpdate( java.util.Collection ids );

    /**
     * <p>
     * Test whether the candidateSubsumer subsumes the candidateSubsumee. If so, the array designs are updated to
     * reflect this fact. The boolean value returned indicates whether there was indeed a subsuming relationship found.
     * </p>
     */
    public java.lang.Boolean updateSubsumingStatus(
            ubic.gemma.model.expression.arrayDesign.ArrayDesign candidateSubsumer,
            ubic.gemma.model.expression.arrayDesign.ArrayDesign candidateSubsumee );

    /**
     * 
     */
    public java.util.Map isSubsumer( java.util.Collection ids );

    /**
     * 
     */
    public java.util.Map isSubsumed( java.util.Collection ids );

    /**
     * <p>
     * Does a 'thaw' of an arrayDesign given an id. Returns the thawed arrayDesign.
     * </p>
     */
    public ubic.gemma.model.expression.arrayDesign.ArrayDesign loadFully( java.lang.Long id );

    /**
     * <p>
     * Perform a less intensive thaw of an array design.
     * </p>
     */
    public void thawLite( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.util.Map isMergee( java.util.Collection ids );

    /**
     * 
     */
    public java.util.Map isMerged( java.util.Collection ids );

    /**
     * 
     */
    public java.util.Map getLastRepeatAnalysis( java.util.Collection ids );

    /**
     * <p>
     * Remove all associations that this array design has with BioSequences. This is needed for cases where the original
     * import has associated the probes with the wrong sequences. A common case is for GEO data sets where the actual
     * oligonucleotide is not given. Instead the submitter provides Genbank accessions, which are misleading. This
     * method can be used to clear those until the "right" sequences can be identified and filled in. Note that this
     * does not delete the BioSequences, it just nulls the BiologicalCharacteristics of the CompositeSequences.
     * </p>
     */
    public void removeBiologicalCharacteristics( ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.util.Map getLastTroubleEvent( java.util.Collection ids );

    /**
     * 
     */
    public java.util.Map getLastValidationEvent( java.util.Collection ids );

    /**
     * <p>
     * Given a collection of ID (longs) will return a collection of ArrayDesigns
     * </p>
     */
    public java.util.Collection loadMultiple( java.util.Collection ids );

    /**
     * 
     */
    public java.util.Collection findByAlternateName( java.lang.String queryString );

}
