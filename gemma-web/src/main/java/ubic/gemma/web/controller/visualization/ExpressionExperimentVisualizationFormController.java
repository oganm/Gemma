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
package ubic.gemma.web.controller.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.datastructure.matrix.ExpressionDataMatrix;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVectorService;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.designElement.CompositeSequenceService;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.visualization.DefaultExpressionDataMatrixVisualizer;
import ubic.gemma.visualization.ExpressionDataMatrixVisualizer;
import ubic.gemma.web.controller.BaseFormController;
import ubic.gemma.web.propertyeditor.QuantitationTypePropertyEditor;

/**
 * A <link>SimpleFormController<link> providing search functionality of genes or design elements (probe sets). The
 * success view returns either a visual representation of the result set or a downloadable data file.
 * <p>
 * {@link viewSampling} sets whether or not just some randomly selected vectors will be shown, and {@link species} sets
 * the type of species to search. {@link keywords} restrict the search.
 * 
 * @author keshav
 * @version $Id$
 * @spring.bean id="expressionExperimentVisualizationFormController"
 * @spring.property name = "commandName" value="expressionExperimentVisualizationCommand"
 * @spring.property name = "commandClass"
 *                  value="ubic.gemma.web.controller.visualization.ExpressionExperimentVisualizationCommand"
 * @spring.property name = "formView" value="expressionExperimentVisualizationForm"
 * @spring.property name = "successView" value="showExpressionExperimentVisualization"
 * @spring.property name = "expressionExperimentService" ref="expressionExperimentService"
 * @spring.property name = "compositeSequenceService" ref="compositeSequenceService"
 * @spring.property name = "designElementDataVectorService" ref="designElementDataVectorService"
 * @spring.property name = "validator" ref="genericBeanValidator"
 */
public class ExpressionExperimentVisualizationFormController extends BaseFormController {

    public static final String SEARCH_BY_PROBE = "probe set id";
    public static final String SEARCH_BY_GENE = "gene symbol";

    private static Log log = LogFactory.getLog( ExpressionExperimentVisualizationFormController.class.getName() );

    private ExpressionExperimentService expressionExperimentService = null;
    private CompositeSequenceService compositeSequenceService = null;
    private DesignElementDataVectorService designElementDataVectorService;
    private final int MAX_ELEMENTS_TO_VISUALIZE = 50;

    public ExpressionExperimentVisualizationFormController() {
        /*
         * if true, reuses the same command object across the edit-submit-process (get-post-process).
         */
        setSessionForm( true );
    }

    /**
     * @param request
     * @return Object
     * @throws ServletException
     */
    @Override
    protected Object formBackingObject( HttpServletRequest request ) {

        Long id = null;
        try {
            id = Long.parseLong( request.getParameter( "id" ) );
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( "Id was not valid Long integer", e );
        }
        log.debug( id );

        ExpressionExperiment ee = null;
        ExpressionExperimentVisualizationCommand eesc = new ExpressionExperimentVisualizationCommand();

        if ( id != null && StringUtils.isNotBlank( id.toString() ) ) {
            ee = expressionExperimentService.findById( id );
        } else {
            ee = ExpressionExperiment.Factory.newInstance();
        }

        eesc.setExpressionExperimentId( ee.getId() );
        eesc.setName( ee.getName() );
        eesc.setSearchString( "probeset_0,probeset_1,probeset_2,probeset_3,probeset_4,probeset_5" );
        return eesc;

    }

    /**
     * @param command
     * @param errors
     * @param eesc
     * @param expressionExperiment
     * @param quantitationType
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<DesignElementDataVector> getCompositeSequences( Object command, BindException errors,
            ExpressionExperimentVisualizationCommand eesc, ExpressionExperiment expressionExperiment,
            QuantitationType quantitationType ) {
        Collection<DesignElementDataVector> vectors = null;

        String[] searchIds = new String[MAX_ELEMENTS_TO_VISUALIZE];
        boolean viewSampling = ( ( ExpressionExperimentVisualizationCommand ) command ).isViewSampling();
        if ( viewSampling ) {/* check size if 'viewSampling' is set. */
            vectors = expressionExperimentService.getSamplingOfVectors( expressionExperiment, quantitationType,
                    MAX_ELEMENTS_TO_VISUALIZE );
            /* handle search by design element */
        } else if ( eesc.getSearchCriteria().equalsIgnoreCase( SEARCH_BY_PROBE ) ) {

            String searchString = eesc.getSearchString();
            searchIds = StringUtils.split( searchString, "," );

            Collection<ArrayDesign> arrayDesigns = expressionExperimentService
                    .getArrayDesignsUsed( expressionExperiment );
            if ( arrayDesigns.size() == 0 ) {
                String message = "No array designs found for " + expressionExperiment;
                log.error( message );
                errors.addError( new ObjectError( command.toString(), null, null, message ) );
                return null;
            }

            Collection<CompositeSequence> compositeSequences = getMatchingDesignElements( searchIds, arrayDesigns );

            if ( compositeSequences.size() == 0 ) {
                String message = "No probes could be found matching the query.";
                log.error( message );
                errors.addError( new ObjectError( command.toString(), null, null, message ) );
                return null;
            }

            vectors = expressionExperimentService.getDesignElementDataVectors( expressionExperiment,
                    compositeSequences, quantitationType );

        } else if ( eesc.getSearchCriteria().equalsIgnoreCase( SEARCH_BY_GENE ) ) {
            /* handle search by gene */

            String searchString = eesc.getSearchString();
            searchIds = StringUtils.split( searchString, "," );

            // TODO add search by gene symbol; use regular gene search.
            errors.addError( new ObjectError( command.toString(), null, null,
                    "Search by gene symbol unsupported at this time." ) );

        }
        if ( vectors == null || vectors.size() == 0 ) {
            errors.addError( new ObjectError( command.toString(), null, null, "No data could be found." ) );
        }
        return vectors;
    }

    /**
     * @param searchIds
     * @param arrayDesigns
     * @return Collection<CompositeSequences>
     */
    private Collection<CompositeSequence> getMatchingDesignElements( String[] searchIds,
            Collection<ArrayDesign> arrayDesigns ) {
        Collection<CompositeSequence> compositeSequences = new HashSet<CompositeSequence>();
        for ( ArrayDesign design : arrayDesigns ) {
            for ( String searchId : searchIds ) {
                searchId = StringUtils.trim( searchId );
                CompositeSequence cs = compositeSequenceService.findByName( design, searchId );
                if ( cs != null ) {
                    compositeSequences.add( cs );
                }
            }
        }
        return compositeSequences;
    }

    /**
     * @param request
     * @return Collection<QuantitationType>
     */
    @SuppressWarnings("unchecked")
    private Collection<QuantitationType> getQuantitationTypes( HttpServletRequest request ) {
        Long id = null;
        try {
            id = Long.parseLong( request.getParameter( "id" ) );
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( "Id was not valid Long integer", e );
        }
        ExpressionExperiment expressionExperiment = this.expressionExperimentService.findById( id );
        Collection<QuantitationType> types = expressionExperimentService.getQuantitationTypes( expressionExperiment );
        return types;
    }

    /**
     * 
     */
    @Override
    protected void initBinder( HttpServletRequest request, ServletRequestDataBinder binder ) {
        super.initBinder( request, binder );
        binder.registerCustomEditor( QuantitationType.class, new QuantitationTypePropertyEditor(
                getQuantitationTypes( request ) ) );
    }

    /**
     * @param request
     * @param response
     * @param command
     * @param errors
     * @return ModelAndView
     * @throws Exception
     */
    @SuppressWarnings( { "unused", "unchecked" })
    @Override
    public ModelAndView onSubmit( HttpServletRequest request, HttpServletResponse response, Object command,
            BindException errors ) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();
        ExpressionExperimentVisualizationCommand eesc = ( ( ExpressionExperimentVisualizationCommand ) command );
        String searchCriteria = eesc.getSearchCriteria();
        Long id = eesc.getExpressionExperimentId();

        ExpressionExperiment expressionExperiment = this.expressionExperimentService.findById( id );
        if ( expressionExperiment == null ) {
            return processError( request, response, command, errors, "No expression experiment with id " + id
                    + " found" );
        }

        QuantitationType quantitationType = eesc.getQuantitationType();
        if ( quantitationType == null ) {
            return processError( request, response, command, errors, "Quantitation type must be provided" );
        }

        Collection<DesignElementDataVector> dataVectors = getCompositeSequences( command, errors, eesc,
                expressionExperiment, quantitationType );

        if ( errors.hasErrors() ) {
            return super.processFormSubmission( request, response, command, errors );
        }

        designElementDataVectorService.thaw( dataVectors );
        ExpressionDataMatrix expressionDataMatrix = new ExpressionDataDoubleMatrix( dataVectors, quantitationType );
        /* deals with the case of probes don't match, for the given quantitation type. */
        if ( expressionDataMatrix.getRowMap().size() == 0 && expressionDataMatrix.getColumnMap().size() == 0 ) {
            String message = "None of the probe sets match the given quantitation type "
                    + quantitationType.getType().getValue();

            return processError( request, response, command, errors, message );

        }

        ExpressionDataMatrixVisualizer expressionDataMatrixVisualizer = new DefaultExpressionDataMatrixVisualizer(
                expressionDataMatrix );

        return new ModelAndView( getSuccessView() ).addObject( "expressionDataMatrixVisualizer",
                expressionDataMatrixVisualizer );
    }

    /**
     * @param request
     * @param response
     * @param command
     * @param errors
     * @param message
     * @return ModelAndView
     * @throws Exception
     */
    private ModelAndView processError( HttpServletRequest request, HttpServletResponse response, Object command,
            BindException errors, String message ) throws Exception {
        log.error( message );
        errors.addError( new ObjectError( command.toString(), null, null, message ) );
        return super.processFormSubmission( request, response, command, errors );

    }

    /**
     * @param request
     * @param response
     * @param command
     * @param errors
     * @return ModelAndView
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public ModelAndView processFormSubmission( HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors ) throws Exception {

        ExpressionExperimentVisualizationCommand eesc = ( ( ExpressionExperimentVisualizationCommand ) command );
        Long id = eesc.getExpressionExperimentId();

        if ( request.getParameter( "cancel" ) != null ) {
            log.info( "Cancelled" );

            if ( id != null ) {
                return new ModelAndView( new RedirectView( "/expressionExperiment/showExpressionExperiment.html?id="
                        + id ) );
            }

            log.warn( "Cannot find details view due to null id.  Redirecting to overview" );
            return new ModelAndView( new RedirectView( "/expressionExperiment/showAllExpressionExperiments.html" ) );

        }

        return super.processFormSubmission( request, response, command, errors );
    }

    /**
     * @param request
     * @return Map
     */
    @Override
    protected Map referenceData( HttpServletRequest request ) {

        Map<String, List<? extends Object>> searchByMap = new HashMap<String, List<? extends Object>>();
        List<String> searchCategories = new ArrayList<String>();
        searchCategories.add( SEARCH_BY_GENE );
        searchCategories.add( SEARCH_BY_PROBE );
        searchByMap.put( "searchCategories", searchCategories );

        Collection<QuantitationType> types = getQuantitationTypes( request );
        List<QuantitationType> listedTypes = new ArrayList<QuantitationType>();
        listedTypes.addAll( types );

        searchByMap.put( "quantitationTypes", listedTypes );

        return searchByMap;
    }

    /**
     * @param compositeSequenceService
     */
    public void setCompositeSequenceService( CompositeSequenceService compositeSequenceService ) {
        this.compositeSequenceService = compositeSequenceService;
    }

    /**
     * @param designElementDataVectorService
     */
    public void setDesignElementDataVectorService( DesignElementDataVectorService designElementDataVectorService ) {
        this.designElementDataVectorService = designElementDataVectorService;
    }

    /**
     * @param expressionExperimentService
     */
    public void setExpressionExperimentService( ExpressionExperimentService expressionExperimentService ) {
        this.expressionExperimentService = expressionExperimentService;
    }
}