/*
 * The Gemma project Copyright (c) 2009 University of British Columbia Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */
package ubic.gemma.web.controller.expression.experiment;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.FactorValue;

/**
 * @author ?
 * @version $Id$
 */
public class FactorValueObject implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3378801249808036785L;
    private String factor;
    private long id;
    private String category;
    private String description;
    private Characteristic categoryCharacteritic;
    private String categoryUri;
    private String value;
    private String valueUri;
    private boolean measurement = false;
    
    /*
     * This is used simply as a distinguishing id - it could be the id of the measurement if there is no characteristic.
     */
    private long charId;

    public long getCharId() {
        return charId;
    }

    public void setCharId( long charId ) {
        this.charId = charId;
    }
    
    public String getValueUri() {
        return valueUri;
    }

    public void setValueUri( String valueUri ) {
        this.valueUri = valueUri;
    }

    
    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }
    
    public String getCategoryUri() {
        return categoryUri;
    }

    public void setCategoryUri( String categoryUri ) {
        this.categoryUri = categoryUri;
    }

    public boolean isMeasurement() {
        return measurement;
    }

    public void setMeasurement( boolean measurement ) {
        this.measurement = measurement;
    }
    
    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory( String category ) {
        this.category = category;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId( long id ) {
        this.id = id;
    }

    public FactorValueObject() {
        super();

    }

    
    
    /**
     * @param value
     * @param c
     */
    public FactorValueObject( FactorValue value, Characteristic c ) {

        this.setId(  value.getId() );
        this.setFactorValue( getSummaryString( value ) );

        if ( value.getMeasurement() != null ) {
            this.setMeasurement( true );
            this.value = value.getMeasurement().getValue();
            this.setCharId( value.getMeasurement().getId() );
        } else if ( c.getId() != null ) {
            this.setCharId( c.getId() );
        }

        this.setCategory( c.getCategory() );
        this.setValue( c.getValue() );
        if ( c instanceof VocabCharacteristic ) {
            VocabCharacteristic vc = ( VocabCharacteristic ) c;
            this.setCategoryUri( vc.getCategoryUri() );
            this.setValueUri( vc.getValueUri() );
        }
    }
    
    
    public FactorValueObject( FactorValue fv ) {

        this.id = fv.getId();
        this.factor = "";

        if ( fv.getCharacteristics().size() > 0 ) {
            for ( Characteristic c : fv.getCharacteristics() ) {
                factor += c.getValue();
                // FIXME: with will always be the category and description of the last characteristic....
                category = c.getCategory();
                description = c.getDescription();
            }

        } else {
            factor += fv.getValue();
        }

    }

    public FactorValueObject( ExperimentalFactor ef ) {

        this.description = ef.getDescription();
        this.factor = ef.getName();
        this.id = ef.getId();

        Characteristic c = ef.getCategory();
        if ( c == null )
            this.category = "none";
        else if ( c instanceof VocabCharacteristic ) {
            VocabCharacteristic vc = ( VocabCharacteristic ) c;
            this.category = vc.getCategory();
        } else
            this.category = c.getCategory();
    }

    public String getFactorValue() {

        return factor;
    }

    public void setFactorValue( String value ) {

        this.factor = value;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription( String description ) {
        this.description = description;
    }

    /**
     * @return the categoryCharacteritic
     */
    public Characteristic getCategoryCharacteritic() {
        return categoryCharacteritic;
    }

    /**
     * @param categoryCharacteritic the categoryCharacteritic to set
     */
    public void setCategoryCharacteritic( Characteristic categoryCharacteritic ) {
        this.categoryCharacteritic = categoryCharacteritic;
    }
    
    
    /**
     * @param fv
     * @return
     */
    private String getSummaryString( FactorValue fv ) {
        StringBuffer buf = new StringBuffer();
        if ( fv.getCharacteristics().size() > 0 ) {
            for ( Iterator<Characteristic> iter = fv.getCharacteristics().iterator(); iter.hasNext(); ) {
                Characteristic c = iter.next();
                buf.append( c.getCategory() );
                buf.append( ": " );
                buf.append( c.getValue() == null ? "no value" : c.getValue() );
                if ( iter.hasNext() ) buf.append( ", " );
            }
        } else if ( fv.getMeasurement() != null ) {
            buf.append( fv.getMeasurement().getValue() );
        } else if ( StringUtils.isNotBlank( fv.getValue() ) ) {
            buf.append( fv.getValue() );
        } else {
            buf.append( "?" );
        }
        return buf.toString();
    }
    

}
