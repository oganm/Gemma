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
package ubic.gemma.web.controller.analysis.preprocess;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import ubic.gemma.analysis.preprocess.TwoChannelMissingValues;
import ubic.gemma.grid.javaspaces.TaskCommand;
import ubic.gemma.grid.javaspaces.TaskResult;
import ubic.gemma.grid.javaspaces.task.analysis.preprocess.TwoChannelMissingValueTask;
import ubic.gemma.grid.javaspaces.task.analysis.preprocess.TwoChannelMissingValueTaskCommand;
import ubic.gemma.grid.javaspaces.util.SpacesEnum;
import ubic.gemma.model.expression.bioAssayData.RawExpressionDataVector;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.web.controller.BackgroundControllerJob;
import ubic.gemma.web.controller.BaseControllerJob;
import ubic.gemma.web.controller.grid.AbstractSpacesController;

/**
 * Run misssing value computation via web request.
 * 
 * @author paul
 * @version $Id$
 */
@Controller
public class TwoChannelMissingValueController extends AbstractSpacesController<Boolean> {

    private class TwoChannelMissingValueJob extends BaseControllerJob<Boolean> {

        public TwoChannelMissingValueJob( String taskId, Object commandObj ) {
            super( taskId, commandObj );
        }

        public Boolean call() throws Exception {

            TwoChannelMissingValueTaskCommand tc = ( ( TwoChannelMissingValueTaskCommand ) command );

            super.initializeProgressJob( tc.getExpressionExperiment().getShortName() );

            return processJob( tc );
        }

        @Override
        protected Boolean processJob( TaskCommand c ) {
            TwoChannelMissingValueTaskCommand tc = ( ( TwoChannelMissingValueTaskCommand ) c );

            try {
                Collection<RawExpressionDataVector> results = twoChannelMissingValues.computeMissingValues( tc
                        .getExpressionExperiment(), tc.getS2n(), tc.getExtraMissingValueIndicators() );
                return results.size() > 0;
            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }

        }

    }

    private class TwoChannelMissingValueSpaceJob extends TwoChannelMissingValueJob {

        final TwoChannelMissingValueTask taskProxy = ( TwoChannelMissingValueTask ) updatedContext.getBean( "proxy" );

        public TwoChannelMissingValueSpaceJob( String taskId, Object commandObj ) {
            super( taskId, commandObj );
        }

        /**
         * @param command
         * @return
         */
        protected TwoChannelMissingValueTaskCommand createCommandObject( TwoChannelMissingValueTaskCommand c ) {
            return new TwoChannelMissingValueTaskCommand( taskId, c.getExpressionExperiment() );
        }

        /*
         * (non-Javadoc)
         * @seeubic.gemma.web.controller.analysis.preprocess.ProcessedExpressionDataVectorCreateController.
         * ProcessedExpressionDataVectorCreateJob#processJob(ubic.gemma.web.controller.BaseCommand)
         */
        @Override
        protected Boolean processJob( TaskCommand baseCommand ) {
            baseCommand.setTaskId( this.taskId );
            TwoChannelMissingValueTaskCommand vectorCommand = ( TwoChannelMissingValueTaskCommand ) baseCommand;
            process( vectorCommand );
            return true;
        }

        /**
         * @param command
         * @return
         */
        private TaskResult process( TwoChannelMissingValueTaskCommand c ) {
            TwoChannelMissingValueTaskCommand jsCommand = createCommandObject( c );
            TaskResult result = taskProxy.execute( jsCommand );
            return result;
        }

    }

    @Autowired
    private ExpressionExperimentService expressionExperimentService = null;

    @Autowired
    private TwoChannelMissingValues twoChannelMissingValues = null;

    /**
     * AJAX entry point. -- uses default settings
     * 
     * @param id
     * @return
     * @throws Exception
     */
    public String run( Long id ) throws Exception {
        /* this 'run' method is exported in the spring-beans.xml */

        ExpressionExperiment ee = expressionExperimentService.load( id );

        if ( ee == null ) {
            throw new IllegalArgumentException( "Cannot access experiment with id=" + id );
        }

        TwoChannelMissingValueTaskCommand cmd = new TwoChannelMissingValueTaskCommand( null, ee );

        return super
                .run( cmd, SpacesEnum.DEFAULT_SPACE.getSpaceUrl(), TwoChannelMissingValueTask.class.getName(), true );
    }

    public void setExpressionExperimentService( ExpressionExperimentService expressionExperimentService ) {
        this.expressionExperimentService = expressionExperimentService;
    }

    /**
     * @param twoChannelMissingValues the twoChannelMissingValues to set
     */
    public void setTwoChannelMissingValues( TwoChannelMissingValues twoChannelMissingValues ) {
        this.twoChannelMissingValues = twoChannelMissingValues;
    }

    @Override
    protected BackgroundControllerJob<Boolean> getRunner( String jobId, Object command ) {
        return new TwoChannelMissingValueJob( jobId, command );
    }

    @Override
    protected BackgroundControllerJob<Boolean> getSpaceRunner( String jobId, Object command ) {
        return new TwoChannelMissingValueSpaceJob( jobId, command );
    }

    @Override
    protected String getViewNameForRequest( HttpServletRequest arg0 ) {
        return null;
    }

}
