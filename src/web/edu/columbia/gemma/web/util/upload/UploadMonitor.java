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
 * Credit:
 *   If you're nice, you'll leave this bit:
 *
 *   Class by Pierre-Alexandre Losson -- http://www.telio.be/blog
 *   email : plosson@users.sourceforge.net
 */
package edu.columbia.gemma.web.util.upload;

import uk.ltd.getahead.dwr.ExecutionContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Original : plosson on 05-janv.-2006 10:46:33 - Last modified by Author: plosson $ on $Date: 2006/01/05
 *         10:09:38
 * @author pavlidis
 * @version $Id$
 */
public class UploadMonitor {

    private static Log log = LogFactory.getLog( UploadMonitor.class.getName() );

    /**
     * @return
     */
    public UploadInfo getUploadInfo() {
        HttpServletRequest req = ExecutionContext.get().getHttpServletRequest();
        assert req != null : "Request was null";
        if ( log.isDebugEnabled() ) {
            log.debug( "Getting info: " + req.getSession().getAttribute( "uploadInfo" ) );
        }
        if ( req.getSession().getAttribute( "uploadInfo" ) != null ) {
            return ( UploadInfo ) req.getSession().getAttribute( "uploadInfo" );
        }
        return new UploadInfo();
    }
}
