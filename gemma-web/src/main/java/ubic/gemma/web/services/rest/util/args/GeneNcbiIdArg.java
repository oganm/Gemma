package ubic.gemma.web.services.rest.util.args;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import ubic.gemma.core.genome.gene.service.GeneService;
import ubic.gemma.model.genome.Gene;

/**
 * Long argument type for Gene API, referencing the Gene NCBI ID.
 *
 * @author tesarst
 */
@Schema(type = "string", description = "An NCBI gene identifier.",
        externalDocs = @ExternalDocumentation(url = "https://www.ncbi.nlm.nih.gov/gene"))
public class GeneNcbiIdArg extends GeneAnyIdArg<Integer> {

    /**
     * @param l intentionally primitive type, so the value property can never be null.
     */
    GeneNcbiIdArg( int l ) {
        super( l );
    }

    @Override
    public Gene getEntity( GeneService service ) {
        return checkEntity( service.findByNCBIId( this.getValue() ) );
    }

    @Override
    public String getPropertyName() {
        return "ncbiGeneId";
    }

}
