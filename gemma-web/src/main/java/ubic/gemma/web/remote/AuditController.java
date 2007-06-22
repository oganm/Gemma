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
package ubic.gemma.web.remote;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.web.controller.common.auditAndSecurity.AuditEventValueObject;

/**
 * This is required soley for exposing auditables to remote services would try to marshall the abstract class Auditable.
 * 
 * @spring.bean id="auditController"
 * @spring.property name="arrayDesignService" ref="arrayDesignService"
 * @spring.property name="expressionExperimentService" ref="expressionExperimentService"
 * @author pavlidis
 * @version $Id$
 */
public class AuditController {

    private static Log log = LogFactory.getLog( AuditController.class.getName() );

    ArrayDesignService arrayDesignService;
    ExpressionExperimentService expressionExperimentService;

    @SuppressWarnings("unchecked")
    public Collection<AuditEventValueObject> getEvents( EntityDelegator e ) {
        if ( e == null || e.getId() == null ) return null;
        if ( e.getClassDelegatingFor() == null ) return null;

        Collection<AuditEventValueObject> result = new HashSet<AuditEventValueObject>();
        Collection events = new HashSet<AuditEvent>();
        Class<?> clazz;
        try {
            clazz = Class.forName( e.getClassDelegatingFor() );
        } catch ( ClassNotFoundException e1 ) {
            throw new RuntimeException( e1 );
        }
        if ( ExpressionExperiment.class.isAssignableFrom( clazz ) ) {
            ExpressionExperiment entity = expressionExperimentService.load( e.getId() );

            if ( entity == null ) {
                log.warn( "Entity with id = " + e.getId() + " not found" );
                return new HashSet();
            }
            events = expressionExperimentService.getEvents( entity );
        } else if ( ArrayDesign.class.isAssignableFrom( clazz ) ) {
            ArrayDesign entity = arrayDesignService.load( e.getId() );
            if ( entity == null ) {
                log.warn( "Entity with id = " + e.getId() + " not found" );
                return new HashSet();
            }
            events = arrayDesignService.getEvents( entity );
        } else {
            log.warn( "We don't support that class yet, sorry" );
        }

        for ( AuditEvent ev : ( Collection<AuditEvent> ) events ) {
            result.add( new AuditEventValueObject( ev ) );
        }

        return result;
    }

    public void setArrayDesignService( ArrayDesignService auditableService ) {
        this.arrayDesignService = auditableService;
    }

    public void setExpressionExperimentService( ExpressionExperimentService expressionExperimentService ) {
        this.expressionExperimentService = expressionExperimentService;
    }
}
