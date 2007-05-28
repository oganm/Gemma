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

import java.util.concurrent.FutureTask;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ubic.gemma.web.util.MessageUtil;

/**
 * Extends this when the controller needs to run a long task (show a progress bar). To use it, implement getRunner and
 * call startJob in your onSubmit method.
 * 
 * @author pavlidis
 * @version $Id$
 * @spring.property name="taskRunningService" ref="taskRunningService"
 */
public abstract class BackgroundProcessingFormController extends BaseFormController {

    /**
     * Use this to access the task id in the request.
     */
    public final static String JOB_ATTRIBUTE = "taskId";

    protected TaskRunningService taskRunningService;

    /**
     * @param taskRunningService the taskRunningService to set
     */
    public void setTaskRunningService( TaskRunningService taskRunningService ) {
        this.taskRunningService = taskRunningService;
    }

    /**
     * @param command
     * @param request
     * @returns a model and view
     */
    protected synchronized ModelAndView startJob( Object command, HttpServletRequest request ) {
        /*
         * all new threads need this to acccess protected resources (like services)
         */
        SecurityContext context = SecurityContextHolder.getContext();

        String taskId = TaskRunningService.generateTaskId();

        BackgroundControllerJob<ModelAndView> job = getRunner( taskId, context, request, command, this.getMessageUtil() );

        assert taskId != null;
        request.getSession().setAttribute( JOB_ATTRIBUTE, taskId );

        taskRunningService.submitTask( taskId, new FutureTask<ModelAndView>( job ) );

        ModelAndView mnv = new ModelAndView( new RedirectView( "/Gemma/processProgress.html?taskid=" + taskId ) );
        mnv.addObject( "taskId", taskId );
        return mnv;
    }

    /**
     * You have to implement this in your subclass.
     * 
     * @param securityContext
     * @param command from form
     * @return
     */
    protected abstract BackgroundControllerJob<ModelAndView> getRunner( String jobId, SecurityContext securityContext,
            HttpServletRequest request, Object command, MessageUtil messenger );

}
