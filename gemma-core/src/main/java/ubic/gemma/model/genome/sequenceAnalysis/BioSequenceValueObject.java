package ubic.gemma.model.genome.sequenceAnalysis;

import lombok.EqualsAndHashCode;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.common.description.DatabaseEntryValueObject;
import ubic.gemma.model.genome.TaxonValueObject;
import ubic.gemma.model.genome.biosequence.BioSequence;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({ "unused", "WeakerAccess" }) // Used in frontend
@EqualsAndHashCode(of = { "sequenceDatabaseEntry", "name", "type" }, callSuper = true)
public class BioSequenceValueObject extends IdentifiableValueObject<BioSequence> {

    private String description;
    private Double fractionRepeats;
    private Long length;
    private String name;
    private String sequence;
    private DatabaseEntryValueObject sequenceDatabaseEntry;
    private TaxonValueObject taxon;
    private ubic.gemma.model.genome.biosequence.SequenceType type;

    private BioSequenceValueObject( BioSequence bioSequence ) {
        super( bioSequence );
    }

    public static Collection<BioSequenceValueObject> fromEntities( Collection<BioSequence> bsList ) {
        Collection<BioSequenceValueObject> result = new ArrayList<>();
        for ( BioSequence bs : bsList ) {
            result.add( BioSequenceValueObject.fromEntity( bs ) );
        }
        return result;
    }

    public static BioSequenceValueObject fromEntity( BioSequence bs ) {
        BioSequenceValueObject vo = new BioSequenceValueObject( bs );
        vo.setName( bs.getName() );
        vo.setDescription( bs.getDescription() );
        vo.setSequence( bs.getSequence() );
        if ( bs.getSequenceDatabaseEntry() != null ) {
            vo.setSequenceDatabaseEntry( new DatabaseEntryValueObject( bs.getSequenceDatabaseEntry() ) );
        }
        vo.setLength( bs.getLength() );
        vo.setType( bs.getType() );
        vo.setFractionRepeats( bs.getFractionRepeats() );
        // FIXME: BioSequence returned by the SearchService might have a null taxon
        if ( bs.getTaxon() != null ) {
            vo.setTaxon( TaxonValueObject.fromEntity( bs.getTaxon() ) );
        }
        return vo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public Double getFractionRepeats() {
        return this.fractionRepeats;
    }

    public void setFractionRepeats( Double fractionRepeats ) {
        this.fractionRepeats = fractionRepeats;
    }

    public Long getLength() {
        return this.length;
    }

    public void setLength( Long length ) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getSequence() {
        return this.sequence;
    }

    public void setSequence( String sequence ) {
        this.sequence = sequence;
    }

    public DatabaseEntryValueObject getSequenceDatabaseEntry() {
        return this.sequenceDatabaseEntry;
    }

    public void setSequenceDatabaseEntry( DatabaseEntryValueObject sequenceDatabaseEntry ) {
        this.sequenceDatabaseEntry = sequenceDatabaseEntry;
    }

    public TaxonValueObject getTaxon() {
        return this.taxon;
    }

    public void setTaxon( TaxonValueObject taxon ) {
        this.taxon = taxon;
    }

    public ubic.gemma.model.genome.biosequence.SequenceType getType() {
        return this.type;
    }

    public void setType( ubic.gemma.model.genome.biosequence.SequenceType type ) {
        this.type = type;
    }
}
