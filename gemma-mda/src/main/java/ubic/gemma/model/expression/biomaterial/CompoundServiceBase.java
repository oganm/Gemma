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
package ubic.gemma.model.expression.biomaterial;

/**
 * <p>
 * Spring Service base class for <code>ubic.gemma.model.expression.biomaterial.CompoundService</code>, provides access
 * to all services and entities referenced by this service.
 * </p>
 * 
 * @see ubic.gemma.model.expression.biomaterial.CompoundService
 */
public abstract class CompoundServiceBase implements ubic.gemma.model.expression.biomaterial.CompoundService {

    private ubic.gemma.model.expression.biomaterial.CompoundDao compoundDao;

    /**
     * @see ubic.gemma.model.expression.biomaterial.CompoundService#find(ubic.gemma.model.expression.biomaterial.Compound)
     */
    public ubic.gemma.model.expression.biomaterial.Compound find(
            final ubic.gemma.model.expression.biomaterial.Compound compound ) {
        try {
            return this.handleFind( compound );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.biomaterial.CompoundServiceException(
                    "Error performing 'ubic.gemma.model.expression.biomaterial.CompoundService.find(ubic.gemma.model.expression.biomaterial.Compound compound)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.CompoundService#findOrCreate(ubic.gemma.model.expression.biomaterial.Compound)
     */
    public ubic.gemma.model.expression.biomaterial.Compound findOrCreate(
            final ubic.gemma.model.expression.biomaterial.Compound compound ) {
        try {
            return this.handleFindOrCreate( compound );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.biomaterial.CompoundServiceException(
                    "Error performing 'ubic.gemma.model.expression.biomaterial.CompoundService.findOrCreate(ubic.gemma.model.expression.biomaterial.Compound compound)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.CompoundService#remove(ubic.gemma.model.expression.biomaterial.Compound)
     */
    public void remove( final ubic.gemma.model.expression.biomaterial.Compound compound ) {
        try {
            this.handleRemove( compound );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.biomaterial.CompoundServiceException(
                    "Error performing 'ubic.gemma.model.expression.biomaterial.CompoundService.remove(ubic.gemma.model.expression.biomaterial.Compound compound)' --> "
                            + th, th );
        }
    }

    /**
     * Sets the reference to <code>compound</code>'s DAO.
     */
    public void setCompoundDao( ubic.gemma.model.expression.biomaterial.CompoundDao compoundDao ) {
        this.compoundDao = compoundDao;
    }

    /**
     * @see ubic.gemma.model.expression.biomaterial.CompoundService#update(ubic.gemma.model.expression.biomaterial.Compound)
     */
    public void update( final ubic.gemma.model.expression.biomaterial.Compound compound ) {
        try {
            this.handleUpdate( compound );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.biomaterial.CompoundServiceException(
                    "Error performing 'ubic.gemma.model.expression.biomaterial.CompoundService.update(ubic.gemma.model.expression.biomaterial.Compound compound)' --> "
                            + th, th );
        }
    }

    /**
     * Gets the reference to <code>compound</code>'s DAO.
     */
    protected ubic.gemma.model.expression.biomaterial.CompoundDao getCompoundDao() {
        return this.compoundDao;
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
     * Performs the core logic for {@link #find(ubic.gemma.model.expression.biomaterial.Compound)}
     */
    protected abstract ubic.gemma.model.expression.biomaterial.Compound handleFind(
            ubic.gemma.model.expression.biomaterial.Compound compound ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findOrCreate(ubic.gemma.model.expression.biomaterial.Compound)}
     */
    protected abstract ubic.gemma.model.expression.biomaterial.Compound handleFindOrCreate(
            ubic.gemma.model.expression.biomaterial.Compound compound ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #remove(ubic.gemma.model.expression.biomaterial.Compound)}
     */
    protected abstract void handleRemove( ubic.gemma.model.expression.biomaterial.Compound compound )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #update(ubic.gemma.model.expression.biomaterial.Compound)}
     */
    protected abstract void handleUpdate( ubic.gemma.model.expression.biomaterial.Compound compound )
            throws java.lang.Exception;

}