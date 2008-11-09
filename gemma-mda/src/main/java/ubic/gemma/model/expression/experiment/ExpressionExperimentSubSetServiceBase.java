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
// Generated by: SpringServiceBase.vsl in andromda-spring-cartridge.
//
package ubic.gemma.model.expression.experiment;

/**
 * <p>
 * Spring Service base class for <code>ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService</code>,
 * provides access to all services and entities referenced by this service.
 * </p>
 * 
 * @see ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService
 */
public abstract class ExpressionExperimentSubSetServiceBase implements
        ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService {

    private ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetDao expressionExperimentSubSetDao;

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService#create(ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet create(
            final ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet expressionExperimentSubSet ) {
        try {
            return this.handleCreate( expressionExperimentSubSet );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetServiceException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService.create(ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet expressionExperimentSubSet)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService#load(java.lang.Long)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet load( final java.lang.Long id ) {
        try {
            return this.handleLoad( id );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetServiceException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService.load(java.lang.Long id)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService#loadAll()
     */
    public java.util.Collection loadAll() {
        try {
            return this.handleLoadAll();
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetServiceException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetService.loadAll()' --> "
                            + th, th );
        }
    }

    /**
     * Sets the reference to <code>expressionExperimentSubSet</code>'s DAO.
     */
    public void setExpressionExperimentSubSetDao(
            ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetDao expressionExperimentSubSetDao ) {
        this.expressionExperimentSubSetDao = expressionExperimentSubSetDao;
    }

    /**
     * Gets the reference to <code>expressionExperimentSubSet</code>'s DAO.
     */
    protected ubic.gemma.model.expression.experiment.ExpressionExperimentSubSetDao getExpressionExperimentSubSetDao() {
        return this.expressionExperimentSubSetDao;
    }

    /**
     * Gets the message having the given <code>key</code> using the given <code>arguments</code> for the given
     * <code>locale</code>.
     * 
     * @param key the key of the message in the messages.properties message bundle.
     * @param arguments any arguments to substitute when resolving the message.
     * @param locale the locale of the messages to retrieve.
     */
    protected String getMessage( final java.lang.String key, final java.lang.Object[] arguments,
            final java.util.Locale locale ) {
        return this.getMessages().getMessage( key, arguments, locale );
    }

    /**
     * Gets the message having the given <code>key</code> in the underlying message bundle.
     * 
     * @param key the key of the message in the messages.properties message bundle.
     */
    protected String getMessage( final String key ) {
        return this.getMessages().getMessage( key, null, null );
    }

    /**
     * Gets the message having the given <code>key</code> and <code>arguments</code> in the underlying message bundle.
     * 
     * @param key the key of the message in the messages.properties message bundle.
     * @param arguments any arguments to substitute when resolving the message.
     */
    protected String getMessage( final String key, final Object[] arguments ) {
        return this.getMessages().getMessage( key, arguments, null );
    }

    /**
     * Gets the message source available to this service.
     */
    protected org.springframework.context.MessageSource getMessages() {
        return ( org.springframework.context.MessageSource ) ubic.gemma.spring.BeanLocator.instance().getBean(
                "messageSource" );
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return ubic.gemma.spring.PrincipalStore.get();
    }

    /**
     * Performs the core logic for {@link #create(ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet)}
     */
    protected abstract ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet handleCreate(
            ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet expressionExperimentSubSet )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #load(java.lang.Long)}
     */
    protected abstract ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet handleLoad( java.lang.Long id )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadAll()}
     */
    protected abstract java.util.Collection handleLoadAll() throws java.lang.Exception;

}