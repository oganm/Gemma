/*
 * The Gemma project
 * 
 * Copyright (c) 2006 University of British Columbia
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
package ubic.gemma.persistence;

import java.util.concurrent.CancellationException;

import org.hibernate.FlushMode;
import org.springframework.beans.factory.annotation.Autowired;

import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.common.description.LocalFile;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.designElement.CompositeSequenceDao;
import ubic.gemma.model.genome.Taxon;

/**
 * This class handles persisting array designs. This is a bit of a special case, because ArrayDesigns are very large
 * (with associated reporters, CompositeSequences, and BioSequences), and also very likely to be submitted more than
 * once to the system. Therefore we want to take care not to get multiple slightly different copies of them, but we also
 * don't want to have to spend an inordinate amount of time checking a submitted version against the database.
 * <p>
 * The association between ArrayDesign and DesignElement is compositional - the lifecycle of a designelement is tied to
 * the arraydesign. However, designelements have associations with biosequence, which have their own lifecycle, in
 * general.
 * 
 * @author pavlidis
 * @version $Id$
 */
abstract public class ArrayDesignPersister extends GenomePersister {

    @Autowired
    protected ArrayDesignService arrayDesignService;

    @Autowired
    protected CompositeSequenceDao compositeSequenceDao;

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.loader.util.persister.Persister#persist(java.lang.Object)
     */
    @Override
    public Object persist( Object entity ) {
        Object result;

        if ( entity instanceof ArrayDesign ) {
            result = findOrPersistArrayDesign( ( ArrayDesign ) entity );
            return result;
        }

        return super.persist( entity );

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.CommonPersister#persistOrUpdate(java.lang.Object)
     */
    @Override
    public Object persistOrUpdate( Object entity ) {
        if ( entity == null ) return null;
        return super.persistOrUpdate( entity );
    }

    /**
     * Persist an array design.
     * 
     * @param arrayDesign
     */
    protected ArrayDesign findOrPersistArrayDesign( ArrayDesign arrayDesign ) {
        if ( arrayDesign == null ) return null;

        if ( !isTransient( arrayDesign ) ) return arrayDesign;

        /*
         * Note we don't do a full find here.
         */
        ArrayDesign existing = arrayDesignService.find( arrayDesign );

        if ( existing == null ) {

            /*
             * Try less stringent search.
             */
            existing = arrayDesignService.findByShortName( arrayDesign.getShortName() );

            if ( existing == null ) {
                log.info( arrayDesign + " is new, processing..." );
                return persistNewArrayDesign( arrayDesign );
            }

            log.info( "Array design exactly matching " + arrayDesign + " doesn't exist, but found " + existing
                    + "; returning" );

        } else {
            log.info( "Array Design " + arrayDesign + " already exists, returning..." );
        }

        return existing;

    }

    /**
     * Persist an entirely new array design, including composite sequences and any associated new sequences.
     * 
     * @param arrayDesign
     * @return
     */
    protected ArrayDesign persistNewArrayDesign( ArrayDesign arrayDesign ) {
        assert isTransient( arrayDesign );

        if ( arrayDesign == null ) return null;

        log.info( "Persisting new array design " + arrayDesign.getName() );

        try {
            this.getSession().setFlushMode( FlushMode.COMMIT );

            if ( arrayDesign.getDesignProvider() != null )
                arrayDesign.setDesignProvider( persistContact( arrayDesign.getDesignProvider() ) );

            if ( arrayDesign.getLocalFiles() != null ) {
                for ( LocalFile file : arrayDesign.getLocalFiles() ) {
                    file = persistLocalFile( file );
                }
            }

            if ( arrayDesign.getPrimaryTaxon() == null ) {
                throw new IllegalArgumentException( "Primary taxon cannot be null" );
            }

            arrayDesign.setPrimaryTaxon( ( Taxon ) persist( arrayDesign.getPrimaryTaxon() ) );

            for ( DatabaseEntry externalRef : arrayDesign.getExternalReferences() ) {
                externalRef.setExternalDatabase( persistExternalDatabase( externalRef.getExternalDatabase() ) );
            }

            log.info( "Persisting " + arrayDesign );

            arrayDesign = persistArrayDesignCompositeSequenceAssociations( arrayDesign );

            arrayDesign = arrayDesignService.create( arrayDesign );

            if ( Thread.currentThread().isInterrupted() ) {
                log.info( "Cancelled" );
                /*
                 * FIXME this shouldn't be necessary as this method now runs in a transaction.
                 */
                arrayDesignService.remove( arrayDesign );
                throw new CancellationException(
                        "Thread was terminated during the final stage of persisting the arraydesign. "
                                + this.getClass() );
            }
        } finally {
            this.getSession().setFlushMode( FlushMode.AUTO );
        }

        return arrayDesign;
    }

    /**
     * @param arrayDesign
     */
    private ArrayDesign persistArrayDesignCompositeSequenceAssociations( ArrayDesign arrayDesign ) {
        int numElements = arrayDesign.getCompositeSequences().size();
        if ( numElements == 0 ) return arrayDesign;
        log.info( "Filling in or updating sequences in composite seqences for " + arrayDesign );

        int persistedBioSequences = 0;

        assert arrayDesign.getId() == null;
        int numElementsPerUpdate = numElementsPerUpdate( arrayDesign.getCompositeSequences() );
        for ( CompositeSequence compositeSequence : arrayDesign.getCompositeSequences() ) {

            compositeSequence.setArrayDesign( arrayDesign );

            compositeSequence.setBiologicalCharacteristic( persistBioSequence( compositeSequence
                    .getBiologicalCharacteristic() ) );

            if ( ++persistedBioSequences % numElementsPerUpdate == 0 && numElements > 1000 ) {
                log.info( persistedBioSequences + "/" + numElements + " compositeSequence sequences examined for "
                        + arrayDesign );
            }

        }

        if ( persistedBioSequences > 0 ) {
            log.info( "Total of " + persistedBioSequences + " compositeSequence sequences examined for " + arrayDesign );
        }

        return arrayDesign;
    }

}
