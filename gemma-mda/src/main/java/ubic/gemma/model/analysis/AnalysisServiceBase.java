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
package ubic.gemma.model.analysis;

/**
 * <p>
 * Spring Service base class for <code>ubic.gemma.model.analysis.AnalysisService</code>, provides access to all services
 * and entities referenced by this service.
 * </p>
 * 
 * @see ubic.gemma.model.analysis.AnalysisService
 */
public abstract class AnalysisServiceBase implements ubic.gemma.model.analysis.AnalysisService {

    private ubic.gemma.model.analysis.AnalysisDao analysisDao;

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#delete(java.lang.Long)
     */
    public void delete( final java.lang.Long idToDelete ) {
        try {
            this.handleDelete( idToDelete );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.delete(java.lang.Long idToDelete)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#delete(ubic.gemma.model.analysis.Analysis)
     */
    public void delete( final ubic.gemma.model.analysis.Analysis toDelete ) {
        try {
            this.handleDelete( toDelete );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.delete(ubic.gemma.model.analysis.Analysis toDelete)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#findByInvestigation(ubic.gemma.model.analysis.Investigation)
     */
    public java.util.Collection findByInvestigation( final ubic.gemma.model.analysis.Investigation investigation ) {
        try {
            return this.handleFindByInvestigation( investigation );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.findByInvestigation(ubic.gemma.model.analysis.Investigation investigation)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#findByInvestigations(java.util.Collection)
     */
    public java.util.Map findByInvestigations( final java.util.Collection investigations ) {
        try {
            return this.handleFindByInvestigations( investigations );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.findByInvestigations(java.util.Collection investigations)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#findByName(java.lang.String)
     */
    public ubic.gemma.model.analysis.Analysis findByName( final java.lang.String name ) {
        try {
            return this.handleFindByName( name );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.findByName(java.lang.String name)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#findByTaxon(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection findByTaxon( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleFindByTaxon( taxon );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.findByTaxon(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#findByUniqueInvestigations(java.util.Collection)
     */
    public ubic.gemma.model.analysis.Analysis findByUniqueInvestigations( final java.util.Collection investigations ) {
        try {
            return this.handleFindByUniqueInvestigations( investigations );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.findByUniqueInvestigations(java.util.Collection investigations)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#load(java.lang.Long)
     */
    public ubic.gemma.model.analysis.Analysis load( final java.lang.Long id ) {
        try {
            return this.handleLoad( id );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.load(java.lang.Long id)' --> " + th,
                    th );
        }
    }

    /**
     * @see ubic.gemma.model.analysis.AnalysisService#loadAll()
     */
    public java.util.Collection loadAll() {
        try {
            return this.handleLoadAll();
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.analysis.AnalysisServiceException(
                    "Error performing 'ubic.gemma.model.analysis.AnalysisService.loadAll()' --> " + th, th );
        }
    }

    /**
     * Sets the reference to <code>analysis</code>'s DAO.
     */
    public void setAnalysisDao( ubic.gemma.model.analysis.AnalysisDao analysisDao ) {
        this.analysisDao = analysisDao;
    }

    /**
     * Gets the reference to <code>analysis</code>'s DAO.
     */
    protected ubic.gemma.model.analysis.AnalysisDao getAnalysisDao() {
        return this.analysisDao;
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
     * Performs the core logic for {@link #delete(java.lang.Long)}
     */
    protected abstract void handleDelete( java.lang.Long idToDelete ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #delete(ubic.gemma.model.analysis.Analysis)}
     */
    protected abstract void handleDelete( ubic.gemma.model.analysis.Analysis toDelete ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByInvestigation(ubic.gemma.model.analysis.Investigation)}
     */
    protected abstract java.util.Collection handleFindByInvestigation(
            ubic.gemma.model.analysis.Investigation investigation ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByInvestigations(java.util.Collection)}
     */
    protected abstract java.util.Map handleFindByInvestigations( java.util.Collection investigations )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByName(java.lang.String)}
     */
    protected abstract ubic.gemma.model.analysis.Analysis handleFindByName( java.lang.String name )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByTaxon(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection handleFindByTaxon( ubic.gemma.model.genome.Taxon taxon )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByUniqueInvestigations(java.util.Collection)}
     */
    protected abstract ubic.gemma.model.analysis.Analysis handleFindByUniqueInvestigations(
            java.util.Collection investigations ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #load(java.lang.Long)}
     */
    protected abstract ubic.gemma.model.analysis.Analysis handleLoad( java.lang.Long id ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadAll()}
     */
    protected abstract java.util.Collection handleLoadAll() throws java.lang.Exception;

}