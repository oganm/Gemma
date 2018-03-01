/*
 * The gemma project
 *
 * Copyright (c) 2015 University of British Columbia
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

package ubic.gemma.model.common.auditAndSecurity.eventType;

/**
 * @author paul
 */
public class BatchCorrectionEvent extends ExpressionExperimentAnalysisEvent {

    private static final long serialVersionUID = -3061045506228031201L;

    /**
     * No-arg constructor added to satisfy javabean contract
     *
     * @author Paul
     */
    public BatchCorrectionEvent() {
    }

    @SuppressWarnings({ "unused", "WeakerAccess" }) // Possible external use
    public static final class Factory {

        public static ubic.gemma.model.common.auditAndSecurity.eventType.BatchCorrectionEvent newInstance() {
            return new ubic.gemma.model.common.auditAndSecurity.eventType.BatchCorrectionEvent();
        }

    }
}
