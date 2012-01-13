package ubic.gemma.model.genome.gene.phenotype.valueObject;

import ubic.gemma.model.DatabaseEntryValueObject;
import ubic.gemma.model.ExternalDatabaseValueObject;
import ubic.gemma.model.common.description.DatabaseEntry;

public class EvidenceSourceValueObject extends DatabaseEntryValueObject {

    // used by neurocarta to find the url of an evidence source
    private String externalUrl = "";

    public String getExternalUrl() {
        return this.externalUrl;
    }

    public void setExternalUrl( String externalUrl ) {
        this.externalUrl = externalUrl;
    }

    public EvidenceSourceValueObject( DatabaseEntry de ) {
        super( de );
        this.externalUrl = de.getExternalDatabase().getWebUri() + de.getAccession();
    }

    public EvidenceSourceValueObject( String accession, ExternalDatabaseValueObject externalDatabase ) {
        setAccession( accession );
        setExternalDatabase( externalDatabase );
    }

}
