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
package ubic.gemma.web.controller.expression.designElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ubic.gemma.analysis.sequence.ArrayDesignMapResultService;
import ubic.gemma.analysis.sequence.BlatResultGeneSummary;
import ubic.gemma.analysis.sequence.CompositeSequenceMapValueObject;
import ubic.gemma.model.association.BioSequence2GeneProduct;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.designElement.CompositeSequenceService;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.biosequence.SequenceType;
import ubic.gemma.model.genome.gene.GeneProduct;
import ubic.gemma.model.genome.sequenceAnalysis.AnnotationAssociation;
import ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation;
import ubic.gemma.model.genome.sequenceAnalysis.BlatResult;
import ubic.gemma.model.genome.sequenceAnalysis.BlatResultService;
import ubic.gemma.search.SearchResult;
import ubic.gemma.search.SearchService;
import ubic.gemma.search.SearchSettings;
import ubic.gemma.web.controller.BaseController;
import ubic.gemma.web.propertyeditor.SequenceTypePropertyEditor;
import ubic.gemma.web.remote.EntityDelegator;

/**
 * @author joseph
 * @author paul
 * @version $Id$
 */
@Controller
@RequestMapping("/compositeSequence")
public class CompositeSequenceController extends BaseController {

    @Autowired
    private CompositeSequenceService compositeSequenceService = null;
    @Autowired
    private BlatResultService blatResultService = null;
    @Autowired
    private ArrayDesignMapResultService arrayDesignMapResultService = null;
    @Autowired
    private SearchService searchService;
    @Autowired
    private ArrayDesignService arrayDesignService = null;

    /**
     * Search for probes.
     * 
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/filter")
    public ModelAndView filter( HttpServletRequest request, HttpServletResponse response ) {
        String filter = request.getParameter( "filter" );
        String arid = request.getParameter( "arid" );

        // Validate the filtering search criteria.
        if ( StringUtils.isBlank( filter ) ) {
            this.saveMessage( request, "No search critera provided" );
            // return showAll( request, response );
        }

        Collection<CompositeSequenceMapValueObject> compositeSequenceSummary = search( filter, arid );

        if ( ( compositeSequenceSummary == null ) || ( compositeSequenceSummary.size() == 0 ) ) {
            this.saveMessage( request, "Your search yielded no results" );
            compositeSequenceSummary = new ArrayList<CompositeSequenceMapValueObject>();
        } else {
            this.saveMessage( request, compositeSequenceSummary.size() + " probes matched your search." );
        }

        ModelAndView mav = new ModelAndView( "compositeSequences.geneMap" );
        mav.addObject( "arrayDesign", loadArrayDesign( arid ) );
        mav.addObject( "sequenceData", compositeSequenceSummary );
        mav.addObject( "numCompositeSequences", compositeSequenceSummary.size() );
        this.saveMessage( request, "Search Criteria: " + filter );

        return mav;
    }

    /**
     * Exposed for AJAX calls.
     * 
     * @param csd
     * @return
     */
    public Collection<BlatResultGeneSummary> getBlatMappingSummary( EntityDelegator csd ) {
        if ( csd == null || csd.getId() == null ) {
            return new HashSet<BlatResultGeneSummary>();
        }
        CompositeSequence cs = compositeSequenceService.load( csd.getId() );
        return this.getBlatMappingSummary( cs ).values();
    }

    /**
     * Exposed for AJAX calls (Probe browser)
     * 
     * @param ids
     * @return
     */
    public Collection<CompositeSequenceMapValueObject> getCsSummaries( Collection<Long> ids ) {

        if ( ids == null || ids.size() == 0 ) {
            return new HashSet<CompositeSequenceMapValueObject>();
        }

        Collection<CompositeSequence> compositeSequences = compositeSequenceService.loadMultiple( ids );
        Collection<Object[]> rawSummaries = compositeSequenceService.getRawSummary( compositeSequences, 0 );
        return arrayDesignMapResultService.getSummaryMapValueObjects( rawSummaries );
    }

    /**
     * @param searchString
     * @param arrayDesign
     * @return
     */
    public Collection<CompositeSequenceMapValueObject> search( String searchString, String arrayDesignId ) {

        if ( StringUtils.isBlank( searchString ) ) {
            return new HashSet<CompositeSequenceMapValueObject>();
        }

        /*
         * There have to be a few ways of searching: - by ID, by bioSequence, by Gene name. An array design may or may
         * not be given.
         */
        ArrayDesign arrayDesign = loadArrayDesign( arrayDesignId );

        Collection<SearchResult> searchResults = searchService.search(
                SearchSettings.CompositeSequenceSearch( searchString, arrayDesign ) ).get( CompositeSequence.class );

        Collection<CompositeSequence> css = new HashSet<CompositeSequence>();
        for ( SearchResult sr : searchResults ) {
            CompositeSequence cs = ( CompositeSequence ) sr.getResultObject();
            if ( arrayDesign == null || cs.getArrayDesign().equals( arrayDesign ) ) {
                css.add( cs );
            }
        }

        return getSummaries( css );
    }

    /**
     * @param request
     * @param response
     * @param errors
     * @return ModelAndView
     */
    @RequestMapping("/show")
    public ModelAndView show( HttpServletRequest request, HttpServletResponse response ) {
        Long id = Long.parseLong( request.getParameter( "id" ) );
        CompositeSequence cs = compositeSequenceService.load( id );
        if ( cs == null ) {
            addMessage( request, "object.notfound", new Object[] { "composite sequence " + id } );
            return new ModelAndView( "mainMenu.html" );
        }

        Map<BlatResult, BlatResultGeneSummary> blatResults = getBlatMappingSummary( cs );

        ModelAndView mav = new ModelAndView( "compositeSequence.detail" );

        mav.addObject( "compositeSequence", cs );
        mav.addObject( "blatResults", blatResults );
        return mav;
    }

    /**
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping("/showAll")
    public ModelAndView showAll( HttpServletRequest request, HttpServletResponse response ) {

        String sId = request.getParameter( "id" );
        Collection<CompositeSequence> compositeSequences = new ArrayList<CompositeSequence>();
        // if no IDs are specified, then show an error message
        if ( sId == null ) {
            addMessage( request, "object.notfound", new Object[] { "All composite sequences cannot be listed. " } );
        }

        // if ids are specified, then display only those bioSequences
        else {
            String[] idList = StringUtils.split( sId, ',' );
            Collection<Long> ids = new ArrayList<Long>();

            for ( int i = 0; i < idList.length; i++ ) {
                Long id = Long.parseLong( idList[i] );
                ids.add( id );
            }
            compositeSequences.addAll( compositeSequenceService.loadMultiple( ids ) );
        }
        return new ModelAndView( "compositeSequences" ).addObject( "compositeSequences", compositeSequences );

    }

    @InitBinder
    protected void initBinder( ServletRequestDataBinder binder ) throws Exception {
        binder.registerCustomEditor( SequenceType.class, new SequenceTypePropertyEditor() );
    }

    /**
     * @param cs
     * @param blatResults
     */
    private void addBlatResultsLackingGenes( CompositeSequence cs, Map<BlatResult, BlatResultGeneSummary> blatResults ) {
        /*
         * Pick up blat results that didn't map to genes.
         */
        Collection<BlatResult> allBlatResultsForCs = blatResultService.findByBioSequence( cs
                .getBiologicalCharacteristic() );
        for ( BlatResult blatResult : allBlatResultsForCs ) {
            if ( !blatResults.containsKey( blatResult ) ) {
                BlatResultGeneSummary summary = new BlatResultGeneSummary();
                summary.setBlatResult( blatResult );
                // no gene...
                blatResults.put( blatResult, summary );
            }
        }
    }

    /**
     * @param cs
     * @return
     */
    private Map<BlatResult, BlatResultGeneSummary> getBlatMappingSummary( CompositeSequence cs ) {
        BioSequence bs = cs.getBiologicalCharacteristic();

        Map<BlatResult, BlatResultGeneSummary> blatResults = new HashMap<BlatResult, BlatResultGeneSummary>();
        if ( bs == null || bs.getBioSequence2GeneProduct() == null ) {
            return blatResults;
        }

        Collection<BioSequence2GeneProduct> bs2gps = cs.getBiologicalCharacteristic().getBioSequence2GeneProduct();

        for ( BioSequence2GeneProduct bs2gp : bs2gps ) {

            if ( ( bs2gp instanceof BlatAssociation ) ) {
                BlatAssociation blatAssociation = ( BlatAssociation ) bs2gp;
                GeneProduct geneProduct = blatAssociation.getGeneProduct();
                Gene gene = geneProduct.getGene();
                BlatResult blatResult = blatAssociation.getBlatResult();
                if ( blatResult instanceof HibernateProxy ) {
                    // this code is reached.
                    blatResult = ( BlatResult ) ( ( HibernateProxy ) blatResult ).getHibernateLazyInitializer()
                            .getImplementation();
                }

                blatResult.getQuerySequence().getTaxon(); // FIXME: Cruft or thaw attempt? Tested and apparently not
                // needed
                // (PP)

                if ( blatResults.containsKey( blatResult ) ) {
                    blatResults.get( blatResult ).addGene( geneProduct, gene );
                } else {
                    BlatResultGeneSummary summary = new BlatResultGeneSummary();
                    summary.addGene( geneProduct, gene );
                    summary.setBlatResult( blatResult );
                    summary.setCompositeSequence( cs );
                    blatResults.put( blatResult, summary );
                }
            }
            // ugly doing this but this code needs refactor to take away emphasis on BLAT
            else if ( bs2gp instanceof AnnotationAssociation ) {
                AnnotationAssociation blatAssociation = ( AnnotationAssociation ) bs2gp;
                GeneProduct geneProduct = blatAssociation.getGeneProduct();
                Gene gene = geneProduct.getGene();
                BlatResult newInstance = BlatResult.Factory.newInstance();
                BlatResultGeneSummary summary = new BlatResultGeneSummary();
                summary.addGene( geneProduct, gene );
                summary.setBlatResult( newInstance );
                summary.setCompositeSequence( cs );
                blatResults.put( newInstance, summary );
            }

        }

        addBlatResultsLackingGenes( cs, blatResults );

        if ( blatResults.size() == 0 ) {
            // add a 'dummy' that at least contains the information about the CS. This is a bit of a hack...
            BlatResultGeneSummary summary = new BlatResultGeneSummary();
            summary.setCompositeSequence( cs );
            BlatResult newInstance = BlatResult.Factory.newInstance();
            newInstance.setQuerySequence( cs.getBiologicalCharacteristic() );
            newInstance.setId( -1L );
            summary.setBlatResult( newInstance );
            blatResults.put( newInstance, summary );
        }

        return blatResults;
    }

    /**
     * @param compositeSequences
     * @return
     */
    private Collection<CompositeSequenceMapValueObject> getSummaries( Collection<CompositeSequence> compositeSequences ) {
        Collection<CompositeSequenceMapValueObject> compositeSequenceSummary = new HashSet<CompositeSequenceMapValueObject>();
        if ( compositeSequences.size() > 0 ) {
            Collection<Object[]> rawSummaries = compositeSequenceService.getRawSummary( compositeSequences, 0 );
            compositeSequenceSummary = arrayDesignMapResultService.getSummaryMapValueObjects( rawSummaries );
        }
        return compositeSequenceSummary;
    }

    /**
     * @param arrayDesignId
     * @return
     */
    private ArrayDesign loadArrayDesign( String arrayDesignId ) {
        ArrayDesign arrayDesign = null;
        if ( arrayDesignId != null ) {
            try {
                arrayDesign = arrayDesignService.load( Long.parseLong( arrayDesignId ) );
            } catch ( NumberFormatException e ) {
                log.info( "Invalid array design id: " + arrayDesignId );
                // Fail gracefully, please.
            }
        }
        return arrayDesign;
    }

}