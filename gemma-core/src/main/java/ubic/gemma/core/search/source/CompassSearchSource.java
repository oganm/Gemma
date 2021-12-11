package ubic.gemma.core.search.source;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.compass.core.*;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.spi.InternalCompassSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ubic.gemma.core.annotation.reference.BibliographicReferenceService;
import ubic.gemma.core.genome.gene.service.GeneService;
import ubic.gemma.core.genome.gene.service.GeneSetService;
import ubic.gemma.core.search.SearchResult;
import ubic.gemma.core.search.SearchSource;
import ubic.gemma.model.analysis.expression.ExpressionExperimentSet;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.search.SearchSettings;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.gene.GeneSet;
import ubic.gemma.model.genome.gene.phenotype.valueObject.CharacteristicValueObject;
import ubic.gemma.persistence.service.BaseService;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.expression.designElement.CompositeSequenceService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentSetService;
import ubic.gemma.persistence.service.genome.biosequence.BioSequenceService;
import ubic.gemma.persistence.util.EntityUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compass-based search source.
 * @author klc
 * @author paul
 * @author keshav
 * @author poirigui
 */
@Component
@CommonsLog
public class CompassSearchSource implements SearchSource {

    /**
     * Penalty applied to score hits for entities that derive from an association. For example, if a hit to an EE
     * came from text associated with one of its biomaterials, its score is penalized by this amount (or, this is just
     * the actual score used)
     */
    private static final double INDIRECT_DB_HIT_PENALTY = 0.8;

    /**
     * Penalty applied to all 'index' hits.
     */
    private static final double COMPASS_HIT_SCORE_PENALTY_FACTOR = 0.5;

    /**
     * Setting this too high is unnecessary as characteristic searches are more accurate (for experiments)
     */
    private static final int MAX_LUCENE_HITS = 300;

    /**
     * Minimum trimmed query length to trigger a full-text search.
     */
    private static final int MINIMUM_STRING_LENGTH_FOR_FREE_TEXT_SEARCH = 2;

    /**
     * Text displayed when we fail to retrieve the information on why a hit was retrieved. This was "[Matching text not
     * available]" but we decided that was confusing.
     */
    private static final String HIGHLIGHT_TEXT_NOT_AVAILABLE_MESSAGE = "";

    @Autowired
    @Qualifier("compassArray")
    private Compass compassArray;
    @Autowired
    @Qualifier("compassBibliographic")
    private Compass compassBibliographic;
    @Autowired
    @Qualifier("compassBiosequence")
    private Compass compassBiosequence;
    @Autowired
    @Qualifier("compassExperimentSet")
    private Compass compassExperimentSet;
    @Autowired
    @Qualifier("compassExpression")
    private Compass compassExpression;
    @Autowired
    @Qualifier("compassGene")
    private Compass compassGene;
    @Autowired
    @Qualifier("compassGeneSet")
    private Compass compassGeneSet;
    @Autowired
    @Qualifier("compassProbe")
    private Compass compassProbe;

    @Autowired
    private ArrayDesignService arrayDesignService;
    @Autowired
    private BibliographicReferenceService bibliographicReferenceService;
    @Autowired
    private BioSequenceService bioSequenceService;
    @Autowired
    private CompositeSequenceService compositeSequenceService;
    @Autowired
    private ExpressionExperimentService expressionExperimentService;
    @Autowired
    private ExpressionExperimentSetService expressionExperimentSetService;
    @Autowired
    private GeneService geneService;
    @Autowired
    private GeneSetService geneSetService;

    @Override
    public Collection<SearchResult<ArrayDesign>> searchArrayDesign( SearchSettings settings ) {
        return this.compassSearch( compassArray, settings, ArrayDesign.class, arrayDesignService );
    }

    @Override
    public Collection<SearchResult<BibliographicReference>> searchBibliographicReference( SearchSettings settings ) {
        return this.compassSearch( compassBibliographic, settings, BibliographicReference.class, bibliographicReferenceService );
    }

    @Override
    public Collection<SearchResult<ExpressionExperimentSet>> searchExperimentSet( SearchSettings settings ) {
        return this.compassSearch( compassExperimentSet, settings, ExpressionExperimentSet.class, expressionExperimentSetService );
    }

    @Override
    public Collection<SearchResult<BioSequence>> searchBioSequence( SearchSettings settings ) {
        return this.compassSearch( compassBiosequence, settings, BioSequence.class, bioSequenceService );
    }

    /**
     * A compass backed search that finds {@link BioSequence} that match the search string. Searches the gene and probe
     * indexes for matches then converts those results to {@link BioSequence}
     *
     * @param previousGeneSearchResults Can be null, otherwise used to avoid a second search for genes. The {@link BioSequence}
     *                                  for the genes are added to the final results.
     */
    @Override
    public Collection<SearchResult> searchBioSequenceAndGene( SearchSettings settings,
            Collection<SearchResult<Gene>> previousGeneSearchResults ) {
        Collection<SearchResult> results = new HashSet<>( this.compassSearch( compassBiosequence, settings, BioSequence.class, bioSequenceService ) );

        // FIXME: incorporate the genes in the biosequence results (breaks generics)
        Collection<SearchResult<Gene>> geneResults;
        if ( previousGeneSearchResults == null ) {
            CompassSearchSource.log.info( "Biosequence Search:  running gene search with " + settings.getQuery() );
            geneResults = this.searchGene( settings );
        } else {
            CompassSearchSource.log.info( "Biosequence Search:  using previous results" );
            geneResults = previousGeneSearchResults;
        }

        Map<Gene, SearchResult<Gene>> genes = new HashMap<>();
        for ( SearchResult<Gene> sr : geneResults ) {
            genes.put( sr.getResultObject(), sr );
        }

        Map<Gene, Collection<BioSequence>> sequencesFromDb = bioSequenceService.findByGenes( genes.keySet() );
        for ( Gene gene : sequencesFromDb.keySet() ) {
            Collection<BioSequence> bs = sequencesFromDb.get( gene );
            // bioSequenceService.thawRawAndProcessed( bs );
            for ( BioSequence e : bs ) {
                results.add( this.dbHitToSearchResult( BioSequence.class, e, genes.get( gene ) ) );
            }
        }

        return results;
    }

    @Override
    public Collection<SearchResult<CompositeSequence>> searchCompositeSequence( SearchSettings settings ) {
        return this.compassSearch( compassProbe, settings, CompositeSequence.class, compositeSequenceService );
    }

    @Override
    public Collection<SearchResult> searchCompositeSequenceAndGene( final SearchSettings settings ) {
        return this.searchBioSequence( settings ).stream()
                .map( o -> ( SearchResult ) o )
                .collect( Collectors.toList() );
    }

    /**
     * A compass search on expressionExperiments. The results are filtered by taxon so that our limits are meaningfully
     * applied to next stages of the querying.
     *
     * @return {@link Collection}
     */
    @Override
    public Collection<SearchResult<ExpressionExperiment>> searchExpressionExperiment( SearchSettings settings ) {
        Collection<SearchResult<ExpressionExperiment>> unfilteredResults = this.compassSearch( compassExpression, settings, ExpressionExperiment.class, expressionExperimentService );
        return filterExperimentHitsByTaxon( unfilteredResults, settings.getTaxon() );
    }

    @Override
    public Collection<SearchResult<Gene>> searchGene( final SearchSettings settings ) {
        return this.compassSearch( compassGene, settings, Gene.class, geneService );
    }

    @Override
    public Collection<SearchResult<GeneSet>> searchGeneSet( SearchSettings settings ) {
        return this.compassSearch( compassGeneSet, settings, GeneSet.class, geneSetService );
    }

    @Override
    public Collection<SearchResult<CharacteristicValueObject>> searchPhenotype( SearchSettings settings ) {
        throw new NotImplementedException( "Searching phenotypes is not supported for the Compass source." );
    }

    /**
     * Generic method for searching Lucene indices for entities (excluding ontology terms, which use the OntologySearch)
     */
    private <T extends Identifiable> Set<SearchResult<T>> compassSearch( Compass bean, final SearchSettings settings, Class<T> clazz, BaseService<T> service ) {

        if ( !settings.isUseIndices() )
            return new HashSet<>();

        CompassTemplate template = new CompassTemplate( bean );
        Set<SearchResult<T>> searchResults = template.execute( session -> CompassSearchSource.this.performSearch( settings, session, clazz, service ) );
        if ( CompassSearchSource.log.isDebugEnabled() ) {
            CompassSearchSource.log
                    .debug( "Compass search via " + bean.getSettings().getSetting( "compass.name" ) + " : " + settings
                            + " -> " + searchResults.size() + " hits" );
        }
        return searchResults;
    }

    /**
     * Runs inside Compass transaction
     */
    private <T extends Identifiable> Set<SearchResult<T>> performSearch( SearchSettings settings, CompassSession session, Class<T> clazz, BaseService<T> service ) {
        StopWatch watch = new StopWatch();
        watch.start();
        String enhancedQuery = settings.getQuery(); // query is already trimmed

        //noinspection ConstantConditions
        if ( StringUtils.isBlank( enhancedQuery )
                || enhancedQuery.length() < CompassSearchSource.MINIMUM_STRING_LENGTH_FOR_FREE_TEXT_SEARCH
                // FIXME: this is ignored because of the minimum string length
                || enhancedQuery.equals( "*" ) )
            return new HashSet<>();

        CompassQuery compassQuery = session.queryBuilder().queryString( enhancedQuery ).toQuery();
        CompassSearchSource.log.debug( "Parsed query: " + compassQuery );

        CompassHits hits = compassQuery.hits();

        // highlighting.
        if ( settings.isDoHighlighting() ) {
            if ( session instanceof InternalCompassSession ) {
                // always ...
                CompassMapping mapping = ( ( InternalCompassSession ) session ).getMapping();
                ResourceMapping[] rootMappings = mapping.getRootMappings();
                // should only be one rootMapping.
                this.process( rootMappings, hits );
            }
        }

        watch.stop();
        if ( watch.getTime() > 100 ) {
            CompassSearchSource.log
                    .info( "Getting " + hits.getLength() + " lucene hits for " + enhancedQuery + " took " + watch
                            .getTime() + " ms" );
        }
        if ( watch.getTime() > 5000 ) {
            CompassSearchSource.log
                    .info( "***** Slow Lucene Index Search!  " + hits.getLength() + " lucene hits for " + enhancedQuery
                            + " took " + watch.getTime() + " ms" );
        }

        return this.getSearchResults( hits, clazz, service, settings.isFillObjects() );
    }

    /**
     * Recursively cache the highlighted text. This must be done during the search transaction.
     *
     * @param givenMappings on first call, the root mapping(s)
     */
    private void process( ResourceMapping[] givenMappings, CompassHits hits ) {
        for ( ResourceMapping resourceMapping : givenMappings ) {
            Iterator<Mapping> mappings = resourceMapping.mappingsIt(); // one for each property.
            while ( mappings.hasNext() ) {
                Mapping m = mappings.next();

                if ( m instanceof ComponentMapping ) {
                    ClassMapping[] refClassMappings = ( ( ComponentMapping ) m ).getRefClassMappings();
                    this.process( refClassMappings, hits );
                } else { // should be a ClassPropertyMapping
                    String name = m.getName();
                    for ( int i = 0; i < hits.getLength(); i++ ) {
                        try {
                            String frag = hits.highlighter( i ).fragment( name );
                            if ( log.isDebugEnabled() )
                                log.debug( "Highlighted fragment: " + frag + " for " + hits.hit( i ) );
                        } catch ( Exception e ) {
                            break; // skip this property entirely for all hits ...
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param  hits CompassHits object
     * @return collection of SearchResult. These *do not* contain the actual entities, just their IDs and class.
     */
    private <T extends Identifiable> Set<SearchResult<T>> getSearchResults( CompassHits hits, Class<T> hitsClass, BaseService<T> service, boolean fillObjects ) {
        StopWatch timer = new StopWatch();
        timer.start();
        Set<SearchResult<T>> results = new HashSet<>();
        /*
         * Note that hits come in decreasing score order.
         */
        int maxHits = Math.min( CompassSearchSource.MAX_LUCENE_HITS, hits.getLength() );
        for ( int i = 0; i < maxHits; i++ ) {
            Object resultObject = hits.data( i );

            SearchResult<T> r;
            if ( hitsClass.isAssignableFrom( resultObject.getClass() ) ) {
                //noinspection unchecked
                T identifiableObject = ( T ) resultObject;
                if ( fillObjects ) {
                    r = new SearchResult<>( hitsClass, identifiableObject );
                } else {
                    r = new SearchResult<>( hitsClass, identifiableObject.getId() );
                }
            } else {
                log.warn( "Incompatible result from compass: " + resultObject );
                continue;
            }

            // FIXME: score is generally (always?) NaN
            double score = hits.score( i );
            if ( Double.isNaN( score ) ) {
                score = 1.0;
            }

            /*
             * Always give compass hits a lower score, so they can be differentiated from exact database hits.
             */
            r.setScore( score * CompassSearchSource.COMPASS_HIT_SCORE_PENALTY_FACTOR );

            CompassHighlightedText highlightedText = hits.highlightedText( i );
            if ( highlightedText != null && highlightedText.getHighlightedText() != null ) {
                r.setHighlightedText( highlightedText.getHighlightedText() );
            } else {
                r.setHighlightedText( HIGHLIGHT_TEXT_NOT_AVAILABLE_MESSAGE );
            }

            results.add( r );
        }

        if ( fillObjects ) {
            fillSearchResults( results, service );
        }

        if ( timer.getTime() > 100 ) {
            CompassSearchSource.log.info( results.size() + " hits retrieved (out of " + Math
                    .min( CompassSearchSource.MAX_LUCENE_HITS, hits.getLength() ) + " raw hits tested) in "
                    + timer
                    .getTime()
                    + "ms" );
        }
        if ( timer.getTime() > 5000 ) {
            CompassSearchSource.log
                    .info( "****Extremely long Lucene Search processing! " + results.size() + " hits retrieved (out of "
                            + Math.min( CompassSearchSource.MAX_LUCENE_HITS, hits.getLength() ) + " raw hits tested) in "
                            + timer.getTime() + "ms" );
        }

        return results;
    }

    private <T extends Identifiable> void fillSearchResults( Collection<SearchResult<T>> results, BaseService<T> service ) {
        // load results from the database since Compass only stores indexed attributes
        List<Long> resultObjectIds = results.stream()
                .map( SearchResult::getResultObject )
                .map( Identifiable::getId )
                .sorted().distinct() // for cache consistency
                .collect( Collectors.toList() );

        Collection<T> resultObjects = service.load( resultObjectIds );
        Map<Long, T> resultObjectsById = EntityUtils.getIdMap( resultObjects );

        // only retain results that exist in the database (or that are visible to the current user via ACL rules)
        // unfortunately, this can only be performed when fillObjects is used, otherwise there is no way to know from
        // this source to tell if an indexed object still exists
        //noinspection unchecked
        CollectionUtils.filter( results, result -> resultObjectsById.containsKey( result.getResultId() ) );

        // replace search results objects by loaded ones
        for ( SearchResult<T> result : results ) {
            result.setResultObject( resultObjectsById.get( result.getResultId() ) );
        }
    }

    private Collection<SearchResult<ExpressionExperiment>> filterExperimentHitsByTaxon
            ( Collection<SearchResult<ExpressionExperiment>> unfilteredResults,
                    Taxon t ) {
        if ( t == null || unfilteredResults.isEmpty() )
            return unfilteredResults;

        Collection<SearchResult<ExpressionExperiment>> filteredResults = new HashSet<>();
        Collection<Long> eeIds = this.expressionExperimentService
                .filterByTaxon( EntityUtils.getIds( unfilteredResults ), t );
        for ( SearchResult<ExpressionExperiment> sr : unfilteredResults ) {
            if ( eeIds.contains( sr.getResultId() ) ) {
                filteredResults.add( sr );
            }
        }
        if ( filteredResults.size() < unfilteredResults.size() ) {
            log.info( "Filtered for taxon = " + t.getCommonName() + ", removed " + ( unfilteredResults.size()
                    - filteredResults.size() ) + " results" );
        }
        return filteredResults;
    }

    private <T extends Identifiable> SearchResult<T> dbHitToSearchResult( Class<T> resultType, T e, SearchResult<? extends Identifiable> compassHitDerivedFrom ) {
        SearchResult<T> esr;
        if ( compassHitDerivedFrom != null ) {
            esr = new SearchResult<>( resultType, e, compassHitDerivedFrom.getScore() * CompassSearchSource.INDIRECT_DB_HIT_PENALTY, compassHitDerivedFrom.getHighlightedText() );
        } else {
            esr = new SearchResult<>( resultType, e, 1.0, null );
        }
        return esr;
    }
}
