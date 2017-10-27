package ubic.gemma.web.services.rest.util.args;

import ubic.gemma.model.genome.Taxon;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;

/**
 * String argument type for taxon API, referencing the Taxon scientific name, common name or abbreviation. Can also be null.
 *
 * @author tesarst
 */
public class TaxonStringArg extends TaxonArg<String> {

    TaxonStringArg( String s ) {
        this.value = s;
        setNullCause( "common or scientific name, or abbreviation,", "Taxon" );
    }

    @Override
    public Taxon getPersistentObject( TaxonService service ) {
        return check( this.value == null ? null : this.tryAllNameProperties( service ) );
    }

    @Override
    public String getPropertyName( TaxonService service ) {
        Taxon taxon = service.findByCommonName( this.value );

        if ( taxon != null ) {
            return "commonName";
        }
        taxon = service.findByScientificName( this.value );

        if ( taxon != null ) {
            return "scientificName";
        }
        taxon = service.findByAbbreviation( this.value );
        if ( taxon != null ) {
            return "abbreviation";
        }

        return null;
    }

    /**
     * Tries to retrieve a Taxon based on its names.
     *
     * @param service the TaxonService that handles the search.
     * @return Taxon or null if no taxon with any property matching this#value was found.
     */
    private Taxon tryAllNameProperties( TaxonService service ) {
        // Most commonly used
        Taxon taxon = service.findByCommonName( this.value );

        if ( taxon == null ) {
            taxon = service.findByScientificName( this.value );
        }

        if ( taxon == null ) {
            taxon = service.findByAbbreviation( this.value );
        }

        return taxon;
    }
}
