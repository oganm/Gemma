/*
 * The Gemma project
 * 
 * Copyright (c) 2007 University of British Columbia
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

package ubic.gemma.model.analysis.expression.coexpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The details about coexpression for multiple target genes with respect to a query gene. The bulk of the information is
 * stored as a collection of CoexpressionValeuObjects.
 * <p>
 * All the genes should be of the same subclass (known genes, predicted genes or probe aligned regions). Thus we usually
 * have three instances of this for each query gene.
 * 
 * @see CoexpressionValueObject
 * @author klc
 * @version $Id$
 */
public class CoexpressedGenesDetails {

    private static Log log = LogFactory.getLog( CoexpressedGenesDetails.class.getName() );
    /**
     * Details for each gene. Map of gene id to vos.
     */
    private Map<Long, CoexpressionValueObject> coexpressionData;

    /**
     * Map of EE -> Probes -> Genes for the coexpressed genes.
     */
    private Map<Long, Map<Long, Collection<Long>>> expressionExperimentProbe2GeneMaps;

    /**
     * Map of EE -> Probes -> Genes for the query gene's probes.
     */
    private Map<Long, Map<Long, Collection<Long>>> queryGeneExpressionExperimentProbe2GeneMaps;

    /**
     * the expression experiments that show up in the results - they contribute raw links. Value is the number of links.
     */
    private Map<Long, Integer> expressionExperiments;

    private Map<Long, Integer> expressionExperimentsRawLinkCounts;

    private Map<Long, Boolean> expressionExperimentHasSpecificProbeForQueryGene;

    private int negativeStringencyLinkCount;

    /**
     * Number of links that passed the stringency requirements
     */
    private int positiveStringencyLinkCount;

    /**
     * The gene used to search from.
     */
    private Long queryGene;

    private final int supportThreshold;

    private boolean warned = false;

    @Override
    public void finalize() {
        expressionExperiments.clear();

        for ( Long l : coexpressionData.keySet() ) {
            coexpressionData.get( l ).finalize();
        }
        for ( Long l : queryGeneExpressionExperimentProbe2GeneMaps.keySet() ) {
            queryGeneExpressionExperimentProbe2GeneMaps.get( l ).clear();
        }
        for ( Long l : expressionExperimentProbe2GeneMaps.keySet() ) {
            expressionExperimentProbe2GeneMaps.get( l ).clear();
        }

        this.queryGeneExpressionExperimentProbe2GeneMaps.clear();
        this.expressionExperimentProbe2GeneMaps.clear();
        this.coexpressionData.clear();

    }

    /**
     * @param queryGene
     * @param supportThreshold
     */
    public CoexpressedGenesDetails( Long queryGene, int supportThreshold ) {

        this.queryGene = queryGene;
        this.supportThreshold = supportThreshold;

        positiveStringencyLinkCount = 0;
        negativeStringencyLinkCount = 0;

        coexpressionData = new HashMap<Long, CoexpressionValueObject>();
        expressionExperiments = new HashMap<Long, Integer>();
        expressionExperimentsRawLinkCounts = new HashMap<Long, Integer>();
        expressionExperimentProbe2GeneMaps = new HashMap<Long, Map<Long, Collection<Long>>>();
        expressionExperimentHasSpecificProbeForQueryGene = new HashMap<Long, Boolean>();
        queryGeneExpressionExperimentProbe2GeneMaps = new HashMap<Long, Map<Long, Collection<Long>>>();
    }

    /**
     * @param value
     * @return
     */
    public CoexpressionValueObject add( CoexpressionValueObject value ) {
        if ( coexpressionData.containsKey( value.getGeneId() ) ) {
            // FIXME this seems like it would be an error.
            if ( log.isDebugEnabled() ) log.debug( "Clobbering when adding " + value );
        }
        return coexpressionData.put( value.getGeneId(), value );
    }

    /**
     * Add an expression experiment to the list
     * 
     * @param vo
     */
    public void addExpressionExperiment( Long vo ) {

        this.expressionExperiments.put( vo, 0 );
    }

    /**
     * Populate information about probe -> gene relationships for a single probe, for the query gene's probe.
     * 
     * @param eeID
     * @param probe2geneMap populated from cs2gene query.
     */
    public void addQuerySpecificityInfo( Long eeID, Map<Long, Collection<Long>> probe2geneMap ) {
        this.queryGeneExpressionExperimentProbe2GeneMaps.put( eeID, probe2geneMap );
    }

    public void addTargetSpecificityInfo( Long eeID, Map<Long, Collection<Long>> probe2geneMap ) {
        if ( !this.expressionExperimentProbe2GeneMaps.containsKey( eeID ) ) {
            this.expressionExperimentProbe2GeneMaps.put( eeID, new HashMap<Long, Collection<Long>>() );
        }
        this.expressionExperimentProbe2GeneMaps.get( eeID ).putAll( probe2geneMap );
    }

    public boolean containsKey( Long key ) {
        return coexpressionData.containsKey( key );
    }

    /**
     * Filter out all results except for the top N, where N = limit. Important: only run this after you've populated the
     * object!
     * 
     * @param limit
     * @param stringency
     */
    public void filter( int limit, int stringency ) {
        // remove from the coexpressionData
        int count = 0;

        // we need to sort this map by the values.
        class Vk implements Comparable<Vk> {
            private Long i;
            private CoexpressionValueObject v;

            public Vk( Long i, CoexpressionValueObject v ) {
                this.i = i;
                this.v = v;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.lang.Object#hashCode()
             */
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ( ( v == null ) ? 0 : v.hashCode() );
                return result;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals( Object obj ) {
                if ( this == obj ) return true;
                if ( obj == null ) return false;
                if ( getClass() != obj.getClass() ) return false;
                Vk other = ( Vk ) obj;
                if ( v == null ) {
                    if ( other.v != null ) return false;
                } else if ( !v.equals( other.v ) ) return false;
                return true;
            }

            @Override
            public int compareTo( Vk o ) {
                return o.getV().compareTo( this.v );
            }

            public Long getI() {
                return i;
            }

            public CoexpressionValueObject getV() {
                return v;
            }
        }

        List<Vk> vks = new ArrayList<Vk>();

        int revisedPosLinks = 0;
        int revisedNegLinks = 0;

        for ( Long l : coexpressionData.keySet() ) {
            CoexpressionValueObject link = coexpressionData.get( l );

            vks.add( new Vk( l, link ) );
        }

        Collections.sort( vks );
        Collections.reverse( vks );

        for ( Vk vk : vks ) {
            if ( count > limit ) {
                coexpressionData.remove( vk.getI() );
            } else {
                if ( vk.getV().getPositiveLinkSupport() >= stringency ) {
                    revisedPosLinks++;
                }
                if ( vk.getV().getNegativeLinkSupport() >= stringency ) {
                    revisedNegLinks++;
                }
            }
            count++;
        }

        this.setPositiveStringencyLinkCount( revisedPosLinks );
        this.setNegativeStringencyLinkCount( revisedNegLinks );

        /*
         * Note that it is possible for the sum to be greater than the limit, as links can have both + and - correlation
         * support > threshold.
         */
        assert revisedPosLinks <= limit : "Got " + revisedPosLinks + " pos links but limit was " + limit;
        assert revisedNegLinks <= limit : "Got " + revisedNegLinks + " neg links but limit was " + limit;

    }

    /**
     * @param geneId
     * @return
     */
    public CoexpressionValueObject get( Long geneId ) {
        return this.coexpressionData.get( geneId );
    }

    /**
     * @param supportThreshold
     * @return the coexpressionData sorted in order of decreasing support.
     */
    public List<CoexpressionValueObject> getCoexpressionData( int threshold ) {
        List<CoexpressionValueObject> result = new ArrayList<CoexpressionValueObject>();
        for ( CoexpressionValueObject o : coexpressionData.values() ) {
            if ( o.getNegativeLinkSupport() >= threshold || o.getPositiveLinkSupport() >= threshold ) {
                result.add( o );
            }
        }
        Collections.sort( result );
        return result;
    }

    /**
     * @return a collection of expressionExperiment Ids that were searched for coexpression (including those that had no
     *         results)
     */
    public Collection<Long> getExpressionExperimentIds() {
        return expressionExperiments.keySet();
    }

    /**
     * @param eeID
     * @param probeID
     * @return a collection of gene IDs that the probe is predicted to detect
     */
    public Collection<Long> getGenesForProbe( Long eeID, Long probeID ) {
        Map<Long, Collection<Long>> map = expressionExperimentProbe2GeneMaps.get( eeID );
        if ( map == null ) {
            return new HashSet<Long>();
        }
        return map.get( probeID );
    }

    /**
     * @param eeID
     * @param probeID
     * @return
     */
    public Collection<Long> getGenesForQueryProbe( Long eeID, Long probeID ) {
        return queryGeneExpressionExperimentProbe2GeneMaps.get( eeID ).get( probeID );
    }

    /**
     * @param id
     * @return
     */
    public Integer getLinkCountForEE( Long id ) {

        Integer eeVo = expressionExperiments.get( id );

        if ( eeVo == null ) return 0;

        return eeVo;

    }

    /**
     * @return the stringencyLinkCount
     */
    public int getNegativeStringencyLinkCount() {
        return negativeStringencyLinkCount;
    }

    /**
     * @return the number of StringencyGenes
     */
    public int getNumberOfGenes() {
        return this.coexpressionData.size();
    }

    /**
     * @return
     */
    public int getNumberOfUsedExpressionExperiments() {
        return expressionExperimentProbe2GeneMaps.keySet().size();

    }

    /**
     * @return the stringencyLinkCount
     */
    public int getPositiveStringencyLinkCount() {
        return positiveStringencyLinkCount;
    }

    /**
     * @param id
     * @return an int representing the raw number of links a given ee contributed to the coexpression search
     */
    public Integer getRawLinkCountForEE( Long id ) {

        if ( expressionExperimentsRawLinkCounts.get( id ) == null ) {
            return 0;
        }

        return expressionExperimentsRawLinkCounts.get( id );
    }

    /**
     * This computes the support for each link from a single query gene, as well as updating information on specificity
     * of probes. Terminology:
     * <ul>
     * <li>Query gene: the gene that was used to initate the search</li>
     * <li>Coexpressed gene: 'answer' to the query</li>
     * <li>Query probe: a probe that provided evidence for the query gene</li>
     * <li>Coexpressed probe: probe that provided evidence for the coexpressed gene</li>
     * <li>'hyb' is shorthand for 'hybridizes'. Really we don't know what hybridizes to what: 'hyb' means that sequence
     * similarity was above some threshold during sequence analysis.</li>
     * </ul>
     * <p>
     * For any query probe - coexpressed probe combination, computing specificity requires considering several cases:
     * </p>
     * <ol>
     * <li>query probe and the coexpressed probe are both specific for separate query and coexpressed genes. This is the
     * 'easy' case.</li>
     * <li>query probe is non-specific. There are two subcases (which can co-occur)
     * <ol>
     * <li>query probe hybs to target gene</li>
     * <li>query probe hybs to other gene(s).</li>
     * </ol>
     * </li>
     * <li>target probe is non-specific. There are two subcases (which can co-occur)
     * <ol>
     * <li>target probe hybs to the query gene</li>
     * <li>target probe hybs to some other gene(s).</li>
     * </ol>
     * </li>
     * <li>Both the target and query probe are non-specific, in which case there are four combinations of the above
     * subcases. These cases aren't handled separately.</li>
     * </ol>
     * <p>
     * The current policy (please see the code for details) is that if there is potential cross-hybridization between
     * ANY gene that the query probe picks up and ANY gene that the coexpressed probe picks up, then we remove the link
     * entirely. For all other cases we just flag the probe as non-specific. This lets downstream analysis know that the
     * "query gene" might really be something else; likewise for the coexpressed gene. In other words, for most of the
     * above cases the concern is that the issue is not kept track of. Only two seriously problematic cases (2.1 and
     * 3.1) result in data removal.
     */
    public void postProcess() {

        /*
         * Believe it or not, this is some of the trickiest and most important code in Gemma. Don't modify it without
         * DUE care. Case 1 does not require any special handling. Case 2.1 is partly handled earlier, when flags are
         * added to the coexpression objects for non-specific query gene probes.
         */

        int positiveLinkCount = 0;
        int negativeLinkCount = 0;

        Set<CoexpressionValueObject> toRemove = new HashSet<CoexpressionValueObject>();

        /*
         * Iterate over all the coexpression data (per target gene)
         */
        coexp: for ( CoexpressionValueObject coExValObj : getCoexpressionData( 0 ) ) {

            Map<Long, Collection<ProbePair>> links = coExValObj.getLinks();

            Map<Long, Collection<Long>> queryProbeInfo = coExValObj.getQueryProbeInfo();

            if ( log.isDebugEnabled() ) log.debug( "Gene: " + coExValObj );

            // Fill in information about the other genes these probes hybridize to, if any.

            // For each experiment with coexpression data...
            for ( Long eeID : coExValObj.getExpressionExperiments() ) {

                /*
                 * Fill in 'experiment has sepcific probes' information.
                 */
                processLinksForSpecificity( coExValObj, links, eeID );

                /*
                 * Look for probes that cross-hybridize to the query. These are completely removed.
                 */
                Collection<Long> queryProbes = queryProbeInfo.get( eeID );

                if ( log.isDebugEnabled() )
                    log.debug( "Query probes in " + eeID + ": " + StringUtils.join( queryProbes, "," ) );

                // For each probe ...
                Collection<Long> coexpressedProbes = coExValObj.getProbes( eeID );
                probe: for ( Long coexpressedProbe : coexpressedProbes ) {

                    Collection<Long> genesForProbe = getGenesForQueryProbe( eeID, coexpressedProbe );
                    if ( genesForProbe == null || genesForProbe.isEmpty() ) {
                        // This can happen if we removed the probe in the inner loop.
                        continue;
                    }

                    /*
                     * Each probe can hybridize to multiple genes. There are two cases (which can co-occur): 1) The
                     * 'other' gene is the same as the query gene, in which case we remove the result; 2) otherwise we
                     * just make a record of the crosshyb.
                     */
                    for ( Long geneID : genesForProbe ) {

                        /*
                         * Check for query probe that hits the coexpressed gene. This is case 2.1
                         */
                        for ( Long queryProbe : queryProbes ) {
                            Collection<Long> genesForQueryProbe = getGenesForProbe( eeID, queryProbe );
                            if ( genesForQueryProbe != null && genesForQueryProbe.contains( geneID ) ) {
                                /*
                                 * Note: the coexpressedProbe is often the same as the queryID
                                 */
                                this.removeProbeDataForEE( eeID, coexpressedProbe );
                                toRemove.add( coExValObj );
                                continue probe;
                            }
                        }

                        if ( log.isDebugEnabled() ) {
                            log.debug( "EEID=" + eeID + " Probe=" + coexpressedProbe + " Gene=" + geneID );
                        }

                        if ( geneID.equals( queryGene ) ) {
                            /*
                             * Case 3.1
                             */
                            if ( log.isDebugEnabled() ) {
                                log.debug( "Crosshyb with query gene id=" + queryGene + ": ee=" + eeID + " , probe="
                                        + coexpressedProbe + " hits genes=" + StringUtils.join( genesForProbe, "," ) );
                            }

                            /*
                             * If this probe also hybridizes with the query gene's probe, then we chuck it.
                             */

                            boolean hasAnyEvidenceLeft = coExValObj.removeProbeEvidence( coexpressedProbe, eeID );

                            /*
                             * This call modifies the map we are iterating over, so we have to check that there is still
                             * anything left.
                             */
                            this.removeProbeDataForEE( eeID, coexpressedProbe );

                            if ( !hasAnyEvidenceLeft ) {
                                // We have to remove this coExValObj from the resultset.
                                if ( log.isDebugEnabled() )
                                    log.debug( "No evidence left : ee=" + eeID + " , crosshyb probe="
                                            + coexpressedProbe + " to genes "
                                            + org.apache.commons.lang.StringUtils.join( genesForProbe, ',' ) );
                                toRemove.add( coExValObj );
                                continue coexp;
                            }
                            continue probe;
                        }
                        /*
                         * Otherwise we add a crosshybridizing gene. Case 3.2.
                         */
                        coExValObj.addCrossHybridizingGene( geneID );
                    }
                }
            }

            if ( toRemove.contains( coExValObj ) ) continue;

            boolean keep = false;
            if ( coExValObj.getPositiveLinkSupport() != 0
                    && coExValObj.getPositiveLinkSupport() >= this.supportThreshold ) {
                positiveLinkCount++;
                incrementEEContributions( coExValObj.getEEContributing2PositiveLinks() );
                keep = true;
            }

            if ( coExValObj.getNegativeLinkSupport() != 0
                    && coExValObj.getNegativeLinkSupport() >= this.supportThreshold ) {
                negativeLinkCount++;
                incrementEEContributions( coExValObj.getEEContributing2NegativeLinks() );
                keep = true;
            }

            if ( !keep ) {
                toRemove.add( coExValObj );
            } else {
                incrementRawEEContributions( coExValObj.getExpressionExperiments() );
            }

            assert coExValObj.getExpressionExperiments().size() <= coExValObj.getPositiveLinkSupport()
                    + coExValObj.getNegativeLinkSupport() : "got " + coExValObj.getExpressionExperiments().size()
                    + " expected " + ( coExValObj.getPositiveLinkSupport() + coExValObj.getNegativeLinkSupport() );

        }

        for ( CoexpressionValueObject cvo : toRemove ) {
            if ( log.isDebugEnabled() ) log.debug( "Removing gene: " + cvo.getGeneId() );
            this.coexpressionData.remove( cvo.getGeneId() );
        }

        // add count of pruned matches to coexpression data
        setPositiveStringencyLinkCount( positiveLinkCount );
        setNegativeStringencyLinkCount( negativeLinkCount );
    }

    /**
     * @param stringencyLinkCount the stringencyLinkCount to set
     */
    public void setNegativeStringencyLinkCount( int stringencyLinkCount ) {
        this.negativeStringencyLinkCount = stringencyLinkCount;
    }

    /**
     * @param stringencyLinkCount the stringencyLinkCount to set
     */
    public void setPositiveStringencyLinkCount( int stringencyLinkCount ) {
        this.positiveStringencyLinkCount = stringencyLinkCount;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append( this.coexpressionData.size() + " coexpressions with " + this.queryGene + ":\n" );

        for ( CoexpressionValueObject cvo : this.coexpressionData.values() ) {
            buf.append( cvo.toString() + "\n-------------------\n" );
        }
        return buf.toString();
    }

    /**
     * Counting up how many support-threshold exceeding links each data set contributed.
     * 
     * @param contributingEEs
     */
    private void incrementEEContributions( Collection<Long> contributingEEs ) {

        for ( Long eeID : contributingEEs ) {

            if ( expressionExperiments == null ) {
                expressionExperiments.put( eeID, 1 );
            } else {
                expressionExperiments.put( eeID, expressionExperiments.get( eeID ) + 1 );
            }

        }
    }

    /**
     * Counting up how many links each data set contributed (including links that did not meet the stringency
     * threshold).
     * 
     * @param contributingEEs
     */
    private void incrementRawEEContributions( Collection<Long> contributingEEs ) {
        for ( Long eeID : contributingEEs ) {

            if ( expressionExperimentsRawLinkCounts.get( eeID ) == null ) {
                expressionExperimentsRawLinkCounts.put( eeID, 1 );
            } else {
                expressionExperimentsRawLinkCounts.put( eeID, expressionExperimentsRawLinkCounts.get( eeID ) + 1 );
            }
        }
    }

    /**
     * Fill in specificity information for this experiment, based on the pairs.
     * 
     * @param coExValObj
     * @param links
     * @param eeID
     */
    private void processLinksForSpecificity( CoexpressionValueObject coExValObj,
            Map<Long, Collection<ProbePair>> links, Long eeID ) {
        Collection<ProbePair> rawLinks = links.get( eeID );

        if ( rawLinks == null || rawLinks.size() == 0 ) {
            throw new IllegalStateException();
        }

        boolean isSpecific = false;
        for ( ProbePair probePair : rawLinks ) {

            Long queryProbeId = probePair.getQueryProbeId();
            Collection<Long> genesForQueryProbe = getGenesForQueryProbe( eeID, queryProbeId );
            if ( genesForQueryProbe == null ) {
                /*
                 * There could be probes for which no gene mapping exists any more.
                 */
                if ( !warned ) {
                    log.warn( "No genes for query probe=" + queryProbeId + " in ee=" + eeID + " [" + this.queryGene
                            + "] " + " (Any additional warnings for this query gene will be at DEBUG level only)" );
                } else if ( log.isDebugEnabled() ) {
                    log.debug( "No genes for query probe=" + queryProbeId + " in ee=" + eeID );
                }
                warned = true;
                continue;
            }
            int numQueryGenesHit = genesForQueryProbe.size();

            Long targetProbeId = probePair.getTargetProbeId();
            Collection<Long> genesForProbe = getGenesForProbe( eeID, targetProbeId );
            if ( genesForProbe == null ) {
                if ( !warned ) {
                    log.warn( "No genes for target probe=" + targetProbeId + " in ee=" + eeID
                            + " (Any additional warnings for this link will be at DEBUG level only)" );
                } else if ( log.isDebugEnabled() ) {
                    log.debug( "No genes for target probe=" + targetProbeId + " in ee=" + eeID );
                }
                warned = true;
                continue;
            }
            int numTargetGenesHit = genesForProbe.size();

            /*
             * If a _single_ link is 'specific', then we count the ee. as 'specific'.
             */
            if ( numQueryGenesHit == 1 && numTargetGenesHit == 1 ) {
                isSpecific = true;
            }
        }

        if ( !isSpecific ) {
            coExValObj.getNonspecificEE().add( eeID );
        }
    }

    /**
     * Remove a probe.
     * 
     * @param eeID
     * @param probeID
     */
    private void removeProbeDataForEE( Long eeID, Long probeID ) {
        Map<Long, Collection<Long>> eeData = this.expressionExperimentProbe2GeneMaps.get( eeID );
        if ( eeData != null ) {
            eeData.remove( probeID );
            if ( eeData.size() == 0 ) {
                /*
                 * This includes 'low-stringency' data so usually this doesn't really happen.
                 */
                this.expressionExperimentProbe2GeneMaps.remove( eeID );
                this.expressionExperiments.remove( eeID );
            }
        }
    }

    public boolean hasExpressionExperiment( Long eeID ) {
        return this.expressionExperiments.containsKey( eeID );
    }

    public boolean getHasProbeSpecificForQueryGene( Long eeID ) {
        return expressionExperimentHasSpecificProbeForQueryGene.containsKey( eeID );
    }

    public void setHasProbeSpecificForQueryGene( Long eeID ) {
        expressionExperimentHasSpecificProbeForQueryGene.put( eeID, true );
    }

}
