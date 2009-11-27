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
package ubic.gemma.model.expression.bioAssay;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * Spring Service base class for <code>ubic.gemma.model.expression.bioAssay.BioAssayService</code>, provides access to
 * all services and entities referenced by this service.
 * </p>
 * 
 * @see ubic.gemma.model.expression.bioAssay.BioAssayService
 */
public abstract class BioAssayServiceBase implements ubic.gemma.model.expression.bioAssay.BioAssayService {

    @Autowired
    private ubic.gemma.model.expression.biomaterial.BioMaterialService bioMaterialService;

    @Autowired
    private ubic.gemma.model.expression.bioAssay.BioAssayDao bioAssayDao;

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#addBioMaterialAssociation(ubic.gemma.model.expression.bioAssay.BioAssay,
     *      ubic.gemma.model.expression.biomaterial.BioMaterial)
     */
    public void addBioMaterialAssociation( final ubic.gemma.model.expression.bioAssay.BioAssay bioAssay,
            final ubic.gemma.model.expression.biomaterial.BioMaterial bioMaterial ) {
        try {
            this.handleAddBioMaterialAssociation( bioAssay, bioMaterial );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.addBioMaterialAssociation(ubic.gemma.model.expression.bioAssay.BioAssay bioAssay, ubic.gemma.model.expression.biomaterial.BioMaterial bioMaterial)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#countAll()
     */
    public java.lang.Integer countAll() {
        try {
            return this.handleCountAll();
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.countAll()' --> " + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#findBioAssayDimensions(ubic.gemma.model.expression.bioAssay.BioAssay)
     */
    public java.util.Collection findBioAssayDimensions( final ubic.gemma.model.expression.bioAssay.BioAssay bioAssay ) {
        try {
            return this.handleFindBioAssayDimensions( bioAssay );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.findBioAssayDimensions(ubic.gemma.model.expression.bioAssay.BioAssay bioAssay)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#findOrCreate(ubic.gemma.model.expression.bioAssay.BioAssay)
     */
    public ubic.gemma.model.expression.bioAssay.BioAssay findOrCreate(
            final ubic.gemma.model.expression.bioAssay.BioAssay bioAssay ) {
        try {
            return this.handleFindOrCreate( bioAssay );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.findOrCreate(ubic.gemma.model.expression.bioAssay.BioAssay bioAssay)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#load(java.lang.Long)
     */
    public ubic.gemma.model.expression.bioAssay.BioAssay load( final java.lang.Long id ) {
        try {
            return this.handleLoad( id );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.load(java.lang.Long id)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#loadAll()
     */
    public java.util.Collection<BioAssay> loadAll() {
        try {
            return this.handleLoadAll();
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.loadAll()' --> " + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#remove(ubic.gemma.model.expression.bioAssay.BioAssay)
     */
    public void remove( final ubic.gemma.model.expression.bioAssay.BioAssay bioAssay ) {
        try {
            this.handleRemove( bioAssay );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.remove(ubic.gemma.model.expression.bioAssay.BioAssay bioAssay)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#removeBioMaterialAssociation(ubic.gemma.model.expression.bioAssay.BioAssay,
     *      ubic.gemma.model.expression.biomaterial.BioMaterial)
     */
    public void removeBioMaterialAssociation( final ubic.gemma.model.expression.bioAssay.BioAssay bioAssay,
            final ubic.gemma.model.expression.biomaterial.BioMaterial bioMaterial ) {
        try {
            this.handleRemoveBioMaterialAssociation( bioAssay, bioMaterial );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.removeBioMaterialAssociation(ubic.gemma.model.expression.bioAssay.BioAssay bioAssay, ubic.gemma.model.expression.biomaterial.BioMaterial bioMaterial)' --> "
                            + th, th );
        }
    }

    /**
     * Sets the reference to <code>bioAssay</code>'s DAO.
     */
    public void setBioAssayDao( ubic.gemma.model.expression.bioAssay.BioAssayDao bioAssayDao ) {
        this.bioAssayDao = bioAssayDao;
    }

    /**
     * Sets the reference to <code>bioMaterialService</code>.
     */
    public void setBioMaterialService( ubic.gemma.model.expression.biomaterial.BioMaterialService bioMaterialService ) {
        this.bioMaterialService = bioMaterialService;
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#thaw(ubic.gemma.model.expression.bioAssay.BioAssay)
     */
    public void thaw( final ubic.gemma.model.expression.bioAssay.BioAssay bioAssay ) {
        try {
            this.handleThaw( bioAssay );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.thaw(ubic.gemma.model.expression.bioAssay.BioAssay bioAssay)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.bioAssay.BioAssayService#update(ubic.gemma.model.expression.bioAssay.BioAssay)
     */
    public void update( final ubic.gemma.model.expression.bioAssay.BioAssay bioAssay ) {
        try {
            this.handleUpdate( bioAssay );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.expression.bioAssay.BioAssayServiceException(
                    "Error performing 'ubic.gemma.model.expression.bioAssay.BioAssayService.update(ubic.gemma.model.expression.bioAssay.BioAssay bioAssay)' --> "
                            + th, th );
        }
    }

    /**
     * Gets the reference to <code>bioAssay</code>'s DAO.
     */
    protected ubic.gemma.model.expression.bioAssay.BioAssayDao getBioAssayDao() {
        return this.bioAssayDao;
    }

    /**
     * Gets the reference to <code>bioMaterialService</code>.
     */
    protected ubic.gemma.model.expression.biomaterial.BioMaterialService getBioMaterialService() {
        return this.bioMaterialService;
    }

    /**
     * Performs the core logic for
     * {@link #addBioMaterialAssociation(ubic.gemma.model.expression.bioAssay.BioAssay, ubic.gemma.model.expression.biomaterial.BioMaterial)}
     */
    protected abstract void handleAddBioMaterialAssociation( ubic.gemma.model.expression.bioAssay.BioAssay bioAssay,
            ubic.gemma.model.expression.biomaterial.BioMaterial bioMaterial ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #countAll()}
     */
    protected abstract java.lang.Integer handleCountAll() throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findBioAssayDimensions(ubic.gemma.model.expression.bioAssay.BioAssay)}
     */
    protected abstract java.util.Collection handleFindBioAssayDimensions(
            ubic.gemma.model.expression.bioAssay.BioAssay bioAssay ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findOrCreate(ubic.gemma.model.expression.bioAssay.BioAssay)}
     */
    protected abstract ubic.gemma.model.expression.bioAssay.BioAssay handleFindOrCreate(
            ubic.gemma.model.expression.bioAssay.BioAssay bioAssay ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #load(java.lang.Long)}
     */
    protected abstract ubic.gemma.model.expression.bioAssay.BioAssay handleLoad( java.lang.Long id )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadAll()}
     */
    protected abstract java.util.Collection<BioAssay> handleLoadAll() throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #remove(ubic.gemma.model.expression.bioAssay.BioAssay)}
     */
    protected abstract void handleRemove( ubic.gemma.model.expression.bioAssay.BioAssay bioAssay )
            throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #removeBioMaterialAssociation(ubic.gemma.model.expression.bioAssay.BioAssay, ubic.gemma.model.expression.biomaterial.BioMaterial)}
     */
    protected abstract void handleRemoveBioMaterialAssociation( ubic.gemma.model.expression.bioAssay.BioAssay bioAssay,
            ubic.gemma.model.expression.biomaterial.BioMaterial bioMaterial ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #thaw(ubic.gemma.model.expression.bioAssay.BioAssay)}
     */
    protected abstract void handleThaw( ubic.gemma.model.expression.bioAssay.BioAssay bioAssay )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #update(ubic.gemma.model.expression.bioAssay.BioAssay)}
     */
    protected abstract void handleUpdate( ubic.gemma.model.expression.bioAssay.BioAssay bioAssay )
            throws java.lang.Exception;

}