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
package ubic.gemma.model.genome;

/**
 * <p>
 * Spring Service base class for <code>ubic.gemma.model.genome.ProbeAlignedRegionService</code>, provides access to all
 * services and entities referenced by this service.
 * </p>
 * 
 * @see ubic.gemma.model.genome.ProbeAlignedRegionService
 */
public abstract class ProbeAlignedRegionServiceBase implements ubic.gemma.model.genome.ProbeAlignedRegionService {

    private ubic.gemma.model.genome.ProbeAlignedRegionDao probeAlignedRegionDao;

    /**
     * @see ubic.gemma.model.genome.ProbeAlignedRegionService#findAssociations(ubic.gemma.model.genome.PhysicalLocation)
     */
    public java.util.Collection findAssociations( final ubic.gemma.model.genome.PhysicalLocation physicalLocation ) {
        try {
            return this.handleFindAssociations( physicalLocation );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.ProbeAlignedRegionServiceException(
                    "Error performing 'ubic.gemma.model.genome.ProbeAlignedRegionService.findAssociations(ubic.gemma.model.genome.PhysicalLocation physicalLocation)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.ProbeAlignedRegionService#findAssociations(ubic.gemma.model.genome.sequenceAnalysis.BlatResult)
     */
    public java.util.Collection findAssociations( final ubic.gemma.model.genome.sequenceAnalysis.BlatResult blatResult ) {
        try {
            return this.handleFindAssociations( blatResult );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.ProbeAlignedRegionServiceException(
                    "Error performing 'ubic.gemma.model.genome.ProbeAlignedRegionService.findAssociations(ubic.gemma.model.genome.sequenceAnalysis.BlatResult blatResult)' --> "
                            + th, th );
        }
    }

    /**
     * Sets the reference to <code>probeAlignedRegion</code>'s DAO.
     */
    public void setProbeAlignedRegionDao( ubic.gemma.model.genome.ProbeAlignedRegionDao probeAlignedRegionDao ) {
        this.probeAlignedRegionDao = probeAlignedRegionDao;
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
     * Gets the reference to <code>probeAlignedRegion</code>'s DAO.
     */
    protected ubic.gemma.model.genome.ProbeAlignedRegionDao getProbeAlignedRegionDao() {
        return this.probeAlignedRegionDao;
    }

    /**
     * Performs the core logic for {@link #findAssociations(ubic.gemma.model.genome.PhysicalLocation)}
     */
    protected abstract java.util.Collection handleFindAssociations(
            ubic.gemma.model.genome.PhysicalLocation physicalLocation ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findAssociations(ubic.gemma.model.genome.sequenceAnalysis.BlatResult)}
     */
    protected abstract java.util.Collection handleFindAssociations(
            ubic.gemma.model.genome.sequenceAnalysis.BlatResult blatResult ) throws java.lang.Exception;

}