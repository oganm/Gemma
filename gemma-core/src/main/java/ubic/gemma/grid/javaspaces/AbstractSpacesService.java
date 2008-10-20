/*
 * The Gemma project
 * 
 * Copyright (c) 2007 University of British Columbia
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
package ubic.gemma.grid.javaspaces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import ubic.gemma.util.grid.javaspaces.SpacesEnum;
import ubic.gemma.util.grid.javaspaces.SpacesUtil;
import ubic.gemma.util.progress.TaskRunningService;

/**
 * DOCUMENT ME
 * 
 * @spring.property name="spacesUtil" ref="spacesUtil"
 * @author keshav
 * @version $Id$
 */
public abstract class AbstractSpacesService {
    private Log log = LogFactory.getLog( AbstractSpacesService.class );

    protected SpacesUtil spacesUtil = null;

    protected ApplicationContext updatedContext = null;

    /**
     * @return ApplicationContext
     */
    public ApplicationContext addGemmaSpacesToApplicationContext() {
        assert spacesUtil != null;
        return spacesUtil.addGemmaSpacesToApplicationContext( SpacesEnum.DEFAULT_SPACE.getSpaceUrl() );
    }

    protected void startJob( String spaceUrl, String taskName, boolean runInLocalContext ) {
        run( spaceUrl, taskName, runInLocalContext );
    }

    protected String run( String spaceUrl, String taskName, boolean runInLocalContext ) {

        return run( spaceUrl, taskName, runInLocalContext, null );
    }

    protected String run( String spaceUrl, String taskName, boolean runInLocalContext, Object command ) {

        String taskId = null;

        updatedContext = addGemmaSpacesToApplicationContext();

        if ( updatedContext.containsBean( "gigaspacesTemplate" ) && spacesUtil.canServiceTask( taskName, spaceUrl ) ) {
            log.info( "Running task " + taskName + " remotely." );

            taskId = SpacesHelper.getTaskIdFromTask( updatedContext, taskName );
            runRemotely( taskId, command );
        } else if ( !updatedContext.containsBean( "gigaspacesTemplate" ) && !runInLocalContext ) {
            throw new RuntimeException(
                    "This task must be run on the compute server, but the space is not running. Please try again later" );
        }

        else {
            log.info( "Running task " + taskName + " locally." );
            taskId = TaskRunningService.generateTaskId();
            runLocally( taskId, command );
        }

        return taskId;
    }

    /**
     * @param taskId
     * @param command could be null
     */
    public abstract void runLocally( String taskId, Object command );

    /**
     * @param taskId
     * @param command could be null
     */
    public abstract void runRemotely( String taskId, Object command );

    /**
     * @param spacesUtil
     */
    public void setSpacesUtil( SpacesUtil spacesUtil ) {
        this.spacesUtil = spacesUtil;
    }

    /**
     * @param spacesUtil
     */
    protected void injectSpacesUtil( SpacesUtil s ) {
        this.spacesUtil = s;
    }

}
