/*
 * The Gemma project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.core.apps;

import ubic.gemma.model.expression.experiment.BioAssaySet;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * Delete one or more experiments from the system.
 *
 * @author paul
 */
public class DeleteExperimentsCli extends ExpressionExperimentManipulatingCLI {

    public static int main( String[] args ) {
        DeleteExperimentsCli d = new DeleteExperimentsCli();
        return executeCommand( d, args );
    }

    @Override
    public String getCommandName() {
        return "deleteExperiments";
    }

    @Override
    protected void doWork( String[] args ) throws Exception {
        this.force = true;
        super.processCommandLine( args );

        for ( BioAssaySet bas : this.expressionExperiments ) {
            try {
                log.info( "--------- Deleting " + bas + " --------" );
                this.eeService.remove( ( ExpressionExperiment ) bas );
                successObjects.add( bas );
                log.info( "--------- Finished Deleting " + bas + " -------" );
            } catch ( Exception ex ) {
                log.error( ex, ex );
                errorObjects.add( bas + " " + ex.getMessage() );
            }
        }

        summarizeProcessing();
    }

    @Override
    public String getShortDesc() {
        return "Delete experiment(s) from the system";
    }

}
