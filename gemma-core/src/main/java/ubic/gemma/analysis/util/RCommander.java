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
package ubic.gemma.analysis.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.RCommand;

/**
 * Encapsulates a connection to the RServer.
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class RCommander {

    private static final int TIMEOUT_MILLI_SECONDS = 1000;

    protected static Log log = LogFactory.getLog( RCommander.class.getName() );

    protected RCommand rc;

    public RCommander() {
        this.init();
    }

    /**
     * @param rc2
     */
    public RCommander( RCommand connection ) {
        if ( connection != null && connection.isConnected() ) {
            this.rc = connection;
        } else {
            throw new IllegalArgumentException( "connection was invalid" );
        }
    }

    protected void init() {
        rc = RCommand.newInstance( TIMEOUT_MILLI_SECONDS );
        if ( rc == null ) {
            throw new RuntimeException( "Error during getting RServer instance" );
        }
    }

    /**
     * Users should call this method when they are ready to release this object. Otherwise memory leaks and other
     * badness can occur.
     */
    public void cleanup() {
        if ( rc == null ) {
            log.warn( "Cleanup called, but no connection" );
            return;
        }
        rc.voidEval( " rm(list=ls())" ); // attempt to release all memory used by this connection.
        log.debug( "Disconnecting from RServer..." );
        rc.disconnect();
        log.debug( "...disconnected" );
    }

    protected void finalize() throws Throwable {
        super.finalize();
        cleanup();
    }

    public RCommand getRCommandObject() {
        return rc;
    }

}
