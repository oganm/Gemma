/*
 * The Gemma project
 * 
 * Copyright (c) 2008 University of British Columbia
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
package ubic.gemma.loader.expression.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubic.gemma.datastructure.matrix.ExperimentalDesignWriter;
import ubic.gemma.datastructure.matrix.ExpressionDataWriterUtils;
import ubic.gemma.model.association.GOEvidenceCode;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.common.measurement.Measurement;
import ubic.gemma.model.common.measurement.MeasurementType;
import ubic.gemma.model.common.quantitationtype.PrimitiveType;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.biomaterial.BioMaterialService;
import ubic.gemma.model.expression.experiment.ExperimentalDesign;
import ubic.gemma.model.expression.experiment.ExperimentalDesignService;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.model.expression.experiment.FactorValueService;
import ubic.gemma.ontology.MgedOntologyService;
import ubic.gemma.ontology.OntologyTerm;

/**
 * <p>
 * Parse a description of ExperimentalFactors from a file, and associate it with a given ExpressionExperiment. The
 * format is specified by {@link ExperimentalDesignWriter}
 * </p>
 * <p>
 * Example of format, where 'Category' is an MGED term and 'Type' is either "Categorical' or 'Continuous' with no extra
 * white space around the '='s. The ID column MUST match the names on the BioAssays the design will be attached to. Main
 * section is tab-delmited. Column headings in the main table must match the identifiers given in the header.
 * </p>
 * 
 * <pre>
 *    #$ Age : Category=Age Type=Continuous                
 *    #$ Profile : Category=DiseaseState Type=Categorical              
 *    #$ PMI (h) : Category=EnvironmentalHistory Type=Continuous              
 *    #$ Lifetime Alcohol : Category=EnvironmentalHistory Type=Categorical             
 *    #ID  Age     Profile     PMI (h)     Lifetime Alcohol    
 *    f-aa     50  Bipolar     48  Moderate present 
 *    f-ab     50  Bipolar     60  Heavy in present 
 *    f-ac     55  Schizophrenia   26  Little or none 
 *    f-ad     35  Bipolar     28  Unknown 
 *    f-af     60  Bipolar     70  Little or none
 * </pre>
 * 
 * @author Paul
 * @version $Id$
 * @see ExperimentalDesignWriter
 */
@Service
public class ExperimentalDesignImporter {

    enum FactorType {
        CATEGORICAL, CONTINUOUS
    }

    public static final String EXPERIMENTAL_FACTOR_DESCRIPTION_LINE_INDICATOR = "#$";

    private static Log log = LogFactory.getLog( ExperimentalDesignImporter.class.getName() );

    @Autowired
    private BioMaterialService bioMaterialService;

    @Autowired
    private ExperimentalDesignService experimentalDesignService;

    @Autowired
    private FactorValueService factorValueService;

    @Autowired
    private MgedOntologyService mgedOntologyService;

    /**
     * @param experiment
     * @param is
     * @throws IOException
     */
    public void importDesign( ExpressionExperiment experiment, InputStream is ) throws IOException {
        this.importDesign( experiment, is, false );
    }

    /**
     * @param experiment
     * @param is
     * @param dryRun
     * @throws IOException
     */
    @Transactional
    public void importDesign( ExpressionExperiment experiment, InputStream is, boolean dryRun ) throws IOException {

        if ( mgedOntologyService == null ) {
            throw new IllegalStateException( "Please set the MGED OntologyService, thanks." );
        }

        // eeService.thawLite( experiment );
        Map<String, BioMaterial> name2BioMaterial = buildBmMap( experiment );

        Map<String, FactorType> factorTypes = new HashMap<String, FactorType>();

        Collection<OntologyTerm> terms = mgedOntologyService.getMgedTermsByKey( "factor" );

        BufferedReader r = new BufferedReader( new InputStreamReader( is ) );
        String line = null;

        ExperimentalDesign ed = experiment.getExperimentalDesign();
        ed.setDescription( "Parsed from file." );
        log.info( "Filling in experimental design: " + ed.getId() );

        Map<String, ExperimentalFactor> column2Factor = new HashMap<String, ExperimentalFactor>();
        Map<Integer, String> index2Column = new HashMap<Integer, String>();
        boolean readHeader = false;
        while ( ( line = r.readLine() ) != null ) {
            if ( line.startsWith( EXPERIMENTAL_FACTOR_DESCRIPTION_LINE_INDICATOR ) ) {
                buildExperimentalFactor( line, ed, column2Factor, factorTypes, terms, dryRun );
            } else if ( line.startsWith( "#" ) || StringUtils.isBlank( line ) ) {
                continue;
            } else if ( !readHeader ) {
                /*
                 * The simple header line before the table starts.
                 */
                String[] headerFields = StringUtils.splitPreserveAllTokens( line, "\t" );

                if ( headerFields.length != column2Factor.size() + 1 ) {
                    throw new IOException( "Expected " + ( column2Factor.size() + 1 )
                            + " columns based on EF descriptions (plus id column), got " + headerFields.length );
                }

                indexHeader( column2Factor, index2Column, headerFields );
                readHeader = true;
            } else {
                /*
                 * Regular data line.
                 */
                String[] fields = StringUtils.splitPreserveAllTokens( line, "\t" );
                if ( fields.length != column2Factor.size() + 1 ) {
                    throw new IOException( "Expected " + ( column2Factor.size() + 1 )
                            + " columns based on EF descriptions (plus id column), got " + fields.length );
                }

                assignValuesToSamples( column2Factor, index2Column, fields, factorTypes, name2BioMaterial, dryRun );

            }
        }

        if ( dryRun ) {
            log.info( "Seems like it should be okay" );
            return;
        }

        experimentalDesignService.update( ed );

        // now update the biomaterials.

        Collection<BioMaterial> bms = new HashSet<BioMaterial>();
        for ( BioAssay ba : experiment.getBioAssays() ) {
            for ( BioMaterial bm : ba.getSamplesUsed() ) {
                bms.add( bm );
            }
        }
        updateBioMaterials( bms, ed );
    }

    public void setBioMaterialService( BioMaterialService bioMaterialService ) {
        this.bioMaterialService = bioMaterialService;
    }

    public void setExperimentalDesignService( ExperimentalDesignService experimentalDesignService ) {
        this.experimentalDesignService = experimentalDesignService;
    }

    public void setFactorValueService( FactorValueService factorValueService ) {
        this.factorValueService = factorValueService;
    }

    public void setMgedOntologyService( MgedOntologyService mgedOntologyService ) {
        this.mgedOntologyService = mgedOntologyService;
    }

    /**
     * @param value
     * @param bm
     */
    private void addNewMeasurement( ExperimentalFactor ef, String value, BioMaterial bm, boolean dryRun ) {

        // make sure we don't add two values.
        for ( FactorValue existingfv : bm.getFactorValues() ) {
            if ( existingfv.getExperimentalFactor().equals( ef ) ) {
                throw new IllegalStateException( bm + " already has a factorvalue for " + ef + "(" + existingfv + ")" );
            }
        }

        FactorValue fv = FactorValue.Factory.newInstance( ef );

        fv.setValue( value );

        Measurement m = Measurement.Factory.newInstance();
        m.setType( MeasurementType.ABSOLUTE );
        m.setValue( value );
        try {
            Double.parseDouble( value ); // check if it is a number, don't need the value.
            m.setRepresentation( PrimitiveType.DOUBLE );
        } catch ( NumberFormatException e ) {
            m.setRepresentation( PrimitiveType.STRING );
        }

        fv.setValue( value );
        fv.setMeasurement( m );
        fv.setExperimentalFactor( ef );

        if ( !dryRun ) fv = factorValueService.create( fv );

        ef.getFactorValues().add( fv );
        log.debug( "Adding " + fv + " to " + bm );
        bm.getFactorValues().add( fv );
    }

    /**
     * Try to interpret the first field of a row as a biomaterial, and apply the factor values to it.
     * 
     * @param column2Factor
     * @param index2Column
     * @param fields
     */
    private void assignValuesToSamples( Map<String, ExperimentalFactor> column2Factor,
            Map<Integer, String> index2Column, String[] fields, Map<String, FactorType> factorTypes,
            Map<String, BioMaterial> name2BioMaterial, boolean dryRun ) {
        String sampleId = StringUtils.strip( fields[0] );

        BioMaterial bm = getBioMaterial( sampleId, name2BioMaterial );
        if ( bm == null ) {
            /*
             * It is arguable whether this should be an exception. I think it has to be to make sure that simple errors
             * in the format are caught. But it's inconvenient for cases where a single 'design' file is to be used for
             * multiple microarray studies.
             */
            throw new IllegalArgumentException(
                    "The uploaded file has a biomaterial name that does not match the study: " + sampleId );
        }

        for ( int i = 1; i < fields.length; i++ ) {

            String value = StringUtils.strip( fields[i] );

            if ( StringUtils.isBlank( value ) ) {
                /*
                 * Missing value. Note that catching 'NA' etc. is hard, because they could be valid strings.
                 */
                continue;
            }

            String key = index2Column.get( i );

            ExperimentalFactor ef = column2Factor.get( key );

            FactorType ft = factorTypes.get( key );

            assert ft != null;

            if ( ft.equals( FactorType.CONTINUOUS ) ) {
                addNewMeasurement( ef, value, bm, dryRun );
            } else {

                VocabCharacteristic category = ( VocabCharacteristic ) ef.getCategory();

                boolean found = seekExistingFactorValue( ef, value, bm );

                if ( !found ) {
                    newFactorValue( ef, category, value, bm );
                }
            }

        }
    }

    /**
     * 
     */
    private Map<String, BioMaterial> buildBmMap( ExpressionExperiment experiment ) {
        Map<String, BioMaterial> name2BioMaterial = new HashMap<String, BioMaterial>();
        for ( BioAssay ba : experiment.getBioAssays() ) {
            // String name = ba.getName();
            BioMaterial bm = ba.getSamplesUsed().iterator().next();// FIXME handle the case of a collection;
            // name2BioMaterial.put( name, bm );
            name2BioMaterial.put( bm.getName(), bm );
        }
        return name2BioMaterial;

    }

    /**
     * Format: NAME : Category=CATEGORY Type=TYPE CATEGORY: Any MGED term supported by Gemma TYPE :
     * {continuous|categorical}.
     * 
     * @param line
     * @param ed
     * @param column2Factor
     * @param dryRun
     * @throws IOException
     */
    private void buildExperimentalFactor( String line, ExperimentalDesign ed,
            Map<String, ExperimentalFactor> column2Factor, Map<String, FactorType> factorTypes,
            Collection<OntologyTerm> terms, boolean dryRun ) throws IOException {
        String[] fields = line.split( ":" );
        if ( fields.length != 2 ) {
            throw new IOException( "EF description must have two fields with a single ':' in between (" + line + ")" );
        }

        String columnHeader = StringUtils.strip( fields[0].replaceFirst( Pattern
                .quote( EXPERIMENTAL_FACTOR_DESCRIPTION_LINE_INDICATOR )
                + "\\s*", "" ) );
        String factorString = StringUtils.strip( fields[1] );

        String[] descriptions = StringUtils.split( factorString );

        if ( descriptions.length != 2 ) {
            throw new IOException( "EF details should have the format 'Category=CATEGORY Type=TYPE'" );
        }

        String categoryS = descriptions[0];

        String category = StringUtils.split( categoryS, "=" )[1];

        VocabCharacteristic vc = mgedLookup( category, terms );

        String typeS = descriptions[1];

        String type = StringUtils.split( typeS, "=" )[1];

        FactorType ftype = FactorType.valueOf( type.toUpperCase() );
        factorTypes.put( columnHeader, ftype );

        ExperimentalFactor ef = ExperimentalFactor.Factory.newInstance( ed );
        ef.setCategory( vc );
        ef.setName( columnHeader );
        ef.setDescription( columnHeader );

        /*
         * Attempt to match to existing experimental factor.
         */
        boolean foundMatch = false;
        for ( ExperimentalFactor existingEfs : ed.getExperimentalFactors() ) {
            if ( existingEfs.getName().equals( ef.getName() ) ) {
                log.info( ef + " matches existing " + existingEfs );
                ef = existingEfs;
                foundMatch = true;
            }
        }

        if ( !foundMatch ) {
            ed.getExperimentalFactors().add( ef );
            if ( !dryRun ) {
                experimentalDesignService.update( ed );
                assert ef.getId() != null;
            }
        }

        log.info( "Factor: " + columnHeader );

        column2Factor.put( columnHeader, ef );

    }

    /**
     * make sure the biomaterial doesn't already have a factorvalue for the given factor.
     * 
     * @param ef
     * @param bm
     */
    private void checkForDuplicateValueForFactorOnBiomaterial( ExperimentalFactor ef, BioMaterial bm ) {
        for ( FactorValue existingfv : bm.getFactorValues() ) {
            assert existingfv.getExperimentalFactor() != null;
            if ( existingfv.getExperimentalFactor().equals( ef ) ) {
                /*
                 * We might change this to allow either over-writing or skipping.
                 */
                throw new IllegalStateException( bm + " already has a factorvalue for " + ef + "(" + existingfv + ")" );
            }
        }
    }

    /**
     * @param sampleId
     * @return
     */
    private BioMaterial getBioMaterial( String sampleId, Map<String, BioMaterial> name2BioMaterial ) {
        if ( !name2BioMaterial.containsKey( sampleId ) ) {
            String[] fields = StringUtils.splitByWholeSeparator( sampleId,
                    ExpressionDataWriterUtils.DELIMITER_BETWEEN_BIOMATERIAL_AND_BIOASSAYS );
            if ( fields.length > 1 && name2BioMaterial.containsKey( fields[0] ) ) {
                return name2BioMaterial.get( fields[0] );
            }

            return null;
        }
        return name2BioMaterial.get( sampleId );

    }

    /**
     * @param column2Factor
     * @param index2Column
     * @param headerFields
     * @throws IOException
     */
    private void indexHeader( Map<String, ExperimentalFactor> column2Factor, Map<Integer, String> index2Column,
            String[] headerFields ) throws IOException {
        for ( int i = 1; i < headerFields.length; i++ ) {

            String value = headerFields[i];

            if ( !column2Factor.containsKey( value ) ) {
                throw new IOException( "Expected to find an EF matching the column heading '" + value + "'" );
            }

            index2Column.put( i, value );

        }
    }

    /**
     * @param category
     * @return
     */
    private VocabCharacteristic mgedLookup( String category, Collection<OntologyTerm> terms ) {

        OntologyTerm t = null;
        for ( OntologyTerm to : terms ) {
            if ( to.getTerm().equals( category ) ) {
                t = to;
                break;
            }
        }

        if ( t == null ) {
            throw new IllegalArgumentException( "No MGED term matches '" + category + "'" );
        }

        VocabCharacteristic vc = VocabCharacteristic.Factory.newInstance();
        vc.setCategoryUri( t.getUri() );
        vc.setCategory( t.getTerm() );
        vc.setValueUri( t.getUri() );
        vc.setValue( t.getTerm() );
        vc.setEvidenceCode( GOEvidenceCode.IC );
        return vc;
    }

    /**
     * @param ef
     * @param category
     * @param value
     * @param bm
     */
    private void newFactorValue( ExperimentalFactor ef, VocabCharacteristic category, String value, BioMaterial bm ) {
        FactorValue newFv = FactorValue.Factory.newInstance();
        newFv.setValue( value );
        VocabCharacteristic newVc = VocabCharacteristic.Factory.newInstance();
        String category2 = category.getCategory();
        assert category2 != null;
        newVc.setCategory( category2 );
        newVc.setCategoryUri( category.getCategoryUri() );
        newVc.setValue( value );
        newVc.setEvidenceCode( GOEvidenceCode.IC );
        newFv.getCharacteristics().add( newVc );
        ef.getFactorValues().add( newFv );

        newFv.setExperimentalFactor( ef ); // this needs to be persisted first

        this.checkForDuplicateValueForFactorOnBiomaterial( ef, bm );

        log.debug( "Adding " + newFv + " to " + bm );
        bm.getFactorValues().add( newFv );
        log.debug( "New factor value: " + value );
    }

    /**
     * Check to make sure we don't try to put two factorValues for the same factor on a given biomaterial.
     * 
     * @param ef
     * @param value
     * @param bm
     * @return true if a match was found, false otherwise.
     */
    private boolean seekExistingFactorValue( ExperimentalFactor ef, String value, BioMaterial bm ) {
        for ( FactorValue fv : ef.getFactorValues() ) {
            if ( fv.getCharacteristics().size() > 0 ) {
                for ( Characteristic c : fv.getCharacteristics() ) {
                    if ( c.getValue().equals( value ) ) {
                        checkForDuplicateValueForFactorOnBiomaterial( ef, bm );
                        // add this factorvalue to the corresponding sample.
                        log.debug( "Adding " + fv + " to " + bm );
                        bm.getFactorValues().add( fv );
                        return true;
                    }
                }
            } else {
                if ( fv.getValue().equals( value ) ) {
                    checkForDuplicateValueForFactorOnBiomaterial( ef, bm );
                    // add this factorvalue to the corresponding sample.
                    log.debug( "Adding " + fv + " to " + bm );
                    bm.getFactorValues().add( fv );
                    return true;
                }
            }
            // measurements are a separate case.

        }
        return false;
    }

    /**
     * Replace the placeholder factorvalues associated with the biomaterials with the persistent ones.
     * 
     * @param bms
     * @param design
     */
    private void updateBioMaterials( Collection<BioMaterial> bms, ExperimentalDesign design ) {
        assert design.getExperimentalFactors().size() > 0;
        Collection<FactorValue> usedFactorValues = new HashSet<FactorValue>();
        for ( BioMaterial bm : bms ) {
            Collection<FactorValue> values = new HashSet<FactorValue>();
            /*
             * For each factor, find a value that matches the one on the biomaterial.
             */
            factor: for ( ExperimentalFactor factor : design.getExperimentalFactors() ) {
                boolean found = false;
                for ( FactorValue temp : bm.getFactorValues() ) {

                    // if ( temp.getId() != null ) {
                    // // don't throw away old factors, but it won't be replaced either.
                    // values.add( temp );
                    // continue factor;
                    // }

                    if ( !temp.getExperimentalFactor().getName().equals( factor.getName() ) ) {
                        continue;
                    }

                    if ( log.isDebugEnabled() ) log.debug( "Trying to find match for " + temp );

                    for ( FactorValue fv : factor.getFactorValues() ) {
                        if ( log.isDebugEnabled() ) log.debug( "Candidate: " + fv );
                        assert temp.getValue() != null;
                        assert fv.getValue() != null;

                        if ( temp.getValue().equals( fv.getValue() ) ) {
                            values.add( factorValueService.load( fv.getId() ) );
                            found = true;
                            log.debug( "Match found for " + temp );
                            continue factor;
                        }
                    }
                }
                if ( !found ) {
                    // this is not uncommon...
                    log.debug( "Missing data for " + factor + " on " + bm );
                }
            }
            usedFactorValues.addAll( values );
            bm.setFactorValues( values );
            bioMaterialService.update( bm );
        }

        /*
         * Remove factors that were never used. This is necessary because the design file could contain information
         * about samples that aren't in the current data set.
         */
        for ( ExperimentalFactor factor : design.getExperimentalFactors() ) {
            for ( Iterator<FactorValue> fvit = factor.getFactorValues().iterator(); fvit.hasNext(); ) {
                if ( !usedFactorValues.contains( fvit.next() ) ) {
                    fvit.remove();
                }
            }
        }

        this.experimentalDesignService.update( design );

    }

}
