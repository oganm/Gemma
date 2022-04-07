/*
 * The Gemma project
 *
 * Copyright (c) 2012 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.model.expression.bioAssay;

import lombok.EqualsAndHashCode;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.common.description.DatabaseEntryValueObject;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;
import ubic.gemma.model.expression.biomaterial.BioMaterialValueObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Paul
 */
@SuppressWarnings("unused") // ValueObject accessed from JS
@EqualsAndHashCode(of = { "name" }, callSuper = true)
public class BioAssayValueObject extends IdentifiableValueObject<BioAssay> implements Serializable {

    private static final long serialVersionUID = 9164284536309673585L;

    public static Collection<BioAssayValueObject> convert2ValueObjects( Collection<BioAssay> bioAssays ) {
        Collection<BioAssayValueObject> result = new HashSet<>();
        for ( BioAssay bioAssay : bioAssays ) {
            result.add( new BioAssayValueObject( bioAssay, false ) );
        }
        return result;
    }

    private DatabaseEntryValueObject accession = null;
    private ArrayDesignValueObject arrayDesign;
    private String description = "";
    private String metadata;
    private String name = "";
    private ArrayDesignValueObject originalPlatform;
    // if it was removed as an outlier
    private Boolean outlier = false;
    // if our algorithm says it might be an outlier.
    private Boolean predictedOutlier = false;
    private Date processingDate;
    private BioMaterialValueObject sample;
    private Boolean sequencePairedReads;
    private Integer sequenceReadCount;
    private Integer sequenceReadLength;

    // to hold state change, initialized as this.outlier
    private Boolean userFlaggedOutlier = false;

    public BioAssayValueObject( BioAssay bioAssay, boolean basic ) {
        super( bioAssay );
        this.name = bioAssay.getName();
        this.description = bioAssay.getDescription();

        ArrayDesign ad = bioAssay.getArrayDesignUsed();
        assert ad != null;
        this.arrayDesign = new ArrayDesignValueObject( ad );

        ArrayDesign op = bioAssay.getOriginalPlatform();
        if ( op != null ) {
            this.originalPlatform = new ArrayDesignValueObject( op );
        }

        this.processingDate = bioAssay.getProcessingDate();
        this.sequencePairedReads = bioAssay.getSequencePairedReads();
        this.sequenceReadLength = bioAssay.getSequenceReadLength();
        this.sequenceReadCount = bioAssay.getSequenceReadCount();
        this.metadata = bioAssay.getMetadata();

        if ( bioAssay.getAccession() != null ) {
            this.accession = new DatabaseEntryValueObject( bioAssay.getAccession() );
        }

        if ( bioAssay.getSampleUsed() != null ) {
            this.sample = new BioMaterialValueObject( bioAssay.getSampleUsed(), basic );
            sample.getBioAssays().add( this.getId() );
        }

        if ( bioAssay.getIsOutlier() != null ) {
            this.outlier = bioAssay.getIsOutlier();
        }

        this.userFlaggedOutlier = this.outlier;
    }

    public BioAssayValueObject( BioAssay bioAssay, boolean basic, boolean predictedOutlier ) {
        this( bioAssay, basic );
        this.predictedOutlier = predictedOutlier;
    }

    public BioAssayValueObject( Long id ) {
        super( id );
    }

    public DatabaseEntryValueObject getAccession() {
        return accession;
    }

    public ArrayDesignValueObject getArrayDesign() {
        return arrayDesign;
    }

    public String getDescription() {
        return description;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getName() {
        return name;
    }

    public ArrayDesignValueObject getOriginalPlatform() {
        return originalPlatform;
    }

    public Boolean getPredictedOutlier() {
        return predictedOutlier;
    }

    public Date getProcessingDate() {
        return processingDate;
    }

    public BioMaterialValueObject getSample() {
        return sample;
    }

    public Boolean getSequencePairedReads() {
        return sequencePairedReads;
    }

    public Integer getSequenceReadCount() {
        return sequenceReadCount;
    }

    public Integer getSequenceReadLength() {
        return sequenceReadLength;
    }

    public Boolean getUserFlaggedOutlier() {
        return userFlaggedOutlier;
    }

    public boolean isOutlier() {
        return outlier;
    }

    public void setAccession( DatabaseEntryValueObject accession ) {
        this.accession = accession;
    }

    public void setArrayDesign( ArrayDesignValueObject arrayDesign ) {
        this.arrayDesign = arrayDesign;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setMetadata( String metadata ) {
        this.metadata = metadata;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setOriginalPlatform( ArrayDesignValueObject originalPlatform ) {
        this.originalPlatform = originalPlatform;
    }

    public void setOutlier( boolean outlier ) {
        this.outlier = outlier;
    }

    public void setPredictedOutlier( Boolean predictedOutlier ) {
        this.predictedOutlier = predictedOutlier;
    }

    public void setProcessingDate( Date processingDate ) {
        this.processingDate = processingDate;
    }

    public void setSample( BioMaterialValueObject sample ) {
        this.sample = sample;
    }

    public void setSequencePairedReads( Boolean sequencePairedReads ) {
        this.sequencePairedReads = sequencePairedReads;
    }

    public void setSequenceReadCount( Integer sequenceReadCount ) {
        this.sequenceReadCount = sequenceReadCount;
    }

    public void setSequenceReadLength( Integer sequenceReadLength ) {
        this.sequenceReadLength = sequenceReadLength;
    }

    public void setUserFlaggedOutlier( Boolean userFlaggedOutlier ) {
        this.userFlaggedOutlier = userFlaggedOutlier;
    }

    @Override
    public String toString() {
        return "BioAssayVO [" + ( getId() != null ? "id=" + getId() + ", " : "" ) + ( name != null ? "name=" + name + ", " : "" )
                + ( description != null ? "description=" + description : "" ) + "]";
    }
}
