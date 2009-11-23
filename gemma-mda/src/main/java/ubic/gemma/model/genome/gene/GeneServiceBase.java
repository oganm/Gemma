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
package ubic.gemma.model.genome.gene;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ubic.gemma.model.analysis.expression.coexpression.CoexpressionCollectionValueObject;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.BioAssaySet;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.ProbeAlignedRegion;
import ubic.gemma.model.genome.Qtl;

/**
 * <p>
 * Spring Service base class for <code>ubic.gemma.model.genome.gene.GeneService</code>, provides access to all services
 * and entities referenced by this service.
 * </p>
 * 
 * @see ubic.gemma.model.genome.gene.GeneService
 */
public abstract class GeneServiceBase implements ubic.gemma.model.genome.gene.GeneService {

    @Autowired
    private ubic.gemma.model.genome.GeneDao geneDao;

    @Autowired
    private ubic.gemma.model.genome.BaseQtlDao qtlDao;

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#countAll()
     */
    public java.lang.Integer countAll() {
        try {
            return this.handleCountAll();
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.countAll()' --> " + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#create(java.util.Collection)
     */
    public java.util.Collection<Gene> create( final java.util.Collection<Gene> genes ) {
        try {
            return this.handleCreate( genes );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.create(java.util.Collection genes)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#create(ubic.gemma.model.genome.Gene)
     */
    public ubic.gemma.model.genome.Gene create( final ubic.gemma.model.genome.Gene gene ) {
        try {
            return this.handleCreate( gene );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.create(ubic.gemma.model.genome.Gene gene)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#find(ubic.gemma.model.genome.Gene)
     */
    public ubic.gemma.model.genome.Gene find( final ubic.gemma.model.genome.Gene gene ) {
        try {
            return this.handleFind( gene );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.find(ubic.gemma.model.genome.Gene gene)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findAllQtlsByPhysicalMapLocation(ubic.gemma.model.genome.PhysicalLocation)
     */
    public java.util.Collection<Qtl> findAllQtlsByPhysicalMapLocation(
            final ubic.gemma.model.genome.PhysicalLocation physicalMapLocation ) {
        try {
            return this.handleFindAllQtlsByPhysicalMapLocation( physicalMapLocation );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findAllQtlsByPhysicalMapLocation(ubic.gemma.model.genome.PhysicalLocation physicalMapLocation)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findByAccession(java.lang.String,
     *      ubic.gemma.model.common.description.ExternalDatabase)
     */
    public ubic.gemma.model.genome.Gene findByAccession( final java.lang.String accession,
            final ubic.gemma.model.common.description.ExternalDatabase source ) {
        try {
            return this.handleFindByAccession( accession, source );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findByAccession(java.lang.String accession, ubic.gemma.model.common.description.ExternalDatabase source)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findByAlias(java.lang.String)
     */
    public java.util.Collection<Gene> findByAlias( final java.lang.String search ) {
        try {
            return this.handleFindByAlias( search );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findByAlias(java.lang.String search)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findByNCBIId(java.lang.String)
     */
    public ubic.gemma.model.genome.Gene findByNCBIId( final java.lang.String accession ) {
        try {
            return this.handleFindByNCBIId( accession );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findByNCBIId(java.lang.String accession)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findByOfficialName(java.lang.String)
     */
    public java.util.Collection<Gene> findByOfficialName( final java.lang.String officialName ) {
        try {
            return this.handleFindByOfficialName( officialName );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findByOfficialName(java.lang.String officialName)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findByOfficialSymbol(java.lang.String)
     */
    public java.util.Collection<Gene> findByOfficialSymbol( final java.lang.String officialSymbol ) {
        try {
            return this.handleFindByOfficialSymbol( officialSymbol );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findByOfficialSymbol(java.lang.String officialSymbol)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findByOfficialSymbol(java.lang.String,
     *      ubic.gemma.model.genome.Taxon)
     */
    public ubic.gemma.model.genome.Gene findByOfficialSymbol( final java.lang.String symbol,
            final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleFindByOfficialSymbol( symbol, taxon );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findByOfficialSymbol(java.lang.String symbol, ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findByOfficialSymbolInexact(java.lang.String)
     */
    public java.util.Collection<Gene> findByOfficialSymbolInexact( final java.lang.String officialSymbol ) {
        try {
            return this.handleFindByOfficialSymbolInexact( officialSymbol );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findByOfficialSymbolInexact(java.lang.String officialSymbol)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#findOrCreate(ubic.gemma.model.genome.Gene)
     */
    public ubic.gemma.model.genome.Gene findOrCreate( final ubic.gemma.model.genome.Gene gene ) {
        try {
            return this.handleFindOrCreate( gene );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.findOrCreate(ubic.gemma.model.genome.Gene gene)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getCoexpressedGenes(ubic.gemma.model.genome.Gene,
     *      java.util.Collection, java.lang.Integer, boolean)
     */
    public Map<Gene, CoexpressionCollectionValueObject> getCoexpressedGenes(
            final Collection<ubic.gemma.model.genome.Gene> genes,
            final java.util.Collection<? extends BioAssaySet> ees, final java.lang.Integer stringency,
            final boolean knownGenesOnly, final boolean interGenesOnly ) {
        try {
            return this.handleGetCoexpressedGenes( genes, ees, stringency, knownGenesOnly, interGenesOnly );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getCoexpressedGenes(Collection<ubic.gemma.model.genome.Gene> genes, java.util.Collection ees, java.lang.Integer stringency, boolean knownGenesOnly)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getCoexpressedGenes(ubic.gemma.model.genome.Gene,
     *      java.util.Collection, java.lang.Integer, boolean)
     */
    public CoexpressionCollectionValueObject getCoexpressedGenes( final ubic.gemma.model.genome.Gene gene,
            final java.util.Collection<? extends BioAssaySet> ees, final java.lang.Integer stringency,
            final boolean knownGenesOnly ) {
        try {
            return this.handleGetCoexpressedGenes( gene, ees, stringency, knownGenesOnly );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getCoexpressedGenes(ubic.gemma.model.genome.Gene gene, java.util.Collection ees, java.lang.Integer stringency, boolean knownGenesOnly)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getCoexpressedKnownGenes(ubic.gemma.model.genome.Gene,
     *      java.util.Collection, java.lang.Integer)
     */
    public java.util.Collection<Gene> getCoexpressedKnownGenes( final ubic.gemma.model.genome.Gene gene,
            final java.util.Collection ees, final java.lang.Integer stringency ) {
        try {
            return this.handleGetCoexpressedKnownGenes( gene, ees, stringency );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getCoexpressedKnownGenes(ubic.gemma.model.genome.Gene gene, java.util.Collection ees, java.lang.Integer stringency)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getCompositeSequenceCountById(java.lang.Long)
     */
    public long getCompositeSequenceCountById( final java.lang.Long id ) {
        try {
            return this.handleGetCompositeSequenceCountById( id );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getCompositeSequenceCountById(java.lang.Long id)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getCompositeSequences(ubic.gemma.model.genome.Gene,
     *      ubic.gemma.model.expression.arrayDesign.ArrayDesign)
     */
    public java.util.Collection<CompositeSequence> getCompositeSequences( final ubic.gemma.model.genome.Gene gene,
            final ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign ) {
        try {
            return this.handleGetCompositeSequences( gene, arrayDesign );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getCompositeSequences(ubic.gemma.model.genome.Gene gene, ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getCompositeSequencesById(java.lang.Long)
     */
    public java.util.Collection getCompositeSequencesById( final java.lang.Long id ) {
        try {
            return this.handleGetCompositeSequencesById( id );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getCompositeSequencesById(java.lang.Long id)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getGenesByTaxon(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection<Gene> getGenesByTaxon( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleGetGenesByTaxon( taxon );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getGenesByTaxon(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#getMicroRnaByTaxon(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection getMicroRnaByTaxon( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleGetMicroRnaByTaxon( taxon );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.getMicroRnaByTaxon(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#load(long)
     */
    public ubic.gemma.model.genome.Gene load( final long id ) {
        try {
            return this.handleLoad( id );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.load(long id)' --> " + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#loadAll()
     */
    public java.util.Collection<Gene> loadAll() {
        try {
            return this.handleLoadAll();
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.loadAll()' --> " + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#loadKnownGenes(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection<Gene> loadKnownGenes( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleLoadKnownGenes( taxon );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.loadKnownGenes(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#loadMultiple(java.util.Collection)
     */
    public java.util.Collection<Gene> loadMultiple( final java.util.Collection<Long> ids ) {
        try {
            return this.handleLoadMultiple( ids );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.loadMultiple(java.util.Collection ids)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#loadPredictedGenes(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection loadPredictedGenes( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleLoadPredictedGenes( taxon );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.loadPredictedGenes(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#loadProbeAlignedRegions(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection loadProbeAlignedRegions( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleLoadProbeAlignedRegions( taxon );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.loadProbeAlignedRegions(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#remove(java.lang.String)
     */
    public void remove( Gene gene ) {
        try {
            this.handleRemove( gene );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.remove(java.lang.String officialName)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#remove(java.util.Collection)
     */
    public void remove( final java.util.Collection genes ) {
        try {
            this.handleRemove( genes );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.remove(java.util.Collection genes)' --> "
                            + th, th );
        }
    }

    /**
     * Sets the reference to <code>gene</code>'s DAO.
     */
    public void setGeneDao( ubic.gemma.model.genome.GeneDao geneDao ) {
        this.geneDao = geneDao;
    }

    /**
     * Sets the reference to <code>qtl</code>'s DAO.
     */
    public void setQtlDao( ubic.gemma.model.genome.BaseQtlDao qtlDao ) {
        this.qtlDao = qtlDao;
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#thaw(ubic.gemma.model.genome.Gene)
     */
    public void thaw( final ubic.gemma.model.genome.Gene gene ) {
        try {
            this.handleThaw( gene );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.thaw(ubic.gemma.model.genome.Gene gene)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#thawLite(java.util.Collection)
     */
    public void thawLite( final java.util.Collection genes ) {
        try {
            this.handleThawLite( genes );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.thawLite(java.util.Collection genes)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.genome.gene.GeneService#update(ubic.gemma.model.genome.Gene)
     */
    public void update( final ubic.gemma.model.genome.Gene gene ) {
        try {
            this.handleUpdate( gene );
        } catch ( Throwable th ) {
            throw new ubic.gemma.model.genome.gene.GeneServiceException(
                    "Error performing 'ubic.gemma.model.genome.gene.GeneService.update(ubic.gemma.model.genome.Gene gene)' --> "
                            + th, th );
        }
    }

    /**
     * Gets the reference to <code>gene</code>'s DAO.
     */
    protected ubic.gemma.model.genome.GeneDao getGeneDao() {
        return this.geneDao;
    }

    /**
     * Gets the reference to <code>qtl</code>'s DAO.
     */
    protected ubic.gemma.model.genome.BaseQtlDao getQtlDao() {
        return this.qtlDao;
    }

    /**
     * Performs the core logic for {@link #countAll()}
     */
    protected abstract java.lang.Integer handleCountAll() throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #create(java.util.Collection)}
     */
    protected abstract java.util.Collection<Gene> handleCreate( java.util.Collection<Gene> genes )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #create(ubic.gemma.model.genome.Gene)}
     */
    protected abstract ubic.gemma.model.genome.Gene handleCreate( ubic.gemma.model.genome.Gene gene )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #find(ubic.gemma.model.genome.Gene)}
     */
    protected abstract ubic.gemma.model.genome.Gene handleFind( ubic.gemma.model.genome.Gene gene )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findAllQtlsByPhysicalMapLocation(ubic.gemma.model.genome.PhysicalLocation)}
     */
    protected abstract java.util.Collection<Qtl> handleFindAllQtlsByPhysicalMapLocation(
            ubic.gemma.model.genome.PhysicalLocation physicalMapLocation ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #findByAccession(java.lang.String, ubic.gemma.model.common.description.ExternalDatabase)}
     */
    protected abstract ubic.gemma.model.genome.Gene handleFindByAccession( java.lang.String accession,
            ubic.gemma.model.common.description.ExternalDatabase source ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByAlias(java.lang.String)}
     */
    protected abstract java.util.Collection<Gene> handleFindByAlias( java.lang.String search )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByNCBIId(java.lang.String)}
     */
    protected abstract ubic.gemma.model.genome.Gene handleFindByNCBIId( java.lang.String accession )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByOfficialName(java.lang.String)}
     */
    protected abstract java.util.Collection<Gene> handleFindByOfficialName( java.lang.String officialName )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByOfficialSymbol(java.lang.String)}
     */
    protected abstract java.util.Collection<Gene> handleFindByOfficialSymbol( java.lang.String officialSymbol )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByOfficialSymbol(java.lang.String, ubic.gemma.model.genome.Taxon)}
     */
    protected abstract ubic.gemma.model.genome.Gene handleFindByOfficialSymbol( java.lang.String symbol,
            ubic.gemma.model.genome.Taxon taxon ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByOfficialSymbolInexact(java.lang.String)}
     */
    protected abstract java.util.Collection<Gene> handleFindByOfficialSymbolInexact( java.lang.String officialSymbol )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findOrCreate(ubic.gemma.model.genome.Gene)}
     */
    protected abstract ubic.gemma.model.genome.Gene handleFindOrCreate( ubic.gemma.model.genome.Gene gene )
            throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getCoexpressedGenes(ubic.gemma.model.genome.Gene, java.util.Collection, java.lang.Integer, boolean)}
     */
    protected abstract Map<Gene, CoexpressionCollectionValueObject> handleGetCoexpressedGenes(
            Collection<ubic.gemma.model.genome.Gene> genes, java.util.Collection<? extends BioAssaySet> ees,
            java.lang.Integer stringency, boolean knownGenesOnly, boolean interGenesOnly ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getCoexpressedGenes(ubic.gemma.model.genome.Gene, java.util.Collection, java.lang.Integer, boolean)}
     */
    protected abstract CoexpressionCollectionValueObject handleGetCoexpressedGenes( ubic.gemma.model.genome.Gene gene,
            java.util.Collection<? extends BioAssaySet> ees, java.lang.Integer stringency, boolean knownGenesOnly )
            throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getCoexpressedKnownGenes(ubic.gemma.model.genome.Gene, java.util.Collection, java.lang.Integer)}
     */
    protected abstract java.util.Collection handleGetCoexpressedKnownGenes( ubic.gemma.model.genome.Gene gene,
            java.util.Collection ees, java.lang.Integer stringency ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getCompositeSequenceCountById(java.lang.Long)}
     */
    protected abstract long handleGetCompositeSequenceCountById( java.lang.Long id ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getCompositeSequences(ubic.gemma.model.genome.Gene, ubic.gemma.model.expression.arrayDesign.ArrayDesign)}
     */
    protected abstract java.util.Collection<CompositeSequence> handleGetCompositeSequences(
            ubic.gemma.model.genome.Gene gene, ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getCompositeSequencesById(java.lang.Long)}
     */
    protected abstract java.util.Collection<CompositeSequence> handleGetCompositeSequencesById( java.lang.Long id )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getGenesByTaxon(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection<Gene> handleGetGenesByTaxon( ubic.gemma.model.genome.Taxon taxon )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getMicroRnaByTaxon(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection handleGetMicroRnaByTaxon( ubic.gemma.model.genome.Taxon taxon )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #load(long)}
     */
    protected abstract ubic.gemma.model.genome.Gene handleLoad( long id ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadAll()}
     */
    protected abstract java.util.Collection<Gene> handleLoadAll() throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadKnownGenes(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection<Gene> handleLoadKnownGenes( ubic.gemma.model.genome.Taxon taxon )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadMultiple(java.util.Collection)}
     */
    protected abstract java.util.Collection<Gene> handleLoadMultiple( java.util.Collection<Long> ids )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadPredictedGenes(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection handleLoadPredictedGenes( ubic.gemma.model.genome.Taxon taxon )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadProbeAlignedRegions(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection<ProbeAlignedRegion> handleLoadProbeAlignedRegions(
            ubic.gemma.model.genome.Taxon taxon ) throws java.lang.Exception;

    /**
     */
    protected abstract void handleRemove( Gene gene ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #remove(java.util.Collection)}
     */
    protected abstract void handleRemove( java.util.Collection<Gene> genes ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #thaw(ubic.gemma.model.genome.Gene)}
     */
    protected abstract void handleThaw( ubic.gemma.model.genome.Gene gene ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #thawLite(java.util.Collection)}
     */
    protected abstract void handleThawLite( java.util.Collection<Gene> genes ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #update(ubic.gemma.model.genome.Gene)}
     */
    protected abstract void handleUpdate( ubic.gemma.model.genome.Gene gene ) throws java.lang.Exception;

}