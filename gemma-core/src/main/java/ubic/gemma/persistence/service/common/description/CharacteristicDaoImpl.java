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
package ubic.gemma.persistence.service.common.description;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ubic.basecode.util.BatchIterator;
import ubic.gemma.model.association.Gene2GOAssociationImpl;
import ubic.gemma.model.association.phenotype.PhenotypeAssociation;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.biomaterial.Treatment;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.phenotype.valueObject.CharacteristicValueObject;
import ubic.gemma.persistence.service.AbstractDao;
import ubic.gemma.persistence.service.AbstractVoEnabledDao;
import ubic.gemma.persistence.util.EntityUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Luke
 * @author Paul
 * @see    Characteristic
 */
@Repository
public class CharacteristicDaoImpl extends AbstractVoEnabledDao<Characteristic, CharacteristicValueObject>
        implements CharacteristicDao {

    private static final int BATCH_SIZE = 500;

    @Autowired
    public CharacteristicDaoImpl( SessionFactory sessionFactory ) {
        super( Characteristic.class, sessionFactory );
    }

    @Override
    public List<Characteristic> browse( Integer start, Integer limit ) {
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession()
                .createQuery( "from Characteristic where value not like 'GO_%'" ).setMaxResults( limit )
                .setFirstResult( start ).list();
    }

    @Override
    public List<Characteristic> browse( Integer start, Integer limit, String orderField, boolean descending ) {
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession().createQuery(
                        "from Characteristic where value not like 'GO_%' order by " + orderField + " " + ( descending ? "desc" : "" ) ).setMaxResults( limit )
                .setFirstResult( start ).list();
    }

    @Override
    public Collection<? extends Characteristic> findByCategory( String query ) {

        //noinspection unchecked
        return this.getSession()
                .createQuery( "select distinct char from Characteristic as char where char.category like :search" )
                .setParameter( "search", query + "%" ).list();
    }

    @Override
    public Collection<Characteristic> findByUri( Collection<Class<?>> classes, Collection<String> characteristicUris ) {

        Collection<Characteristic> result = new HashSet<>();

        if ( characteristicUris == null || characteristicUris.isEmpty() )
            return result;

        for ( Class<?> clazz : classes ) {
            String field = this.getCharacteristicFieldName( clazz );
            final String queryString = "select char from " + EntityUtils.getImplClass( clazz ).getSimpleName() + " as parent "
                    + " join parent." + field + " as char where char.valueUri in (:uriStrings) ";
            //noinspection unchecked
            result.addAll( this.getSessionFactory().getCurrentSession().createQuery( queryString )
                    .setParameterList( "uriStrings", characteristicUris ).list() );
        }

        return result;
    }

    @Override
    public LinkedHashMap<Class<? extends Identifiable>, Map<Characteristic, Set<ExpressionExperiment>>> findExperimentsByUris( Collection<String> characteristicUris, Taxon taxon ) {
        LinkedHashMap<Class<? extends Identifiable>, Map<Characteristic, Set<ExpressionExperiment>>> result = new LinkedHashMap<>();

        if ( characteristicUris.isEmpty() )
            return result;

        StopWatch timer = StopWatch.createStarted();

        // Note that the limit isn't strictly adhered to; we just stop querying when we have enough. We avoid duplicates
        Query q = this.getSessionFactory().getCurrentSession().createSQLQuery( "select {ee.*}, {c.*}, max(c.INVESTIGATION_FK) as eeId, max(c.EXPERIMENTAL_DESIGN_FK) as experimentalDesignId, max(c.EXPERIMENTAL_FACTOR_FK) as experimentalFactorId, max(c.FACTOR_VALUE_FK) as factorValueId, max(c.BIO_MATERIAL_FK) as bioMaterialId "
                        + "from INVESTIGATION as ee, CHARACTERISTIC as c "
                        + "where c.VALUE_URI in (:valueUris) "
                        // filter by taxon
                        + ( taxon != null ? "and ee.TAXON_FK = :taxonId " : "" )
                        // here goes the expensive clause...
                        // the non-null checks below are very important because they avoid expensive sub-select when it
                        // does not apply for the given characteristic
                        + "and ("
                        // directly
                        + "(c.INVESTIGATION_FK is not NULL and ee.ID = c.INVESTIGATION_FK) "
                        // via experimental design
                        + "or (c.EXPERIMENTAL_DESIGN_FK is not NULL and ee.EXPERIMENTAL_DESIGN_FK in (select ed.ID from EXPERIMENTAL_DESIGN ed where ed.ID = c.EXPERIMENTAL_DESIGN_FK)) "
                        // via experimental factor
                        + "or (c.EXPERIMENTAL_FACTOR_FK is not NULL and ee.EXPERIMENTAL_DESIGN_FK in (select ef.EXPERIMENTAL_DESIGN_FK from EXPERIMENTAL_FACTOR ef where ef.ID = c.EXPERIMENTAL_FACTOR_FK)) "
                        // via factor values (very expensive)
                        + "or (c.FACTOR_VALUE_FK is not NULL and ee.EXPERIMENTAL_DESIGN_FK in (select ef.EXPERIMENTAL_DESIGN_FK from EXPERIMENTAL_FACTOR ef join FACTOR_VALUE fv on fv.EXPERIMENTAL_FACTOR_FK = ef.ID where fv.ID = c.FACTOR_VALUE_FK)) "
                        // via biomaterial (a bit expensive)
                        + "or (c.BIO_MATERIAL_FK is not NULL and ee.ID in (select ba.EXPRESSION_EXPERIMENT_FK from BIO_ASSAY ba where ba.SAMPLE_USED_FK = c.BIO_MATERIAL_FK))) "
                        // ensure we have distinct EEs and characteristics
                        + "group by ee.ID, c.ID "
                        // NULLs are put last in descending order, so we get the priority we want (ee -> ed -> ef -> fv -> bm)
                        + "order by eeId desc, experimentalDesignId desc, experimentalFactorId desc, factorValueId desc, bioMaterialId desc" )
                .addEntity( "ee", ExpressionExperiment.class )
                .addEntity( "c", Characteristic.class )
                .addScalar( "eeId" )
                .addScalar( "experimentalDesignId" )
                .addScalar( "experimentalFactorId" )
                .addScalar( "factorValueId" )
                .addScalar( "bioMaterialId" )
                .setParameterList( "valueUris", characteristicUris );

        if ( taxon != null )
            q.setParameter( "taxonId", taxon.getId() );

        //noinspection unchecked
        List<Object[]> r = ( List<Object[]> ) q.list();
        for ( Object[] o : r ) {
            ExpressionExperiment ee = ( ExpressionExperiment ) o[0];
            Characteristic uri = ( Characteristic ) o[1];
            BigInteger eeId = ( BigInteger ) o[2];
            BigInteger experimentalDesignId = ( BigInteger ) o[3];
            BigInteger experimentalFactorId = ( BigInteger ) o[4];
            BigInteger factorValueId = ( BigInteger ) o[5];
            BigInteger bioMaterialId = ( BigInteger ) o[6];

            Class<? extends Identifiable> characteristicType;
            if ( eeId != null ) {
                characteristicType = ExpressionExperiment.class;
            } else if ( factorValueId != null ) {
                characteristicType = FactorValue.class;
            } else if ( bioMaterialId != null ) {
                characteristicType = BioMaterial.class;
            } else {
                throw new IllegalStateException( "Invalid row when retrieving EEs by characteristic URIs: the characteristic type could not be inferred." );
            }

            if ( !result.containsKey( characteristicType ) ) {
                result.put( characteristicType, new HashMap<>() );
            }

            Map<Characteristic, Set<ExpressionExperiment>> eesByUri = result.get( characteristicType );

            if ( !eesByUri.containsKey( uri ) ) {
                eesByUri.put( uri, new HashSet<>() );
            }

            eesByUri.get( uri ).add( ee );
        }

        timer.stop();

        if ( timer.getTime() > 20 ) {
            log.warn( "Retrieving " + r.size() + " EEs by characteristic URIs [" + String.join( ", ", characteristicUris ) + "] took " + timer.getTime() + " ms." );
        }

        return result;
    }

    @Override
    public Collection<Characteristic> findByUri( Collection<String> uris ) {
        int batchSize = 1000; // to avoid HQL parser barfing
        Collection<String> batch = new HashSet<>();
        Collection<Characteristic> results = new HashSet<>();
        if ( uris.isEmpty() )
            return results;

        //language=HQL
        final String queryString = "from Characteristic where valueUri in (:uris)";

        for ( String uri : uris ) {
            batch.add( uri );
            if ( batch.size() >= batchSize ) {
                results.addAll( this.getBatchList( batch, queryString ) );
                batch.clear();
            }
        }
        if ( batch.size() > 0 ) {
            results.addAll( this.getBatchList( batch, queryString ) );
        }
        return results;
    }

    @Override
    public Collection<Characteristic> findByUri( String searchString ) {
        if ( StringUtils.isBlank( searchString ) )
            return new HashSet<>();
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession()
                .createQuery( "select char from Characteristic as char where  char.valueUri = :search" )
                .setParameter( "search", searchString ).list();
    }

    @Override
    public Collection<Characteristic> findByValue( String search ) {
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession()
                .createQuery( "select char from Characteristic as char where char.value like :search " )
                .setParameter( "search", search.endsWith( "%" ) ? search : search + "%" ).list();
    }

    @Override
    public Map<Characteristic, Object> getParents( Class<?> parentClass, Collection<Characteristic> characteristics ) {

        Map<Characteristic, Object> charToParent = new HashMap<>();
        if ( characteristics == null || characteristics.size() == 0 ) {
            return charToParent;
        }
        if ( AbstractDao.log.isDebugEnabled() ) {
            Collection<String> uris = new HashSet<>();
            for ( Characteristic c : characteristics ) {

                if ( c.getValueUri() == null )
                    continue;
                uris.add( c.getValueUri() );

            }
            AbstractDao.log.debug( "For class=" + parentClass.getSimpleName() + ": " + characteristics.size()
                    + " Characteristics have URIS:\n" + StringUtils.join( uris, "\n" ) );
        }

        StopWatch timer = new StopWatch();
        timer.start();
        for ( Collection<Characteristic> batch : new BatchIterator<>( characteristics,
                CharacteristicDaoImpl.BATCH_SIZE ) ) {
            this.batchGetParents( parentClass, batch, charToParent );
        }

        if ( timer.getTime() > 1000 ) {
            AbstractDao.log
                    .info( "Fetch parents of characteristics: " + timer.getTime() + "ms for " + characteristics.size()
                            + " elements for class=" + parentClass.getSimpleName() );
        }

        return charToParent;
    }

    @Override
    public Map<Characteristic, Long> getParentIds( Class<?> parentClass, Collection<Characteristic> characteristics ) {

        Map<Characteristic, Long> charToParent = new HashMap<>();
        if ( characteristics == null || characteristics.size() == 0 ) {
            return charToParent;
        }
        if ( AbstractDao.log.isDebugEnabled() ) {
            Collection<String> uris = new HashSet<>();
            for ( Characteristic c : characteristics ) {

                if ( c.getValueUri() == null )
                    continue;
                uris.add( c.getValueUri() );

            }
            AbstractDao.log.debug( "For class=" + parentClass.getSimpleName() + ": " + characteristics.size()
                    + " Characteristics have URIS:\n" + StringUtils.join( uris, "\n" ) );
        }

        StopWatch timer = new StopWatch();
        timer.start();
        for ( Collection<Characteristic> batch : new BatchIterator<>( characteristics,
                CharacteristicDaoImpl.BATCH_SIZE ) ) {
            this.batchGetParentIds( parentClass, batch, charToParent );
        }

        if ( timer.getTime() > 1000 ) {
            AbstractDao.log
                    .info( "Fetch parents of characteristics: " + timer.getTime() + "ms for " + characteristics.size()
                            + " elements for class=" + parentClass.getSimpleName() );
        }

        return charToParent;

    }

    @Override
    public CharacteristicValueObject loadValueObject( Characteristic entity ) {
        return new CharacteristicValueObject( entity );
    }

    @SuppressWarnings("SameParameterValue") // Better for general use
    private Collection<Characteristic> getBatchList( Collection<String> batch, String queryString ) {
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession().createQuery( queryString ).setParameterList( "uris", batch )
                .list();
    }

    /*
     * Retrieve the objects that have these associated characteristics. Time-critical.
     */
    private void batchGetParents( Class<?> parentClass, Collection<Characteristic> characteristics,
            Map<Characteristic, Object> charToParent ) {
        if ( characteristics.isEmpty() )
            return;

        String field = this.getCharacteristicFieldName( parentClass );
        String queryString = "select parent, char from " + parentClass.getSimpleName() + " as parent " + " join parent." + field
                + " as char " + "where char in (:chars)";

        List<?> results = this.getSessionFactory().getCurrentSession().createQuery( queryString )
                .setParameterList( "chars", characteristics ).list();
        for ( Object o : results ) {
            Object[] row = ( Object[] ) o;
            charToParent.put( ( Characteristic ) row[1], row[0] );
        }
    }

    /*
     * Retrieve the objects that have these associated characteristics. Time-critical.
     */
    private void batchGetParentIds( Class<?> parentClass, Collection<Characteristic> characteristics,
            Map<Characteristic, Long> charToParent ) {
        if ( characteristics.isEmpty() )
            return;

        String field = this.getCharacteristicFieldName( parentClass );
        String queryString = "select parent.id, char from " + parentClass.getSimpleName() + " as parent " + " join parent." + field
                + " as char " + "where char in (:chars)";

        List<?> results = this.getSessionFactory().getCurrentSession().createQuery( queryString )
                .setParameterList( "chars", characteristics ).list();
        for ( Object o : results ) {
            Object[] row = ( Object[] ) o;
            charToParent.put( ( Characteristic ) row[1], ( Long ) row[0] );
        }
    }

    private String getCharacteristicFieldName( Class<?> parentClass ) {
        String field = "characteristics";
        if ( parentClass.isAssignableFrom( ExperimentalFactor.class ) )
            field = "category";
        else if ( parentClass.isAssignableFrom( Gene2GOAssociationImpl.class ) )
            field = "ontologyEntry";
        else if ( parentClass.isAssignableFrom( PhenotypeAssociation.class ) ) {
            field = "phenotypes";
        } else if ( parentClass.isAssignableFrom( Treatment.class ) ) {
            field = "action";
        } else if ( parentClass.isAssignableFrom( BioMaterial.class ) ) {
            field = "characteristics";
        }
        return field;
    }
}