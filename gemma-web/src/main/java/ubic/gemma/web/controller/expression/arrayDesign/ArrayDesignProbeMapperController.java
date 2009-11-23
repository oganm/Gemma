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
package ubic.gemma.web.controller.expression.arrayDesign;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ubic.gemma.grid.javaspaces.TaskCommand;
import ubic.gemma.grid.javaspaces.TaskResult;
import ubic.gemma.grid.javaspaces.task.expression.arrayDesign.ArrayDesignProbeMapTaskCommand;
import ubic.gemma.grid.javaspaces.task.expression.arrayDesign.ArrayDesignProbeMapperTask;
import ubic.gemma.grid.javaspaces.util.SpacesEnum;
import ubic.gemma.loader.expression.arrayDesign.ArrayDesignProbeMapperService;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.web.controller.BackgroundControllerJob;
import ubic.gemma.web.controller.BaseControllerJob;
import ubic.gemma.web.controller.grid.AbstractSpacesController;

/**
 * A controller to run array design probe mapper either locally or in a space.
 * 
 * @author keshav
 * @version $Id$
 */
@Controller
public class ArrayDesignProbeMapperController extends AbstractSpacesController<ModelAndView> {

    /**
     * Regular (local) job.
     */
    private class ArrayDesignProbeMapperJob extends BaseControllerJob<ModelAndView> {

        /**
         * @param taskId
         * @param commandObj
         */
        public ArrayDesignProbeMapperJob( String taskId, Object commandObj ) {
            super( taskId, commandObj );
        }

        public ModelAndView call() throws Exception {

            ArrayDesignProbeMapTaskCommand pmCommand = ( ( ArrayDesignProbeMapTaskCommand ) command );

            super.initializeProgressJob( pmCommand.getArrayDesign().getShortName() );

            return processJob( pmCommand );
        }

        /*
         * (non-Javadoc)
         * @see ubic.gemma.web.controller.BaseControllerJob#processJob(ubic.gemma.web.controller.BaseCommand)
         */
        @Override
        protected ModelAndView processJob( TaskCommand c ) {
            ArrayDesignProbeMapTaskCommand probeMapperCommand = ( ArrayDesignProbeMapTaskCommand ) c;
            arrayDesignProbeMapperService.processArrayDesign( probeMapperCommand.getArrayDesign() );
            return new ModelAndView( new RedirectView( "/Gemma" ) );

        }
    }

    /**
     * Job that loads in a javaspace.
     * 
     * @author keshav
     * @version $Id$
     */
    private class ArrayDesignProbeMapperSpaceJob extends ArrayDesignProbeMapperJob {

        final ArrayDesignProbeMapperTask taskProxy = ( ArrayDesignProbeMapperTask ) updatedContext.getBean( "proxy" );

        /**
         * @param taskId
         * @param commandObj
         */
        public ArrayDesignProbeMapperSpaceJob( String taskId, Object commandObj ) {
            super( taskId, commandObj );

        }

        protected ArrayDesignProbeMapTaskCommand createCommandObject( ArrayDesignProbeMapTaskCommand c ) {
            return new ArrayDesignProbeMapTaskCommand( taskId, c.isForceAnalysis(), c.getArrayDesign() );
        }

        /*
         * (non-Javadoc)
         * @see
         * ubic.gemma.web.controller.expression.arrayDesign.ArrayDesignProbeMapperController.ArrayDesignProbeMapperJob
         * #processJob(ubic.gemma.web.controller.BaseCommand)
         */
        @Override
        protected ModelAndView processJob( TaskCommand baseCommand ) {
            ArrayDesignProbeMapTaskCommand c = ( ArrayDesignProbeMapTaskCommand ) baseCommand;
            process( c );
            return new ModelAndView( new RedirectView( "/Gemma" ) );
        }

        /**
         * @param command
         * @return
         */
        private TaskResult process( ArrayDesignProbeMapTaskCommand c ) {
            TaskResult result = taskProxy.execute( c );
            return result;
        }

    }

    @Autowired
    private ArrayDesignProbeMapperService arrayDesignProbeMapperService = null;

    @Autowired
    private ArrayDesignService arrayDesignService = null;

    /**
     * AJAX entry point.
     * 
     * @param cmd
     * @return
     * @throws Exception
     */
    public String run( Long id ) throws Exception {
        /* this 'run' method is exported in the spring-beans.xml */

        ArrayDesign ad = arrayDesignService.load( id );
        arrayDesignService.thaw( ad );

        ArrayDesignProbeMapTaskCommand cmd = new ArrayDesignProbeMapTaskCommand();
        cmd.setArrayDesign( ad );

        return super
                .run( cmd, SpacesEnum.DEFAULT_SPACE.getSpaceUrl(), ArrayDesignProbeMapperTask.class.getName(), true );
    }

    public void setArrayDesignProbeMapperService( ArrayDesignProbeMapperService arrayDesignProbeMapperService ) {
        this.arrayDesignProbeMapperService = arrayDesignProbeMapperService;
    }

    public void setArrayDesignService( ArrayDesignService arrayDesignService ) {
        this.arrayDesignService = arrayDesignService;
    }

    /*
     * (non-Javadoc)
     * @see ubic.gemma.web.controller.grid.AbstractSpacesController#getRunner(java.lang.String, java.lang.Object)
     */
    @Override
    protected BackgroundControllerJob<ModelAndView> getRunner( String jobId, Object command ) {
        return new ArrayDesignProbeMapperJob( jobId, command );
    }

    /*
     * (non-Javadoc)
     * @see ubic.gemma.web.controller.grid.AbstractSpacesController#getSpaceRunner(java.lang.String, java.lang.Object)
     */
    @Override
    protected BackgroundControllerJob<ModelAndView> getSpaceRunner( String jobId, Object command ) {
        return new ArrayDesignProbeMapperSpaceJob( jobId, command );

    }

    /*
     * (non-Javadoc)
     * @seeorg.springframework.web.servlet.mvc.AbstractUrlViewController#getViewNameForRequest(javax.servlet.http.
     * HttpServletRequest)
     */
    @Override
    protected String getViewNameForRequest( HttpServletRequest arg0 ) {
        return "arrayDesignProbeMapper";
    }
}
