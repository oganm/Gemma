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
package ubic.gemma.grid.javaspaces;

import java.io.Serializable;

/**
 * This class describes the result of the task executed by a worker.
 * <p>
 * The is used in the Master Worker pattern using the GigaSpaces Spring based remote invocation.
 * 
 * @author keshav
 * @version $Id$
 */
public class TaskResult implements Serializable {
    /**
     * The answer
     */
    private Object answer = null; // result
    /**
     * The task id
     */
    private Object taskID; // requestor

    /**
     * Constructor
     */
    public TaskResult() {
    }

    public TaskResult( Object taskId ) {
        this.taskID = taskId;
    }

    public Object getAnswer() {
        return answer;
    }

    public Object getTaskID() {
        return taskID;
    }

    public void setAnswer( Object answer ) {
        this.answer = answer;
    }

    public void setTaskID( Object taskID ) {
        this.taskID = taskID;
    }
}
