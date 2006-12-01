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
package ubic.gemma.web.controller.expression.arrayDesign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.search.SearchService;
import ubic.gemma.util.progress.ProgressJob;
import ubic.gemma.util.progress.ProgressManager;
import ubic.gemma.web.controller.BackgroundControllerJob;
import ubic.gemma.web.controller.BackgroundProcessingMultiActionController;
import ubic.gemma.web.util.EntityNotFoundException;
import ubic.gemma.web.util.MessageUtil;

/**
 * @author keshav
 * @version $Id$
 * @spring.bean id="arrayDesignController" name="arrayDesignController"
 * @springproperty name="validator" ref="arrayDesignValidator"
 * @spring.property name = "arrayDesignService" ref="arrayDesignService"
 * @spring.property name="methodNameResolver" ref="arrayDesignActions"
 * @spring.property name="searchService" ref="searchService"
 */
public class ArrayDesignController extends BackgroundProcessingMultiActionController {

    private static Log log = LogFactory.getLog( ArrayDesignController.class.getName() );

    private SearchService searchService;
    private ArrayDesignService arrayDesignService = null;
    private final String messageName = "Array design with name";
    private final String messageId = "Array design with id";
    private final String identifierNotFound = "Must provide a valid Array Design identifier";

    /**
     * @param arrayDesignService The arrayDesignService to set.
     */
    public void setArrayDesignService( ArrayDesignService arrayDesignService ) {
        this.arrayDesignService = arrayDesignService;
    }

    /**
     * @param request
     * @param response
     * @param errors
     * @return
     */
   // @SuppressWarnings({ "unused", "unchecked" })
    public ModelAndView show( HttpServletRequest request, HttpServletResponse response ) {
        String name = request.getParameter( "name" );
        String idStr = request.getParameter( "id" );

        if ( ( name == null ) && ( idStr == null ) ) {
            // should be a validation error, on 'submit'.
            throw new EntityNotFoundException( "Must provide an Array Design name or Id" );
        }
        ArrayDesign arrayDesign = null;
        if ( idStr != null ) {
            arrayDesign = arrayDesignService.load( Long.parseLong( idStr ) );
            this.addMessage( request, "object.found", new Object[] { messageId, idStr } );
            request.setAttribute( "id", idStr );
        } else if ( name != null ) {
            arrayDesign = arrayDesignService.findArrayDesignByName( name );
            this.addMessage( request, "object.found", new Object[] { messageName, name } );
            request.setAttribute( "name", name );
        }

        if ( arrayDesign == null ) {
            throw new EntityNotFoundException( name + " not found" );
        }
        long id = arrayDesign.getId();
        
        Long numCsBioSequences = arrayDesignService.numCompositeSequenceWithBioSequences( arrayDesign );
        Long numCsBlatResults = arrayDesignService.numCompositeSequenceWithBlatResults( arrayDesign );
        Long numCsGenes = arrayDesignService.numCompositeSequenceWithGenes( arrayDesign );
        Long numGenes = arrayDesignService.numGenes( arrayDesign );
        Long numCompositeSequences = new Long(arrayDesignService.getCompositeSequenceCount( arrayDesign ));
        Collection<ExpressionExperiment> ee = arrayDesignService.getExpressionExperiments( arrayDesign );
        Long numExpressionExperiments = new Long(ee.size());
        Taxon t = arrayDesignService.getTaxon( id );
        String taxon = "";
        if (t != null) {
            taxon = t.getScientificName();
        }
        else {
            taxon = "(No taxon available)";
        }
        String techType = arrayDesign.getTechnologyType().getValue();
        String colorString = "";
        if (techType.equalsIgnoreCase( "ONECOLOR" )) {
            colorString = "one-color";
        }
        else if (techType.equalsIgnoreCase( "TWOCOLOR" )) {
            colorString = "two-color";   
        }
        else if (techType.equalsIgnoreCase( "DUALMODE" )) {
            colorString = "dual mode"; 
        }
        else {
            colorString = "No color";
        }
        
        String[] eeIdList = new String[ee.size()];
        int i = 0;
        for (ExpressionExperiment e : ee) {
            eeIdList[i] = e.getId().toString();
            i++;
        }
        String eeIds = StringUtils.join( eeIdList,",");

        ModelAndView mav =  new ModelAndView( "arrayDesign.detail" );
        mav.addObject( "taxon", taxon );
        mav.addObject( "arrayDesign", arrayDesign );
        mav.addObject( "numCsBioSequences", numCsBioSequences );
        mav.addObject( "numCsBlatResults",numCsBlatResults);
        mav.addObject( "numCsGenes", numCsGenes );
        mav.addObject( "numGenes", numGenes );
        mav.addObject( "numCompositeSequences",  numCompositeSequences );
        mav.addObject( "numExpressionExperiments", numExpressionExperiments );
        mav.addObject( "expressionExperimentIds", eeIds );      
        mav.addObject( "technologyType", colorString );
        return mav;
    }

    /**
     * Disabled for now
     * 
     * @param request
     * @param response
     * @return
     */
  //  @SuppressWarnings({ "unused", "unchecked" })
    public ModelAndView showAll( HttpServletRequest request, HttpServletResponse response ) {
        
        String sId = request.getParameter( "id" );
        Collection<ArrayDesignValueObject> arrayDesigns = new ArrayList<ArrayDesignValueObject>();
        // if no IDs are specified, then load all expressionExperiments
        if ( sId == null ) {
            this.saveMessage( request, "Displaying all Array Designs" );
            arrayDesigns.addAll( arrayDesignService.loadAllValueObjects()); 
        }

        // if ids are specified, then display only those arrayDesigns
        else {
            Collection ids = new ArrayList<Long>();

            String[] idList = StringUtils.split( sId, ',' );
            for ( int i = 0; i < idList.length; i++ ) {
                ids.add( new Long( idList[i] ) );
            }
            arrayDesigns.addAll( arrayDesignService.loadValueObjects( ids ) );
        }
        
        Long numArrayDesigns = new Long(arrayDesigns.size());
        ModelAndView mav = new ModelAndView( "arrayDesigns" );
        mav.addObject( "arrayDesigns", arrayDesigns );
        mav.addObject("numArrayDesigns", numArrayDesigns);
        return mav;
    }

    /**
     * @param request
     * @param response
     * @return
     */
    @SuppressWarnings("unused")
    public ModelAndView delete( HttpServletRequest request, HttpServletResponse response ) {
        String stringId = request.getParameter( "id" );

        if ( stringId == null ) {
            // should be a validation error.
            throw new EntityNotFoundException( "Must provide an id" );
        }

        Long id = null;
        try {
            id = Long.parseLong( stringId );
        } catch ( NumberFormatException e ) {
            throw new EntityNotFoundException( "Identifier was invalid" );
        }

        ArrayDesign arrayDesign = arrayDesignService.load( id );
        if ( arrayDesign == null ) {
            throw new EntityNotFoundException( arrayDesign + " not found" );
        }

        // check that no EE depend on the arraydesign we want to delete
        // Do this by checking if there are any bioassays that depend this AD
        Collection assays = arrayDesignService.getAllAssociatedBioAssays( id );
        if ( assays.size() != 0 ) {
            // String eeName = ( ( BioAssay ) assays.iterator().next() )
            // todo tell user what EE depends on this array design
            addMessage( request, "Array Design " + arrayDesign.getName()
                    + " can't be Deleted. ExpressionExperiments depend on it.", new Object[] { messageName,
                    arrayDesign.getName() } );
            return new ModelAndView( new RedirectView( "/Gemma/arrays/showAllArrayDesigns.html" ) );
        }

        String taskId = startJob( arrayDesign, request );
        return new ModelAndView( new RedirectView( "/Gemma/processProgress.html?taskid=" + taskId ) );
       

    }
    
    /**
     * shows a list of BioAssays for an expression experiment subset
     * 
     * @param request
     * @param response
     * @param errors
     * @return ModelAndView
     */
    @SuppressWarnings("unused")
  public ModelAndView showExpressionExperiments( HttpServletRequest request, HttpServletResponse response ) {
        Long id = Long.parseLong( request.getParameter( "id" ) );
        if ( id == null ) {
            // should be a validation error, on 'submit'.
            throw new EntityNotFoundException( identifierNotFound );
        }

        ArrayDesign arrayDesign = arrayDesignService.load( id );
        if (arrayDesign == null) {
            this.addMessage( request, "errors.objectnotfound", new Object[] { "Array Design "} );
            return new ModelAndView( new RedirectView( "/Gemma/arrays/showAllArrayDesigns.html" ) );
        }
    
        Collection ees = arrayDesignService.getExpressionExperiments( arrayDesign );
        Collection<Long> eeIds = new ArrayList<Long>();
        for ( Object object : ees ) {
            eeIds.add( ((ExpressionExperiment) object).getId() );
        }
        String ids = StringUtils.join( eeIds.toArray(),"," );
        return new ModelAndView( new RedirectView( "/Gemma/expressionExperiment/showAllExpressionExperiments.html?id=" + ids ) );
    }

    
    
    public ModelAndView filter( HttpServletRequest request, HttpServletResponse response ) {
        String filter = request.getParameter( "filter" );

        // Validate the filtering search criteria.
        if ( StringUtils.isBlank( filter ) ) {
            this.saveMessage( request, "No search critera provided" );
            return showAll( request, response );
        }

        List<ArrayDesign> searchResults = searchService.compassArrayDesignSearch( filter );

       if ((searchResults == null) || (searchResults.size() == 0)) {
           this.saveMessage( request, "Your search yielded no results");
           return showAll(request, response);
       }
           
        String list = "";
        for ( ArrayDesign ad : searchResults )
            list += ad.getId() + ",";
        
        this.saveMessage( request, "Search Criteria: " + filter );
        this.saveMessage( request, searchResults.size() + " Array Designs matched your search." );
        return new ModelAndView( new RedirectView( "/Gemma/arrays/showAllArrayDesigns.html?id=" + list ));
    }

    
     
    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.web.controller.BaseBackgroundProcessingFormController#getRunner(org.acegisecurity.context.SecurityContext,
     *      java.lang.Object, java.lang.String)
     */
    @Override
    protected BackgroundControllerJob<ModelAndView> getRunner( String taskId, SecurityContext securityContext,
            HttpServletRequest request, Object command, MessageUtil messenger ) {

        return new BackgroundControllerJob<ModelAndView>( taskId, securityContext, request, command, messenger ) {

            @SuppressWarnings("unchecked")
            public ModelAndView call() throws Exception {

                SecurityContextHolder.setContext( securityContext );
     
                ArrayDesign ad = (ArrayDesign) command;
                ProgressJob job = ProgressManager.createProgressJob( this.getTaskId(), securityContext
                        .getAuthentication().getName(), "Deleting Array Design: "
                        + ad.getShortName());
                            
                arrayDesignService.remove( ad );
                saveMessage( "Array Design "+ad.getShortName()  +" removed from Database." );                
                ad = null;


                ProgressManager.destroyProgressJob( job );
                return new ModelAndView( new RedirectView( "/Gemma/arrays/showAllArrayDesigns.html") );
            }
        };
    }

    /**
     * @return the searchService
     */
    public SearchService getSearchService() {
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService( SearchService searchService ) {
        this.searchService = searchService;
    }

}
