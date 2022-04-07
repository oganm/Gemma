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
package ubic.gemma.model.common.auditAndSecurity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author klc (orginally generated by model)
 */
@SuppressWarnings("unused") // Used in frontend
public class AuditEventValueObject extends IdentifiableValueObject<AuditEvent> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String TROUBLE_UNKNOWN_NAME = "Unknown performer";

    private String performer;
    private Date date;
    private String action;
    private String note;
    private String detail;
    private AuditEventType eventType;

    public AuditEventValueObject( AuditEvent ae ) {
        super( ae );
        if ( ae.getPerformer() != null )
            this.setPerformer( ae.getPerformer().getUserName() );
        if ( ae.getAction() != null )
            this.setAction( ae.getAction().getValue() );
        this.setEventType( ae.getEventType() );
        this.setNote( ae.getNote() );
        this.setDate( ae.getDate() );
        this.setDetail( ae.getDetail() );
    }

    public String getAction() {
        return this.action;
    }

    public void setAction( String action ) {
        this.action = action;
    }

    public String getActionName() {
        if ( getAction() == null ) {
            return null;
        } else if ( this.getAction().equals( "C" ) ) {
            return "Create";
        } else {
            return "Update";
        }
    }

    public java.util.Date getDate() {
        return this.date;
    }

    public void setDate( java.util.Date date ) {
        this.date = date;
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail( String detail ) {
        this.detail = detail;
    }

    public AuditEventType getEventType() {
        return this.eventType;
    }

    public void setEventType( AuditEventType eventType ) {
        this.eventType = eventType;
    }

    public String getEventTypeName() {
        return this.getEventType() == null ? "" : this.getEventType().getClass().getSimpleName();
    }

    public String getNote() {
        return this.note;
    }

    public void setNote( String note ) {
        this.note = note;
    }

    public String getPerformer() {
        return this.performer;
    }

    public void setPerformer( String name ) {
        this.performer = name;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if ( this.getPerformer() == null ) {
            buf.append( AuditEventValueObject.TROUBLE_UNKNOWN_NAME );
        } else {
            buf.append( this.getPerformer() );
        }

        try {
            buf.append( " on " ).append(
                    DateFormat.getDateInstance( DateFormat.LONG, Locale.getDefault() ).format( this.getDate() ) );
        } catch ( Exception ex ) {
            LogFactory.getLog( this.getClass() ).error( ex );
        }
        buf.append( ": " );

        boolean hasNote = false;

        if ( !StringUtils.isEmpty( this.getNote() ) ) {
            buf.append( this.getNote() );
            hasNote = true;
        }
        if ( !StringUtils.isEmpty( this.getDetail() ) ) {
            if ( hasNote ) {
                buf.append( " - " );
            }
            buf.append( this.getDetail() );
        }
        return buf.toString();
    }

}
