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
package ubic.gemma.model.expression.arrayDesign;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ubic.gemma.model.common.auditAndSecurity.curation.AbstractCuratableValueObject;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Value object for quickly displaying varied information about Array Designs.
 *
 * @author paul et al
 */
@SuppressWarnings("unused") // Used in front end
@Data
@EqualsAndHashCode(of = { "shortName" }, callSuper = true)
public class ArrayDesignValueObject extends AbstractCuratableValueObject<ArrayDesign> implements Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8259245319391937522L;

    public static Collection<ArrayDesignValueObject> create( Collection<ArrayDesign> subsumees ) {
        Collection<ArrayDesignValueObject> r = new HashSet<>();
        for ( ArrayDesign ad : subsumees ) {
            r.add( new ArrayDesignValueObject( ad ) );
        }
        return r;
    }

    private Boolean blackListed = false;
    private String color; // FIXME redundant with technologyType
    @JsonIgnore
    private String dateCached;
    private String description;
    @JsonIgnore
    private Integer designElementCount;
    private Integer expressionExperimentCount;
    @JsonIgnore
    private Boolean hasBlatAssociations;

    @JsonIgnore
    private Boolean hasGeneAssociations;

    private Boolean hasSequenceAssociations;
    private Boolean isAffymetrixAltCdf = false;
    /**
     * Indicates this array design is the merger of other array designs.
     */
    @JsonIgnore
    private Boolean isMerged;
    /**
     * Indicates that this array design has been merged into another.
     */
    private Boolean isMergee;
    /**
     * Indicate if this array design is subsumed by some other array design.
     */
    @JsonIgnore
    private Boolean isSubsumed;
    /**
     * Indicates if this array design subsumes some other array design(s)
     */
    @JsonIgnore
    private Boolean isSubsumer;
    @JsonIgnore
    private Date lastGeneMapping;
    @JsonIgnore
    private Date lastRepeatMask;
    @JsonIgnore
    private Date lastSequenceAnalysis;
    @JsonIgnore
    private Date lastSequenceUpdate;
    private String name;
    /**
     * The number of unique genes that this array design maps to.
     *
     * @deprecated this should have never been a {@link String}, use {@link #numberOfGenes} instead.
     */
    @Deprecated
    @JsonIgnore
    private String numGenes;
    /**
     * The number of unique genes that this array design maps to, or null if unspecified.
     */
    @Nullable
    private Long numberOfGenes;
    /**
     * The number of probes that have BLAT alignments.
     */
    @JsonIgnore
    private String numProbeAlignments;
    /**
     * The number of probes that map to bioSequences.
     */
    @JsonIgnore
    private String numProbeSequences;
    /**
     * The number of probes that map to genes. This count includes probe-aligned regions, predicted genes, and known
     * genes.
     */
    @JsonIgnore
    private String numProbesToGenes;
    private String shortName;
    private Integer switchedExpressionExperimentCount = 0; // how many "hidden" assocations there are.
    private String taxon;
    private Long taxonID;

    private String technologyType;

    public ArrayDesignValueObject( Long id ) {
        super( id );
    }

    /**
     * This will only work if the object is thawed (lightly). Not everything will be filled in -- test before using!
     *
     * @param ad ad
     */
    public ArrayDesignValueObject( ArrayDesign ad ) {
        super( ad );
        this.name = ad.getName();
        this.shortName = ad.getShortName();
        this.description = ad.getDescription();
        this.taxon = ad.getPrimaryTaxon().getCommonName();
        this.taxonID = ad.getPrimaryTaxon().getId();
        if ( ad.getTechnologyType() != null ) {
            this.technologyType = ad.getTechnologyType().toString();
        }

        TechnologyType c = ad.getTechnologyType();
        if ( c != null ) {
            this.technologyType = c.toString();
            this.color = c.getValue();
        }

        // no need to initialize them to know if the entities exist
        this.isMergee = ad.getMergedInto() != null;
        this.isAffymetrixAltCdf = ad.getAlternativeTo() != null;
    }

    /**
     * Copies constructor from other ArrayDesignValueObject
     */
    protected ArrayDesignValueObject( ArrayDesignValueObject arrayDesignValueObject ) {
        super( arrayDesignValueObject );
        this.color = arrayDesignValueObject.color;
        this.dateCached = arrayDesignValueObject.dateCached;
        this.description = arrayDesignValueObject.description;
        this.designElementCount = arrayDesignValueObject.designElementCount;
        this.expressionExperimentCount = arrayDesignValueObject.expressionExperimentCount;
        this.hasBlatAssociations = arrayDesignValueObject.hasBlatAssociations;
        this.hasGeneAssociations = arrayDesignValueObject.hasGeneAssociations;
        this.hasSequenceAssociations = arrayDesignValueObject.hasSequenceAssociations;
        this.isMerged = arrayDesignValueObject.isMerged;
        this.isMergee = arrayDesignValueObject.isMergee;
        this.isSubsumed = arrayDesignValueObject.isSubsumed;
        this.isSubsumer = arrayDesignValueObject.isSubsumer;
        this.lastGeneMapping = arrayDesignValueObject.lastGeneMapping;
        this.lastRepeatMask = arrayDesignValueObject.lastRepeatMask;
        this.lastSequenceAnalysis = arrayDesignValueObject.lastSequenceAnalysis;
        this.lastSequenceUpdate = arrayDesignValueObject.lastSequenceUpdate;
        this.name = arrayDesignValueObject.name;
        this.numGenes = arrayDesignValueObject.numGenes;
        this.numProbeAlignments = arrayDesignValueObject.numProbeAlignments;
        this.numProbeSequences = arrayDesignValueObject.numProbeSequences;
        this.numProbesToGenes = arrayDesignValueObject.numProbesToGenes;
        this.shortName = arrayDesignValueObject.shortName;
        this.taxon = arrayDesignValueObject.taxon;
        this.taxonID = arrayDesignValueObject.taxonID;
        this.technologyType = arrayDesignValueObject.technologyType;
        this.isAffymetrixAltCdf = arrayDesignValueObject.isAffymetrixAltCdf;
        this.blackListed = arrayDesignValueObject.blackListed;
    }

    @Override
    public String toString() {
        return this.getShortName();
    }
}