package edu.columbia.gemma.web.controller.entrez.pubmed;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.InternalResourceView;

import edu.columbia.gemma.common.description.BibliographicReference;
import edu.columbia.gemma.common.description.BibliographicReferenceService;
import edu.columbia.gemma.loader.entrez.pubmed.PubMedXMLFetcher;

/**
 * Allows user to view the results of a pubMed search, with the option of submitting the results.
 * <hr>
 * <p>
 * Copyright (c) 2004 - 2005 Columbia University
 * 
 * @author keshav
 * @version $Id$
 * @spring.bean id="pubMedXmlController"
 * @spring.property name="sessionForm" value="true"
 * @spring.property name="formView" value="pubMedForm"
 * @spring.property name="successView" value="pubMedSuccess"
 * @spring.property name = "pubMedXmlFetcher" ref="pubMedXmlFetcher"
 * @spring.property name = "bibliographicReferenceService" ref="bibliographicReferenceService"
 */
public class PubMedXmlController extends SimpleFormController {
    private String alreadyExists;
    private boolean alreadyViewed = false;
    private BibliographicReferenceService bibliographicReferenceService;
    private String bibRef;
    private Configuration conf;
    private String pubMedId = null;
    private PubMedXMLFetcher pubMedXmlFetcher;
    private String requestPubMedId;
    private String sessionViewOne = null;

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog( getClass() );

    /**
     * @throws ConfigurationException
     */
    public PubMedXmlController() throws ConfigurationException {
        conf = new PropertiesConfiguration( "entrez.properties" );
        alreadyExists = conf.getString( "entrez.pubmed.alreadyExists" );
        bibRef = conf.getString( "entrez.pubmed.bibRef" );
        requestPubMedId = conf.getString( "entrez.pubmed.pubMedId" );
        sessionViewOne = conf.getString( "entrez.pubmed.sessionViewOne" );
    }

    /**
     * @return Returns the bibliographicReferenceService.
     */
    public BibliographicReferenceService getBibliographicReferenceService() {
        return bibliographicReferenceService;
    }

    /**
     * @return Returns the pubMedXmlFetcher.
     */
    public PubMedXMLFetcher getPubMedXmlFetcher() {
        return pubMedXmlFetcher;
    }

    /**
     * Useful for debugging, specifically with Tomcat security issues.
     * 
     * @param request
     * @param response
     */

    // TODO put in an mvcUtils class if used elsewhere. I found this helpful when working with Spring's MVC.
    public void logHttp( HttpServletRequest request, HttpServletResponse response ) {
        log.info( "Context Path: " + request.getContextPath() );
        log.info( "Requested Uri: " + request.getRequestURI() );
        log.info( "Authentication Type: " + request.getAuthType() );
    }

    /**
     * Obtains filename to be read from the form.
     * 
     * @param command
     * @return ModelAndView
     * @throws Exception
     */

    public ModelAndView onSubmit( HttpServletRequest request, HttpServletResponse response, Object command,
            BindException errors ) throws Exception {
        String view = null;

        logHttp( request, response );

        pubMedIdState( request, response );

        try {
            pubMedId = pubMedId.trim();
            int pubMedIdNumber = Integer.parseInt( pubMedId );
            BibliographicReference br = pubMedXmlFetcher.retrieveByHTTP( pubMedIdNumber );
            Map myModel = new HashMap();
            myModel.put( "bibRef", br );
            request.setAttribute( "model", myModel );
            view = resolveView( request, response, br, myModel );
        } catch ( NumberFormatException e ) {
            throw new NumberFormatException(); // FIXME - user gets an error page because they didn't input a valid integer.
        } catch ( NullPointerException e ) {
            alreadyViewed = false;
            pubMedId = null;
            throw new NullPointerException();
        }

        return new ModelAndView( view );

    }

    /**
     * @param bibliographicReferenceService The bibliographicReferenceService to set.
     */
    public void setBibliographicReferenceService( BibliographicReferenceService bibliographicReferenceService ) {
        this.bibliographicReferenceService = bibliographicReferenceService;
    }

    /**
     * @param pubMedXmlFetcher The pubMedXmlFetcher to set.
     */
    public void setPubMedXmlFetcher( PubMedXMLFetcher pubMedXmlFetcher ) {
        this.pubMedXmlFetcher = pubMedXmlFetcher;
    }

    /**
     * @param request
     * @param response
     */
    private void pubMedIdState( HttpServletRequest request, HttpServletResponse response ) {
        if ( pubMedId == null )
            try {
                pubMedId = RequestUtils.getStringParameter( request, requestPubMedId, null );
            } catch ( NumberFormatException e ) {
                alreadyViewed = false;
                pubMedId = null;
                response.reset();
                throw new NumberFormatException();
            }
        else {
            request.setAttribute( requestPubMedId, pubMedId );
        }

    }

    /**
     * Check to see if the InternalResourceView has already been viewed. Sets the return view.
     * 
     * @param request
     * @param response
     * @param myModel
     * @throws Exception
     */

    // TODO there must be a better (Springish) way to do this. This uses global variables.
    private String resolveView( HttpServletRequest request, HttpServletResponse response, Object object, Map model )
            throws Exception {

        String view = sessionViewOne;

        if ( !alreadyViewed ) {
            alreadyViewed = true;
            // TODO cannot make use of view Resolver. Can I define this view declaritively
            // in the context and the access the viewResolver?
            View v = new InternalResourceView( "/WEB-INF/pages/pubMedSuccess.jsp" );
            v.render( model, request, response );
        } else {
            alreadyViewed = false;
            pubMedId = null;
            if ( getBibliographicReferenceService().alreadyExists( ( BibliographicReference ) object ) )
                view = alreadyExists;
            else {
                getBibliographicReferenceService().saveBibliographicReference( ( BibliographicReference ) object );
                view = bibRef;
            }
        }
        return view;
    }

    /**
     * This is needed or you will have to specify a commandClass in the DispatcherServlet's context
     * 
     * @param request
     * @return Object
     * @throws Exception
     */
    protected Object formBackingObject( HttpServletRequest request ) throws Exception {
        return request;
    }
}