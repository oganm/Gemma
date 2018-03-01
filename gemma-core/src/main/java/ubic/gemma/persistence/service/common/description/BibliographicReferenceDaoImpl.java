/*
 * The Gemma project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.persistence.service.common.description;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ubic.basecode.util.BatchIterator;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.description.BibliographicReferenceValueObject;
import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.persistence.service.AbstractVoEnabledDao;
import ubic.gemma.persistence.util.BusinessKey;
import ubic.gemma.persistence.util.EntityUtils;

import java.util.*;

/**
 * @author pavlidis
 * @see BibliographicReference
 */
@Repository
public class BibliographicReferenceDaoImpl
        extends AbstractVoEnabledDao<BibliographicReference, BibliographicReferenceValueObject>
        implements BibliographicReferenceDao {

    @Autowired
    public BibliographicReferenceDaoImpl( SessionFactory sessionFactory ) {
        super( BibliographicReference.class, sessionFactory );
    }

    @Override
    public BibliographicReference findByExternalId( final String id, final String databaseName ) {
        //noinspection unchecked
        return ( BibliographicReference ) this.getSessionFactory().getCurrentSession().createQuery(
                "from BibliographicReference b where b.pubAccession.accession=:id AND b.pubAccession.externalDatabase.name=:databaseName" )
                .setParameter( "id", id ).setParameter( "databaseName", databaseName ).uniqueResult();
    }

    @Override
    public BibliographicReference findByExternalId( final DatabaseEntry externalId ) {
        //noinspection unchecked
        return ( BibliographicReference ) this.getSessionFactory().getCurrentSession()
                .createQuery( "from BibliographicReference b where b.pubAccession=:externalId" )
                .setParameter( "externalId", externalId ).uniqueResult();
    }

    @Override
    public Map<ExpressionExperiment, BibliographicReference> getAllExperimentLinkedReferences() {
        final String query = "select distinct e, b from ExpressionExperiment e join e.primaryPublication b left join fetch b.pubAccession left join fetch b.publicationTypes ";
        Map<ExpressionExperiment, BibliographicReference> result = new HashMap<>();
        //noinspection unchecked
        List<Object[]> os = this.getSessionFactory().getCurrentSession().createQuery( query ).list();
        for ( Object[] o : os ) {
            result.put( ( ExpressionExperiment ) o[0], ( BibliographicReference ) o[1] );
        }
        return result;
    }

    @Override
    public BibliographicReference thaw( BibliographicReference bibliographicReference ) {
        if ( bibliographicReference == null || bibliographicReference.getId() == null )
            return bibliographicReference;
        return ( BibliographicReference ) this.getSessionFactory().getCurrentSession().createQuery(
                "select b from BibliographicReference b left join fetch b.pubAccession left join fetch b.chemicals "
                        + "left join fetch b.meshTerms left join fetch b.keywords left join fetch b.publicationTypes where b.id = :id " )
                .setParameter( "id", bibliographicReference.getId() ).uniqueResult();
    }

    @Override
    public Collection<BibliographicReference> thaw( Collection<BibliographicReference> bibliographicReferences ) {
        if ( bibliographicReferences.isEmpty() )
            return bibliographicReferences;
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession().createQuery(
                "select b from BibliographicReference b left join fetch b.pubAccession left join fetch b.chemicals "
                        + "left join fetch b.meshTerms left join fetch b.keywords left join fetch b.publicationTypes where b.id in (:ids) " )
                .setParameterList( "ids", EntityUtils.getIds( bibliographicReferences ) ).list();
    }

    @Override
    public Map<BibliographicReference, Collection<ExpressionExperiment>> getRelatedExperiments(
            Collection<BibliographicReference> records ) {
        final String query = "select distinct e, b from ExpressionExperiment "
                + "e join e.primaryPublication b left join fetch b.pubAccession where b in (:recs)";

        Map<BibliographicReference, Collection<ExpressionExperiment>> result = new HashMap<>();

        for ( Collection<BibliographicReference> batch : BatchIterator.batches( records, 200 ) ) {
            //noinspection unchecked
            List<Object[]> os = this.getSessionFactory().getCurrentSession().createQuery( query )
                    .setParameterList( "recs", batch ).list();
            for ( Object[] o : os ) {
                ExpressionExperiment e = ( ExpressionExperiment ) o[0];
                BibliographicReference b = ( BibliographicReference ) o[1];
                if ( !result.containsKey( b ) ) {
                    result.put( b, new HashSet<ExpressionExperiment>() );
                }
                result.get( b ).add( e );
            }
        }
        return result;
    }

    @Override
    public Collection<Long> listAll() {
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession().createQuery( "select id from BibliographicReference" )
                .list();
    }

    @Override
    public List<BibliographicReference> browse( Integer start, Integer limit ) {
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession().createQuery( "from BibliographicReference" )
                .setMaxResults( limit ).setFirstResult( start ).list();
    }

    @Override
    public List<BibliographicReference> browse( Integer start, Integer limit, String orderField, boolean descending ) {
        //noinspection unchecked
        return this.getSessionFactory().getCurrentSession()
                .createQuery( "from BibliographicReference order by :orderField " + ( descending ? "desc" : "" ) )
                .setMaxResults( limit ).setFirstResult( start ).setParameter( "orderField", orderField ).list();
    }

    @Override
    public BibliographicReference find( BibliographicReference bibliographicReference ) {

        BusinessKey.checkKey( bibliographicReference );
        Criteria queryObject = this.getSessionFactory().getCurrentSession()
                .createCriteria( BibliographicReference.class );

        /*
         * This syntax allows you to look at an association.
         */
        if ( bibliographicReference.getPubAccession() != null ) {
            queryObject.createCriteria( "pubAccession" )
                    .add( Restrictions.eq( "accession", bibliographicReference.getPubAccession().getAccession() ) );
        } else {
            throw new NullPointerException( "PubAccession cannot be null" );
        }

        java.util.List<?> results = queryObject.list();
        Object result = null;
        if ( results != null ) {
            if ( results.size() > 1 ) {
                throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                        "More than one instance of '" + BibliographicReference.class.getName() + "' with accession "
                                + bibliographicReference.getPubAccession().getAccession()
                                + " was found when executing query" );

            } else if ( results.size() == 1 ) {
                result = results.iterator().next();
            }
        }
        return ( BibliographicReference ) result;

    }

    @Override
    public BibliographicReferenceValueObject loadValueObject( BibliographicReference entity ) {
        return new BibliographicReferenceValueObject( entity );
    }

    @Override
    public Collection<BibliographicReferenceValueObject> loadValueObjects(
            Collection<BibliographicReference> entities ) {
        Collection<BibliographicReferenceValueObject> vos = new LinkedHashSet<>();
        for ( BibliographicReference e : entities ) {
            vos.add( this.loadValueObject( e ) );
        }
        return vos;
    }

}