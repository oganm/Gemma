/*
 * The Gemma project
 * 
 * Copyright (c) 2009 University of British Columbia
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

package ubic.gemma.model.genome.gene;

import java.util.Collection;
import java.util.HashSet;

import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.phenotype.valueObject.CharacteristicValueObject;

/**
 * @author kelsey
 * @version $Id$
 */
public class GeneValueObject implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7098036090107647318L;

    /**
     * FIXME does not convert aliases.
     * 
     * @param genes
     * @return
     */
    public static Collection<GeneValueObject> convert2ValueObjects( Collection<Gene> genes ) {

        Collection<GeneValueObject> converted = new HashSet<>();
        if ( genes == null ) return converted;

        for ( Gene g : genes ) {
            GeneValueObject geneValueObject = new GeneValueObject( g.getId(), g.getName(), null, g.getNcbiGeneId(),
                    g.getOfficialSymbol(), g.getOfficialName(), g.getDescription(), null, g.getTaxon().getId(), g
                            .getTaxon().getScientificName(), g.getTaxon().getCommonName() );
            converted.add( geneValueObject );

        }

        return converted;
    }

    /**
     * A static method for easily converting GeneSetMembers into GeneValueObjects FIXME does not convert aliases.
     * 
     * @param genes
     * @return
     */
    public static Collection<GeneValueObject> convertMembers2GeneValueObjects( Collection<GeneSetMember> genes ) {

        Collection<GeneValueObject> converted = new HashSet<GeneValueObject>();
        if ( genes == null ) return converted;

        for ( GeneSetMember g : genes ) {
            if ( g == null ) continue;
            converted.add( new GeneValueObject( g ) );
        }

        return converted;
    }

    private Collection<String> aliases;

    /**
     * How many experiments "involve" (manipulate, etc.) this gene
     */
    private Integer associatedExperimentCount = 0;

    private Integer compositeSequenceCount = 0; // number of probes

    private String description;

    private Collection<GeneSetValueObject> geneSets = null;

    private Collection<GeneValueObject> homologues = null;

    private Long id;

    private Double multifunctionalityRank = 0.0;

    private String name;

    private Integer ncbiId;

    private double[] nodeDegreeNegRanks;

    private double[] nodeDegreePosRanks;
    
    /**
     * Array containing number of links supported by 0,1,2, .... data sets (value in first index is always 0) for
     * negative correlation coexpression
     */
    private int[] nodeDegreesNeg;
    
    /**
     * Array containing number of links supported by 0,1,2, .... data sets (value in first index is always 0) for
     * positive coexpression
     */
    private int[] nodeDegreesPos;

    private Integer numGoTerms = 0;

    private String officialName;

    private String officialSymbol;

    private Collection<CharacteristicValueObject> phenotypes;

    private Integer platformCount;

    private Double score; // This is for genes in genesets might have a rank or a score associated with them.

    private String taxonCommonName;

    private Long taxonId;

    private String taxonScientificName;

    public GeneValueObject() {
    }

    /**
     * Aliases are not filled in.
     * 
     * @param gene
     */
    public GeneValueObject( Gene gene ) {
        this.id = gene.getId();
        this.ncbiId = gene.getNcbiGeneId();
        this.officialName = gene.getOfficialName();
        this.officialSymbol = gene.getOfficialSymbol();
        this.taxonScientificName = gene.getTaxon().getScientificName();
        this.setTaxonCommonName( gene.getTaxon().getCommonName() );
        this.name = gene.getName();
        this.description = gene.getDescription();
        this.taxonId = gene.getTaxon().getId();
    }

    /**
     * Copy constructor for GeneSetMember
     * 
     * @param otherBean
     */
    public GeneValueObject( GeneSetMember otherBean ) {
        this( otherBean.getGene().getId(), otherBean.getGene().getName(), null, otherBean.getGene().getNcbiGeneId(),
                otherBean.getGene().getOfficialSymbol(), otherBean.getGene().getOfficialName(), otherBean.getGene()
                        .getDescription(), otherBean.getScore(), otherBean.getGene().getTaxon().getId(), otherBean
                        .getGene().getTaxon().getScientificName(), otherBean.getGene().getTaxon().getCommonName() );
    }

    /**
     * Copies constructor from other GeneValueObject
     * 
     * @param otherBean, cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public GeneValueObject( GeneValueObject otherBean ) {
        this( otherBean.getId(), otherBean.getName(), null, otherBean.getNcbiId(), otherBean.getOfficialSymbol(),
                otherBean.getOfficialName(), otherBean.getDescription(), otherBean.getScore(), otherBean.getTaxonId(),
                otherBean.getTaxonScientificName(), otherBean.getTaxonCommonName() );
    }

    public GeneValueObject( Long id, String name, Collection<String> aliases, Integer ncbiId, String officialSymbol,
            String officialName, String description, Double score, Long taxonId, String taxonScientificName,
            String taxonCommonName ) {
        this.id = id;
        this.name = name;
        this.ncbiId = ncbiId;
        this.officialSymbol = officialSymbol;
        this.officialName = officialName;
        this.description = description;
        this.score = score;
        this.taxonId = taxonId;
        this.taxonScientificName = taxonScientificName;
        this.taxonCommonName = taxonCommonName;
        this.aliases = aliases;
    }

    public GeneValueObject( Long geneId, String geneSymbol, String geneOfficialName, Taxon taxon ) {
        this.id = geneId;
        this.officialSymbol = geneSymbol;
        this.officialName = geneOfficialName;
        this.taxonId = taxon.getId();
        this.taxonCommonName = taxon.getCommonName();
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy( GeneValueObject otherBean ) {
        if ( otherBean != null ) {
            this.setId( otherBean.getId() );
            this.setName( otherBean.getName() );
            this.setNcbiId( otherBean.getNcbiId() );
            this.setOfficialSymbol( otherBean.getOfficialSymbol() );
            this.setOfficialName( otherBean.getOfficialName() );
            this.setDescription( otherBean.getDescription() );
            this.setScore( otherBean.getScore() );
            this.setAliases( otherBean.getAliases() );
        }
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        GeneValueObject other = ( GeneValueObject ) obj;

        if ( id != null ) {
            if ( other.id == null ) return false;
            return ( this.id == other.id );
        }
        if ( ncbiId != null ) {
            if ( other.ncbiId == null ) return false;
            return this.ncbiId == other.ncbiId;
        }

        if ( officialSymbol == null ) {
            if ( other.officialSymbol != null ) return false;
        } else if ( !officialSymbol.equals( other.officialSymbol ) ) return false;
        if ( taxonId == null ) {
            if ( other.taxonId != null ) return false;
        } else if ( !taxonId.equals( other.taxonId ) ) return false;
        return true;
    }

    public Collection<String> getAliases() {
        return aliases;
    }

    public Integer getAssociatedExperimentCount() {
        return associatedExperimentCount;
    }

    public Integer getCompositeSequenceCount() {
        return compositeSequenceCount;
    }

    /**
     * public Long getTaxonId() { return taxonId; } public void setTaxonId( Long taxonId ) { this.taxonId = taxonId; }
     */
    public String getDescription() {
        return this.description;
    }

    public Collection<GeneSetValueObject> getGeneSets() {
        return geneSets;
    }

    public Collection<GeneValueObject> getHomologues() {
        return homologues;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    public Double getMultifunctionalityRank() {
        return multifunctionalityRank;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }

    /**
     * 
     */
    public Integer getNcbiId() {
        return this.ncbiId;
    }

    public double[] getNodeDegreeNegRanks() {
        return nodeDegreeNegRanks;
    }

    public double[] getNodeDegreePosRanks() {
        return nodeDegreePosRanks;
    }

    public int[] getNodeDegreesNeg() {
        return nodeDegreesNeg;
    }

    public int[] getNodeDegreesPos() {
        return nodeDegreesPos;
    }

    public Integer getNumGoTerms() {
        return numGoTerms;
    }

    /**
     * 
     */
    public String getOfficialName() {
        return this.officialName;
    }

    /**
     * 
     */
    public String getOfficialSymbol() {
        return this.officialSymbol;
    }

    public Collection<CharacteristicValueObject> getPhenotypes() {
        return phenotypes;
    }

    public Integer getPlatformCount() {
        return platformCount;
    }

    public Double getScore() {
        return score;
    }

    /**
     * @return the taxonCommonName
     */
    public String getTaxonCommonName() {
        return taxonCommonName;
    }

    public Long getTaxonId() {
        return taxonId;
    }

    public String getTaxonScientificName() {
        return taxonScientificName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        if ( id != null ) return id.hashCode();
        if ( ncbiId != null ) return ncbiId.hashCode();
        result = prime * result + ( ( officialSymbol == null ) ? 0 : officialSymbol.hashCode() );
        result = prime * result + ( ( taxonId == null ) ? 0 : taxonId.hashCode() );
        return result;
    }

    public void setAliases( Collection<String> aliases ) {
        this.aliases = aliases;
    }

    public void setAssociatedExperimentCount( Integer associatedExperimentCount ) {
        this.associatedExperimentCount = associatedExperimentCount;
    }

    public void setCompositeSequenceCount( Integer compositeSequenceCount ) {
        this.compositeSequenceCount = compositeSequenceCount;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setGeneSets( Collection<GeneSetValueObject> geneSets ) {
        this.geneSets = geneSets;
    }

    public void setHomologues( Collection<GeneValueObject> homologues ) {
        this.homologues = homologues;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setMultifunctionalityRank( Double multifunctionalityRank ) {
        this.multifunctionalityRank = multifunctionalityRank;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setNcbiId( Integer ncbiId ) {
        this.ncbiId = ncbiId;
    }

    public void setNodeDegreeNegRanks( double[] nodeDegreeNegRanks ) {
        this.nodeDegreeNegRanks = nodeDegreeNegRanks;
    }

    public void setNodeDegreePosRanks( double[] nodeDegreePosRanks ) {
        this.nodeDegreePosRanks = nodeDegreePosRanks;
    }

    public void setNodeDegreesNeg( int[] nodeDegreesNeg ) {
        this.nodeDegreesNeg = nodeDegreesNeg;
    }

    public void setNodeDegreesPos( int[] nodeDegreesPos ) {
        this.nodeDegreesPos = nodeDegreesPos;
    }

    public void setNumGoTerms( Integer numGoTerms ) {
        this.numGoTerms = numGoTerms;
    }

    public void setOfficialName( String officialName ) {
        this.officialName = officialName;
    }

    public void setOfficialSymbol( String officialSymbol ) {
        this.officialSymbol = officialSymbol;
    }

    public void setPhenotypes( Collection<CharacteristicValueObject> phenotypes ) {
        this.phenotypes = phenotypes;
    }

    public void setPlatformCount( Integer platformCount ) {
        this.platformCount = platformCount;
    }

    public void setScore( Double score ) {
        this.score = score;
    }

    /**
     * @param taxonCommonName the taxonCommonName to set
     */
    public void setTaxonCommonName( String taxonCommonName ) {
        this.taxonCommonName = taxonCommonName;
    }

    public void setTaxonId( Long taxonId ) {
        this.taxonId = taxonId;
    }

    public void setTaxonScientificName( String taxonScientificName ) {
        this.taxonScientificName = taxonScientificName;
    }

    @Override
    public String toString() {
        return "GeneValueObject [" + ( id != null ? "id=" + id + ", " : "" )
                + ( officialSymbol != null ? "officialSymbol=" + officialSymbol + ", " : "" )
                + ( officialName != null ? "officialName=" + officialName : "" ) + "]";
    }

}