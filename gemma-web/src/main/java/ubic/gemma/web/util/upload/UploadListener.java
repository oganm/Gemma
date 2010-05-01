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
package ubic.gemma.web.util.upload;

import java.util.Queue;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.job.TaskCommand;
import ubic.gemma.job.progress.ProgressData;
import ubic.gemma.job.progress.ProgressJob;
import ubic.gemma.job.progress.ProgressManager;

/**
 * This is created when a multipart request is received (via the CommonsMultipartMonitoredResolver). It starts of a
 * 'progress job',
 * 
 * @author Original : plosson
 * @author pavlidis
 * @version $Id$
 */
public class UploadListener implements OutputStreamListener {

    private static Log log = LogFactory.getLog( UploadListener.class.getName() );

    private long delay = 0;
    private long totalToRead = 0;
    private long totalBytesRead = 0;
    private ProgressJob pJob;
    private String taskId;

    /**
     * @param request
     * @param debugDelay
     */
    public UploadListener( HttpServletRequest request, long debugDelay ) {
        this( request );
        this.delay = debugDelay;
    }

    public UploadListener( HttpServletRequest request ) {
        this.totalToRead = request.getContentLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.upload.OutputStreamListener#start()
     */
    public void start() {
        this.pJob = ProgressManager.createProgressJob( new TaskCommand() );
        pJob.setForwardWhenDone( false );
        log.info( "Upload started ..." );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.upload.OutputStreamListener#bytesRead(int)
     */
    public void bytesRead( int bytesRead ) {

        int oldPercent = 0;
        if ( pJob != null ) {
            Queue<ProgressData> progressData = pJob.getProgressData();
            if ( progressData == null || progressData.size() == 0 ) {
                return; // probably it is finshed.
            }
            oldPercent = progressData.peek().getPercent();
        }

        this.totalBytesRead = totalBytesRead + bytesRead;
        int newPercent = ( int ) ( ( ( double ) totalBytesRead / totalToRead ) * 100.00 );
        if ( newPercent > oldPercent + 5 || newPercent == 100 ) {
            if ( pJob != null ) {
                pJob.updateProgress( newPercent );
            }
            // FIXME the oldPercent is always zero.
            log.debug( newPercent + "% read (" + totalBytesRead + "/" + totalToRead + " bytes) old percent="
                    + oldPercent );
        }

        if ( delay > 0 ) {
            try {
                Thread.sleep( delay );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.upload.OutputStreamListener#error(java.lang.String)
     */
    public void error( String message ) {
        log.error( "There was an error in uploading a file in " + this );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.upload.OutputStreamListener#done()
     */
    public void done() {
        if ( pJob != null ) {
            log.info( "Finished Uploading. Processing File..." );
        }
    }

    public String getTaskId() {
        return taskId;
    }

}
