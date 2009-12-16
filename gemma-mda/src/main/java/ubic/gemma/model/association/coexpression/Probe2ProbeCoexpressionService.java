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
package ubic.gemma.model.association.coexpression;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.access.annotation.Secured;

import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.model.expression.experiment.BioAssaySet;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Gene;

/**
 * @author paul
 * @version $Id$
 */
public interface Probe2ProbeCoexpressionService {

    /*
     * Security notes: p2p mod methods set so users can update coexpression.
     */

    /**
     * @param expressionExperiment
     * @return number of coexpression links for the given experiments.
     */
    public java.lang.Integer countLinks(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * Adds a collection of probe2probeCoexpression objects at one time to the DB, in the order given.
     */
    @Secured( { "GROUP_USER" })
    public Collection<? extends Probe2ProbeCoexpression> create(
            Collection<? extends Probe2ProbeCoexpression> p2pExpressions );

    /**
     * @param deletes
     */
    @Secured( { "GROUP_USER" })
    public void delete( Collection<? extends Probe2ProbeCoexpression> deletes );

    /**
     * @param toDelete
     */
    @Secured( { "GROUP_USER" })
    public void delete( ubic.gemma.model.association.coexpression.Probe2ProbeCoexpression toDelete );

    /**
     * removes all the probe2probeCoexpression links for the given expression experiment
     * 
     * @param ee
     */
    @Secured( { "GROUP_USER", "ACL_SECURABLE_EDIT" })
    public void deleteLinks( ubic.gemma.model.expression.experiment.ExpressionExperiment ee );

    /***
     * Return a list of all ExpressionExperiments in which the given gene was tested for coexpression in, among the
     * given ExpressionExperiments. A gene was tested if any probe for that gene passed filtering criteria during
     * analysis. It is assumed that in the database there is only one analysis per ExpressionExperiment. The boolean
     * parameter filterNonSpecific can be used to exclude ExpressionExperiments in which the gene was detected by only
     * probes predicted to be non-specific for the gene.
     * 
     * @param gene
     * @param expressionExperiments
     * @param filterNonSpecific
     * @return
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ", "ACL_SECURABLE_COLLECTION_READ" })
    public Collection<BioAssaySet> getExpressionExperimentsLinkTestedIn( ubic.gemma.model.genome.Gene gene,
            Collection<BioAssaySet> expressionExperiments, boolean filterNonSpecific );

    /***
     * Return a map of genes in genesB to all ExpressionExperiments in which the given set of pairs of genes was tested
     * for coexpression in, among the given ExpressionExperiments. A gene was tested if any probe for that gene passed
     * filtering criteria during analysis. It is assumed that in the database there is only one analysis per
     * ExpressionExperiment. The boolean parameter filterNonSpecific can be used to exclude ExpressionExperiments in
     * which one or both of the genes were detected by only probes predicted to be non-specific for the gene.
     * 
     * @param geneA
     * @param genesB
     * @param expressionExperiments
     * @param filterNonSpecific
     * @return
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_COLLECTION_READ" })
    public Map<Long, Collection<BioAssaySet>> getExpressionExperimentsLinkTestedIn( ubic.gemma.model.genome.Gene geneA,
            Collection<Long> genesB, Collection<BioAssaySet> expressionExperiments, boolean filterNonSpecific );

    /**
     * @param geneIds
     * @param experiments
     * @param filterNonSpecific
     * @return Map of gene ids to BioAssaySets among those provided in which the gene was tested for coexpression.
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_COLLECTION_READ" })
    public Map<Long, Collection<BioAssaySet>> getExpressionExperimentsTestedIn( Collection<Long> geneIds,
            Collection<BioAssaySet> experiments, boolean filterNonSpecific );

    /**
     * Retrieve all genes that were included in the link analysis for the experiment.
     * 
     * @param expressionExperiment
     * @param filterNonSpecific
     * @return
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    public Collection<Long> getGenesTestedBy( ubic.gemma.model.expression.experiment.BioAssaySet expressionExperiment,
            boolean filterNonSpecific );

    /**
     * get the co-expression by using native sql query but doesn't use a temporary DB table.
     * 
     * @param expressionExperiment
     * @param taxon
     * @return
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    public Collection<ProbeLink> getProbeCoExpression(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment, java.lang.String taxon );

    /**
     * Get the co-expression by using native sql query
     * 
     * @param expressionExperiment
     * @param taxon
     * @param useWorkingTable
     * @return
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    public Collection<ProbeLink> getProbeCoExpression(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment, java.lang.String taxon,
            boolean useWorkingTable );

    /**
     * Returns the top coexpressed links under a given threshold for a given experiment up to a given limit. If the
     * limit is null then all results under the threshold will be returned.
     * 
     * @param ee
     * @param threshold
     * @param limit
     * @return
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    public Collection<ProbeLink> getTopCoexpressedLinks( ExpressionExperiment ee, double threshold, Integer limit );

    /**
     * Returns a map of Genes to a Collection of DesignElementDataVectors for genes coexpressed with the gene (and
     * including the gene).
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_COLLECTION_READ" })
    public Map<Gene, Collection<DesignElementDataVector>> getVectorsForLinks( Collection<Gene> genes,
            Collection<ExpressionExperiment> ees );

    /**
     * Given a Gene, a collection of EE's returns a collection of all the designElementDataVectors that were coexpressed
     * under the said given conditions.
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_COLLECTION_READ" })
    public Collection<DesignElementDataVector> getVectorsForLinks( ubic.gemma.model.genome.Gene gene,
            Collection<ExpressionExperiment> ees );

    /**
     * Create a working table containing links by removing redundant and (optionally) non-specific probes from
     * PROBE_CO_EXPRESSION. Results are stored in a species-specific temporary table managed by this method. This is
     * only used for statistics gathering in probe evaluation experiments, not used by normal applications.
     * 
     * @param ees
     * @param taxon
     * @param filterNonSpecific
     */
    @Secured( { "GROUP_ADMIN", "ACL_SECURABLE_COLLECTION_READ" })
    public void prepareForShuffling( Collection<BioAssaySet> ees, java.lang.String taxon, boolean filterNonSpecific );

    /**
     * Given a list of probeIds and a taxon tests to see if the given list of probeIds were invloved in any coexpression
     * links. That is to say: which of the given probes could have been involved in any coexpression results
     * 
     * @param queryProbeIds
     * @param coexpressedProbeIds
     * @param ee
     * @param taxon
     * @return
     */
    @Secured( { "IS_AUTHENTICATED_ANONYMOUSLY", "ACL_SECURABLE_READ" })
    public Collection<Long> validateProbesInCoexpression( Collection<Long> queryProbeIds,
            Collection<Long> coexpressedProbeIds, ExpressionExperiment ee, String taxon );

}
