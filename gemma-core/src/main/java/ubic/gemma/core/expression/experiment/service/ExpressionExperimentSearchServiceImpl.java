/*
 * The Gemma project
 *
 * Copyright (c) 2009 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package ubic.gemma.core.expression.experiment.service;

import com.google.common.collect.Sets;
import gemma.gsec.SecurityService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ubic.gemma.core.search.SearchException;
import ubic.gemma.core.search.SearchResult;
import ubic.gemma.core.search.SearchResultDisplayObject;
import ubic.gemma.core.search.SearchService;
import ubic.gemma.model.analysis.expression.ExpressionExperimentSet;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.model.common.search.SearchSettings;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentSetValueObject;
import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;
import ubic.gemma.model.expression.experiment.FreeTextExpressionExperimentResultsValueObject;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.persistence.service.analysis.expression.coexpression.CoexpressionAnalysisService;
import ubic.gemma.persistence.service.analysis.expression.diff.DifferentialExpressionAnalysisService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentSetService;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;
import ubic.gemma.persistence.util.EntityUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles searching for experiments and experiment sets
 *
 * @author tvrossum
 */
@Component
public class ExpressionExperimentSearchServiceImpl implements ExpressionExperimentSearchService {

    private static final Log log = LogFactory.getLog( ExpressionExperimentSearchServiceImpl.class );
    private static final String MASTER_SET_PREFIX = "Master set for";

    private final ExpressionExperimentSetService expressionExperimentSetService;
    private final CoexpressionAnalysisService coexpressionAnalysisService;
    private final DifferentialExpressionAnalysisService differentialExpressionAnalysisService;
    private final SecurityService securityService;
    private final SearchService searchService;
    private final TaxonService taxonService;
    private final ExpressionExperimentService expressionExperimentService;

    @Autowired
    public ExpressionExperimentSearchServiceImpl( ExpressionExperimentSetService expressionExperimentSetService,
            CoexpressionAnalysisService coexpressionAnalysisService,
            DifferentialExpressionAnalysisService differentialExpressionAnalysisService,
            SecurityService securityService, SearchService searchService, TaxonService taxonService,
            ExpressionExperimentService expressionExperimentService ) {
        this.expressionExperimentSetService = expressionExperimentSetService;
        this.coexpressionAnalysisService = coexpressionAnalysisService;
        this.differentialExpressionAnalysisService = differentialExpressionAnalysisService;
        this.securityService = securityService;
        this.searchService = searchService;
        this.taxonService = taxonService;
        this.expressionExperimentService = expressionExperimentService;
    }

    @Override
    public Collection<ExpressionExperimentValueObject> searchExpressionExperiments( String query ) throws SearchException {

        SearchSettings settings = SearchSettings.expressionExperimentSearch( query );
        List<SearchResult<ExpressionExperiment>> experimentSearchResults = searchService.search( settings, ExpressionExperiment.class );

        if ( experimentSearchResults == null || experimentSearchResults.isEmpty() ) {
            ExpressionExperimentSearchServiceImpl.log.info( "No experiments for search: " + query );
            return new HashSet<>();
        }

        ExpressionExperimentSearchServiceImpl.log
                .info( "Experiment search: " + query + ", " + experimentSearchResults.size() + " found" );
        List<Long> eeIds = experimentSearchResults.stream().map( SearchResult::getResultId ).collect( Collectors.toList() );
        Collection<ExpressionExperimentValueObject> experimentValueObjects = expressionExperimentService
                .loadValueObjectsByIds( experimentSearchResults.stream().map( SearchResult::getResultId ).collect( Collectors.toList() ), true );
        ExpressionExperimentSearchServiceImpl.log
                .info( "Experiment search: " + experimentValueObjects.size() + " value objects returned." );
        return experimentValueObjects;
    }

    @Override
    public Collection<ExpressionExperimentValueObject> searchExpressionExperiments( List<String> query ) throws SearchException {

        Set<ExpressionExperimentValueObject> all = new HashSet<>();
        Set<ExpressionExperimentValueObject> prev = null;
        Set<ExpressionExperimentValueObject> current;
        for ( String s : query ) {
            s = StringUtils.strip( s );
            if ( prev == null ) {
                prev = new HashSet<>( this.searchExpressionExperiments( s ) );
                all = new HashSet<>( prev );
                continue;
            }
            current = new HashSet<>( this.searchExpressionExperiments( s ) );

            all = Sets.intersection( all, current );
        }
        return all;
    }

    @Override
    public List<SearchResultDisplayObject> searchExperimentsAndExperimentGroups( String query, Long taxonId ) throws SearchException {

        List<SearchResultDisplayObject> displayResults = new LinkedList<>();

        // if query is blank, return list of public sets, user-owned sets (if logged in) and user's recent
        // session-bound sets (not autogen sets until handling of large searches is fixed)
        if ( StringUtils.isBlank( query ) ) {
            return this.searchExperimentsAndExperimentGroupBlankQuery( taxonId );
        }

        Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> results = this.initialSearch( query, taxonId );

        List<SearchResultDisplayObject> experimentSets = this.getExpressionExperimentSetResults( results );
        List<SearchResultDisplayObject> experiments = this.getExpressionExperimentResults( results );

        if ( experimentSets.isEmpty() && experiments.isEmpty() ) {
            return displayResults;
        }

        /*
         * ALL RESULTS BY TAXON GROUPS
         */

        // if >1 result, add a group whose members are all experiments returned from search

        Map<Long, Set<Long>> eeIdsByTaxonId = new HashMap<>();

        // add every individual experiment to the set, grouped by taxon and also altogether.
        for ( SearchResultDisplayObject srdo : experiments ) {

            Long taxId = srdo.getTaxonId();

            if ( !eeIdsByTaxonId.containsKey( taxId ) ) {
                eeIdsByTaxonId.put( taxId, new HashSet<Long>() );
            }
            ExpressionExperimentValueObject eevo = ( ExpressionExperimentValueObject ) srdo.getResultValueObject();
            eeIdsByTaxonId.get( taxId ).add( eevo.getId() );
        }

        // if there's a group, get the number of members
        // assuming the taxon of the members is the same as that of the group

        // for each group
        for ( SearchResultDisplayObject eesSRO : experimentSets ) {
            ExpressionExperimentSetValueObject set = ( ExpressionExperimentSetValueObject ) eesSRO
                    .getResultValueObject();

            /*
             * This is security filtered.
             */
            Collection<Long> ids = EntityUtils
                    .getIds( expressionExperimentSetService.getExperimentValueObjectsInSet( set.getId() ) );

            set.setSize( ids.size() ); // to account for security filtering.

            if ( !eeIdsByTaxonId.containsKey( set.getTaxonId() ) ) {
                eeIdsByTaxonId.put( set.getTaxonId(), new HashSet<Long>() );
            }
            eeIdsByTaxonId.get( set.getTaxonId() ).addAll( ids );
        }

        // make an entry for each taxon

        Long taxonId2;
        for ( Map.Entry<Long, Set<Long>> entry : eeIdsByTaxonId.entrySet() ) {
            taxonId2 = entry.getKey();
            Taxon taxon = taxonService.load( taxonId2 );
            if ( taxon != null && entry.getValue().size() > 0 ) {

                FreeTextExpressionExperimentResultsValueObject ftvo = new FreeTextExpressionExperimentResultsValueObject(
                        "All " + taxon.getCommonName() + " results for '" + query + "'",
                        "All " + taxon.getCommonName() + " experiments found for your query", taxon.getId(),
                        taxon.getCommonName(), entry.getValue(), query );

                int numWithDifferentialExpressionAnalysis = differentialExpressionAnalysisService
                        .getExperimentsWithAnalysis( entry.getValue() ).size();

                assert numWithDifferentialExpressionAnalysis <= entry.getValue().size();

                int numWithCoexpressionAnalysis = coexpressionAnalysisService
                        .getExperimentsWithAnalysis( entry.getValue() ).size();

                ftvo.setNumWithCoexpressionAnalysis( numWithCoexpressionAnalysis );
                ftvo.setNumWithDifferentialExpressionAnalysis( numWithDifferentialExpressionAnalysis );
                displayResults.add( new SearchResultDisplayObject( ftvo ) );
            }
        }

        displayResults.addAll( experimentSets );
        displayResults.addAll( experiments );

        if ( displayResults.isEmpty() ) {
            ExpressionExperimentSearchServiceImpl.log.info( "No results for search: " + query );
        } else {
            ExpressionExperimentSearchServiceImpl.log
                    .info( "Results for search: " + query + " size=" + displayResults.size() + " entry0: "
                            + ( ( SearchResultDisplayObject ) ( displayResults.toArray() )[0] ).getName()
                            + " valueObject:" + ( ( SearchResultDisplayObject ) ( displayResults.toArray() )[0] )
                            .getResultValueObject().toString() );
        }
        return displayResults;
    }

    @Override
    public List<SearchResultDisplayObject> getAllTaxonExperimentGroup( Long taxonId ) {

        List<SearchResultDisplayObject> setResults = new LinkedList<>();

        Taxon taxon = taxonService.load( taxonId );

        Collection<ExpressionExperimentSet> sets = expressionExperimentSetService
                .findByName( "Master set for " + taxon.getCommonName().toLowerCase() );
        SearchResultDisplayObject newSRDO;
        for ( ExpressionExperimentSet set : sets ) {
            expressionExperimentSetService.thaw( set );
            if ( set.getTaxon().getId().equals( taxonId ) ) {
                ExpressionExperimentSetValueObject eevo = expressionExperimentSetService.loadValueObject( set );
                newSRDO = new SearchResultDisplayObject( eevo );
                newSRDO.setUserOwned( securityService.isPrivate( set ) );
                ( ( ExpressionExperimentSetValueObject ) newSRDO.getResultValueObject() )
                        .setIsPublic( securityService.isPublic( set ) );
                setResults.add( newSRDO );
            }
        }

        Collections.sort( setResults );

        return setResults;
    }

    private List<SearchResultDisplayObject> getExpressionExperimentResults(
            Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> results ) {
        // get all expressionExperiment results and convert result object into a value object
        List<SearchResult<? extends Identifiable>> srEEs = results.get( ExpressionExperiment.class );
        if ( srEEs == null ) {
            srEEs = new ArrayList<>();
        }

        List<Long> eeIds = new ArrayList<>();
        for ( SearchResult sr : srEEs ) {
            eeIds.add( sr.getResultId() );
        }

        Collection<ExpressionExperimentValueObject> eevos = expressionExperimentService.loadValueObjectsByIds( eeIds, true );
        List<SearchResultDisplayObject> experiments = new ArrayList<>();
        for ( ExpressionExperimentValueObject eevo : eevos ) {
            experiments.add( new SearchResultDisplayObject( eevo ) );
        }
        return experiments;
    }

    private List<SearchResultDisplayObject> getExpressionExperimentSetResults(
            Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> results ) {
        List<SearchResultDisplayObject> experimentSets = new ArrayList<>();

        if ( results.get( ExpressionExperimentSet.class ) != null ) {
            List<Long> eeSetIds = new ArrayList<>();
            for ( SearchResult sr : results.get( ExpressionExperimentSet.class ) ) {
                eeSetIds.add( ( ( ExpressionExperimentSet ) sr.getResultObject() ).getId() );
            }

            if ( eeSetIds.isEmpty() ) {
                return experimentSets;
            }
            for ( ExpressionExperimentSetValueObject eesvo : expressionExperimentSetService
                    .loadValueObjectsByIds( eeSetIds ) ) {
                experimentSets.add( new SearchResultDisplayObject( eesvo ) );
            }
        }
        return experimentSets;
    }

    private Map<Class<? extends Identifiable>, List<SearchResult<? extends Identifiable>>> initialSearch( String query, Long taxonId ) throws SearchException {
        SearchSettings settings = SearchSettings.builder()
                .query( query )
                .resultType( ExpressionExperiment.class )
                .resultType( ExpressionExperimentSet.class ) // add searching for experimentSets
                .build();
        Taxon taxonParam;
        if ( taxonId != null ) {
            taxonParam = taxonService.load( taxonId );
            settings.setTaxon( taxonParam );
        }
        return searchService.search( settings );
    }

    /**
     * if query is blank, return list of public sets, user-owned sets (if logged in) and user's recent session-bound
     * sets called by ubic.gemma.web.controller .expression.experiment.ExpressionExperimentController.
     * searchExperimentsAndExperimentGroup(String, Long) does not include session bound sets
     */
    private List<SearchResultDisplayObject> searchExperimentsAndExperimentGroupBlankQuery( Long taxonId ) {
        boolean taxonLimited = taxonId != null;

        List<SearchResultDisplayObject> displayResults = new LinkedList<>();

        // These are widely considered to be the most important results and
        // therefore need to be at the top
        List<SearchResultDisplayObject> masterResults = new LinkedList<>();

        Collection<ExpressionExperimentSetValueObject> evos = expressionExperimentSetService
                .loadAllExperimentSetValueObjects( true );

        for ( ExpressionExperimentSetValueObject evo : evos ) {

            if ( taxonLimited && !evo.getTaxonId().equals( taxonId ) ) {
                continue;
            }

            SearchResultDisplayObject srdvo = new SearchResultDisplayObject( evo );
            if ( evo.getName().startsWith( ExpressionExperimentSearchServiceImpl.MASTER_SET_PREFIX ) ) {
                masterResults.add( srdvo );
            } else {
                displayResults.add( srdvo );
            }
        }

        Collections.sort( displayResults );

        // should we also sort by which species is most important(humans obviously) or is that not politically
        // correct???
        displayResults.addAll( 0, masterResults );

        return displayResults;
    }
}
