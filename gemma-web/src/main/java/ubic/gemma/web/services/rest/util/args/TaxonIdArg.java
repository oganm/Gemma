package ubic.gemma.web.services.rest.util.args;

import io.swagger.v3.oas.annotations.media.Schema;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;

/**
 * Long argument type for taxon API, referencing the Taxon ID.
 *
 * @author tesarst
 */
@Schema(type = "integer", format = "int64", description = "A numerical taxon identifier.")
public class TaxonIdArg extends TaxonArg<Long> {

    /**
     * @param l intentionally primitive type, so the value property can never be null.
     */
    TaxonIdArg( long l ) {
        super( l );
    }

    @Override
    public Taxon getEntity( TaxonService service ) {
        return checkEntity( service.load( this.getValue() ) );
    }

    @Override
    public String getPropertyName() {
        return "id";
    }
}
