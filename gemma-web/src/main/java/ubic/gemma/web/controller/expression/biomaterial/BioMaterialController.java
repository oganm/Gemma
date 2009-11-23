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
package ubic.gemma.web.controller.expression.biomaterial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.biomaterial.BioMaterialService;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.model.expression.experiment.FactorValueService;
import ubic.gemma.model.expression.experiment.FactorValueValueObject;
import ubic.gemma.ontology.OntologyResource;
import ubic.gemma.ontology.OntologyService;
import ubic.gemma.web.controller.BaseController;
import ubic.gemma.web.controller.expression.experiment.AnnotationValueObject;
import ubic.gemma.web.remote.EntityDelegator;
import ubic.gemma.web.util.EntityNotFoundException;

/**
 * <bean id="bioMaterialActions" class="org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver">
 * <property name="mappings"> <props> <prop key="/bioMaterial/showAllBioMaterials.html">showAll</prop> <prop
 * key="/bioMaterial/showBioMaterial.html">show</prop> <prop key="/bioMaterial/">show</prop> <prop
 * key="/bioMaterial/annotate.html">annot</prop> </props> </property> </bean> <bean id="bioMaterialController"
 * class="ubic.gemma.web.controller.expression.biomaterial.BioMaterialController"> <property name="bioMaterialService">
 * <ref bean="bioMaterialService" /> </property> <property name="expressionExperimentService"> <ref
 * bean="expressionExperimentService" /> </property> <property name="methodNameResolver"> <ref bean="bioMaterialActions"
 * /> </property> <property name="ontologyService"> <ref bean="ontologyService" /> </property> <property
 * name="factorValueService"> <ref bean="factorValueService" /> </property> </bean>
 * 
 * @author keshav
 * @version $Id$
 */
@Controller
@RequestMapping("/bioMaterial")
public class BioMaterialController extends BaseController {

    private static Log log = LogFactory.getLog( BioMaterialController.class.getName() );

    @Autowired
    private BioMaterialService bioMaterialService = null;

    @Autowired
    private OntologyService ontologyService = null;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    @Autowired
    private FactorValueService factorValueService;

    private boolean AJAX = true;

    /**
     * AJAX
     * 
     * @param bmIds
     * @param factorValueId given a collection of biomaterial ids, and a factor value id will add that factor value to
     *        all of the biomaterials in the collection. If the factor is already defined for one of the biomaterials
     *        will remove the previous one and add the new one.
     */
    public void addFactorValueTo( Collection<Long> bmIds, EntityDelegator factorValueId ) {

        Collection<BioMaterial> bms = this.getBioMaterials( bmIds );
        FactorValue factorVToAdd = factorValueService.load( factorValueId.getId() );
        ExperimentalFactor eFactor = factorVToAdd.getExperimentalFactor();

        for ( BioMaterial material : bms ) {
            Collection<FactorValue> oldValues = material.getFactorValues();
            Collection<FactorValue> updatedValues = new HashSet<FactorValue>();

            // Make sure that the BM doesn't have a FactorValue for the Factor
            // we are adding already
            for ( FactorValue value : oldValues ) {
                if ( value.getExperimentalFactor() != eFactor ) updatedValues.add( value );
            }

            updatedValues.add( factorVToAdd );
            material.setFactorValues( updatedValues );
            bioMaterialService.update( material );
        }
    }

    /**
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/annotate.html")
    public ModelAndView annot( HttpServletRequest request, HttpServletResponse response ) {

        log.debug( request.getParameter( "eeid" ) );

        Long id = Long.parseLong( request.getParameter( "eeid" ) );

        if ( id == null ) {
            // should be a validation error, on 'submit'.
            throw new EntityNotFoundException( "Must provide an expression experiment id" );
        }

        Collection<BioMaterial> bioMaterials = getBioMaterialsForEE( id );

        ModelAndView mav = new ModelAndView( "bioMaterialAnnotator" );
        if ( AJAX ) {
            StringBuilder buf = new StringBuilder();
            for ( BioMaterial bm : bioMaterials ) {
                buf.append( bm.getId() );
                buf.append( "," );
            }
            mav.addObject( "bioMaterialIdList", buf.toString().replaceAll( ",$", "" ) );
        }

        Long numBioMaterials = new Long( bioMaterials.size() );
        mav.addObject( "numBioMaterials", numBioMaterials );
        mav.addObject( "bioMaterials", bioMaterials );
        return mav;
    }

    /**
     * AJAX
     * 
     * @param bm
     * @return
     */
    public Collection<AnnotationValueObject> getAnnotation( EntityDelegator bm ) {
        if ( bm == null || bm.getId() == null ) return null;
        BioMaterial bioM = bioMaterialService.load( bm.getId() );

        Collection<AnnotationValueObject> annotation = new ArrayList<AnnotationValueObject>();

        for ( Characteristic c : bioM.getCharacteristics() ) {
            AnnotationValueObject annotationValue = new AnnotationValueObject();
            annotationValue.setId( c.getId() );
            annotationValue.setClassName( c.getCategory() );
            annotationValue.setTermName( c.getValue() );
            annotationValue.setObjectClass( BioMaterial.class.getSimpleName() );

            if ( c.getEvidenceCode() != null ) {
                annotationValue.setEvidenceCode( c.getEvidenceCode().toString() );
            }
            if ( c instanceof VocabCharacteristic ) {
                VocabCharacteristic vc = ( VocabCharacteristic ) c;
                annotationValue.setClassUri( vc.getCategoryUri() );
                String className = getLabelFromUri( vc.getCategoryUri() );
                if ( className != null ) annotationValue.setClassName( className );
                annotationValue.setTermUri( vc.getValueUri() );
                String termName = getLabelFromUri( vc.getValueUri() );
                if ( termName != null ) annotationValue.setTermName( termName );
            }
            annotation.add( annotationValue );
        }
        return annotation;
    }

    /**
     * AJAX
     * 
     * @param ids
     * @return
     */
    public Collection<BioMaterial> getBioMaterials( Collection<Long> ids ) {
        return bioMaterialService.loadMultiple( ids );
    }

    /**
     * @param id of experiment
     * @return
     */
    public Collection<BioMaterial> getBioMaterialsForEE( Long id ) {
        ExpressionExperiment expressionExperiment = expressionExperimentService.load( id );
        if ( expressionExperiment == null ) {
            throw new EntityNotFoundException( "Expression experiment with id=" + id + " not found" );
        }

        expressionExperimentService.thawLite( expressionExperiment );
        Collection<BioAssay> bioAssays = expressionExperiment.getBioAssays();
        Collection<BioMaterial> bioMaterials = new ArrayList<BioMaterial>();
        for ( BioAssay assay : bioAssays ) {
            Collection<BioMaterial> materials = assay.getSamplesUsed();
            if ( materials != null ) {
                bioMaterials.addAll( materials );
            }
        }
        return bioMaterials;
    }

    /**
     * AJAX
     * 
     * @param bm
     * @return
     */
    public Collection<FactorValueValueObject> getFactorValues( EntityDelegator bm ) {

        if ( bm == null || bm.getId() == null ) return null;

        BioMaterial bioM = bioMaterialService.load( bm.getId() );

        Collection<FactorValueValueObject> results = new HashSet<FactorValueValueObject>();
        Collection<FactorValue> factorValues = bioM.getFactorValues();

        for ( FactorValue value : factorValues )
            results.add( new FactorValueValueObject( value ) );

        return results;

    }

    /**
     * @param bioMaterialService
     */
    public void setBioMaterialService( BioMaterialService bioMaterialService ) {
        this.bioMaterialService = bioMaterialService;
    }

    public void setExpressionExperimentService( ExpressionExperimentService expressionExperimentService ) {
        this.expressionExperimentService = expressionExperimentService;
    }

    /**
     * @param factorValueService the factorValueService to set
     */
    public void setFactorValueService( FactorValueService factorValueService ) {
        this.factorValueService = factorValueService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setOntologyService( OntologyService ontologyService ) {
        this.ontologyService = ontologyService;
    }

    /**
     * @param request
     * @param response
     * @param errors
     * @return ModelAndView
     */
    @RequestMapping( { "/showBioMaterial.html", "/" })
    public ModelAndView show( HttpServletRequest request, HttpServletResponse response ) {

        log.debug( request.getParameter( "id" ) );

        Long id = null;

        try {
            id = Long.parseLong( request.getParameter( "id" ) );
        } catch ( NumberFormatException e ) {
            saveMessage( request, "Must provide a biomaterial id" );
            return new ModelAndView( "mainMenu.html" );
        }

        if ( id == null ) {
            // should also be a validation error, on 'submit'.
            saveMessage( request, "Must provide a biomaterial id" );
            return new ModelAndView( "mainMenu.html" );
        }

        BioMaterial bioMaterial = bioMaterialService.load( id );
        if ( bioMaterial == null ) {
            throw new EntityNotFoundException( id + " not found" );
        }

        this.saveMessage( request, "biomaterial with id " + id + " found" );
        request.setAttribute( "id", id );
        ModelAndView mnv = new ModelAndView( "bioMaterial.detail" ).addObject( "bioMaterial", bioMaterial ).addObject(
                "expressionExperiment", bioMaterialService.getExpressionExperiment( id ) );

        return mnv;
    }

    /**
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/showAllBioMaterials.html")
    public ModelAndView showAll( HttpServletRequest request, HttpServletResponse response ) {
        /*
         * FIXME this is not really useful; we need to take in an id list.
         */
        return new ModelAndView( "bioMaterials" ).addObject( "bioMaterials", bioMaterialService.loadAll() );
    }

    private String getLabelFromUri( String uri ) {
        OntologyResource resource = ontologyService.getResource( uri );
        if ( resource != null ) return resource.getLabel();

        return null;
    }

}
