/*
 * The Gemma project.
 * 
 * Copyright (c) 2006-2012 University of British Columbia
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

import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.common.auditAndSecurity.curation.Curatable;

/**
 * <p>
 * Indicates that previous validation is being invalidated
 * </p>
 *
 * @author Paul
 */
public class CurationNoteUpdateEvent extends CurationDetailsEvent {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1180281595664872508L;

    /**
     * No-arg constructor added to satisfy javabean contract
     */
    public CurationNoteUpdateEvent() {
    }

    /**
     *
     * @param curatable  the curatable object to do the curation action on.
     * @param auditEvent the audit event containing information about the action that should be made.
     *                   With the CurationNoteUpdateEvent, this method expects the auditEvent Note property to
     *                   contain the new text of the note. This can be any String object, including a null object.
     */
    @Override
    public void setCurationDetails( Curatable curatable, AuditEvent auditEvent ) {
        curatable.getCurationDetails().setCurationNote( auditEvent.getNote() );
        curatable.getCurationDetails().setLastNoteUpdateEvent( auditEvent );
    }

    /**
     * Constructs new instances of {@link CurationNoteUpdateEvent}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link CurationNoteUpdateEvent}.
         */
        public static CurationNoteUpdateEvent newInstance() {
            return new CurationNoteUpdateEvent();
        }

    }

}