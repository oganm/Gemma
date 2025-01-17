/*
 * The Gemma project
 *
 * Copyright (c) 2006 University of British Columbia
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

package ubic.gemma.core.search;

import com.google.common.collect.Sets;
import gemma.gsec.util.SecurityUtil;
import lombok.extern.apachecommons.CommonsLog;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ubic.basecode.ontology.model.OntologyIndividual;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.search.OntologySearchException;
import ubic.basecode.util.BatchIterator;
import ubic.gemma.core.association.phenotype.PhenotypeAssociationManagerService;
import ubic.gemma.core.genome.gene.service.GeneSearchService;
import ubic.gemma.core.genome.gene.service.GeneService;
import ubic.gemma.core.genome.gene.service.GeneSetService;
import ubic.gemma.core.ontology.OntologyService;
import ubic.gemma.core.util.ListUtils;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.analysis.expression.ExpressionExperimentSet;
import ubic.gemma.model.association.phenotype.PhenotypeAssociation;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.search.SearchSettings;
import ubic.gemma.model.expression.BlacklistedEntity;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.BlacklistedPlatform;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.BlacklistedExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.gene.GeneSet;
import ubic.gemma.model.genome.gene.phenotype.valueObject.CharacteristicValueObject;
import ubic.gemma.model.genome.gene.phenotype.valueObject.GeneEvidenceValueObject;
import ubic.gemma.persistence.service.BaseVoEnabledService;
import ubic.gemma.persistence.service.common.description.CharacteristicService;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.expression.designElement.CompositeSequenceService;
import ubic.gemma.persistence.service.expression.experiment.BlacklistedEntityDao;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentSetService;
import ubic.gemma.persistence.service.genome.biosequence.BioSequenceService;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;
import ubic.gemma.persistence.util.CacheUtils;
import ubic.gemma.persistence.util.EntityUtils;
import ubic.gemma.persistence.util.Settings;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This service is used for performing searches using free text or exact matches to items in the database.
 * <h2>Implementation notes</h2>
 * Internally, there are generally two kinds of searches performed, precise database searches looking for exact matches
 * in the database and compass/lucene searches which look for matches in the stored index.
 * To add more dependencies to this Service edit the applicationContext-search.xml
 *
 * @author klc
 * @author paul
 * @author keshav
 */
@Service
@CommonsLog
public class SearchServiceImpl implements SearchService {

    private static final int MINIMUM_EE_QUERY_LENGTH = 3;

    private static final String NCBI_GENE = "ncbi_gene";

    /**
     * How long after creation before an object is evicted, no matter what (seconds)
     */
    private static final int ONTOLOGY_CACHE_TIME_TO_DIE = 10000;

    /**
     * How long an item in the cache lasts when it is not accessed.
     */
    private static final int ONTOLOGY_CACHE_TIME_TO_IDLE = 3600;

    private static final String ONTOLOGY_CHILDREN_CACHE_NAME = "OntologyChildrenCache";

    /**
     * How many term children can stay in memory
     */
    private static final int ONTOLOGY_INFO_CACHE_SIZE = 30000;

    private final Map<String, Taxon> nameToTaxonMap = new LinkedHashMap<>();

    @Autowired
    private CacheManager cacheManager;
    private Cache childTermCache;

    /* sources */
    @Autowired
    @Qualifier("compassSearchSource")
    private SearchSource compassSearchSource;
    @Autowired
    @Qualifier("databaseSearchSource")
    private SearchSource databaseSearchSource;

    // TODO: move all this under DatabaseSearchSource
    @Autowired
    private ArrayDesignService arrayDesignService;
    @Autowired
    private CharacteristicService characteristicService;
    @Autowired
    private ExpressionExperimentService expressionExperimentService;
    @Autowired
    private ExpressionExperimentSetService experimentSetService;
    @Autowired
    private GeneSearchService geneSearchService;
    @Autowired
    private GeneService geneService;
    @Autowired
    private GeneSetService geneSetService;
    @Autowired
    private OntologyService ontologyService;
    @Autowired
    private PhenotypeAssociationManagerService phenotypeAssociationManagerService;
    @Autowired
    private BioSequenceService bioSequenceService;
    @Autowired
    private CompositeSequenceService compositeSequenceService;

    // TODO: use services instead of DAO here
    @Autowired
    private BlacklistedEntityDao blackListDao;
    @Autowired
    private TaxonService taxonService;

    private final Set<Class<? extends Identifiable>> supportedResultTypes = new HashSet<>();
    private final ConfigurableConversionService resultObjectConversionService = new GenericConversionService();

    /*
     * This is the method used by the main search page.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> search( SearchSettings settings ) throws SearchException {
        return this.search( settings, true /* fill objects */, false /* web speed search */ );
    }

    /*
     * This is only used for gene and gene-set searches?
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> speedSearch( SearchSettings settings ) throws SearchException {
        return this.search( settings, true, true );
    }

    /*
     * Many calls will end up here.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> search( SearchSettings settings, boolean fillObjects,
            boolean webSpeedSearch ) throws SearchException {
        if ( !supportedResultTypes.containsAll( settings.getResultTypes() ) ) {
            throw new IllegalArgumentException( "The search settings contains unsupported result types:" + Sets.difference( settings.getResultTypes(), supportedResultTypes ) + "." );
        }

        StopWatch timer = StopWatch.createStarted();

        Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> results;
        if ( settings.isTermQuery() ) {
            // we only attempt an ontology search if the uri looks remotely like a url.
            results = this.ontologyUriSearch( settings );
        } else {
            results = this.generalSearch( settings, fillObjects, webSpeedSearch );
        }

        Integer totalResults = results.values().stream().map( Collection::size ).reduce( 0, Integer::sum );
        if ( totalResults > 0 ) {
            log.info( "Search for " + settings + " yielded " + totalResults + " results in " + timer.getTime( TimeUnit.MILLISECONDS ) + " ms." );
        }

        return results;

    }

    /*
     * NOTE used via the DataSetSearchAndGrabToolbar -> DatasetGroupEditor
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<Long> searchExpressionExperiments( String query, Long taxonId ) throws SearchException {
        Taxon taxon = null;
        if ( taxonId != null ) {
            taxon = taxonService.load( taxonId );
        }
        Collection<Long> eeIds = new HashSet<>();
        if ( StringUtils.isNotBlank( query ) ) {

            if ( query.length() < SearchServiceImpl.MINIMUM_EE_QUERY_LENGTH )
                return eeIds;

            // Initial list
            List<SearchResult<?>> results = this
                    .search( SearchSettings.expressionExperimentSearch( query, taxon ), false /* no fill */, false /*
                     * speed
                     * search,
                     * irrelevant
                     */ )
                    .get( ExpressionExperiment.class );
            for ( SearchResult result : results ) {
                eeIds.add( result.getResultId() );
            }
        } else if ( taxonId != null ) {
            // get all for taxon
            eeIds = EntityUtils.getIds( expressionExperimentService.findByTaxon( taxon, /* MAX_LUCENE_HITS */ null ) );
        }
        return eeIds;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public <T extends Identifiable> List<SearchResult<T>> search( SearchSettings settings, Class<T> resultClass ) throws SearchException {
        // only search for the requested class
        settings = settings.withResultTypes( Collections.singleton( resultClass ) );

        List<SearchResult<?>> searchResultObjects = this.search( settings ).get( resultClass );

        if ( searchResultObjects == null )
            return Collections.emptyList();

        return searchResultObjects.stream()
                .map( sr -> ( SearchResult<T> ) sr )
                .collect( Collectors.toList() );
    }

    @Override
    public Set<Class<? extends Identifiable>> getSupportedResultTypes() {
        return supportedResultTypes;
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends Identifiable, U extends IdentifiableValueObject<T>> SearchResult<U> loadValueObject( SearchResult<T> searchResult ) throws IllegalArgumentException {
        if ( searchResult.getResultObject() == null ) {
            // that's a valid state of the result is provisional
            return new SearchResult<>( searchResult.getResultClass(), searchResult.getResultId(), searchResult.getScore(), searchResult.getHighlightedText() );
        } else {
            try {
                //noinspection unchecked
                U resultObjectVo = ( U ) resultObjectConversionService.convert( searchResult.getResultObject(), IdentifiableValueObject.class );
                return new SearchResult<>( searchResult.getResultClass(), resultObjectVo, searchResult.getScore(), searchResult.getHighlightedText() );
            } catch ( ConverterNotFoundException e ) {
                throw new IllegalArgumentException( "Result type " + searchResult.getResultClass() + " is not supported for VO conversion.", e );
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchResult<? extends IdentifiableValueObject<? extends Identifiable>>> loadValueObjects( Collection<SearchResult<ExpressionExperiment>> searchResults ) throws IllegalArgumentException {
        return searchResults.stream()
                .map( this::loadValueObject )
                .collect( Collectors.toList() );
    }

    @PostConstruct
    void initializeSearchService() {
        initializeSupportedResultTypes();
        initializeResultObjectConversionService();
        try {
            this.initializeCache();
        } catch ( CacheException e ) {
            throw new RuntimeException( e );
        }
        this.initializeNameToTaxonMap();
    }

    private void initializeSupportedResultTypes() {
        supportedResultTypes.add( ArrayDesign.class );
        supportedResultTypes.add( BibliographicReference.class );
        supportedResultTypes.add( BioSequence.class );
        supportedResultTypes.add( CompositeSequence.class );
        supportedResultTypes.add( ExpressionExperiment.class );
        supportedResultTypes.add( ExpressionExperimentSet.class );
        supportedResultTypes.add( Gene.class );
        supportedResultTypes.add( GeneSet.class );
        supportedResultTypes.add( PhenotypeAssociation.class );
    }

    private void initializeResultObjectConversionService() {
        addVoConverter( ArrayDesign.class, arrayDesignService );
        addVoConverter( BioSequence.class, bioSequenceService );
        // FIXME: this is used for phenotypes, but really we should be using {@link PhenotypeAssociation}
        resultObjectConversionService.addConverter( CharacteristicValueObject.class, IdentifiableValueObject.class, o -> o );
        addVoConverter( CompositeSequence.class, compositeSequenceService );
        addVoConverter( ExpressionExperiment.class, expressionExperimentService );
        addVoConverter( ExpressionExperimentSet.class, experimentSetService );
        addVoConverter( Gene.class, geneService );
        // FIXME: GeneSetService does not implement BaseVoEnabledService
        resultObjectConversionService.addConverter( GeneSet.class, IdentifiableValueObject.class, o -> geneSetService.loadValueObject( ( GeneSet ) o ) );
    }

    private <O extends Identifiable, VO extends IdentifiableValueObject<O>> void addVoConverter( Class<O> fromClass, BaseVoEnabledService<O, VO> service ) {
        //noinspection unchecked
        resultObjectConversionService.addConverter( fromClass, IdentifiableValueObject.class, o -> service.loadValueObject( ( O ) o ) );
    }

    private void initializeCache() throws CacheException {
        this.childTermCache = CacheUtils
                .createOrLoadCache( cacheManager, SearchServiceImpl.ONTOLOGY_CHILDREN_CACHE_NAME,
                        SearchServiceImpl.ONTOLOGY_INFO_CACHE_SIZE, false, false,
                        SearchServiceImpl.ONTOLOGY_CACHE_TIME_TO_IDLE, SearchServiceImpl.ONTOLOGY_CACHE_TIME_TO_DIE );
    }

    /**
     * Checks whether settings have the search genes flag and does the search if needed.
     *
     * @param results the results to which should any new results be accreted.
     */
    private void accreteResultsGenes( List<SearchResult<?>> results, SearchSettings settings, boolean webSpeedSearch ) throws SearchException {
        if ( settings.hasResultType( Gene.class ) ) {
            Collection<SearchResult<Gene>> genes = this.getGenesFromSettings( settings, webSpeedSearch );
            ListUtils.addAllNewElements( results, genes );
        }
    }

    /**
     * Checks settings for all do-search flags, except for gene (see
     * {@link #accreteResultsGenes(List, SearchSettings, boolean)}), and does the search if needed.
     *
     * @param  results        the results to which should any new results be accreted.
     * @param  webSpeedSearch - only used for gene search?
     * @return same object as given, possibly extended by new items from search.
     */
    private List<SearchResult<?>> accreteResultsOthers( List<SearchResult<?>> results, SearchSettings settings,
            boolean webSpeedSearch ) throws SearchException {

        if ( settings.hasResultType( ExpressionExperiment.class ) ) {
            results.addAll( this.expressionExperimentSearch( settings ) );
        }

        Collection<SearchResult<CompositeSequence>> compositeSequences = null;
        if ( settings.hasResultType( CompositeSequence.class ) ) {
            compositeSequences = this.compositeSequenceSearch( settings );
            ListUtils.addAllNewElements( results, compositeSequences );
        }

        if ( settings.hasResultType( ArrayDesign.class ) ) {
            ListUtils.addAllNewElements( results, this.arrayDesignSearch( settings, compositeSequences ) );
        }

        if ( settings.hasResultType( BioSequence.class ) ) {
            Collection<SearchResult<Gene>> genes = this.getGenesFromSettings( settings, webSpeedSearch );
            Collection<SearchResult> bioSequencesAndGenes = this.bioSequenceAndGeneSearch( settings, genes );

            // split results so that accreteResults can be properly typed

            //noinspection unchecked
            Collection<SearchResult<BioSequence>> bioSequences = bioSequencesAndGenes.stream()
                    .filter( result -> BioSequence.class.isAssignableFrom( result.getResultClass() ) )
                    .map( result -> ( SearchResult<BioSequence> ) result )
                    .collect( Collectors.toSet() );
            ListUtils.addAllNewElements( results, bioSequences );

            //noinspection unchecked
            Collection<SearchResult<Gene>> gen = bioSequencesAndGenes.stream()
                    .filter( result -> Gene.class.isAssignableFrom( result.getResultClass() ) )
                    .map( result -> ( SearchResult<Gene> ) result )
                    .collect( Collectors.toSet() );
            ListUtils.addAllNewElements( results, gen );
        }

        if ( settings.getUseGo() ) {
            try {
                ListUtils.addAllNewElements( results, this.dbHitsToSearchResult(
                        geneSearchService.getGOGroupGenes( settings.getQuery(), settings.getTaxon() ), "From GO group" ) );
            } catch ( OntologySearchException e ) {
                throw new BaseCodeOntologySearchException( e );
            }
        }

        if ( settings.hasResultType( BibliographicReference.class ) ) {
            ListUtils.addAllNewElements( results, this.compassSearchSource.searchBibliographicReference( settings ) );
        }

        if ( settings.hasResultType( GeneSet.class ) ) {
            ListUtils.addAllNewElements( results, this.geneSetSearch( settings ) );
        }

        if ( settings.hasResultType( ExpressionExperimentSet.class ) ) {
            ListUtils.addAllNewElements( results, this.experimentSetSearch( settings ) );
        }

        if ( settings.hasResultType( PhenotypeAssociation.class ) ) {
            ListUtils.addAllNewElements( results, this.databaseSearchSource.searchPhenotype( settings ) );
        }

        return results;
    }

    //    /**
    //     * Convert biomaterial hits into their associated ExpressionExperiments
    //     *
    //     * @param results      will go here
    //     * @param biomaterials
    //     */
    //    private void addEEByBiomaterials( Collection<SearchResult> results, Map<BioMaterial, SearchResult> biomaterials ) {
    //        if ( biomaterials.size() == 0 ) {
    //            return;
    //        }
    //        Map<ExpressionExperiment, BioMaterial> ees = expressionExperimentService
    //                .findByBioMaterials( biomaterials.keySet() );
    //        for ( ExpressionExperiment ee : ees.keySet() ) {
    //            SearchResult searchResult = biomaterials.get( ees.get( ee ) );
    //            results.add( new SearchResult( ee, searchResult.getScore() * SearchServiceImpl.INDIRECT_DB_HIT_PENALTY,
    //                    searchResult.getHighlightedText() + " (BioMaterial characteristic)" ) );
    //        }
    //    }
    //
    //    /**
    //     * Convert biomaterial hits into their associated ExpressionExperiments
    //     *
    //     * @param results      will go here
    //     * @param biomaterials
    //     */
    //    private void addEEByBiomaterialIds( Collection<SearchResult> results, Map<Long, SearchResult> biomaterials ) {
    //        if ( biomaterials.size() == 0 ) {
    //            return;
    //        }
    //        Map<ExpressionExperiment, Long> ees = expressionExperimentService
    //                .findByBioMaterialIds( biomaterials.keySet() );
    //        for ( ExpressionExperiment ee : ees.keySet() ) {
    //            SearchResult searchResult = biomaterials.get( ees.get( ee ) );
    //            results.add( new SearchResult( ee, searchResult.getScore() * SearchServiceImpl.INDIRECT_DB_HIT_PENALTY,
    //                    searchResult.getHighlightedText() + " (BioMaterial characteristic)" ) );
    //        }
    //    }
    //
    //    /**
    //     * Convert factorValue hits into their associated ExpressionExperiments
    //     *
    //     * @param results      will go here
    //     * @param factorValues
    //     */
    //    private void addEEByFactorvalueIds( Collection<SearchResult> results, Map<Long, SearchResult> factorValues ) {
    //        if ( factorValues.size() == 0 ) {
    //            return;
    //        }
    //        Map<ExpressionExperiment, Long> ees = expressionExperimentService
    //                .findByFactorValueIds( factorValues.keySet() );
    //        for ( ExpressionExperiment ee : ees.keySet() ) {
    //            SearchResult searchResult = factorValues.get( ees.get( ee ) );
    //            results.add( new SearchResult( ee, searchResult.getScore() * SearchServiceImpl.INDIRECT_DB_HIT_PENALTY,
    //                    searchResult.getHighlightedText() + " (FactorValue characteristic)" ) );
    //        }
    //
    //    }
    //
    //    /**
    //     * Convert factorValue hits into their associated ExpressionExperiments
    //     *
    //     * @param results      will go here
    //     * @param factorValues
    //     */
    //    private void addEEByFactorvalues( Collection<SearchResult> results, Map<FactorValue, SearchResult> factorValues ) {
    //        if ( factorValues.size() == 0 ) {
    //            return;
    //        }
    //        Map<ExpressionExperiment, FactorValue> ees = expressionExperimentService
    //                .findByFactorValues( factorValues.keySet() );
    //        for ( ExpressionExperiment ee : ees.keySet() ) {
    //            SearchResult searchResult = factorValues.get( ees.get( ee ) );
    //            results.add( new SearchResult( ee, searchResult.getScore() * SearchServiceImpl.INDIRECT_DB_HIT_PENALTY,
    //                    searchResult.getHighlightedText() + " (FactorValue characteristic)" ) );
    //        }
    //
    //    }

    private void addTerms( Taxon taxon, String taxonName ) {
        String[] terms;
        if ( StringUtils.isNotBlank( taxonName ) ) {
            terms = taxonName.split( "\\s+" );
            // Only continue for multi-word
            if ( terms.length > 1 ) {
                for ( String s : terms ) {
                    if ( !nameToTaxonMap.containsKey( s.trim().toLowerCase() ) ) {
                        nameToTaxonMap.put( s.trim().toLowerCase(), taxon );
                    }
                }
            }
        }
    }

    private Collection<SearchResult<ExpressionExperimentSet>> experimentSetSearch( SearchSettings settings ) throws SearchException {
        Collection<SearchResult<ExpressionExperimentSet>> results = this
                .dbHitsToSearchResult( this.experimentSetService.findByName( settings.getQuery() ), null );

        results.addAll( this.compassSearchSource.searchExperimentSet( settings ) );
        return results;
    }

    /**
     * A general search for array designs.
     * This search does both an database search and a compass search. This is also contains an underlying
     * {@link CompositeSequence} search, returning the {@link ArrayDesign} collection for the given composite sequence
     * search string (the returned collection of array designs does not contain duplicates).
     *
     * @param probeResults Collection of results from a previous CompositeSequence search. Can be null; otherwise used
     *                     to avoid a second search for probes. The array designs for the probes are added to the final
     *                     results.
     */
    private Collection<SearchResult<ArrayDesign>> arrayDesignSearch( SearchSettings settings,
            Collection<SearchResult<CompositeSequence>> probeResults ) throws SearchException {

        StopWatch watch = StopWatch.createStarted();
        String searchString = settings.getQuery();
        Collection<SearchResult<ArrayDesign>> results = new HashSet<>();

        ArrayDesign shortNameResult = arrayDesignService.findByShortName( searchString );
        if ( shortNameResult != null ) {
            results.add( new SearchResult<>( shortNameResult, 1.0 ) );
            return results;
        }

        Collection<ArrayDesign> nameResult = arrayDesignService.findByName( searchString );
        if ( nameResult != null && !nameResult.isEmpty() ) {
            for ( ArrayDesign ad : nameResult ) {
                results.add( new SearchResult<>( ad, 1.0 ) );
            }
            return results;
        }

        BlacklistedEntity b = blackListDao.findByAccession( searchString );
        if ( b != null ) {
            // FIXME: I'm not sure the ID is a good thing to put here
            results.add( new SearchResult<>( ArrayDesign.class, b.getId(), 1.0, "Blacklisted accessions are not loaded into Gemma" ) );
            return results;
        }

        Collection<ArrayDesign> altNameResults = arrayDesignService.findByAlternateName( searchString );
        for ( ArrayDesign arrayDesign : altNameResults ) {
            results.add( new SearchResult<>( arrayDesign, 0.9 ) );
        }

        Collection<ArrayDesign> manufacturerResults = arrayDesignService.findByManufacturer( searchString );
        for ( ArrayDesign arrayDesign : manufacturerResults ) {
            results.add( new SearchResult<>( arrayDesign, 0.9 ) );
        }

        /*
         * FIXME: add merged platforms and subsumers
         */

        results.addAll( this.compassSearchSource.searchArrayDesign( settings ) );
        results.addAll( this.databaseSearchSource.searchArrayDesign( settings ) );

        Collection<SearchResult<CompositeSequence>> probes;
        if ( probeResults == null ) {
            //noinspection unchecked
            probes = this.compassSearchSource.searchCompositeSequenceAndGene( settings ).stream()
                    // strip all the gene results
                    .filter( result -> CompositeSequence.class.isAssignableFrom( result.getResultClass() ) )
                    .map( result -> ( SearchResult<CompositeSequence> ) result )
                    .collect( Collectors.toSet() );
        } else {
            probes = probeResults;
        }

        for ( SearchResult<CompositeSequence> r : probes ) {
            CompositeSequence cs = r.getResultObject();
            if ( cs.getArrayDesign() == null ) // This might happen as compass
                // might not have indexed the AD
                // for the CS
                continue;
            results.add( new SearchResult<>( cs.getArrayDesign(), r.getScore() ) );
        }

        watch.stop();
        if ( watch.getTime() > 1000 )
            SearchServiceImpl.log.info( "Array Design search for '" + settings + "' took " + watch.getTime() + " ms" );

        return results;
    }

    /**
     * @param previousGeneSearchResults Can be null, otherwise used to avoid a second search for genes. The biosequences
     *                                  for the genes are added to the final results.
     */
    private Collection<SearchResult> bioSequenceAndGeneSearch( SearchSettings settings,
            Collection<SearchResult<Gene>> previousGeneSearchResults ) throws SearchException {
        StopWatch watch = StopWatch.createStarted();

        Collection<SearchResult> searchResults = new HashSet<>();
        searchResults.addAll( this.compassSearchSource.searchBioSequenceAndGene( settings, previousGeneSearchResults ) );
        searchResults.addAll( this.databaseSearchSource.searchBioSequenceAndGene( settings, previousGeneSearchResults ) );

        watch.stop();
        if ( watch.getTime() > 1000 )
            SearchServiceImpl.log
                    .info( "Biosequence search for '" + settings + "' took " + watch.getTime() + " ms " + searchResults
                            .size() + " results." );

        return searchResults;
    }

    /**
     * Search via characteristics i.e. ontology terms.
     *
     * This is an important type of search but also a point of performance issues. Searches for "specific" terms are
     * generally not a big problem (yielding less than 100 results); searches for "broad" terms can return numerous
     * (thousands)
     * results.
     */
    private Collection<SearchResult<ExpressionExperiment>> characteristicEESearch( final SearchSettings settings ) throws SearchException {

        Collection<SearchResult<ExpressionExperiment>> results = new HashSet<>();

        StopWatch watch = StopWatch.createStarted();

        log.info( "Starting EE search for " + settings.getQuery() );
        String[] subclauses = settings.getQuery().split( " OR " );
        for ( String subclause : subclauses ) {
            /*
             * Note that the AND is applied only within one entity type. The fix would be to apply AND at this
             * level.
             */
            Collection<SearchResult<ExpressionExperiment>> classResults = this
                    .characteristicEESearchWithChildren( subclause, settings.getTaxon(), settings.getMaxResults() );
            if ( classResults.size() > 0 ) {
                log.info( "... Found " + classResults.size() + " EEs matching " + subclause );
            }
            results.addAll( classResults );
        }

        SearchServiceImpl.log
                .info( "ExpressionExperiment search: " + settings + " -> " + results.size() + " characteristic-based hits "
                        + watch.getTime() + " ms" );

        return results;

    }

    /**
     * Perform a Experiment search based on annotations (anchored in ontology terms) - it does not have to be one word,
     * it could be "parkinson's disease"; it can also be a URI.
     *
     * @param  query string
     * @param  t     taxon to limit on, can be null
     * @param  limit stop querying if we hit or surpass this limit. 0 for no limit.
     * @return collection of SearchResults (Experiments)
     */
    private Collection<SearchResult<ExpressionExperiment>> characteristicEESearchTerm( String query, Taxon t, int limit ) throws SearchException {

        StopWatch watch = StopWatch.createStarted();
        Collection<SearchResult<ExpressionExperiment>> results = new HashSet<>();

        // Phase 1: We first search for individuals.
        Map<String, String> uri2value = new HashMap<>();
        Collection<OntologyIndividual> individuals = null;
        try {
            individuals = ontologyService.findIndividuals( query );
        } catch ( OntologySearchException e ) {
            throw new BaseCodeOntologySearchException( e );
        }
        for ( Collection<OntologyIndividual> individualbatch : BatchIterator.batches( individuals, 10 ) ) {
            Collection<String> uris = new HashSet<>();
            for ( OntologyIndividual individual : individualbatch ) {
                uris.add( individual.getUri() );
                uri2value.put( individual.getUri(), individual.getLabel() );
            }

            findExperimentsByUris( uris, results, t, limit, uri2value );
            if ( limit > 0 && results.size() > limit ) {
                break;
            }
        } // end phase 1

        if ( results.size() > 0 && watch.getTime() > 500 ) {
            SearchServiceImpl.log.info( "Found " + individuals.size() + " experiments matching '" + query
                    + "' via characteristic terms (individuals) in " + watch.getTime() + "ms" );
        }

        if ( limit > 0 && results.size() >= limit ) {
            return results;
        }

        /*
         * Phase 2: Search ontology classes matches to the query
         */
        Collection<OntologyTerm> matchingTerms = null;
        try {
            matchingTerms = ontologyService.findTerms( query );
        } catch ( OntologySearchException e ) {
            throw new BaseCodeOntologySearchException( "Failed to find terms via ontology search.", e );
        }

        if ( watch.getTime() > 500 ) {
            SearchServiceImpl.log
                    .info( "Found " + matchingTerms.size() + " ontology classes matching '" + query + "' in "
                            + watch.getTime() + "ms" );
        }

        /*
         * Search for child terms.
         */
        if ( !matchingTerms.isEmpty() ) {
            Collection<OntologyTerm> seenTerms = new HashSet<>();
            watch.reset();
            watch.start();

            for ( OntologyTerm term : matchingTerms ) {
                /*
                 * In this loop, each term is a match directly to our query, and we do a depth-first fetch of the
                 * children.
                 */
                String uri = term.getUri();
                if ( StringUtils.isBlank( uri ) )
                    continue;

                if ( seenTerms.contains( term ) )
                    continue;

                uri2value.put( uri, term.getLabel() );

                // query current term before going to children
                int sizeBefore = results.size();
                findExperimentsByUris( Collections.singleton( uri ), results, t, limit, uri2value );

                if ( limit > 0 && results.size() >= limit ) break;

                this.getCharacteristicsAnnotatedToChildren( term, results, seenTerms, t, limit );

                seenTerms.add( term );

                if ( SearchServiceImpl.log.isDebugEnabled() && results.size() > sizeBefore ) {
                    SearchServiceImpl.log
                            .debug( ( results.size() - sizeBefore ) + " characteristics matching children term of "
                                    + term );
                }

                if ( limit > 0 && results.size() >= limit ) {
                    break;
                }
            }

            if ( watch.getTime() > 1000 ) {
                SearchServiceImpl.log.info( "Found " + results.size() + " characteristics for '" + query
                        + "' including child terms in " + watch.getTime() + "ms" );
            }
            watch.reset();
            watch.start();

        }

        if ( watch.getTime() > 500 ) {
            SearchServiceImpl.log
                    .info( "Retrieved " + results.size() + " entities via characteristics for '" + query
                            + "' in " + watch.getTime() + "ms" );
        }

        return results;
    }

    private void findExperimentsByUris( Collection<String> uris, Collection<SearchResult<ExpressionExperiment>> results, Taxon t, int limit,
            Map<String, String> uri2value ) {
        Map<Class<?>, Map<String, Collection<Long>>> hits = characteristicService.findExperimentsByUris( uris, t, limit );

        for ( Class<?> clazz : hits.keySet() ) {
            for ( String uri : hits.get( clazz ).keySet() ) {
                for ( Long eeID : hits.get( clazz ).get( uri ) ) {
                    String matchedText = "Tagged term: <a href=\"" + Settings.getRootContext()
                            + "/searcher.html?query=" + uri + "\">" + uri2value.get( uri ) + "</a> ";
                    if ( !clazz.isAssignableFrom( ExpressionExperiment.class ) ) {
                        matchedText = matchedText + " via " + clazz.getSimpleName();
                    }
                    SearchResult<ExpressionExperiment> sr = new SearchResult<>( ExpressionExperiment.class, eeID, 1.0, matchedText );
                    results.add( sr );
                    if ( limit > 0 && results.size() >= limit ) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Search for the Experiment query in ontologies, including items that are associated with children of matching
     * query terms. That is, 'brain' should return entities tagged as 'hippocampus'. It can handle AND in searches, so
     * Parkinson's
     * AND neuron finds items tagged with both of those terms. The use of OR is handled by the caller.
     *
     * @param  query string
     * @param  t     taxon, can be null
     * @param  limit try to stop searching if we exceed this (0 for no limit)
     * @return SearchResults of Experiments
     */
    private Collection<SearchResult<ExpressionExperiment>> characteristicEESearchWithChildren( String query, Taxon t, int limit ) throws SearchException {
        StopWatch watch = StopWatch.createStarted();

        /*
         * The tricky part here is if the user has entered a boolean query. If they put in Parkinson's disease AND
         * neuron, then we want to eventually return entities that are associated with both. We don't expect to find
         * single characteristics that match both.
         *
         * But if they put in Parkinson's disease we don't want to do two queries.
         */
        String[] subparts = query.split( " AND " );

        // we would have to first deal with the separate queries, and then apply the logic.
        Collection<SearchResult<ExpressionExperiment>> allResults = new HashSet<>();

        SearchServiceImpl.log
                .info( "Starting characteristic search: '" + query );
        for ( String rawTerm : subparts ) {
            String trimmed = StringUtils.strip( rawTerm );
            if ( StringUtils.isBlank( trimmed ) ) {
                continue;
            }
            Collection<SearchResult<ExpressionExperiment>> subqueryResults = this.characteristicEESearchTerm( trimmed, t, limit );
            if ( allResults.isEmpty() ) {
                allResults.addAll( subqueryResults );
            } else {
                // this is our Intersection operation.
                allResults.retainAll( subqueryResults );

                // aggregate the highlighted text.
                Map<SearchResult, String> highlights = new HashMap<>();
                for ( SearchResult sqr : subqueryResults ) {
                    highlights.put( sqr, sqr.getHighlightedText() );
                }

                for ( SearchResult ar : allResults ) {
                    String k = highlights.get( ar );
                    if ( StringUtils.isNotBlank( k ) ) {
                        String highlightedText = ar.getHighlightedText();
                        if ( StringUtils.isBlank( highlightedText ) ) {
                            ar.setHighlightedText( k );
                        } else {
                            ar.setHighlightedText( highlightedText + "," + k );
                        }
                    }
                }
            }

            if ( watch.getTime() > 1000 ) {
                SearchServiceImpl.log.info( "Characteristic EE search for '" + rawTerm + "': " + allResults.size()
                        + " hits retained so far; " + watch.getTime() + "ms" );
                watch.reset();
                watch.start();
            }

            if ( limit > 0 && allResults.size() > limit ) {
                return allResults;
            }

        }

        return allResults;

    }

    /**
     * Search by name of the composite sequence as well as gene.
     */
    private Collection<SearchResult<CompositeSequence>> compositeSequenceSearch( SearchSettings settings ) throws SearchException {

        StopWatch watch = StopWatch.createStarted();

        /*
         * FIXME: this at least partly ignores any array design that was set as a restriction, especially in a gene
         * search.
         */

        // Skip compass searching of composite sequences because it only bloats the results.
        Collection<SearchResult> compositeSequenceResults = new HashSet<>( this.databaseSearchSource.searchCompositeSequenceAndGene( settings ) );

        /*
         * This last step is needed because the compassSearch for compositeSequences returns bioSequences too.
         */
        Collection<SearchResult<CompositeSequence>> finalResults = new HashSet<>();
        for ( SearchResult sr : compositeSequenceResults ) {
            if ( CompositeSequence.class.isAssignableFrom( sr.getResultClass() ) ) {
                //noinspection unchecked
                finalResults.add( ( SearchResult<CompositeSequence> ) sr );
            }
        }

        watch.stop();
        if ( watch.getTime() > 1000 )
            SearchServiceImpl.log
                    .info( "Composite sequence search for '" + settings + "' took " + watch.getTime() + " ms, "
                            + finalResults.size() + " results." );
        return finalResults;
    }

    //    private List<SearchResult> convertEntitySearchResutsToValueObjectsSearchResults(
    //            Collection<SearchResult> searchResults ) {
    //        List<SearchResult> convertedSearchResults = new ArrayList<>();
    //        StopWatch t = this.startTiming();
    //        for ( SearchResult searchResult : searchResults ) {
    //            // this is a special case ... for some reason.
    //            if ( BioSequence.class.isAssignableFrom( searchResult.getResultClass() ) ) {
    //                SearchResult convertedSearchResult = new SearchResult( BioSequenceValueObject
    //                        .fromEntity( bioSequenceService.thaw( ( BioSequence ) searchResult.getResultObject() ) ),
    //                        searchResult.getScore(), searchResult.getHighlightedText() );
    //                convertedSearchResults.add( convertedSearchResult );
    //            } else {
    //                convertedSearchResults.add( searchResult );
    //            }
    //        }
    //        if ( t.getTime() > 500 ) {
    //            log.info( "Conversion of " + searchResults.size() + " search results: " + t.getTime() + "ms" );
    //        }
    //        return convertedSearchResults;
    //    }

    //    /**
    //     * Takes a list of ontology terms, and classes of objects of interest to be returned. Looks through the
    //     * characteristic table for an exact match with the given ontology terms. Only tries to match the uri's.
    //     *
    //     * @param  classes Class of objects to restrict the search to (typically ExpressionExperiment.class, for
    //     *                 example).
    //     * @param  terms   A list of ontology terms to search for
    //     * @return         Collection of search results for the objects owning the found characteristics, where the owner is
    //     *                 of
    //     *                 class clazz
    //     */
    //    private Collection<SearchResult> databaseCharacteristicExactUriSearchForOwners( Collection<Class<?>> classes,
    //            Collection<OntologyTerm> terms ) {
    //
    //        // Collection<Characteristic> characteristicValueMatches = new ArrayList<Characteristic>();
    //        Collection<Characteristic> characteristicURIMatches = new ArrayList<>();
    //
    //        for ( OntologyTerm term : terms ) {
    //            // characteristicValueMatches.addAll( characteristicService.findByValue( term.getUri() ));
    //            characteristicURIMatches.addAll( characteristicService.findByUri( classes, term.getUri() ) );
    //        }
    //
    //        Map<Characteristic, Object> parentMap = characteristicService.getParents( classes, characteristicURIMatches );
    //        // parentMap.putAll( characteristicService.getParents(characteristicValueMatches ) );
    //
    //        return this.filterCharacteristicOwnersByClass( classes, parentMap );
    //    }

    //    /**
    //     * Convert characteristic hits from database searches into SearchResults.
    //     * @param entities map of classes to characteristics e.g. Experiment.class -> annotated characteristics
    //     * @param matchText used in highlighting
    //     * 
    //     *  FIXME we need the ID of the annotated object if we do it this way
    //     */
    //    private Collection<SearchResult> dbCharacteristicHitsToSearchResultByClass( Map<Class<?>, Collection<Characteristic>> entities,
    //            String matchText ) {
    //        //   return this.dbHitsToSearchResult( entities, null, matchText );
    //
    //        List<SearchResult> results = new ArrayList<>();
    //        for ( Class<?> clazz : entities.keySet() ) {
    //
    //            for ( Characteristic c : entities.get( clazz ) ) {
    //                SearchResult esr = new SearchResult(clazz, /*ID NEEDED*/ , 1.0, matchText );
    //             
    //                results.add( esr );
    //            }
    //
    //        }
    //        return results;
    //
    //    }

    /**
     * Convert hits from database searches into SearchResults.
     */
    private <T extends Identifiable> Collection<SearchResult<T>> dbHitsToSearchResult( Collection<T> entities, String matchText ) {
        StopWatch watch = StopWatch.createStarted();
        List<SearchResult<T>> results = new ArrayList<>();
        for ( T e : entities ) {
            if ( e == null ) {
                if ( log.isDebugEnabled() )
                    log.debug( "Null search result object" );
                continue;
            }
            if ( e.getId() == null ) {
                log.warn( "Search result object with null ID." );
            }
            SearchResult<T> esr = this.dbHitToSearchResult( e, matchText );
            results.add( esr );
        }
        if ( watch.getTime() > 1000 ) {
            log.info( "Unpack " + results.size() + " search resultsS: " + watch.getTime() + "ms" );
        }
        return results;
    }

    /**
     * @param text that matched the query (for highlighting)
     */
    private <T extends Identifiable> SearchResult<T> dbHitToSearchResult( T e, String text ) {
        SearchResult<T> esr;
        esr = new SearchResult<>( e, 1.0, text );
        log.debug( esr );
        return esr;
    }

    //    private void debugParentFetch( Map<Characteristic, Object> parentMap ) {
    //        /*
    //         * This is purely debugging.
    //         */
    //        if ( parentMap.size() > 0 ) {
    //            if ( SearchServiceImpl.log.isDebugEnabled() )
    //                SearchServiceImpl.log.debug( "Found " + parentMap.size() + " owners for " + parentMap.keySet().size()
    //                        + " characteristics:" );
    //        }
    //    }

    /**
     * A key method for experiment search. This search does both an database search and a compass search, and looks at
     * several different associations. To allow maximum flexibility, we try not to limit the number of results here (it
     * can be done via the settings object)
     *
     * If the search matches a GEO ID, short name or full name of an experiment, the search ends. Otherwise, we search
     * free-text indices and ontology annotations.
     *
     * @param  settings object; the maximum results can be set here but also has a default value defined by
     *                  SearchSettings.DEFAULT_MAX_RESULTS_PER_RESULT_TYPE
     * @return          {@link Collection} of SearchResults
     */
    private Collection<SearchResult<ExpressionExperiment>> expressionExperimentSearch( final SearchSettings settings ) throws SearchException {

        StopWatch totalTime = StopWatch.createStarted();
        StopWatch watch = StopWatch.createStarted();

        SearchServiceImpl.log.info( ">>>>> Starting search for '" + settings + "'" );

        Collection<SearchResult<ExpressionExperiment>> results = new HashSet<>();

        // searches for GEO names, etc - "exact" matches.
        if ( settings.getUseDatabase() ) {
            results.addAll( this.databaseSearchSource.searchExpressionExperiment( settings ) );
            if ( watch.getTime() > 500 )
                SearchServiceImpl.log
                        .info( "Expression Experiment database search for '" + settings + "' took " + watch.getTime()
                                + " ms, " + results.size() + " hits." );

            /*
             * If we get results here, probably we want to just stop immediately, because the user is searching for
             * something exact. In response to https://github.com/PavlidisLab/Gemma/issues/140 we continue if the user
             * has admin status.
             */
            if ( !results.isEmpty() && !SecurityUtil.isUserAdmin() ) {
                return results;
            }

            BlacklistedEntity b = blackListDao.findByAccession( settings.getQuery() );
            if ( b != null ) {
                results.add( new SearchResult<>( ExpressionExperiment.class, b.getId(), 1.0, "Blacklisted accessions are not loaded into Gemma" ) );
                return results;
            }

            watch.reset();
            watch.start();
        }

        // special case: search for experiments associated with genes 
        Collection<SearchResult<Gene>> geneHits = this.geneSearch( settings, true );
        if ( geneHits.size() > 0 ) {
            // TODO: make sure this is being hit correctly.
            for ( SearchResult<Gene> gh : geneHits ) {
                Gene g = gh.getResultObject();
                Integer ncbiGeneId = g.getNcbiGeneId();
                String geneUri = "http://" + NCBI_GENE + "/" + ncbiGeneId; // this is just enough to fool the search into looking by NCBI ID, but check working as expected
                SearchSettings gss = SearchSettings.expressionExperimentSearch( geneUri );
                gss.setMaxResults( settings.getMaxResults() );
                gss.setTaxon( settings.getTaxon() );
                gss.setQuery( geneUri );
                Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> eehits = ontologyUriSearch( gss );
                if ( eehits.containsKey( ExpressionExperiment.class ) ) {
                    // FIXME: there should be a nicer, typed way of doing ontology searches
                    //noinspection unchecked
                    results.addAll( eehits.get( ExpressionExperiment.class ).stream()
                            .map( sr -> ( SearchResult<ExpressionExperiment> ) sr )
                            .collect( Collectors.toSet() ) );
                }
                // possibly short-circuit rest of query.
            }
        }

        // fancy search that uses ontologies to infer related terms
        if ( settings.getUseCharacteristics() ) {
            results.addAll( this.characteristicEESearch( settings ) );
            if ( watch.getTime() > 500 )
                SearchServiceImpl.log
                        .info( "Expression Experiment search via characteristics for '" + settings + "' took " + watch.getTime()
                                + " ms, " + results.size() + " hits." );
            watch.reset();
            watch.start();
        }

        // searches for strings in associated free text including factorvalues and biomaterials 
        // we have toyed with having this be done before the characteristic search
        if ( settings.getUseIndices() && results.size() < settings.getMaxResults() ) {
            results.addAll( this.compassSearchSource.searchExpressionExperiment( settings ) );
            if ( watch.getTime() > 500 )
                SearchServiceImpl.log
                        .info( "Expression Experiment index search for '" + settings + "' took " + watch.getTime()
                                + " ms, " + results.size() + " hits." );
            watch.reset();
            watch.start();
        }

        /*
         * this should be unnecessary we we hit bibrefs in our regular lucene-index search. Also as written, this is
         * very slow
         */
        //        // possibly keep looking
        //        if ( results.size() == 0 ) { // 
        //            watch.reset();
        //            watch.start();
        //            log.info( "Searching for experiments via publications..." );
        //            List<BibliographicReferenceValueObject> bibrefs = bibliographicReferenceService
        //                    .search( settings.getQuery() );
        //
        //            if ( !bibrefs.isEmpty() ) {
        //                log.info( "... found " + bibrefs.size() + " papers matching " + settings.getQuery() );
        ////                Collection<BibliographicReference> refs = new HashSet<>();
        ////                // this seems like an extra 
        ////                Collection<SearchResult> r = this.compassBibliographicReferenceSearch( settings );
        ////                for ( SearchResult searchResult : r ) {
        ////                    refs.add( ( BibliographicReference ) searchResult.getResultObject() );
        ////                }
        //
        //                Map<BibliographicReference, Collection<ExpressionExperiment>> relatedExperiments = this.bibliographicReferenceService
        //                        .getRelatedExperiments( bibrefs );
        //                for ( Entry<BibliographicReference, Collection<ExpressionExperiment>> e : relatedExperiments
        //                        .entrySet() ) {
        //                    results.addAll( this.dbHitsToSearchResult( e.getValue(), null ) );
        //                }
        //                if ( watch.getTime() > 500 )
        //                    SearchServiceImpl.log
        //                            .info( "... Publication search for took " + watch
        //                                    .getTime() + " ms, " + results.size() + " hits" );
        //             
        //            }
        //        }

        /*
         * Find data sets that match a platform. This will probably only be trigged if the search is for a GPL id. NOTE:
         * we may want to move this sooner, but we don't want to slow down the process if they are not searching by
         * array design
         */
        if ( results.size() == 0 ) {
            watch.reset();
            watch.start();
            Collection<SearchResult<ArrayDesign>> matchingPlatforms = this.arrayDesignSearch( settings, null );
            for ( SearchResult adRes : matchingPlatforms ) {
                if ( adRes.getResultObject() instanceof ArrayDesign ) {
                    ArrayDesign ad = ( ArrayDesign ) adRes.getResultObject();
                    Collection<ExpressionExperiment> expressionExperiments = this.arrayDesignService
                            .getExpressionExperiments( ad );
                    if ( expressionExperiments.size() > 0 )
                        results.addAll( this.dbHitsToSearchResult( expressionExperiments,
                                ad.getShortName() + " - " + ad.getName() ) );
                }
            }
            if ( watch.getTime() > 500 )
                SearchServiceImpl.log
                        .info( "Expression Experiment platform search for '" + settings + "' took " + watch.getTime()
                                + " ms, " + results.size() + " hits." );

            if ( !results.isEmpty() ) {
                return results;
            }
        }

        if ( totalTime.getTime() > 500 )
            SearchServiceImpl.log
                    .info( ">>>>>>> Expression Experiment search for '" + settings + "' took " + watch.getTime()
                            + " ms, " + results.size() + " hits." );

        return results;

    }

    //    /**
    //     *
    //     * @param  classes
    //     * @param  characteristic2entity
    //     * @return
    //     */
    //    private Collection<SearchResult> filterCharacteristicOwnersByClass( Map<Class<?>, Collection<Long>> parents, String uri, String value ) {
    //
    //        StopWatch t = this.startTiming();
    //        Map<Long, SearchResult> biomaterials = new HashMap<>();
    //        Map<Long, SearchResult> factorValues = new HashMap<>();
    //        Collection<SearchResult> results = new HashSet<>();
    //
    //        for ( Class<?> clazz : parents.keySet() ) {
    //            for ( Long id : parents.get( clazz ) ) {
    //                String matchedText;
    //
    //                if ( StringUtils.isNotBlank( uri ) ) {
    //                    matchedText = "Tagged term: <a href=\"" + Settings.getRootContext() + "/searcher.html?query=" + uri + "\">" + value + "</a>";
    //                } else {
    //                    matchedText = "Free text: " + value;
    //                }
    //
    //                if ( clazz.isAssignableFrom( BioMaterial.class ) ) {
    //                    biomaterials.put( id, new SearchResult( clazz, id, 1.0, matchedText ) );
    //                } else if ( clazz.isAssignableFrom( FactorValue.class ) ) {
    //                    factorValues.put( id, new SearchResult( clazz, id, 1.0, matchedText ) );
    //                } else if ( clazz.isAssignableFrom( ExpressionExperiment.class ) ) {
    //                    results.add( new SearchResult( clazz, id, 1.0, matchedText ) );
    //                } else {
    //                    throw new IllegalStateException();
    //                }
    //            }
    //
    //        }
    //
    //        this.addEEByFactorvalueIds( results, factorValues );
    //
    //        this.addEEByBiomaterialIds( results, biomaterials );
    //
    //        if ( t.getTime() > 500 ) {
    //            log.info( "Retrieving experiments associated with characteristics:  " + t.getTime() + "ms" );
    //        }
    //
    //        return results;
    //
    //    }

    /**
     * Makes no attempt at resolving the search query as a URI. Will tokenize the search query if there are control
     * characters in the String. URI's will get parsed into multiple query terms and lead to bad results.
     *
     * @param settings       Will try to resolve general terms like brain --> to appropriate OntologyTerms and search
     *                       for
     *                       objects tagged with those terms (if isUseCharacte = true)
     * @param fillObjects    If false, the entities will not be filled in inside the searchsettings; instead, they will
     *                       be
     *                       nulled (for security purposes). You can then use the id and Class stored in the
     *                       SearchSettings to load the
     *                       entities at your leisure. If true, the entities are loaded in the usual secure fashion.
     *                       Setting this to
     *                       false can be an optimization if all you need is the id. Note: filtering by taxon will not
     *                       be done unless
     *                       objects are filled
     * @param webSpeedSearch if true, this call is probably coming from a web app combo box and results will be limited
     *                       to improve speed
     */
    private Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> generalSearch( SearchSettings settings, boolean fillObjects,
            boolean webSpeedSearch ) throws SearchException {

        settings = SearchSettingsStringUtils.processSettings( settings, this.nameToTaxonMap );

        List<SearchResult<?>> rawResults = new ArrayList<>();

        // do gene first first before we munge the query too much.
        this.accreteResultsGenes( rawResults, settings, webSpeedSearch );

        // some strings of size 1 cause lucene to barf and they were slipping through in multi-term queries, get rid of
        // them
        settings.setQuery( SearchSettingsStringUtils.stripShortTerms( settings.getQuery() ) );

        // If nothing to search return nothing.
        if ( StringUtils.isBlank( settings.getQuery() ) ) {
            return new HashMap<>();
        }

        //noinspection ConstantConditions
        rawResults = this.accreteResultsOthers( rawResults, settings, webSpeedSearch );

        return this.getSortedLimitedResults( settings, rawResults, fillObjects );
    }

    /**
     * Combines compass style search, the db style search, and the compositeSequence search and returns 1 combined list
     * with no duplicates.
     *
     * @param returnOnDbHit if true and if there is a match for a gene from the database, return immediately - much
     *                      faster
     */
    private Collection<SearchResult<Gene>> geneSearch( final SearchSettings settings, boolean returnOnDbHit ) throws SearchException {

        StopWatch watch = StopWatch.createStarted();

        String searchString = settings.getQuery();

        Collection<SearchResult<Gene>> geneDbList = this.databaseSearchSource.searchGene( settings );

        if ( returnOnDbHit && geneDbList.size() > 0 ) {
            return geneDbList;
        }

        Set<SearchResult<Gene>> combinedGeneList = new HashSet<>( geneDbList );

        Collection<SearchResult<Gene>> geneCompassList = this.compassSearchSource.searchGene( settings );
        combinedGeneList.addAll( geneCompassList );

        if ( combinedGeneList.isEmpty() ) {
            Collection<SearchResult> geneCsList = this.databaseSearchSource.searchCompositeSequenceAndGene( settings );
            for ( SearchResult res : geneCsList ) {
                if ( Gene.class.isAssignableFrom( res.getResultClass() ) )
                    //noinspection unchecked
                    combinedGeneList.add( ( SearchResult<Gene> ) res );
            }
        }

        /*
         * Possibly search for genes linked via a phenotype, but only if we don't have anything here.
         *
         */
        if ( combinedGeneList.isEmpty() ) {
            Collection<CharacteristicValueObject> phenotypeTermHits = null;
            try {
                phenotypeTermHits = this.phenotypeAssociationManagerService
                        .searchInDatabaseForPhenotype( settings.getQuery() );
            } catch ( OntologySearchException e ) {
                throw new BaseCodeOntologySearchException( e );
            }

            for ( CharacteristicValueObject phenotype : phenotypeTermHits ) {
                Set<String> phenotypeUris = new HashSet<>();
                phenotypeUris.add( phenotype.getValueUri() );

                // DATABASE HIT!
                Collection<GeneEvidenceValueObject> phenotypeGenes = phenotypeAssociationManagerService
                        .findCandidateGenes( phenotypeUris, settings.getTaxon() );

                if ( !phenotypeGenes.isEmpty() ) {
                    SearchServiceImpl.log
                            .info( phenotypeGenes.size() + " genes associated with " + phenotype + " (via query='"
                                    + settings.getQuery() + "')" );

                    for ( GeneEvidenceValueObject gvo : phenotypeGenes ) {
                        Gene g = Gene.Factory.newInstance();
                        g.setId( gvo.getId() );
                        g.setTaxon( settings.getTaxon() );
                        SearchResult<Gene> sr = new SearchResult<>( g );
                        sr.setHighlightedText( phenotype.getValue() + " (" + phenotype.getValueUri() + ")" );

                        // if ( gvo.getScore() != null ) {
                        // TODO If we get evidence quality, use that in the score.
                        // }
                        sr.setScore( 1.0 ); // maybe lower, if we do this search when combinedGeneList is nonempty.
                        combinedGeneList.add( sr );
                    }
                    if ( combinedGeneList.size() > 100 /* some limit */ ) {
                        break;
                    }
                }
            }
        }

        if ( watch.getTime() > 1000 )
            SearchServiceImpl.log
                    .info( "Gene search for " + searchString + " took " + watch.getTime() + " ms; " + combinedGeneList
                            .size() + " results." );
        return combinedGeneList;
    }

    private Collection<SearchResult<GeneSet>> geneSetSearch( SearchSettings settings ) throws SearchException {
        Collection<SearchResult<GeneSet>> hits;
        if ( settings.getTaxon() != null ) {
            hits = this
                    .dbHitsToSearchResult( this.geneSetService.findByName( settings.getQuery(), settings.getTaxon() ),
                            null );
        } else {
            hits = this.dbHitsToSearchResult( this.geneSetService.findByName( settings.getQuery() ), null );
        }

        hits.addAll( this.compassSearchSource.searchGeneSet( settings ) );
        return hits;
    }

    //    /**
    //     * Given classes to search and characteristics (experiment search)
    //     *
    //     * @param classes Which classes of entities to look for
    //     */
    //    private Collection<SearchResult> getAnnotatedEntities( Collection<Class<?>> classes,
    //            Collection<Characteristic> cs ) {
    //
    //        //  time-critical
    //        Map<Characteristic, Object> characteristic2entity = characteristicService.getParents( classes, cs );
    //        Collection<SearchResult> matchedEntities = this
    //                .filterCharacteristicOwnersByClass( classes, characteristic2entity );
    //
    //        if ( SearchServiceImpl.log.isDebugEnabled() ) {
    //            this.debugParentFetch( characteristic2entity );
    //        }
    //        return matchedEntities;
    //    }

    /**
     * Recursively
     */
    private void getCharacteristicsAnnotatedToChildren( OntologyTerm term,
            Collection<SearchResult<ExpressionExperiment>> results, Collection<OntologyTerm> seenTerms, Taxon t, int limit ) {

        Collection<OntologyTerm> children = this.getDirectChildTerms( term );
        if ( children.isEmpty() ) {
            return;
        }

        Map<String, String> uri2value = new HashMap<>();
        Collection<String> uris = new ArrayList<>();
        for ( OntologyTerm ontologyTerm : children ) {
            if ( ontologyTerm.getUri() == null )
                continue;
            if ( seenTerms.contains( ontologyTerm ) )
                continue;
            uris.add( ontologyTerm.getUri() );
            uri2value.put( ontologyTerm.getUri(), ontologyTerm.getLabel() );
            seenTerms.add( ontologyTerm );
        }

        if ( uris.isEmpty() ) {
            return;
        }

        findExperimentsByUris( uris, results, t, limit, uri2value );
        if ( limit > 0 && results.size() >= limit ) {
            return;
        }

        for ( OntologyTerm child : children ) { // recurse
            this.getCharacteristicsAnnotatedToChildren( child, results, seenTerms, t, limit );
        }
    }

    /**
     * Returns ontology terms one step down in the DAG
     *
     * @param term starting point
     */
    @SuppressWarnings("unchecked")
    private Collection<OntologyTerm> getDirectChildTerms( OntologyTerm term ) {
        String uri = term.getUri();

        Collection<OntologyTerm> children = null;
        if ( StringUtils.isBlank( uri ) ) {
            // shouldn't happen, but just in case
            SearchServiceImpl.log.warn( "Blank uri for " + term );
            return new HashSet<>();
        }

        Element cachedChildren = this.childTermCache.get( uri );
        if ( cachedChildren == null ) {
            try {
                children = term.getChildren( true );
                childTermCache.put( new Element( uri, children ) );
            } catch ( com.hp.hpl.jena.ontology.ConversionException ce ) {
                SearchServiceImpl.log.warn( "getting children for term: " + term
                        + " caused com.hp.hpl.jena.ontology.ConversionException. " + ce.getMessage() );
            }
        } else {
            children = ( Collection<OntologyTerm> ) cachedChildren.getObjectValue();
        }

        return children;
    }

    /**
     * @return a collection of SearchResults holding all the genes resulting from the search with given SearchSettings.
     */
    private Collection<SearchResult<Gene>> getGenesFromSettings( SearchSettings settings, boolean webSpeedSearch ) throws SearchException {
        Collection<SearchResult<Gene>> genes = null;
        if ( settings.hasResultType( Gene.class ) ) {
            genes = this.geneSearch( settings, webSpeedSearch );
        }
        return genes;
    }

    //    /**
    //     * @return List of ids for the entities held by the search results.
    //     */
    //    private List<Long> getIds( List<SearchResult> searchResults ) {
    //        List<Long> list = new ArrayList<>();
    //        for ( SearchResult r : searchResults ) {
    //            list.add( r.getId() );
    //        }
    //        assert list.size() == searchResults.size();
    //        return list;
    //    }

    /**
     * Given raw results
     *
     * @param  fillObjects should the entities be filled in? Otherwise, the SearchResults will just have the Class and
     *                     Id for later retrieval.
     * @return map of result entity class (e.g. BioSequence or ExpressionExperiment) to SearchResult
     */
    private Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> getSortedLimitedResults( SearchSettings settings,
            List<SearchResult<?>> rawResults, boolean fillObjects ) {

        Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> results = new HashMap<>();
        rawResults = rawResults.stream().sorted().collect( Collectors.toList() );

        results.put( ArrayDesign.class, new ArrayList<>() );
        results.put( BioSequence.class, new ArrayList<>() );
        results.put( BibliographicReference.class, new ArrayList<>() );
        results.put( CompositeSequence.class, new ArrayList<>() );
        results.put( ExpressionExperiment.class, new ArrayList<>() );
        results.put( Gene.class, new ArrayList<>() );
        results.put( GeneSet.class, new ArrayList<>() );
        results.put( ExpressionExperimentSet.class, new ArrayList<>() );
        results.put( CharacteristicValueObject.class, new ArrayList<>() ); // used for phenotypes
        results.put( BlacklistedExperiment.class, new ArrayList<>() );
        results.put( BlacklistedPlatform.class, new ArrayList<>() );

        // Get the top N results for each class.
        for ( SearchResult<?> sr : rawResults ) {
            Class<?> resultClass = sr.getResultClass();
            List<SearchResult<?>> resultsForClass = results.get( resultClass );
            if ( resultsForClass != null && resultsForClass.size() < settings.getMaxResults() ) {
                resultsForClass.add( sr );
            }
        }

        if ( fillObjects ) {
            //            /*
            //             * retrieve the entities and put them in the SearchResult. Entities that are filtered out by the
            //             * SecurityInterceptor will be removed at this stage (if they haven't already)
            //             */
            //    StopWatch t = this.startTiming();
            //    int c = 0;
            // Disabled because I don't think we want to do this. Let the search-using code decide. And in any case we should get value objects
            //            for ( Class<?> clazz : results.keySet() ) {
            //                List<SearchResult> r = results.get( clazz );
            //                if ( r.isEmpty() )
            //                    continue;
            //                Map<Long, SearchResult> rMap = new HashMap<>();
            //                Collection<? extends Identifiable> entities = new HashSet<>();
            //                List<SearchResult> rtofill = new ArrayList<>();
            //                for ( SearchResult searchResult : r ) {
            //                    if ( !rMap.containsKey( searchResult.getId() ) || ( rMap.get( searchResult.getId() ).getScore() < searchResult.getScore() ) ) {
            //                        rMap.put( searchResult.getId(), searchResult );
            //                    }
            //                    if ( searchResult.getResultObject() == null ) {
            //                        rtofill.add( searchResult );
            //                    }
            //                }
            //
            //                //
            //                entities.addAll( this.retrieveResultEntities( clazz, rtofill ) );
            //                List<SearchResult> filteredResults = new ArrayList<>();
            //                for ( Object entity : entities ) {
            //                    Long id = EntityUtils.getId( entity );
            //                    SearchResult keeper = rMap.get( id );
            //                    keeper.setResultObject( entity );
            //                    filteredResults.add( keeper );
            //                    c++;
            //                }
            //
            //                this.filterByTaxon( settings, filteredResults, false );
            //
            //                results.put( clazz, filteredResults );
            //
            //            }
            //  if ( t.getTime() > 500 ) {
            //   log.info( "Retrieval of " + c + " raw (unfiltered) entities: " + t.getTime() + "ms" );
            //   }
        } else {
            for ( SearchResult<?> sr : rawResults ) {
                sr.clearResultObject();
            }
        }

        //        List<SearchResult> convertedResults = this
        //                .convertEntitySearchResutsToValueObjectsSearchResults( results.get( BioSequence.class ) );
        //        results.put( BioSequenceValueObject.class, convertedResults );
        //        results.remove( BioSequence.class );

        return results;
    }

    /*
     *
     */
    private void initializeNameToTaxonMap() {

        Collection<? extends Taxon> taxonCollection = taxonService.loadAll();

        for ( Taxon taxon : taxonCollection ) {
            if ( taxon.getScientificName() != null )
                nameToTaxonMap.put( taxon.getScientificName().trim().toLowerCase(), taxon );
            if ( taxon.getCommonName() != null )
                nameToTaxonMap.put( taxon.getCommonName().trim().toLowerCase(), taxon );
        }

        // Loop through again breaking up multi-word taxon database names.
        // Doing this is a separate loop so that these names take lower precedence when matching than the full terms in
        // the generated keySet.
        for ( Taxon taxon : taxonCollection ) {
            this.addTerms( taxon, taxon.getCommonName() );
            this.addTerms( taxon, taxon.getScientificName() );
        }

    }

    /**
     * @return results, if the settings.termUri is populated. This includes gene uris.
     */
    private Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> ontologyUriSearch( SearchSettings settings ) throws SearchException {
        Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> results = new HashMap<>();

        // 1st check to see if the query is a URI (from an ontology).
        // Do this by seeing if we can find it in the loaded ontologies.
        // Escape with general utilities because might not be doing a lucene backed search. (just a hibernate one).
        String termUri = settings.getQuery();

        if ( !settings.isTermQuery() ) {
            return results;
        }

        String uriString = StringEscapeUtils.escapeJava( StringUtils.strip( termUri ) );

        /*
         * Gene search. We want experiments that are annotated. But also genes.
         */
        if ( StringUtils.containsIgnoreCase( uriString, SearchServiceImpl.NCBI_GENE ) ) {
            // Perhaps is a valid gene URL. Want to search for the gene in gemma.

            // Get the gene
            String ncbiAccessionFromUri = StringUtils.substringAfterLast( uriString, "/" );
            Gene g = null;

            try {
                g = geneService.findByNCBIId( Integer.parseInt( ncbiAccessionFromUri ) );
            } catch ( NumberFormatException e ) {
                // ok
            }
            if ( g != null ) {

                // 1st get objects tagged with the given gene identifier
                if ( settings.hasResultType( ExpressionExperiment.class ) ) { // FIXME maybe we always want this?
                    Collection<SearchResult<ExpressionExperiment>> eeHits = new HashSet<>();
                    Map<String, String> uri2value = new HashMap<>();
                    uri2value.put( termUri, g.getOfficialSymbol() );
                    this.findExperimentsByUris( Collections.singleton( termUri ), eeHits, settings.getTaxon(), settings.getMaxResults(), uri2value );

                    if ( !eeHits.isEmpty() ) {
                        results.put( ExpressionExperiment.class, new ArrayList<>() );
                        results.get( ExpressionExperiment.class ).addAll( eeHits );
                    }
                }

                ////
                if ( settings.hasResultType( Gene.class ) ) {
                    results.put( Gene.class, new ArrayList<>() );
                    results.get( Gene.class ).add( new SearchResult<>( g ) );

                }
            }
            return results;
        }

        /*
         * Not searching for a gene. Only other option is a direct URI search for experiments.
         */
        if ( settings.hasResultType( ExpressionExperiment.class ) ) {
            Collection<SearchResult<ExpressionExperiment>> hits = this.characteristicEESearchTerm( uriString, settings.getTaxon(), settings.getMaxResults() );
            results.put( ExpressionExperiment.class, new ArrayList<>() );
            results.get( ExpressionExperiment.class ).addAll( hits );
        }

        return results;
    }

    //    /**
    //     * Retrieve entities from the persistent store (if we don't have them already)
    //     */
    //    private Collection<? extends Identifiable> retrieveResultEntities( Class<?> entityClass, List<SearchResult> results ) {
    //        List<Long> ids = this.getIds( results );
    //
    //        // FIXME: don't we want value objects?
    //        if ( ExpressionExperiment.class.isAssignableFrom( entityClass ) ) {
    //            return expressionExperimentService.load( ids );
    //        } else if ( ArrayDesign.class.isAssignableFrom( entityClass ) ) {
    //            return arrayDesignService.load( ids );
    //        } else if ( CompositeSequence.class.isAssignableFrom( entityClass ) ) {
    //            return compositeSequenceService.load( ids );
    //        } else if ( BibliographicReference.class.isAssignableFrom( entityClass ) ) {
    //            return bibliographicReferenceService.load( ids );
    //        } else if ( Gene.class.isAssignableFrom( entityClass ) ) {
    //            return geneService.load( ids );
    //        } else if ( BioSequence.class.isAssignableFrom( entityClass ) ) {
    //            return bioSequenceService.load( ids );
    //        } else if ( GeneSet.class.isAssignableFrom( entityClass ) ) {
    //            return geneSetService.load( ids );
    //        } else if ( ExpressionExperimentSet.class.isAssignableFrom( entityClass ) ) {
    //            return experimentSetService.load( ids );
    //        } else if ( Characteristic.class.isAssignableFrom( entityClass ) ) {
    //            Collection<Characteristic> chars = new ArrayList<>();
    //            for ( Long id : ids ) {
    //                chars.add( characteristicService.load( id ) );
    //            }
    //            return chars;
    //        } else if ( CharacteristicValueObject.class.isAssignableFrom( entityClass ) ) {
    //            // TEMP HACK this whole method should not be needed in many cases
    //            Collection<CharacteristicValueObject> chars = new ArrayList<>();
    //            for ( SearchResult result : results ) {
    //                if ( result.getResultClass().isAssignableFrom( CharacteristicValueObject.class ) ) {
    //                    chars.add( ( CharacteristicValueObject ) result.getResultObject() );
    //                }
    //            }
    //            return chars;
    //        } else if ( ExpressionExperimentSet.class.isAssignableFrom( entityClass ) ) {
    //            return experimentSetService.load( ids );
    //        } else if ( BlacklistedEntity.class.isAssignableFrom( entityClass ) ) {
    //            return blackListDao.load( ids );
    //        } else {
    //            throw new UnsupportedOperationException( "Don't know how to retrieve objects for class=" + entityClass );
    //        }
    //    }
}
