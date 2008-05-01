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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ubic.gemma.analysis.expression.coexpression.CoexpressionMetaValueObject;
import ubic.gemma.analysis.expression.coexpression.CoexpressionValueObjectExt;
import ubic.gemma.analysis.expression.coexpression.GeneCoexpressionService;
import ubic.gemma.model.analysis.expression.coexpression.GeneCoexpressionAnalysis;
import ubic.gemma.model.analysis.expression.coexpression.GeneCoexpressionAnalysisService;
import ubic.gemma.model.analysis.expression.coexpression.GeneCoexpressionVirtualAnalysis;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.TaxonService;
import ubic.gemma.model.genome.gene.GeneService;

/**
 *Allows access to the gene co-expression analysis.  Given a gene, a taxon and a stringency this service will return all the related co-expression data.  
 *The stringency is the miniumum number of times we found a particular relationship. 
 *  Returns the coexpressed Gene, the support ( the number of times that coexpression was found )
 * and the experiments that co-expression was found in (since there should be more than 1 experiment this list will be returned as a space delimted string of EE Ids.
 * 
 * @author gavin, klc
 * @version$Id$
 */

public class GeneCoexpressionEndpoint extends AbstractGemmaEndpoint {

    private static Log log = LogFactory.getLog( GeneCoexpressionEndpoint.class );

    private TaxonService taxonService;

    private GeneService geneService;

    private GeneCoexpressionService geneCoexpressionService;

    private GeneCoexpressionAnalysisService geneCoexpressionAnalysisService;

    /**
     * The local name of the expected request/response.
     */
    public static final String LOCAL_NAME = "geneCoexpression";

    // The maximum number of coexpression results to return; a value of zero will return all possible results (ie. max is infinity)
    public static final int MAX_RESULTS = 0;

    /**
     * Sets the "business service" to delegate to.
     */
    public void setTaxonService( TaxonService taxonService ) {
        this.taxonService = taxonService;
    }

    public void setGeneService( GeneService geneS ) {
        this.geneService = geneS;
    }

    public void setgeneCoexpressionAnalysisService( GeneCoexpressionAnalysisService geneCoexpressionAnalysisService ) {
        this.geneCoexpressionAnalysisService = geneCoexpressionAnalysisService;
    }

    public void setgeneCoexpressionService( GeneCoexpressionService geneCoexpressionService ) {
        this.geneCoexpressionService = geneCoexpressionService;
    }

    /**
     * Reads the given <code>requestElement</code>, and sends a the response back.
     * 
     * @param requestElement the contents of the SOAP message as DOM elements
     * @param document a DOM document to be used for constructing <code>Node</code>s
     * @return the response element
     */
    @SuppressWarnings("unchecked")
    protected Element invokeInternal( Element requestElement, Document document ) throws Exception {
        StopWatch watch = new StopWatch();
        watch.start();
        
        setLocalName( LOCAL_NAME );

        Collection<String> geneResults = getNodeValues( requestElement, "gene_id" );
        String geneId = "";
        for ( String id : geneResults ) {
            geneId = id;
        }

        Collection<String> taxonResults = getNodeValues( requestElement, "taxon_id" );
        String taxonId = "";
        for ( String id : taxonResults ) {
            taxonId = id;
        }

        Collection<String> stringencyResults = getNodeValues( requestElement, "stringency" );
        String string = "";
        for ( String id : stringencyResults ) {
            string = id;
        }

        log.info( "XML input read: gene id, "+geneId+" & taxon id, "+taxonId+" & stringency, "+string );
        
        Taxon taxon = taxonService.load( Long.parseLong( taxonId ) );
        if ( taxon == null ) {
            String msg = "No taxon with id, " + taxon + ", can be found.";
            return buildBadResponse( document, msg );
        }
        
        Gene gene = geneService.load( Long.parseLong( geneId ) );
        if ( gene == null ) {
            String msg = "No gene with id, " + geneId + ", can be found.";
            return buildBadResponse( document, msg );
        }
        Collection<Gene> genes = new ArrayList<Gene>();
        genes.add( gene );
        geneService.thawLite( genes );

        int stringency = Integer.parseInt( string );

        Collection<GeneCoexpressionAnalysis> analysisCol = geneCoexpressionAnalysisService.findByTaxon( taxon );
        GeneCoexpressionAnalysis analysis2Use = null;

        // use the 1st canned analysis that isn't virtual  for the given taxon (should be the all"Taxon" analysis)
        for ( GeneCoexpressionAnalysis analysis : analysisCol ) {
            if (analysis instanceof GeneCoexpressionVirtualAnalysis)                
                continue;
            else{
                    analysis2Use = analysis;
                    break;
            }
        }
        
        // get Gene2GeneCoexpressio objects canned analysis
        CoexpressionMetaValueObject coexpressedGenes = geneCoexpressionService.getCannedAnalysisResults( analysis2Use
                .getId(), genes, stringency, MAX_RESULTS, false );

        if ( coexpressedGenes == null || coexpressedGenes.getKnownGeneResults().isEmpty() ) {
            String msg = "No coexpressed genes can be found.";
            return buildBadResponse( document, msg );
        }

        final String GENE_NAME = "gene";
        final String SUPPORT_NAME = "support";
        final String EEID_NAME = "eeIdList";

        Element responseWrapper = document.createElementNS( NAMESPACE_URI, LOCAL_NAME );
        Element responseElement = document.createElementNS( NAMESPACE_URI, LOCAL_NAME + RESPONSE );
        responseWrapper.appendChild( responseElement );

        for ( CoexpressionValueObjectExt cvo : coexpressedGenes.getKnownGeneResults() ) {

            Element e1 = document.createElement( GENE_NAME );
            e1.appendChild( document.createTextNode( cvo.getFoundGene().getOfficialSymbol() ) );
            responseElement.appendChild( e1 );

            Element e2 = document.createElement( SUPPORT_NAME );
            e2.appendChild( document.createTextNode( cvo.getSupportKey().toString() ) );
            responseElement.appendChild( e2 );

            Element e3 = document.createElement( EEID_NAME );
            e3.appendChild( document.createTextNode( cvo.getSupportingExperiments().toString() ) );
            responseElement.appendChild( e3 );

        }
        watch.stop();
        Long time = watch.getTime();
        
        log.info( "XML response for coexpression canned result built in " + time + "ms." );
        return responseWrapper;

    }

}
