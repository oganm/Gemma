/*
 * The Gemma project
 * 
 * Copyright (c) 2011 University of British Columbia
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
package ubic.gemma.web.services.rest;

import com.sun.jersey.api.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.persistence.service.expression.bioAssay.BioAssayDao;
import ubic.gemma.persistence.service.expression.experiment.ExperimentalFactorService;
import ubic.gemma.web.services.rest.util.Responder;
import ubic.gemma.web.services.rest.util.ResponseDataObject;
import ubic.gemma.web.services.rest.util.WebService;
import ubic.gemma.web.services.rest.util.WellComposedErrorBody;
import ubic.gemma.web.services.rest.util.args.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * RESTful interface for datasets.
 *
 * @author tesarst
 */
@Service
@Path("/datasets")
public class DatasetsWebService extends WebService {

    private ExpressionExperimentService expressionExperimentService;

    private BioAssayDao bioAssayDao;

    /* ********************************
     * Constructors
     * ********************************/

    /**
     * Required by spring
     */
    public DatasetsWebService() {
    }

    /**
     * Constructor for service autowiring
     */
    @Autowired
    public DatasetsWebService( ExpressionExperimentService expressionExperimentService, BioAssayDao bioAssayDao ) {
        this.expressionExperimentService = expressionExperimentService;
        this.bioAssayDao = bioAssayDao;
    }

    /* ********************************
     * API GET Methods
     * ********************************/

    /**
     * Lists all datasets available in gemma.
     *
     * @param accession optional parameter, filtering the results by accession - either provide the accession gsm id, or the gemma ID of
     *                  appropriate database entry.
     * @param offset    optional parameter (defaults to 0) skips the specified amount of the datasets when retrieving them from the database.
     * @param limit     optional parameter (defaults to 100) limits the result to specified amount of datasets. Use 0 for no limit.
     * @param sort      optional parameter (defaults to +id) sets the ordering property and direction. Format is [+,-][property name].
     *                  E.g. -accession will convert to descending ordering by the Accession property. Note that this will not necessarily
     *                  sort the objects in the response, but rather tells the SQL query how to order the table before cropping it as
     *                  specified in the offset and limit.
     * @return all datasets in the database, skipping the first [{@code offset}] of dataset, and limiting the amount in the result to
     * the value of the {@code limit} parameter. If the {@code accessionGsmId} parameter is non-null, will limit the result to datasets
     * with specified accession. Note that if the accession GSM id is not valid or no datasets with it are found, a 404 response will be
     * supplied instead.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseDataObject all( // Params:
            @QueryParam("accession") String accession, // Optional, default null
            @QueryParam("offset") @DefaultValue("0") IntArg offset,
            @QueryParam("limit") @DefaultValue("100") IntArg limit,
            @QueryParam("sort") @DefaultValue("+id") SortArg sort, @Context final HttpServletResponse sr
            // The servlet response, needed for response code setting.
    ) {
        return Responder.autoCode( expressionExperimentService
                .loadAllFilter( offset.getValue(), limit.getValue(), sort.getField(), sort.isAsc(), accession ), sr );
    }

    /**
     * Retrieves single dataset based on the given identifier.
     *
     * @param datasetArg can either be the ExpressionExperiment ID or, its short name (e.g. GSE1234). Retrieval by ID
     *                   is most efficient.
     */
    @GET
    @Path("/{datasetArg: [a-zA-Z0-9\\.]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseDataObject dataset( // Params:
            @PathParam("datasetArg") DatasetArg<Object> datasetArg, // Optional, default null
            @Context final HttpServletResponse sr // The servlet response, needed for response code setting.
    ) {
        //FIXME currently not filtering out troubled
        Object response = datasetArg.getValueObject( expressionExperimentService );
        if(response == null){
            WellComposedErrorBody error = new WellComposedErrorBody( Response.Status.NOT_FOUND, "Dataset with the given identifier does not exist" );
            WellComposedErrorBody.addExceptionFields( error, new IllegalArgumentException( datasetArg.getNullCause() ) );
            response = error;
        }
        return Responder.autoCode( response, sr );
    }


    /* ********************************
     * OLD METHODS
     * TODO REMOVE
     * ********************************/

    @GET
    @Path("/findByShortName/{shortName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, String>> getAnnotations( @PathParam("shortName") String shortName ) {

        ExpressionExperiment experiment = this.expressionExperimentService.findByShortName( shortName );
        if ( experiment == null )
            throw new NotFoundException( "Dataset not found." );
        Collection<BioAssay> bioAssays = experiment.getBioAssays();
        Collection<Characteristic> chars = new ArrayList<>();

        return prepareEEAnnotationsUnstructured( bioAssays, chars );
    }

    @GET
    @Path("/findByAccession/{gsmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, String>> getAnnotationsByGSM( @PathParam("gsmId") String gsmId ) {

        Collection<BioAssay> foundBioAssays = this.bioAssayDao.findByAccession( gsmId );

        if ( foundBioAssays.isEmpty() )
            throw new NotFoundException( "Sample not found." );

        Collection<Characteristic> characteristics = new HashSet<>();

        return prepareEEAnnotationsUnstructured( foundBioAssays, characteristics );
    }

    @GET
    @Path("/findByAccession/includeConstantFactorsStructured/{gsmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, Map<String, String>>> getAnnotationsByGSMIncludeTagsStructured(
            @PathParam("gsmId") String gsmId ) {

        Collection<BioAssay> foundBioAssays = this.bioAssayDao.findByAccession( gsmId );

        if ( foundBioAssays.isEmpty() )
            throw new NotFoundException( "Sample not found." );

        Collection<Characteristic> characteristics = new HashSet<>();
        if ( foundBioAssays.size() == 1 ) {
            ExpressionExperiment ee = this.expressionExperimentService
                    .findByBioAssay( foundBioAssays.iterator().next() );
            if ( ee != null ) {
                characteristics.addAll( ee.getCharacteristics() );
            }
        }

        return prepareEEAnnotationsStructured( foundBioAssays, characteristics );
    }

    @GET
    @Path("/findByAccession/includeConstantFactors/{gsmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, String>> getAnnotationsByGSMIncludeTagsUnstructured(
            @PathParam("gsmId") String gsmId ) {

        Collection<BioAssay> foundBioAssays = this.bioAssayDao.findByAccession( gsmId );

        if ( foundBioAssays.isEmpty() )
            throw new NotFoundException( "Sample not found." );

        Collection<Characteristic> characteristics = new HashSet<>();
        if ( foundBioAssays.size() == 1 ) {
            ExpressionExperiment ee = this.expressionExperimentService
                    .findByBioAssay( foundBioAssays.iterator().next() );
            if ( ee != null ) {
                characteristics.addAll( ee.getCharacteristics() );
            }
        }

        return prepareEEAnnotationsUnstructured( foundBioAssays, characteristics );
    }

    @GET
    @Path("/findByShortName/includeConstantFactorsStructured/{shortName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, Map<String, String>>> getAnnotationsIncludeTagsStructured(
            @PathParam("shortName") String shortName ) {

        ExpressionExperiment experiment = this.expressionExperimentService.findByShortName( shortName );
        if ( experiment == null )
            throw new NotFoundException( "Dataset not found." );
        Collection<BioAssay> bioAssays = experiment.getBioAssays();
        Collection<Characteristic> chars = experiment.getCharacteristics();

        return prepareEEAnnotationsStructured( bioAssays, chars );
    }

    @GET
    @Path("/findByShortName/includeConstantFactors/{shortName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, String>> getAnnotationsIncludeTagsUnstructured(
            @PathParam("shortName") String shortName ) {

        ExpressionExperiment experiment = this.expressionExperimentService.findByShortName( shortName );
        if ( experiment == null )
            throw new NotFoundException( "Dataset not found." );
        Collection<BioAssay> bioAssays = experiment.getBioAssays();
        Collection<Characteristic> chars = experiment.getCharacteristics();

        return prepareEEAnnotationsUnstructured( bioAssays, chars );
    }

    private String[] getTagString( Characteristic characteristic ) {

        String[] arr = { "", "" };
        if ( characteristic == null )
            return arr;
        if ( ( characteristic.getCategory() == null || characteristic.getCategory().isEmpty() ) && (
                characteristic.getValue() == null || characteristic.getValue().isEmpty() ) ) {
            return arr;
        } else if ( characteristic.getCategory() == null || characteristic.getCategory().isEmpty() ) {
            arr[0] = characteristic.getValue();
            arr[1] = characteristic.getValue();
        } else if ( characteristic.getValue() == null || characteristic.getValue().isEmpty() ) {
            arr[0] = characteristic.getCategory();
            arr[1] = "no value";
        } else {
            arr[0] = characteristic.getCategory();
            arr[1] = characteristic.getValue();
        }
        return arr;
    }

    private Map<String, Map<String, Map<String, String>>> prepareEEAnnotationsStructured(
            Collection<BioAssay> bioAssays, Collection<Characteristic> characteristics ) {
        Map<String, Map<String, Map<String, String>>> result = new HashMap<>();

        if ( bioAssays.isEmpty() )
            throw new NotFoundException( "BioAssays not found" );
        for ( BioAssay bioAssay : bioAssays ) {

            String accession = bioAssay.getAccession().getAccession();

            Map<String, String> annotations = new HashMap<>();
            Map<String, String> tagAnnotations = new HashMap<>();
            Map<String, Map<String, String>> annotationsCategories = new HashMap<>();

            BioMaterial bioMaterial = bioAssay.getSampleUsed();

            for ( FactorValue factorValue : bioMaterial.getFactorValues() ) {
                if ( !factorValue.getExperimentalFactor().getName()
                        .equals( ExperimentalFactorService.BATCH_FACTOR_NAME ) ) {
                    annotations
                            .put( factorValue.getExperimentalFactor().getName(), factorValue.getDescriptiveString() );
                }
            }

            for ( Characteristic characteristic : characteristics ) {

                String[] tagStringArr = getTagString( characteristic );
                if ( !tagStringArr[0].isEmpty() && !tagStringArr[1].isEmpty() ) {
                    tagAnnotations.put( tagStringArr[0], tagStringArr[1] );
                }
            }

            annotationsCategories.put( "ExperimentFactors", annotations );
            annotationsCategories.put( "ExperimentTags", tagAnnotations );
            result.put( accession, annotationsCategories );
        }

        return result;
    }

    /**
     * Don't introduce structure to separate experimental factors from experiment tags, instead add a prefix to tag
     * categories
     */
    private Map<String, Map<String, String>> prepareEEAnnotationsUnstructured( Collection<BioAssay> bioAssays,
            Collection<Characteristic> characteristics ) {
        Map<String, Map<String, String>> result = new HashMap<>();

        if ( bioAssays.isEmpty() )
            throw new NotFoundException( "BioAssays not found" );
        for ( BioAssay bioAssay : bioAssays ) {

            String accession = bioAssay.getAccession().getAccession();
            Map<String, String> annotations = new HashMap<>();
            BioMaterial bioMaterial = bioAssay.getSampleUsed();

            for ( FactorValue factorValue : bioMaterial.getFactorValues() ) {
                if ( !factorValue.getExperimentalFactor().getName()
                        .equals( ExperimentalFactorService.BATCH_FACTOR_NAME ) ) {
                    annotations
                            .put( factorValue.getExperimentalFactor().getName(), factorValue.getDescriptiveString() );
                }
            }

            for ( Characteristic characteristic : characteristics ) {

                String[] tagStringArr = getTagString( characteristic );
                if ( !tagStringArr[0].isEmpty() && !tagStringArr[1].isEmpty() ) {
                    annotations.put( "constant_" + tagStringArr[0], tagStringArr[1] );
                }
            }
            result.put( accession, annotations );
        }

        return result;
    }

}
