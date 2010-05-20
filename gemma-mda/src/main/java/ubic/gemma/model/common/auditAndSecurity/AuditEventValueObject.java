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

/**
 * @author klc (orginally generated by model)
 * @version $Id$
 */
public class AuditEventValueObject implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String performer;

    private java.util.Date date;

    private java.lang.String action;

    private java.lang.String note;

    private java.lang.String detail;

    private java.lang.Long id;

    private ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType eventType;

    /**
     * No-arg constructor added to satisfy javabean contract
     */

    public AuditEventValueObject() {
    }

    public AuditEventValueObject( AuditEvent ae ) {
        if ( ae == null ) throw new IllegalArgumentException( "Event cannot be null" );
        if ( ae.getPerformer() != null ) this.setPerformer( ae.getPerformer().getUserName() );

        if ( ae.getAction() != null ) this.setAction( ae.getAction().getValue() );
        this.setEventType( ae.getEventType() );
        this.setNote( ae.getNote() );
        this.setDate( ae.getDate() );
        this.setDetail( ae.getDetail() );
    }

    /**
     * 
     */
    public java.lang.String getAction() {
        return this.action;
    }

    public String getActionName() {
        if ( this.getAction().equals( "C" ) ) {
            return "Create";
        }
        return "Update";
    }

    /**
     * 
     */
    public java.util.Date getDate() {
        return this.date;
    }

    /**
     * 
     */
    public java.lang.String getDetail() {
        return this.detail;
    }

    /**
     * 
     */
    public ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType getEventType() {
        return this.eventType;
    }

    public String getEventTypeName() {
        return this.getEventType() == null ? "" : this.getEventType().getClass().getSimpleName();
    }

    /**
     * 
     */
    public java.lang.Long getId() {
        return this.id;
    }

    /**
     * <p>
     * <p>
     * An annotation about the action taken.
     * </p>
     * </p>
     */
    public java.lang.String getNote() {
        return this.note;
    }

    public String getPerformer() {
        return this.performer;
    }

    public void setAction( java.lang.String action ) {
        this.action = action;
    }

    public void setDate( java.util.Date date ) {
        this.date = date;
    }

    public void setDetail( java.lang.String detail ) {
        this.detail = detail;
    }

    public void setEventType( ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType eventType ) {
        this.eventType = eventType;
    }

    public void setId( java.lang.Long id ) {
        this.id = id;
    }

    public void setNote( java.lang.String note ) {
        this.note = note;
    }

    public void setPerformer( String name ) {
        this.performer = name;
    }

}
