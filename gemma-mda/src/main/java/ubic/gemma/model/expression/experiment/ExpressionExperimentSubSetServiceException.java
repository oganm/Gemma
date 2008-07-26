/*
 * The Gemma project.
 * 
 * Copyright (c) 2006-2007 University of British Columbia
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
//
// Attention: Generated code! Do not modify by hand!
// Generated by: DefaultServiceException.vsl in andromda-spring-cartridge.
//
package ubic.gemma.model.expression.experiment;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * The default exception thrown for unexpected errors occurring within
 * {@link ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService}.
 */
public class ExpressionExperimentSubSetServiceException extends java.lang.RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7300491592726931461L;

    /**
     * The default constructor for <code>ExpressionExperimentSubSetServiceException</code>.
     */
    public ExpressionExperimentSubSetServiceException() {
    }

    /**
     * Constructs a new instance of <code>ExpressionExperimentSubSetServiceException</code>.
     * 
     * @param throwable the parent Throwable
     */
    public ExpressionExperimentSubSetServiceException( Throwable throwable ) {
        super( findRootCause( throwable ) );
    }

    /**
     * Constructs a new instance of <code>ExpressionExperimentSubSetServiceException</code>.
     * 
     * @param message the throwable message.
     */
    public ExpressionExperimentSubSetServiceException( String message ) {
        super( message );
    }

    /**
     * Constructs a new instance of <code>ExpressionExperimentSubSetServiceException</code>.
     * 
     * @param message the throwable message.
     * @param throwable the parent of this Throwable.
     */
    public ExpressionExperimentSubSetServiceException( String message, Throwable throwable ) {
        super( message, findRootCause( throwable ) );
    }

    /**
     * Finds the root cause of the parent exception by traveling up the exception tree
     */
    private static Throwable findRootCause( Throwable th ) {
        if ( th != null ) {
            // Reflectively get any exception causes.
            try {
                Throwable targetException = null;

                // java.lang.reflect.InvocationTargetException
                String exceptionProperty = "targetException";
                if ( PropertyUtils.isReadable( th, exceptionProperty ) ) {
                    targetException = ( Throwable ) PropertyUtils.getProperty( th, exceptionProperty );
                } else {
                    exceptionProperty = "causedByException";
                    // javax.ejb.EJBException
                    if ( PropertyUtils.isReadable( th, exceptionProperty ) ) {
                        targetException = ( Throwable ) PropertyUtils.getProperty( th, exceptionProperty );
                    }
                }
                if ( targetException != null ) {
                    th = targetException;
                }
            } catch ( Exception ex ) {
                // just print the exception and continue
                ex.printStackTrace();
            }

            if ( th.getCause() != null ) {
                th = th.getCause();
                th = findRootCause( th );
            }
        }
        return th;
    }
}