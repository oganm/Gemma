/*
 * The Gemma project
 * 
 * Copyright (c) 2006 University of British Columbia
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
package ubic.gemma.web.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import ubic.gemma.model.common.auditAndSecurity.User;
import ubic.gemma.model.common.auditAndSecurity.UserService;
import ubic.gemma.Constants;
import ubic.gemma.util.MailEngine;

/**
 * Implementation of <strong>SimpleFormController</strong> that contains convenience methods for subclasses. For
 * example, getting the current user and saving messages/errors. This class is intended to be a base class for all Form
 * controllers.
 * <p>
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @author pavlidis
 * @version $Id$
 */
public abstract class BaseFormController extends SimpleFormController {
    protected final transient Log log = LogFactory.getLog( getClass() );
    protected MailEngine mailEngine = null;
    protected SimpleMailMessage message = null;
    protected String templateName = null;
    protected UserService userService = null;

    /**
     * Convenience method to get the Configuration HashMap from the servlet context.
     * 
     * @return the user's populated form from the session
     */
    public Map getConfiguration() {
        Map config = ( HashMap ) getServletContext().getAttribute( Constants.CONFIG );

        // so unit tests don't puke when nothing's been set
        if ( config == null ) {
            return new HashMap();
        }
        return config;
    }

    /**
     * Convenience method for getting a i18n key's value. Calling getMessageSourceAccessor() is used because the
     * RequestContext variable is not set in unit tests b/c there's no DispatchServlet Request.
     * 
     * @param msgKey
     * @param locale the current locale
     * @return
     */
    public String getText( String msgKey, Locale locale ) {
        return getMessageSourceAccessor().getMessage( msgKey, locale );
    }

    /**
     * Convenience method for getting a i18n key's value with arguments.
     * 
     * @param msgKey
     * @param args
     * @param locale the current locale
     * @return
     */
    public String getText( String msgKey, Object[] args, Locale locale ) {
        return getMessageSourceAccessor().getMessage( msgKey, args, locale );
    }

    /**
     * Convenient method for getting a i18n key's value with a single string argument.
     * 
     * @param msgKey
     * @param arg
     * @param locale the current locale
     * @return
     */
    public String getText( String msgKey, String arg, Locale locale ) {
        return getText( msgKey, new Object[] { arg }, locale );
    }

    public UserService getUserService() {
        return this.userService;
    }

    /**
     * Default behavior for FormControllers - redirect to the successView when the cancel button has been pressed.
     */
    public ModelAndView processFormSubmission( HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors ) throws Exception {
        if ( request.getParameter( "cancel" ) != null ) {
            return new ModelAndView( getSuccessView() );
        }

        return super.processFormSubmission( request, response, command, errors );
    }

    @SuppressWarnings("unchecked")
    public void saveMessage( HttpServletRequest request, String msg ) {
        List<String> messages = ( List<String> ) request.getSession().getAttribute( "messages" );

        if ( messages == null ) {
            messages = new ArrayList<String>();
        }

        messages.add( msg );
        request.getSession().setAttribute( "messages", messages );
    }

    public void setMailEngine( MailEngine mailEngine ) {
        this.mailEngine = mailEngine;
    }

    public void setMessage( SimpleMailMessage message ) {
        this.message = message;
    }

    public void setTemplateName( String templateName ) {
        this.templateName = templateName;
    }

    public void setUserService( UserService userService ) {
        this.userService = userService;
    }

    /**
     * Convenience method to get the user object from the session
     * 
     * @param request the current request
     * @return the user's populated object from the session
     */
    protected User getUser( HttpServletRequest request ) {
        return ( User ) request.getSession().getAttribute( Constants.USER_KEY );
    }

    /**
     * Set up a custom property editor for converting form inputs to real objects
     */
    @SuppressWarnings("unused")
    protected void initBinder( HttpServletRequest request, ServletRequestDataBinder binder ) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        binder.registerCustomEditor( Integer.class, null, new CustomNumberEditor( Integer.class, nf, true ) );
        binder.registerCustomEditor( Long.class, null, new CustomNumberEditor( Long.class, nf, true ) );
        binder.registerCustomEditor( byte[].class, new ByteArrayMultipartFileEditor() );
    }

    /**
     * Convenience message to send messages to users, includes app URL as footer.
     * 
     * @param user
     * @param msg
     * @param url
     */
    protected void sendEmail( User user, String msg, String url ) {
        log.debug( "sending e-mail to user [" + user.getEmail() + "]..." );
        message.setTo( user.getFullName() + "<" + user.getEmail() + ">" );

        Map<String, Object> model = new HashMap<String, Object>();
        model.put( "user", user );
        model.put( "message", msg );
        model.put( "applicationURL", url );
        mailEngine.sendMessage( message, templateName, model );
    }
}