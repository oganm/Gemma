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
package ubic.gemma.persistence.service.expression.experiment;

import org.springframework.security.access.annotation.Secured;
import ubic.gemma.core.analysis.preprocess.batcheffects.BatchEffectDetails;
import ubic.gemma.core.search.SearchException;
import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.common.auditAndSecurity.eventType.BatchInformationFetchingEvent;
import ubic.gemma.model.common.description.AnnotationValueObject;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimension;
import ubic.gemma.model.expression.bioAssayData.RawExpressionDataVector;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.experiment.*;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.persistence.service.FilteringVoEnabledDao;
import ubic.gemma.persistence.service.FilteringVoEnabledService;
import ubic.gemma.persistence.util.Filters;
import ubic.gemma.persistence.util.Slice;
import ubic.gemma.persistence.util.Sort;
import ubic.gemma.persistence.util.monitor.Monitored;

import java.util.*;

/**
 * @author kelsey
 */
@SuppressWarnings("unused") // Possible external use
public interface ExpressionExperimentService
        extends FilteringVoEnabledService<ExpressionExperiment, ExpressionExperimentValueObject> {

    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    ExperimentalFactor addFactor( ExpressionExperiment ee, ExperimentalFactor factor );

    /**
     * @param ee experiment.
     * @param fv must already have the experimental factor filled in.
     */
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    void addFactorValue( ExpressionExperiment ee, FactorValue fv );

    /**
     * Used when we want to add data for a quantitation type. Does not remove any existing vectors.
     *
     * @param eeToUpdate experiment to be updated.
     * @param newVectors vectors to be added.
     * @return updated experiment.
     */
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    ExpressionExperiment addRawVectors( ExpressionExperiment eeToUpdate,
            Collection<RawExpressionDataVector> newVectors );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    List<ExpressionExperiment> browse( Integer start, Integer limit );

    BatchInformationFetchingEvent checkBatchFetchStatus( ExpressionExperiment ee );

    boolean checkHasBatchInfo( ExpressionExperiment ee );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    Integer countNotTroubled();

    /**
     * returns ids of search results.
     *
     * @param searchString search string
     * @return collection of ids or an empty collection.
     */
    Collection<Long> filter( String searchString ) throws SearchException;

    /**
     * Remove IDs of Experiments that are not from the given taxon.
     *
     * @param ids   collection to purge.
     * @param taxon taxon to retain.
     * @return purged IDs.
     */
    Collection<Long> filterByTaxon( Collection<Long> ids, Taxon taxon );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExpressionExperiment find( ExpressionExperiment expressionExperiment );

    @Override
    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    ExpressionExperiment findOrCreate( ExpressionExperiment expressionExperiment );

    @Override
    @Secured({ "GROUP_USER" })
    ExpressionExperiment create( ExpressionExperiment expressionExperiment );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> load( Collection<Long> ids );

    @Override
    @Monitored
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ_QUIET" })
    ExpressionExperiment load( Long id );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> loadAll();

    @Override
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    void remove( Long id );

    /**
     * Deletes an experiment and all of its associated objects, including coexpression links. Some types of associated
     * objects may need to be deleted before this can be run (example: analyses involving multiple experiments; these
     * will not be deleted automatically).
     *
     * @param expressionExperiment experiment to be deleted.
     */
    @Override
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    void remove( ExpressionExperiment expressionExperiment );

    @Override
    @Secured({ "GROUP_AGENT" })
    void update( ExpressionExperiment expressionExperiment );

    /**
     * @param accession accession
     * @return Experiments which have the given accession. There can be more than one, because one GEO
     * accession can result
     * in multiple experiments in Gemma.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByAccession( DatabaseEntry accession );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByAccession( String accession );

    /**
     * @param bibRef bibliographic reference
     * @return a collection of EE that have that reference that BibliographicReference
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByBibliographicReference( BibliographicReference bibRef );

    /**
     * @param ba bio material
     * @return experiment the given bioassay is associated with
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExpressionExperiment findByBioAssay( BioAssay ba );

    /**
     * @param bm bio material
     * @return experiment the given biomaterial is associated with
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExpressionExperiment findByBioMaterial( BioMaterial bm );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_MAP_READ" })
        // slight security overkill, if they got the biomaterial...
    Map<ExpressionExperiment, BioMaterial> findByBioMaterials( Collection<BioMaterial> bioMaterials );

    /**
     * @param gene gene
     * @param rank rank
     * @return a collection of expression experiment ids that express the given gene above the given expression
     * level
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByExpressedGene( Gene gene, double rank );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExpressionExperiment findByFactor( ExperimentalFactor factor );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExpressionExperiment findByFactorValue( FactorValue factorValue );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExpressionExperiment findByFactorValue( Long factorValueId );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_MAP_READ" })
        // slight security overkill, if they got the factorvalue...
    Map<ExpressionExperiment, FactorValue> findByFactorValues( Collection<FactorValue> factorValues );

    /**
     * @param gene gene
     * @return a collection of expression experiments that have an AD that detects the given Gene (ie a probe on
     * the AD
     * hybridizes to the given Gene)
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByGene( Gene gene );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByName( String name );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExpressionExperiment findByQuantitationType( QuantitationType type );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ_QUIET" })
    ExpressionExperiment findByShortName( String shortName );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByTaxon( Taxon taxon );

    /**
     * @param taxon
     * @param limit max number of hits to return (null == no limit)
     * @return
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findByTaxon( Taxon taxon, Integer limit );

    @Secured({ "GROUP_AGENT", "AFTER_ACL_COLLECTION_READ" })
    List<ExpressionExperiment> findByUpdatedLimit( Integer limit );

    @Secured({ "GROUP_AGENT", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> findUpdatedAfter( Date date );

    /**
     * @param ids ids
     * @return the map of ids to number of terms associated with each expression experiment.
     */
    Map<Long, Integer> getAnnotationCounts( Collection<Long> ids );

    /**
     * @param eeId experiment id.
     * @return the terms associated this expression experiment.
     */
    Set<AnnotationValueObject> getAnnotations( Long eeId );

    /**
     * @param expressionExperiment experiment
     * @return a collection of ArrayDesigns referenced by any of the BioAssays that make up the
     * given
     * ExpressionExperiment.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Collection<ArrayDesign> getArrayDesignsUsed( BioAssaySet expressionExperiment );

    /**
     * Checks the experiment for a batch confound.
     *
     * @param ee the experiment to check.
     * @return a string describing the batch confound, or null if there was no batch confound.[FIXME: String return value is unsafe]
     */
    String getBatchConfound( ExpressionExperiment ee );

    /**
     * @param ee experiment
     * @return details for the principal component most associated with batches (even if it isn't "significant"), or
     * null if there was no batch information available. Note that we don't look at every component, just the
     * first few.
     */
    BatchEffectDetails getBatchEffect( ExpressionExperiment ee );

    /**
     * Composes a string describing the batch effect state of the given experiment.
     *
     * @param ee the experiment to get the batch effect for.
     * @return a string describing the batch effect. If there is no batch effect on the given ee, null is returned.
     */
    String getBatchStatusDescription( ExpressionExperiment ee );

    /**
     * @param expressionExperiment experiment
     * @return the BioAssayDimensions for the study.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Collection<BioAssayDimension> getBioAssayDimensions( ExpressionExperiment expressionExperiment );

    /**
     * @param expressionExperiment experiment
     * @return the amount of biomaterials associated with the given expression experiment.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Integer getBioMaterialCount( ExpressionExperiment expressionExperiment );

    Integer getDesignElementDataVectorCountById( Long id );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> getExperimentsWithOutliers();

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY" })
    Map<Long, Date> getLastArrayDesignUpdate( Collection<ExpressionExperiment> expressionExperiments );

    /**
     * @param expressionExperiment experiment
     * @return the date of the last time any of the array designs associated with this experiment
     * were updated.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Date getLastArrayDesignUpdate( ExpressionExperiment expressionExperiment );

    /**
     * @param ids ids
     * @return AuditEvents of the latest link analyses for the specified expression experiment ids. This returns a
     * map
     * of id -&gt; AuditEvent. If the events do not exist, the map entry will point to null.
     */
    Map<Long, AuditEvent> getLastLinkAnalysis( Collection<Long> ids );

    /**
     * @param ids ids
     * @return AuditEvents of the latest missing value analysis for the specified expression experiment ids. This
     * returns a map of id -&gt; AuditEvent. If the events do not exist, the map entry will point to null.
     */
    Map<Long, AuditEvent> getLastMissingValueAnalysis( Collection<Long> ids );

    /**
     * @param ids ids
     * @return AuditEvents of the latest rank computation for the specified expression experiment ids. This returns
     * a
     * map of id -&gt; AuditEvent. If the events do not exist, the map entry will point to null.
     */
    Map<Long, AuditEvent> getLastProcessedDataUpdate( Collection<Long> ids );

    /**
     * @return a count of expression experiments, grouped by Taxon
     */
    Map<Taxon, Long> getPerTaxonCount();

    /**
     * @param ids ids
     * @return map of ids to how many factor values the experiment has, counting only factor values which are
     * associated
     * with biomaterials.
     */
    Map<Long, Integer> getPopulatedFactorCounts( Collection<Long> ids );

    /**
     * @param ids ids
     * @return map of ids to how many factor values the experiment has, counting only factor values which are
     * associated
     * with biomaterials and only factors that aren't batch
     */
    Map<Long, Integer> getPopulatedFactorCountsExcludeBatch( Collection<Long> ids );

    /**
     * Iterates over the quantitation types for a given expression experiment and returns the preferred quantitation
     * types.
     *
     * @param ee experiment
     * @return quantitation types
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Collection<QuantitationType> getPreferredQuantitationType( ExpressionExperiment ee );

    /**
     * @param id id
     * @return count of an expressionExperiment's design element data vectors, grouped by quantitation type
     */
    Map<QuantitationType, Integer> getQuantitationTypeCountById( Long id );

    /**
     * @param expressionExperiment experiment
     * @return all the quantitation types used by the given expression experiment
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Collection<QuantitationType> getQuantitationTypes( ExpressionExperiment expressionExperiment );

    /**
     * Get the quantitation types for the expression experiment, for the array design specified. This is really only
     * useful for expression experiments that use more than one array design.
     *
     * @param expressionExperiment experiment
     * @param arrayDesign          platform
     * @return quantitation type
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Collection<QuantitationType> getQuantitationTypes( ExpressionExperiment expressionExperiment,
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_MAP_READ" })
    Map<ExpressionExperiment, Collection<AuditEvent>> getSampleRemovalEvents(
            Collection<ExpressionExperiment> expressionExperiments );

    /**
     * @param expressionExperiment experiment
     * @return any ExpressionExperimentSubSets this Experiment might have.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperimentSubSet> getSubSets( ExpressionExperiment expressionExperiment );

    <T extends BioAssaySet> Map<T, Taxon> getTaxa( Collection<T> bioAssaySets );

    /**
     * Returns the taxon of the given expressionExperiment.
     *
     * @param bioAssaySet bioAssaySet.
     * @return taxon, or null if the experiment taxon cannot be determined (i.e., if it has no samples).
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Taxon getTaxon( BioAssaySet bioAssaySet );

    /**
     * @param expressionExperiment ee
     * @return true if this experiment was run on a sequencing-based platform.
     */
    boolean isRNASeq( ExpressionExperiment expressionExperiment );

    boolean isTroubled( ExpressionExperiment expressionExperiment );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_VALUE_OBJECT_COLLECTION_READ" })
    List<ExpressionExperimentValueObject> loadAllValueObjects();

    /**
     * @see FilteringVoEnabledDao#loadValueObjectsPreFilter(Filters, Sort, int, int) for
     * description (no but seriously do look it might not work as you would expect).
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_VALUE_OBJECT_COLLECTION_READ" })
    Slice<ExpressionExperimentValueObject> loadValueObjectsPreFilter( Filters filters, Sort sort, int offset, int limit );

    /**
     * @see ExpressionExperimentDao#loadDetailsValueObjectsByIds(Collection, Taxon, Sort, int, int)
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_VALUE_OBJECT_COLLECTION_READ" })
    Slice<ExpressionExperimentDetailsValueObject> loadDetailsValueObjects( Collection<Long> ids, Taxon taxon, Sort sort, int offset, int limit );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_VALUE_OBJECT_COLLECTION_READ" })
    List<ExpressionExperimentDetailsValueObject> loadDetailsValueObjects( Collection<Long> ids );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> loadLackingFactors();

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    Collection<ExpressionExperiment> loadLackingTags();

    /**
     * @param ids           ids to load
     * @param maintainOrder If true, order of valueObjects returned will correspond to order of ids passed in.
     * @return value objects
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_VALUE_OBJECT_COLLECTION_READ" })
    List<ExpressionExperimentValueObject> loadValueObjectsByIds( List<Long> ids, boolean maintainOrder );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_VALUE_OBJECT_COLLECTION_READ" })
    List<ExpressionExperimentValueObject> loadValueObjectsByIds( Collection<Long> ids );

    /**
     * Remove raw vectors associated with the given quantitation type. It does not touch processed data.
     *
     * @param ee experiment
     * @param qt quantitation type
     * @return number of vectors removed.
     */
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    int removeRawVectors( ExpressionExperiment ee, QuantitationType qt );

    /**
     * Used when we are replacing data, such as when converting an experiment from one platform to another. Examples
     * would be exon array or RNA-seq data sets, or other situations where we are replacing data. Does not take care of
     * computing the processed data vectors, but it does clear them out.
     *
     * @param ee      experiment
     * @param vectors If they are from more than one platform, that will be dealt with.
     * @return the updated Experiment
     */
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    ExpressionExperiment replaceRawVectors( ExpressionExperiment ee, Collection<RawExpressionDataVector> vectors );

    /**
     * Will add the vocab characteristic to the expression experiment and persist the changes.
     *
     * @param vc If the evidence code is null, it will be filled in with IC. A category and value must be provided.
     * @param ee the experiment to add the characteristics to.
     */
    void saveExpressionExperimentStatement( Characteristic vc, ExpressionExperiment ee );

    /**
     * Will add all the vocab characteristics to the expression experiment and persist the changes.
     *
     * @param vc Collection of the characteristics to be added to the experiment. If the evidence code is null, it will
     *           be filled in with IC. A category and value must be provided.
     * @param ee the experiment to add the characteristics to.
     */
    void saveExpressionExperimentStatements( Collection<Characteristic> vc, ExpressionExperiment ee );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    ExpressionExperiment thaw( ExpressionExperiment expressionExperiment );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    ExpressionExperiment thawBioAssays( ExpressionExperiment expressionExperiment );

    /**
     * Partially thaw the expression experiment given - do not thaw the raw data.
     *
     * @param expressionExperiment experiment
     * @return thawed experiment
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    ExpressionExperiment thawLite( ExpressionExperiment expressionExperiment );

    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    ExpressionExperiment thawLiter( ExpressionExperiment expressionExperiment );

    boolean isBlackListed( String geoAccession );

    /**
     * @param ee
     * @return true if the experiment is not explicitly marked as unsuitable for DEA; false otherwise.
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Boolean isSuitableForDEA( ExpressionExperiment ee );

    /**
     *
     * @return collection of GEO experiments which lack an association with a publication (non-GEO experiments will be ignored)
     */
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    Collection<ExpressionExperiment> getExperimentsLackingPublications();
}
