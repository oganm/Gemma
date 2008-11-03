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
package ubic.gemma.web.controller.expression.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import ubic.gemma.loader.expression.simple.ExperimentalDesignImporter;
import ubic.gemma.model.association.GOEvidenceCode;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.CharacteristicService;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.biomaterial.BioMaterialService;
import ubic.gemma.model.expression.experiment.ExperimentalDesign;
import ubic.gemma.model.expression.experiment.ExperimentalDesignService;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExperimentalFactorService;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.model.expression.experiment.FactorValueService;
import ubic.gemma.util.AnchorTagUtil;
import ubic.gemma.web.controller.BaseMultiActionController;
import ubic.gemma.web.remote.EntityDelegator;
import ubic.gemma.web.util.EntityNotFoundException;

/**
 * @author keshav
 * @version $Id$
 * @spring.bean id="experimentalDesignController"
 * @spring.property name = "experimentalDesignService" ref="experimentalDesignService"
 * @spring.property name = "methodNameResolver" ref="experimentalDesignActions"
 * @spring.property name = "expressionExperimentService" ref="expressionExperimentService"
 * @spring.property name = "bioMaterialService" ref="bioMaterialService"
 * @spring.property name = "experimentalFactorService" ref="experimentalFactorService"
 * @spring.property name = "factorValueService" ref="factorValueService"
 * @spring.property name = "characteristicService" ref="characteristicService"
 * @spring.property name="experimentalDesignImporter" ref="experimentalDesignImporter"
 */
public class ExperimentalDesignController extends BaseMultiActionController {

    private BioMaterialService bioMaterialService = null;
    private CharacteristicService characteristicService = null;
    private ExperimentalDesignImporter experimentalDesignImporter = null;
    private ExperimentalDesignService experimentalDesignService = null;
    private ExperimentalFactorService experimentalFactorService = null;
    private ExpressionExperimentService expressionExperimentService = null;
    private FactorValueService factorValueService = null;

    /**
     * @param eeid
     * @param filePath
     */
    public void createDesignFromFile( Long eeid, String filePath ) {
        ExpressionExperiment ee = expressionExperimentService.load( eeid );
        if ( ee == null ) {
            throw new IllegalArgumentException( "Could not access experiment with id=" + eeid );
        }

        File f = new File( filePath );

        if ( !f.canRead() ) {
            throw new IllegalArgumentException( "Cannot read from file:" + f );
        }

        try {
            // Dry run. Try to avoid importing bad data.
            InputStream is = new FileInputStream( f );
            experimentalDesignImporter.importDesign( ee, is, true );
        } catch ( IOException e ) {
            throw new RuntimeException( "Failed to import the design: " + e.getMessage() );
        }

        try {
            InputStream is = new FileInputStream( f );
            experimentalDesignImporter.importDesign( ee, is );
        } catch ( IOException e ) {
            throw new RuntimeException( "Failed to import the design: " + e.getMessage() );
        }

    }

    /**
     * Creates a new ExperimentalFactor and adds it to the ExperimentalDesign specified by the EntityDelegator.
     * 
     * @param e an EntityDelegator representing an ExperimentalDesign
     * @param efvo an ExperimentalFactorValueObject representing the new ExperimentalFactor
     */
    public void createExperimentalFactor( EntityDelegator e, ExperimentalFactorValueObject efvo ) {
        if ( e == null || e.getId() == null ) return;
        ExperimentalDesign ed = experimentalDesignService.load( e.getId() );

        ExperimentalFactor ef = ExperimentalFactor.Factory.newInstance();
        ef.setExperimentalDesign( ed );
        ef.setName( efvo.getName() );
        ef.setDescription( efvo.getDescription() );
        ef.setCategory( createCategoryCharacteristic( efvo.getCategory(), efvo.getCategoryUri() ) );
        experimentalFactorService.create( ef ); // until the larger problem is fixed...

        if ( ed.getExperimentalFactors() == null ) ed.setExperimentalFactors( new HashSet<ExperimentalFactor>() );
        ed.getExperimentalFactors().add( ef );

        experimentalDesignService.update( ed );
    }

    /**
     * Creates a new FactorValue and adds it to the ExperimentalFactor specified by the EntityDelegator. The new
     * FactorValue may have some initial Characteristics created to match any previously existing FactorValues for the
     * same ExperimentalFactor.
     * 
     * @param e an EntityDelegator representing an ExperimentalFactor
     */
    public void createFactorValue( EntityDelegator e ) {
        if ( e == null || e.getId() == null ) return;
        ExperimentalFactor ef = experimentalFactorService.load( e.getId() );

        Collection<Characteristic> chars = new HashSet<Characteristic>();
        for ( FactorValue fv : ef.getFactorValues() ) {
            for ( Characteristic c : fv.getCharacteristics() ) {
                chars.add( createTemplateCharacteristic( c ) );
                break;
            }
        }
        if ( chars.isEmpty() ) {
            if ( ef.getCategory() == null ) {
                throw new IllegalArgumentException(
                        "You cannot create new factor values on a experimental factor that is not defined by a formal Category" );
            }
            chars.add( createTemplateCharacteristic( ef.getCategory() ) );
        }

        FactorValue fv = FactorValue.Factory.newInstance();
        fv.setExperimentalFactor( ef );
        fv.setCharacteristics( chars );
        factorValueService.create( fv ); // until the larger problem is fixed...

        if ( ef.getFactorValues() == null ) ef.setFactorValues( new HashSet<FactorValue>() );
        ef.getFactorValues().add( fv );

        experimentalFactorService.update( ef );
    }

    /**
     * Creates a new Characteristic and adds it to the FactorValue specified by the EntityDelegator.
     * 
     * @param e an EntityDelegator representing a FactorValue
     */
    public void createFactorValueCharacteristic( EntityDelegator e, Characteristic c ) {
        if ( e == null || e.getId() == null ) return;
        FactorValue fv = factorValueService.load( e.getId() );

        if ( fv == null ) {
            throw new EntityNotFoundException( "No such factor value with id=" + e.getId() );
        }

        if ( fv.getCharacteristics() == null ) {
            fv.setCharacteristics( new HashSet<Characteristic>() );
        }

        fv.getCharacteristics().add( c );

        factorValueService.update( fv );
    }

    /**
     * Deletes the specified ExperimentalFactors and removes them from the ExperimentalDesign specified by the
     * EntityDelegator.
     * 
     * @param e an EntityDelegator representing an ExperimentalDesign
     * @param efIds a collection of ExperimentalFactor ids
     */
    public void deleteExperimentalFactors( EntityDelegator e, Collection<Long> efIds ) {
        if ( e == null || e.getId() == null ) return;
        ExperimentalDesign ed = this.experimentalDesignService.load( e.getId() );

        // First, remove the factorValues from the bioassays.
        ExpressionExperiment ee = experimentalDesignService.getExpressionExperiment( ed );

        if ( ee == null ) {
            throw new IllegalArgumentException( "No expression experiment for experimental design " + ed );
        }

        expressionExperimentService.thawLite( ee );

        for ( Long efId : efIds ) {
            ExperimentalFactor ef = experimentalFactorService.load( efId );
            for ( BioAssay ba : ee.getBioAssays() ) {
                for ( BioMaterial bm : ba.getSamplesUsed() ) {
                    boolean removed = false;
                    for ( Iterator<FactorValue> fIt = bm.getFactorValues().iterator(); fIt.hasNext(); ) {
                        if ( fIt.next().getExperimentalFactor().equals( ef ) ) {
                            fIt.remove();
                            removed = true;
                        }
                    }
                    if ( removed ) {
                        bioMaterialService.update( bm );
                    }
                }
            }

            ed.getExperimentalFactors().remove( ef );
            experimentalFactorService.delete( ef );
        }
        experimentalDesignService.update( ed );
    }

    /**
     * Deletes the specified Characteristics from their parent FactorValues.
     * 
     * @param fvvos a collection of FactorValueValueObjects containing the Characteristics to delete
     */
    public void deleteFactorValueCharacteristics( Collection<FactorValueValueObject> fvvos ) {
        for ( FactorValueValueObject fvvo : fvvos ) {
            FactorValue fv = factorValueService.load( fvvo.getFactorValueId() );
            Characteristic c = characteristicService.load( fvvo.getCharId() );
            fv.getCharacteristics().remove( c );
            characteristicService.delete( c );
            factorValueService.update( fv );
        }
    }

    /**
     * Deletes the specified FactorValues and removes them from the ExperimentalFactor specified by the EntityDelegator.
     * 
     * @param e an EntityDelegator representing an ExperimentalFactor
     * @param efIds a collection of FactorValue ids
     */
    public void deleteFactorValues( EntityDelegator e, Collection<Long> fvIds ) {
        if ( e == null || e.getId() == null ) return;
        ExperimentalFactor ef = experimentalFactorService.load( e.getId() );

        for ( Long fvId : fvIds ) {
            FactorValue fv = factorValueService.load( fvId );
            ef.getFactorValues().remove( fv );
            factorValueService.delete( fv );
        }
        experimentalFactorService.update( ef );
    }

    /**
     * Returns BioMaterialValueObjects for each BioMaterial in the ExpressionExperiment specified by the
     * EntityDelegator.
     * 
     * @param e an EntityDelegator representing an ExpressionExperiment
     * @return a collection of BioMaterialValueObjects
     */
    public Collection<BioMaterialValueObject> getBioMaterials( EntityDelegator e ) {
        if ( e == null || e.getId() == null ) return null;
        ExpressionExperiment ee = expressionExperimentService.load( e.getId() );

        Collection<BioMaterialValueObject> result = new HashSet<BioMaterialValueObject>();
        for ( BioAssay assay : ee.getBioAssays() ) {
            for ( BioMaterial sample : assay.getSamplesUsed() ) {
                BioMaterialValueObject bmvo = new BioMaterialValueObject( sample, assay );
                result.add( bmvo );
            }
        }
        return result;
    }

    /**
     * Returns ExperimentalFactorValueObjects for each ExperimentalFactor in the ExperimentalDesign or
     * ExpressionExperiment specified by the EntityDelegator.
     * 
     * @param e an EntityDelegator representing an ExperimentalDesign OR an ExpressionExperiment
     * @return a collection of ExperimentalFactorValueObjects
     */
    public Collection<ExperimentalFactorValueObject> getExperimentalFactors( EntityDelegator e ) {
        if ( e == null || e.getId() == null ) return null;

        Collection<ExperimentalFactorValueObject> result = new HashSet<ExperimentalFactorValueObject>();
        Long designId = null;
        if ( e.getClassDelegatingFor().equalsIgnoreCase( "ExpressionExperiment" ) ) {
            ExpressionExperiment ee = this.expressionExperimentService.load( e.getId() );
            designId = ee.getExperimentalDesign().getId();
        } else if ( e.getClassDelegatingFor().equalsIgnoreCase( "ExperimentalDesign" ) ) {
            designId = e.getId();
        } else {
            throw new RuntimeException( "Don't know how to process a " + e.getClassDelegatingFor() );
        }
        ExperimentalDesign ed = this.experimentalDesignService.load( designId );

        for ( ExperimentalFactor factor : ed.getExperimentalFactors() ) {
            result.add( new ExperimentalFactorValueObject( factor ) );
        }

        return result;
    }

    /**
     * Returns FactorValueValueObjects for each FactorValue in the ExperimentalFactor specified by the EntityDelegator.
     * There will be one row per FactorValue.
     * 
     * @param e an EntityDelegator representing an ExperimentalFactor
     * @return a collection of FactorValueValueObjects
     */
    public Collection<FactorValueValueObject> getFactorValues( EntityDelegator e ) {
        if ( e == null || e.getId() == null ) return null;
        ExperimentalFactor ef = this.experimentalFactorService.load( e.getId() );

        Collection<FactorValueValueObject> result = new HashSet<FactorValueValueObject>();
        for ( FactorValue value : ef.getFactorValues() ) {
            Characteristic category = value.getExperimentalFactor().getCategory();
            if ( category == null ) {
                category = Characteristic.Factory.newInstance();
                category.setValue( value.getExperimentalFactor().getName() );
            }
            result.add( new FactorValueValueObject( value, category ) );
        }
        return result;
    }

    /**
     * Returns FactorValueValueObjects for each Characteristic belonging to a FactorValue in the ExperimentalFactor
     * specified by the EntityDelegator. There will be one row per Characteristic.
     * 
     * @param e an EntityDelegator representing an ExperimentalFactor
     * @return a collection of FactorValueValueObjects
     */
    public Collection<FactorValueValueObject> getFactorValuesWithCharacteristics( EntityDelegator e ) {
        if ( e == null || e.getId() == null ) return null;
        ExperimentalFactor ef = this.experimentalFactorService.load( e.getId() );

        Collection<FactorValueValueObject> result = new HashSet<FactorValueValueObject>();
        for ( FactorValue value : ef.getFactorValues() ) {
            if ( value.getCharacteristics().size() > 0 ) {
                for ( Characteristic c : value.getCharacteristics() ) {
                    result.add( new FactorValueValueObject( value, c ) );
                }
            } else {
                Characteristic category = value.getExperimentalFactor().getCategory();
                if ( category == null ) {
                    category = Characteristic.Factory.newInstance();
                    category.setValue( value.getExperimentalFactor().getName() );
                }
                result.add( new FactorValueValueObject( value, category ) );
            }
        }
        return result;
    }

    /**
     * @param bioMaterialService the bioMaterialService to set
     */
    public void setBioMaterialService( BioMaterialService bioMaterialService ) {
        this.bioMaterialService = bioMaterialService;
    }

    /**
     * @param characteristicService
     */
    public void setCharacteristicService( CharacteristicService characteristicService ) {
        this.characteristicService = characteristicService;
    }

    public void setExperimentalDesignImporter( ExperimentalDesignImporter experimentalDesignImporter ) {
        this.experimentalDesignImporter = experimentalDesignImporter;
    }

    /**
     * @param experimentalDesignService
     */
    public void setExperimentalDesignService( ExperimentalDesignService experimentalDesignService ) {
        this.experimentalDesignService = experimentalDesignService;
    }

    /**
     * @param experimentalFactorService the experimentalFactorService to set
     */
    public void setExperimentalFactorService( ExperimentalFactorService experimentalFactorService ) {
        this.experimentalFactorService = experimentalFactorService;
    }

    /**
     * TODO add delete to the model
     * 
     * @param request
     * @param response
     * @return
     */
    // @SuppressWarnings("unused")
    // public ModelAndView delete(HttpServletRequest request,
    // HttpServletResponse response) {
    // String name = request.getParameter("name");
    //
    // if (name == null) {
    // // should be a validation error.
    // throw new EntityNotFoundException("Must provide a name");
    // }
    //
    // ExperimentalDesign experimentalDesign = experimentalDesignService
    // .findByName(name);
    // if (experimentalDesign == null) {
    // throw new EntityNotFoundException(experimentalDesign
    // + " not found");
    // }
    //
    // return doDelete(request, experimentalDesign);
    // }
    /**
     * TODO add doDelete to the model
     * 
     * @param request
     * @param experimentalDesign
     * @return
     */
    // private ModelAndView doDelete(HttpServletRequest request,
    // ExperimentalDesign experimentalDesign) {
    // experimentalDesignService.delete(experimentalDesign);
    // log.info("Expression Experiment with name: "
    // + experimentalDesign.getName() + " deleted");
    // addMessage(request, "experimentalDesign.deleted",
    // new Object[] { experimentalDesign.getName() });
    // return new ModelAndView("experimentalDesigns",
    // "experimentalDesign", experimentalDesign);
    // }
    /**
     * @param expressionExperimentService the expressionExperimentService to set
     */
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
     * @param request
     * @param response
     * @param errors
     * @return ModelAndView
     */
    public ModelAndView show( HttpServletRequest request, HttpServletResponse response ) {

        String idstr = request.getParameter( "eeid" );
        if ( StringUtils.isBlank( idstr ) ) {
            throw new IllegalArgumentException( "Must supply 'eeid' parameter" );
        }

        Long id = null;
        try {
            id = Long.parseLong( idstr );
        } catch ( NumberFormatException e ) {
            throw new IllegalArgumentException( "eeid must be a number" );
        }

        ExpressionExperiment ee = expressionExperimentService.load( id );

        if ( ee == null ) {
            throw new EntityNotFoundException( "Expression experiment with id=" + id + " cannot be accessed" );
        }

        Long designId = ee.getExperimentalDesign().getId();

        ExperimentalDesign experimentalDesign = experimentalDesignService.load( designId );
        if ( experimentalDesign == null ) {
            throw new EntityNotFoundException( id + " not found" );
        }

        request.setAttribute( "id", id );

        ModelAndView mnv = new ModelAndView( "experimentalDesign.detail" );
        mnv.addObject( "hasPopulatedDesign", experimentalDesign.getExperimentalFactors().size() > 0 );
        mnv.addObject( "experimentalDesign", experimentalDesign );
        mnv.addObject( "expressionExperiment", ee );
        mnv.addObject( "expressionExperimentUrl", AnchorTagUtil.getExpressionExperimentUrl( ee.getId() ) );

        return mnv;
    }

    /**
     * @param request
     * @param response
     * @return
     */
    public ModelAndView showAll( HttpServletRequest request, HttpServletResponse response ) {
        return new ModelAndView( "experimentalDesigns" ).addObject( "experimentalDesigns", experimentalDesignService
                .loadAll() );
    }

    /**
     * Updates the specified BioMaterials's factor values. This completely removes any pre-existing factor values.
     * 
     * @param efvos a collection of BioMaterialValueObjects containing the updated values
     */
    public void updateBioMaterials( Collection<BioMaterialValueObject> bmvos ) {
        for ( BioMaterialValueObject bmvo : bmvos ) {
            BioMaterial bm = bioMaterialService.load( bmvo.getId() );
            bm.getFactorValues().clear();
            for ( String fvIdString : bmvo.getFactorIdToFactorValueId().values() ) {
                if ( fvIdString.matches( "fv\\d+" ) ) {
                    long fvId = Long.parseLong( fvIdString.substring( 2 ) );
                    FactorValue fv = factorValueService.load( fvId );
                    if ( fv == null ) {
                        log.warn( "Illegal request" );
                        throw new EntityNotFoundException( "No such factorValue with id=" + fvId );
                    }
                    bm.getFactorValues().add( fv );
                }
            }
            bioMaterialService.update( bm );
            log.info( bm );
        }
    }

    /**
     * Updates the specified ExperimentalFactors.
     * 
     * @param efvos a collection of ExperimentalFactorValueObjects containing the updated values
     */
    public void updateExperimentalFactors( Collection<ExperimentalFactorValueObject> efvos ) {
        for ( ExperimentalFactorValueObject efvo : efvos ) {
            ExperimentalFactor ef = experimentalFactorService.load( efvo.getId() );
            ef.setName( efvo.getName() );
            ef.setDescription( efvo.getDescription() );

            /*
             * at the moment, the characteristic is always going to be a VocabCharacteristic; if that changes, this will
             * have to...
             */
            VocabCharacteristic vc = ( VocabCharacteristic ) ef.getCategory();

            // VC can be null if this was imported from GEO etc.
            if ( vc == null ) {
                vc = VocabCharacteristic.Factory.newInstance();
                ef.setCategory( vc );
            }

            vc.setCategory( efvo.getCategory() );
            vc.setCategoryUri( efvo.getCategoryUri() );
            vc.setValue( efvo.getCategory() );
            vc.setValueUri( efvo.getCategoryUri() );

            experimentalFactorService.update( ef );
        }
    }

    /**
     * Updates the specified Characteristics.
     * 
     * @param efvos a collection of FactorValueValueObjects containing the updated values
     */
    public void updateFactorValueCharacteristics( Collection<FactorValueValueObject> fvvos ) {
        /*
         * TODO have this use the same code in CharacteristicBrowserController.updateCharacteristics, probably moving
         * that code to CharacteristicService.
         */
        for ( FactorValueValueObject fvvo : fvvos ) {
            Characteristic c = characteristicService.load( fvvo.getCharId() );
            c.setCategory( fvvo.getCategory() );
            c.setValue( fvvo.getValue() );
            if ( c instanceof VocabCharacteristic ) {
                VocabCharacteristic vc = ( VocabCharacteristic ) c;
                vc.setCategoryUri( fvvo.getCategoryUri() );
                vc.setValueUri( fvvo.getValueUri() );
            }
            c.setEvidenceCode( GOEvidenceCode.IC ); // characteristic has been manually updated
            characteristicService.update( c );
        }
    }

    private Characteristic createCategoryCharacteristic( String category, String categoryUri ) {
        Characteristic c;
        if ( categoryUri != null ) {
            VocabCharacteristic vc = VocabCharacteristic.Factory.newInstance();
            vc.setCategoryUri( categoryUri );
            vc.setValueUri( categoryUri );
            c = vc;
        } else {
            c = Characteristic.Factory.newInstance();
        }
        c.setCategory( category );
        c.setValue( category );
        c.setEvidenceCode( GOEvidenceCode.IC ); // manually added characteristic
        return c;
    }

    private Characteristic createTemplateCharacteristic( Characteristic source ) {
        Characteristic template = ( source instanceof VocabCharacteristic ) ? VocabCharacteristic.Factory.newInstance()
                : Characteristic.Factory.newInstance();
        template.setCategory( source.getCategory() );
        if ( source instanceof VocabCharacteristic ) {
            ( ( VocabCharacteristic ) template ).setCategoryUri( ( ( VocabCharacteristic ) source ).getCategoryUri() );
        }
        template.setEvidenceCode( GOEvidenceCode.IEA ); // automatically added characteristic
        return template;
    }

}
