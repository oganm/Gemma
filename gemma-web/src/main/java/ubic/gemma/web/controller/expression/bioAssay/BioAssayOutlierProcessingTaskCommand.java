/*
 * The Gemma project
 * 
 * Copyright (c) 2013 University of British Columbia
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
package ubic.gemma.web.controller.expression.bioAssay;

import ubic.gemma.job.TaskCommand;
import ubic.gemma.tasks.analysis.expression.BioAssayOutlierProcessingTask;

/**
 * @author anton
 * @vesrion $Id$
 */
public class BioAssayOutlierProcessingTaskCommand extends TaskCommand {
    private boolean revert;

    public boolean isRevert() {
        return revert;
    }

    public BioAssayOutlierProcessingTaskCommand( Long id ) {
        this.setEntityId( id );
    }

    public BioAssayOutlierProcessingTaskCommand( Long id, boolean revertAsOutlier ) {
        this( id );
        this.revert = revertAsOutlier;
    }

    @Override
    public Class getTaskClass() {
        return BioAssayOutlierProcessingTask.class;
    }
}
