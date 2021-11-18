/*
 * The Gemma project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.persistence.service.expression.arrayDesign;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.common.auditAndSecurity.eventType.*;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.sequenceAnalysis.BlatResult;
import ubic.gemma.persistence.service.AbstractFilteringVoEnabledService;
import ubic.gemma.persistence.service.common.auditAndSecurity.AuditEventDao;
import ubic.gemma.persistence.service.expression.experiment.BlacklistedEntityService;
import ubic.gemma.persistence.util.EntityUtils;

import java.util.*;

/**
 * @author klc
 * @see ArrayDesignService
 */
@Service
public class ArrayDesignServiceImpl extends AbstractFilteringVoEnabledService<ArrayDesign, ArrayDesignValueObject>
        implements ArrayDesignService {

    private final ArrayDesignDao arrayDesignDao;
    private final AuditEventDao auditEventDao;

    @Autowired
    private BlacklistedEntityService blacklistedEntityService;

    @Autowired
    public ArrayDesignServiceImpl( ArrayDesignDao arrayDesignDao, AuditEventDao auditEventDao ) {
        super( arrayDesignDao );
        this.arrayDesignDao = arrayDesignDao;
        this.auditEventDao = auditEventDao;
    }

    @Override
    @Transactional
    public void addProbes( ArrayDesign arrayDesign, Collection<CompositeSequence> newProbes ) {
        this.arrayDesignDao.addProbes( arrayDesign, newProbes );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CompositeSequence> compositeSequenceWithoutBioSequences( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.compositeSequenceWithoutBioSequences( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CompositeSequence> compositeSequenceWithoutBlatResults( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.compositeSequenceWithoutBlatResults( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CompositeSequence> compositeSequenceWithoutGenes( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.compositeSequenceWithoutGenes( arrayDesign );
    }

    @Override
    @Transactional
    public void deleteAlignmentData( ArrayDesign arrayDesign ) {
        this.arrayDesignDao.deleteAlignmentData( arrayDesign );
    }

    @Override
    @Transactional
    public void deleteGeneProductAssociations( ArrayDesign arrayDesign ) {
        this.arrayDesignDao.deleteGeneProductAssociations( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public ArrayDesign loadAndThawLite( Long id ) {
        ArrayDesign arrayDesign = this.load( id );
        if ( arrayDesign != null ) {
            this.arrayDesignDao.thawLite( arrayDesign );
        }
        return arrayDesign;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ArrayDesign> loadAndThawLite( Collection<Long> id ) {
        Collection<ArrayDesign> results = this.load( id );
        this.arrayDesignDao.thawLite( results );
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ArrayDesign> findByAlternateName( String queryString ) {
        return this.arrayDesignDao.findByAlternateName( queryString );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ArrayDesign> findByManufacturer( String searchString ) {
        return this.arrayDesignDao.findByManufacturer( searchString );
    }

    /**
     * @see ArrayDesignService#findByName(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<ArrayDesign> findByName( String name ) {
        return this.arrayDesignDao.findByName( name );
    }

    @Override
    @Transactional(readOnly = true)
    public ArrayDesign findByShortName( String shortName ) {
        return this.arrayDesignDao.findByShortName( shortName );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ArrayDesign> findByTaxon( Taxon taxon ) {
        return this.arrayDesignDao.findByTaxon( taxon );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<CompositeSequence, Collection<BlatResult>> getAlignments( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.loadAlignments( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Collection<BioAssay> getAllAssociatedBioAssays( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.getAllAssociatedBioAssays( arrayDesign );

    }

    @Override
    @Transactional(readOnly = true)
    public Map<CompositeSequence, BioSequence> getBioSequences( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.getBioSequences( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCompositeSequenceCount( ArrayDesign arrayDesign ) {
        if ( arrayDesign == null )
            throw new IllegalArgumentException( "Array design cannot be null" );
        return this.arrayDesignDao.numCompositeSequences( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CompositeSequence> getCompositeSequences( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.loadCompositeSequences( arrayDesign, -1, 0 );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CompositeSequence> getCompositeSequences( ArrayDesign arrayDesign, int limit, int offset ) {
        return this.arrayDesignDao.loadCompositeSequences( arrayDesign, limit, offset );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ExpressionExperiment> getExpressionExperiments( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.getExpressionExperiments( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, AuditEvent> getLastGeneMapping( Collection<Long> ids ) {
        Map<Long, Collection<AuditEvent>> eventMap = this.arrayDesignDao.getAuditEvents( ids );
        Map<Long, AuditEvent> lastEventMap = new HashMap<>();
        Set<Long> aaIds = eventMap.keySet();
        Class<? extends ArrayDesignAnalysisEvent> eventclass = ArrayDesignGeneMappingEvent.class;
        this.getMostRecentEvents( eventMap, lastEventMap, aaIds, eventclass );
        return lastEventMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, AuditEvent> getLastRepeatAnalysis( Collection<Long> ids ) {
        Map<Long, Collection<AuditEvent>> eventMap = this.arrayDesignDao.getAuditEvents( ids );
        Map<Long, AuditEvent> lastEventMap = new HashMap<>();
        // remove all AuditEvents that are not SequenceAnalysis events
        Set<Long> aaIds = eventMap.keySet();
        Class<? extends ArrayDesignAnalysisEvent> eventclass = ArrayDesignRepeatAnalysisEvent.class;
        this.getMostRecentEvents( eventMap, lastEventMap, aaIds, eventclass );
        return lastEventMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, AuditEvent> getLastSequenceAnalysis( Collection<Long> ids ) {
        Map<Long, Collection<AuditEvent>> eventMap = this.arrayDesignDao.getAuditEvents( ids );
        Map<Long, AuditEvent> lastEventMap = new HashMap<>();
        // remove all AuditEvents that are not SequenceAnalysis events
        Set<Long> aaIds = eventMap.keySet();
        Class<? extends ArrayDesignAnalysisEvent> eventclass = ArrayDesignSequenceAnalysisEvent.class;
        this.getMostRecentEvents( eventMap, lastEventMap, aaIds, eventclass );
        return lastEventMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, AuditEvent> getLastSequenceUpdate( Collection<Long> ids ) {
        Map<Long, Collection<AuditEvent>> eventMap = this.arrayDesignDao.getAuditEvents( ids );
        Map<Long, AuditEvent> lastEventMap = new HashMap<>();
        // remove all AuditEvents that are not Sequence update events
        Set<Long> aaIds = eventMap.keySet();
        Class<? extends ArrayDesignAnalysisEvent> eventclass = ArrayDesignSequenceUpdateEvent.class;
        this.getMostRecentEvents( eventMap, lastEventMap, aaIds, eventclass );
        return lastEventMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Taxon, Long> getPerTaxonCount() {
        return this.arrayDesignDao.getPerTaxonCount();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Long> getSwitchedExperimentIds( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.getSwitchedExpressionExperimentIds( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Taxon> getTaxa( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.getTaxa( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public Taxon getTaxon( java.lang.Long id ) {
        return this.arrayDesignDao.load( id ).getPrimaryTaxon();
    }

    /*
     * (non-Javadoc)
     *
     * @see ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService#isBlackListed(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isBlackListed( String geoAccession ) {
        return this.blacklistedEntityService.isBlacklisted( geoAccession );

    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> isMerged( Collection<Long> ids ) {
        return this.arrayDesignDao.isMerged( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> isMergee( Collection<Long> ids ) {
        return this.arrayDesignDao.isMergee( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> isSubsumed( Collection<Long> ids ) {
        return this.arrayDesignDao.isSubsumed( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> isSubsumer( Collection<Long> ids ) {
        return this.arrayDesignDao.isSubsumer( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArrayDesignValueObject> loadValueObjectsByIds( Collection<Long> ids ) {
        return this.arrayDesignDao.loadValueObjectsByIds( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArrayDesignValueObject> loadValueObjectsForEE( Long eeId ) {
        return this.arrayDesignDao.loadValueObjectsForEE( eeId );
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllCompositeSequenceWithBioSequences() {
        return this.arrayDesignDao.numAllCompositeSequenceWithBioSequences();
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllCompositeSequenceWithBioSequences( Collection<Long> ids ) {
        return this.arrayDesignDao.numAllCompositeSequenceWithBioSequences( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllCompositeSequenceWithBlatResults() {
        return this.arrayDesignDao.numAllCompositeSequenceWithBlatResults();
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllCompositeSequenceWithBlatResults( Collection<Long> ids ) {
        return this.arrayDesignDao.numAllCompositeSequenceWithBlatResults( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllCompositeSequenceWithGenes() {
        return this.arrayDesignDao.numAllCompositeSequenceWithGenes();
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllCompositeSequenceWithGenes( Collection<Long> ids ) {
        return this.arrayDesignDao.numAllCompositeSequenceWithGenes( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllGenes() {
        return this.arrayDesignDao.numAllGenes();
    }

    @Override
    @Transactional(readOnly = true)
    public long numAllGenes( Collection<Long> ids ) {
        return this.arrayDesignDao.numAllGenes( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public long numBioSequences( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.numBioSequences( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public long numBlatResults( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.numBlatResults( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public long numCompositeSequenceWithBioSequences( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.numCompositeSequenceWithBioSequences( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public long numCompositeSequenceWithBlatResults( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.numCompositeSequenceWithBlatResults( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public long numCompositeSequenceWithGenes( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.numCompositeSequenceWithGenes( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public int numExperiments( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.numExperiments( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public long numGenes( ArrayDesign arrayDesign ) {
        return this.arrayDesignDao.numGenes( arrayDesign );
    }

    @Override
    @Transactional
    public void removeBiologicalCharacteristics( ArrayDesign arrayDesign ) {
        this.arrayDesignDao.removeBiologicalCharacteristics( arrayDesign );
    }

    @Override
    @Transactional(readOnly = true)
    public ArrayDesign thawLite( ArrayDesign arrayDesign ) {
        ArrayDesign result = load( arrayDesign.getId() );
        this.arrayDesignDao.thawLite( result );
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ArrayDesign> thawLite( @NonNull Collection<ArrayDesign> arrayDesigns ) {
        if ( arrayDesigns.isEmpty() ) {
            return Collections.emptyList();
        }
        Collection<ArrayDesign> results = this.load( EntityUtils.getIds( arrayDesigns ) );
        this.arrayDesignDao.thawLite( results );
        return results;
    }

    @Override
    @Transactional
    public Boolean updateSubsumingStatus( ArrayDesign candidateSubsumer, ArrayDesign candidateSubsumee ) {
        return this.arrayDesignDao.updateSubsumingStatus( candidateSubsumer, candidateSubsumee );
    }

    private void checkForMoreRecentMethod( Map<Long, AuditEvent> lastEventMap,
            Class<? extends ArrayDesignAnalysisEvent> eventclass, Long arrayDesignId, ArrayDesign subsumedInto ) {
        AuditEvent lastSubsumerEvent = this.auditEventDao.getLastEvent( subsumedInto, eventclass );
        if ( lastSubsumerEvent != null && lastEventMap.containsKey( arrayDesignId )
                && lastEventMap.get( arrayDesignId ) != null && lastEventMap.get( arrayDesignId ).getDate()
                .before( lastSubsumerEvent.getDate() ) ) {
            lastEventMap.put( arrayDesignId, lastSubsumerEvent );
        }
    }

    private void getMostRecentEvents( Map<Long, Collection<AuditEvent>> eventMap, Map<Long, AuditEvent> lastEventMap,
            Set<Long> aaIds, Class<? extends ArrayDesignAnalysisEvent> eventclass ) {
        for ( Long arrayDesignId : aaIds ) {

            Collection<AuditEvent> events = eventMap.get( arrayDesignId );
            AuditEvent lastEvent;

            if ( events == null ) {
                lastEventMap.put( arrayDesignId, null );
            } else {
                ArrayDesign ad = this.load( arrayDesignId );
                lastEvent = this.auditEventDao.getLastEvent( ad, eventclass );
                lastEventMap.put( arrayDesignId, lastEvent );
            }

            /*
             * Check if the subsuming or merged array (if any) was updated more recently. To do this: 1) load the AA; 2)
             * check for merged; check for subsumed; check events for those.
             */
            ArrayDesign arrayDesign = this.load( arrayDesignId );
            if ( arrayDesign.getSubsumingArrayDesign() != null ) {
                ArrayDesign subsumedInto = arrayDesign.getSubsumingArrayDesign();
                this.checkForMoreRecentMethod( lastEventMap, eventclass, arrayDesignId, subsumedInto );
            }
            if ( arrayDesign.getMergedInto() != null ) {
                ArrayDesign mergedInto = arrayDesign.getMergedInto();
                this.checkForMoreRecentMethod( lastEventMap, eventclass, arrayDesignId, mergedInto );
            }

        }
    }
}