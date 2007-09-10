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
package ubic.gemma.web.controller.common.auditAndSecurity;

import ubic.gemma.model.common.auditAndSecurity.AuditAction;
import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.common.auditAndSecurity.AuditEventImpl;

/**
 * @author pavlidis
 * @version $Id$
 */
public class AuditEventValueObject extends AuditEventImpl {

    public AuditEventValueObject() {
    }

    public AuditEventValueObject( AuditEvent ae ) {
        this.setPerformer( ae.getPerformer() );
        this.setAction( ae.getAction() );
        this.setEventType( ae.getEventType() );
        this.setNote( ae.getNote() );
        this.setDate( ae.getDate() );
        this.setDetail( ae.getDetail() );
    }

    public String getActionName() {
        if ( this.getAction().equals( AuditAction.CREATE ) ) {
            return "Create";
        } else {
            return "Update";
        }
    }

    public String getEventTypeName() {
        return this.getEventType() == null ? "" : this.getEventType().getClass().getSimpleName();
    }

}
