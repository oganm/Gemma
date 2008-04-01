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

package ubic.gemma.web.services;

import java.util.Collection;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;

/**
 * GCollection of experiments --> map of experiments to its characteristics 
 * don't forget NaN
 * include URI so we know which ontology it came from
 * 
 * @author klc, gavin
 * @version$Id$
 */

public class ExperimentAnnotationEndpoint extends AbstractGemmaEndpoint {

    private static Log log = LogFactory.getLog( Gene2GoTermEndpoint.class );

    private ExpressionExperimentService expressionExperimentService;

   

    /**
     * The local name of the expected request/response.
     */
    public static final String LOCAL_NAME = "experimentAnnotation";

    /**
     * Sets the "business service" to delegate to.
     */
    public void setExpressionExperimentService( ExpressionExperimentService ExpressionExperimentService ) {
        this.expressionExperimentService = ExpressionExperimentService;
    }

   

    /**
     * Reads the given <code>requestElement</code>, and sends a the response back.
     * 
     * @param requestElement the contents of the SOAP message as DOM elements
     * @param document a DOM document to be used for constructing <code>Node</code>s
     * @return the response element
     */
    protected Element invokeInternal( Element requestElement, Document document ) throws Exception {

        setLocalName( LOCAL_NAME );

        Collection<String> eeResult = getArrayValues( requestElement, "ee_ids" );

        // start building the wrapper
        // build xml manually for mapped result rather than use buildWrapper inherited from AbstractGemmeEndpoint
        log.info( "Building " + LOCAL_NAME + " XML response" );
        StopWatch watch = new StopWatch();
        watch.start();

        Element responseWrapper = document.createElementNS( NAMESPACE_URI, LOCAL_NAME );
        Element responseElement = document.createElementNS( NAMESPACE_URI, LOCAL_NAME + RESPONSE );
        responseWrapper.appendChild( responseElement );

        Long eeId = null;
        for ( String eeString : eeResult ) {

            eeId = Long.parseLong( eeString );
            ExpressionExperiment ee = expressionExperimentService.load( eeId );
            
            if ( ee == null ) {
                String msg = "No expression experiment with id, " + eeId + ", can be found.";
                return buildBadResponse( document, msg );
            }
            expressionExperimentService.thawLite( ee );
            Collection<Characteristic> characterCol = ee.getCharacteristics();            
                
           
            for (Characteristic character : characterCol){
                            
                String elementString1 = eeId.toString();
                String elementString2 = character.getValue();
                String elementString4 = character.getEvidenceCode().getValue();
                String elementString3 = character.getCategory();
    
                Element e1 = document.createElement( "ee_id" );
                e1.appendChild( document.createTextNode( elementString1 ) );
                responseElement.appendChild( e1 );
    
                Element e2 = document.createElement( "Category" );
                e2.appendChild( document.createTextNode( elementString3 ) );
                responseElement.appendChild( e2 );
                
                Element e3 = document.createElement( "Terms");
                e3.appendChild( document.createTextNode( elementString2 ) );
                responseElement.appendChild( e3 );
                
                Element e4 = document.createElement( "GOEvidenceCode" );
                e4.appendChild( document.createTextNode( elementString4 ) );
                responseElement.appendChild( e4 );
            }
        }
        watch.stop();
        Long time = watch.getTime();
        log.info( "Finished generating result. Sending response to client." );
        log.info( "XML response for " + LOCAL_NAME + " endpoint built in " + time + "ms." );       
        return responseWrapper;

    }

}
