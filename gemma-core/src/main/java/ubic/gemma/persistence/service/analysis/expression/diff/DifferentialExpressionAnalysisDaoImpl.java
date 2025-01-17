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
package ubic.gemma.persistence.service.analysis.expression.diff;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ubic.gemma.model.analysis.Investigation;
import ubic.gemma.model.analysis.expression.diff.ExpressionAnalysisResultSet;
import ubic.gemma.model.analysis.expression.diff.*;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.*;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.persistence.service.AbstractDao;
import ubic.gemma.persistence.service.analysis.AnalysisDaoBase;
import ubic.gemma.persistence.util.CommonQueries;
import ubic.gemma.persistence.util.EntityUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * @author paul
 * @see    DifferentialExpressionAnalysis
 */
@Repository
class DifferentialExpressionAnalysisDaoImpl extends AnalysisDaoBase<DifferentialExpressionAnalysis>
        implements DifferentialExpressionAnalysisDao {

    @Autowired
    public DifferentialExpressionAnalysisDaoImpl( SessionFactory sessionFactory ) {
        super( DifferentialExpressionAnalysis.class, sessionFactory );
    }

    @Override
    public Integer countDownregulated( ExpressionAnalysisResultSet par, double threshold ) {
        String query = "select count(distinct r) from ExpressionAnalysisResultSet rs inner join rs.results r "
                + "join r.contrasts c where rs = :rs and r.correctedPvalue < :threshold and c.tstat < 0";

        String[] paramNames = { "rs", "threshold" };
        Object[] objectValues = { par, threshold };

        List<?> qresult = this.getHibernateTemplate().findByNamedParam( query, paramNames, objectValues );

        if ( qresult.isEmpty() ) {
            AbstractDao.log.warn( "No count returned" );
            return 0;
        }
        Long count = ( Long ) qresult.iterator().next();

        AbstractDao.log.debug( "Found " + count + " downregulated genes in result set (" + par.getId()
                + ") at a corrected pvalue threshold of " + threshold );

        return count.intValue();
    }

    @Override
    public Integer countProbesMeetingThreshold( ExpressionAnalysisResultSet ears, double threshold ) {

        String query = "select count(distinct r) from ExpressionAnalysisResultSet rs inner join rs.results r where rs = :rs and r.correctedPvalue < :threshold";

        String[] paramNames = { "rs", "threshold" };
        Object[] objectValues = { ears, threshold };

        List<?> qresult = this.getHibernateTemplate().findByNamedParam( query, paramNames, objectValues );

        if ( qresult.isEmpty() ) {
            AbstractDao.log.warn( "No count returned" );
            return 0;
        }
        Long count = ( Long ) qresult.iterator().next();

        AbstractDao.log.debug( "Found " + count + " differentially expressed genes in result set (" + ears.getId()
                + ") at a corrected pvalue threshold of " + threshold );

        return count.intValue();
    }

    @Override
    public Integer countUpregulated( ExpressionAnalysisResultSet par, double threshold ) {
        String query = "select count(distinct r) from ExpressionAnalysisResultSet rs inner join rs.results r"
                + " join r.contrasts c where rs = :rs and r.correctedPvalue < :threshold and c.tstat > 0";

        String[] paramNames = { "rs", "threshold" };
        Object[] objectValues = { par, threshold };

        List<?> qresult = this.getHibernateTemplate().findByNamedParam( query, paramNames, objectValues );

        if ( qresult.isEmpty() ) {
            AbstractDao.log.warn( "No count returned" );
            return 0;
        }
        Long count = ( Long ) qresult.iterator().next();

        AbstractDao.log.debug( "Found " + count + " upregulated genes in result set (" + par.getId()
                + ") at a corrected pvalue threshold of " + threshold );

        return count.intValue();
    }

    @Override
    public Collection<DifferentialExpressionAnalysis> find( Gene gene, ExpressionAnalysisResultSet resultSet,
            double threshold ) {
        final String findByResultSet = "select distinct r from DifferentialExpressionAnalysis a"
                + "   inner join a.experimentAnalyzed e inner join e.bioAssays ba inner join ba.arrayDesignUsed ad"
                + " inner join ad.compositeSequences cs inner join cs.biologicalCharacteristic bs inner join "
                + "bs.bioSequence2GeneProduct bs2gp inner join bs2gp.geneProduct gp inner join gp.gene g"
                + " inner join a.resultSets rs inner join rs.results r where r.probe=cs and g=:gene and rs=:resultSet"
                + " and r.correctedPvalue < :threshold";

        String[] paramNames = { "gene", "resultSet", "threshold" };
        Object[] objectValues = { gene, resultSet, threshold };

        //noinspection unchecked
        return this.getHibernateTemplate().findByNamedParam( findByResultSet, paramNames, objectValues );
    }

    @Override
    public Collection<DifferentialExpressionAnalysis> findByFactor( ExperimentalFactor ef ) {

        // subset factorValues factors.
        @SuppressWarnings("unchecked")
        Collection<DifferentialExpressionAnalysis> result = this.getHibernateTemplate()
                .findByNamedParam(
                        "select distinct a from DifferentialExpressionAnalysis a join a.subsetFactorValue ssf"
                                + " join ssf.experimentalFactor efa where efa = :ef ",
                        "ef", ef );

        // factors used in the analysis.
        //noinspection unchecked
        result.addAll( this.getHibernateTemplate().findByNamedParam(
                "select distinct a from DifferentialExpressionAnalysis a join a.resultSets rs"
                        + " left join rs.baselineGroup bg join rs.experimentalFactors efa where efa = :ef ",
                "ef",
                ef ) );

        return result;
    }

    @Override
    public Map<Long, Collection<DifferentialExpressionAnalysis>> findByInvestigationIds(
            Collection<Long> investigationIds ) {

        Map<Long, Collection<DifferentialExpressionAnalysis>> results = new HashMap<>();
        //language=HQL
        final String queryString = "select distinct e, a from DifferentialExpressionAnalysis a"
                + "   inner join a.experimentAnalyzed e where e.id in (:eeIds)";
        List<?> qresult = this.getHibernateTemplate().findByNamedParam( queryString, "eeIds", investigationIds );
        for ( Object o : qresult ) {
            Object[] oa = ( Object[] ) o;
            BioAssaySet bas = ( BioAssaySet ) oa[0];
            DifferentialExpressionAnalysis dea = ( DifferentialExpressionAnalysis ) oa[1];
            Long id = bas.getId();
            if ( !results.containsKey( id ) ) {
                results.put( id, new HashSet<DifferentialExpressionAnalysis>() );
            }
            results.get( id ).add( dea );
        }
        return results;
    }

    @Override
    public Collection<BioAssaySet> findExperimentsWithAnalyses( Gene gene ) {

        StopWatch timer = new StopWatch();
        timer.start();

        Collection<CompositeSequence> probes = CommonQueries
                .getCompositeSequences( gene, this.getSessionFactory().getCurrentSession() );
        Collection<BioAssaySet> result = new HashSet<>();
        if ( probes.size() == 0 ) {
            return result;
        }

        if ( timer.getTime() > 1000 ) {
            AbstractDao.log.info( "Find probes: " + timer.getTime() + " ms" );
        }
        timer.reset();
        timer.start();

        /*
         * Note: this query misses ExpressionExperimentSubSets. The native query was implemented because HQL was always
         * constructing a constraint on SubSets. See bug 2173.
         */
        final String queryToUse = "select e.ID from ANALYSIS a inner join INVESTIGATION e ON a.EXPERIMENT_ANALYZED_FK = e.ID "
                + "inner join BIO_ASSAY ba ON ba.EXPRESSION_EXPERIMENT_FK=e.ID "
                + " inner join BIO_MATERIAL bm ON bm.ID=ba.SAMPLE_USED_FK inner join TAXON t ON bm.SOURCE_TAXON_FK=t.ID "
                + " inner join COMPOSITE_SEQUENCE cs ON ba.ARRAY_DESIGN_USED_FK =cs.ARRAY_DESIGN_FK where cs.ID in "
                + " (:probes) and t.ID = :taxon";

        Taxon taxon = gene.getTaxon();

        int batchSize = 1000;
        Collection<CompositeSequence> batch = new HashSet<>();
        for ( CompositeSequence probe : probes ) {
            batch.add( probe );

            if ( batch.size() == batchSize ) {
                this.fetchExperimentsTestingGeneNativeQuery( batch, result, queryToUse, taxon );
                batch.clear();
            }
        }

        if ( !batch.isEmpty() ) {
            this.fetchExperimentsTestingGeneNativeQuery( batch, result, queryToUse, taxon );
        }

        if ( timer.getTime() > 1000 ) {
            AbstractDao.log.info( "Find experiments: " + timer.getTime() + " ms" );
        }

        return result;
    }

    @Override
    public Map<ExpressionExperiment, Collection<DifferentialExpressionAnalysis>> getAnalyses(
            Collection<? extends BioAssaySet> experiments ) {
        Map<ExpressionExperiment, Collection<DifferentialExpressionAnalysis>> result = new HashMap<>();

        StopWatch timer = new StopWatch();
        timer.start();
        final String query = "select distinct a from DifferentialExpressionAnalysis a inner join fetch a.resultSets res "
                + " inner join fetch res.baselineGroup"
                + " inner join fetch res.experimentalFactors facs inner join fetch facs.factorValues "
                + " inner join fetch res.hitListSizes where a.experimentAnalyzed.id in (:ees) ";

        //noinspection unchecked
        List<DifferentialExpressionAnalysis> r1 = this.getHibernateTemplate()
                .findByNamedParam( query, "ees", EntityUtils.getIds( experiments ) );
        int count = 0;
        for ( DifferentialExpressionAnalysis a : r1 ) {
            //noinspection SuspiciousMethodCalls // Ignoring subsets
            if ( !result.containsKey( a.getExperimentAnalyzed() ) ) {
                result.put( ( ExpressionExperiment ) a.getExperimentAnalyzed(),
                        new HashSet<DifferentialExpressionAnalysis>() );
            }
            //noinspection SuspiciousMethodCalls // Ignoring subsets
            result.get( a.getExperimentAnalyzed() ).add( a );
            count++;
        }
        if ( timer.getTime() > 1000 ) {
            AbstractDao.log
                    .info( "Fetch " + count + " analyses for " + result.size() + " experiments: " + timer.getTime()
                            + "ms; Query was:\n" + query );
        }
        timer.reset();
        timer.start();

        /*
         * Deal with the analyses of subsets of the experiments given being analyzed; but we keep things organized by
         * the source experiment. Maybe that is confusing.
         */
        String q2 = "select distinct a from ExpressionExperimentSubSet eess, DifferentialExpressionAnalysis a "
                + " inner join fetch a.resultSets res inner join fetch res.baselineGroup "
                + " inner join fetch res.experimentalFactors facs inner join fetch facs.factorValues"
                + " inner join fetch res.hitListSizes  "
                + " join eess.sourceExperiment see join a.experimentAnalyzed ee  where eess=ee and see.id in (:ees) ";
        //noinspection unchecked
        List<DifferentialExpressionAnalysis> r2 = this.getHibernateTemplate()
                .findByNamedParam( q2, "ees", EntityUtils.getIds( experiments ) );

        if ( !r2.isEmpty() ) {
            count = 0;
            for ( DifferentialExpressionAnalysis a : r2 ) {
                BioAssaySet experimentAnalyzed = a.getExperimentAnalyzed();

                assert experimentAnalyzed instanceof ExpressionExperimentSubSet;

                ExpressionExperiment sourceExperiment = ( ( ExpressionExperimentSubSet ) experimentAnalyzed )
                        .getSourceExperiment();

                if ( !result.containsKey( sourceExperiment ) ) {
                    result.put( sourceExperiment, new HashSet<DifferentialExpressionAnalysis>() );
                }

                result.get( sourceExperiment ).add( a );
                count++;
            }
            if ( timer.getTime() > 1000 ) {
                AbstractDao.log
                        .info( "Fetch " + count + " subset analyses for " + result.size() + " experiment subsets: "
                                + timer.getTime() + "ms" );
                AbstractDao.log.debug( "Query for subsets was: " + q2 );
            }
        }

        return result;

    }

    @Override
    public Collection<Long> getExperimentsWithAnalysis( Collection<Long> idsToFilter ) {
        //language=HQL
        final String queryString = "select distinct e.id from DifferentialExpressionAnalysis a"
                + " inner join a.experimentAnalyzed e where e.id in (:eeIds)";
        //noinspection unchecked
        return this.getHibernateTemplate().findByNamedParam( queryString, "eeIds", idsToFilter );
    }

    @Override
    public Collection<Long> getExperimentsWithAnalysis( Taxon taxon ) {
        //language=HQL
        final String queryString = "select distinct ee.id from DifferentialExpressionAnalysis"
                + " as doa inner join doa.experimentAnalyzed as ee " + "inner join ee.bioAssays as ba "
                + "inner join ba.sampleUsed as sample where sample.sourceTaxon = :taxon ";
        //noinspection unchecked
        return this.getHibernateTemplate().findByNamedParam( queryString, "taxon", taxon );
    }

    @Override
    public void thaw( final Collection<DifferentialExpressionAnalysis> expressionAnalyses ) {
        for ( DifferentialExpressionAnalysis ea : expressionAnalyses ) {
            this.thaw( ea );
        }
    }

    @Override
    public void thaw( DifferentialExpressionAnalysis differentialExpressionAnalysis ) {
        StopWatch timer = new StopWatch();
        timer.start();

        Session session = this.getSessionFactory().getCurrentSession();
        session.flush();
        session.clear();

        session.buildLockRequest( LockOptions.NONE ).lock( differentialExpressionAnalysis );
        Hibernate.initialize( differentialExpressionAnalysis );
        Hibernate.initialize( differentialExpressionAnalysis.getExperimentAnalyzed() );
        session.buildLockRequest( LockOptions.NONE ).lock( differentialExpressionAnalysis.getExperimentAnalyzed() );
        Hibernate.initialize( differentialExpressionAnalysis.getExperimentAnalyzed().getBioAssays() );

        Hibernate.initialize( differentialExpressionAnalysis.getProtocol() );

        if ( differentialExpressionAnalysis.getSubsetFactorValue() != null ) {
            Hibernate.initialize( differentialExpressionAnalysis.getSubsetFactorValue() );
        }

        Collection<ExpressionAnalysisResultSet> ears = differentialExpressionAnalysis.getResultSets();
        Hibernate.initialize( ears );
        for ( ExpressionAnalysisResultSet ear : ears ) {
            session.buildLockRequest( LockOptions.NONE ).lock( ear );
            Hibernate.initialize( ear );
            Hibernate.initialize( ear.getExperimentalFactors() );
        }
        if ( timer.getTime() > 1000 ) {
            AbstractDao.log.info( "Thaw: " + timer.getTime() + "ms" );
        }
    }

    @Override
    public void thawResultSets( DifferentialExpressionAnalysis dea ) {
        Hibernate.initialize( dea.getResultSets() );
    }

    @Override
    public Map<Long, List<DifferentialExpressionAnalysisValueObject>> getAnalysesByExperimentIds(
            Collection<Long> expressionExperimentIds, int offset, int limit ) {

        /*
         * There are three cases to consider: the ids are experiments; the ids are experiment subsets; the ids are
         * experiments that have subsets.
         */
        Map<Long, List<DifferentialExpressionAnalysisValueObject>> r = new HashMap<>();

        Map<Long, Collection<Long>> arrayDesignsUsed = CommonQueries
                .getArrayDesignsUsedEEMap( expressionExperimentIds, this.getSessionFactory().getCurrentSession() );

        /*
         * Fetch analyses of experiments or subsets.
         */
        //noinspection unchecked
        Collection<DifferentialExpressionAnalysis> hits = this.getSessionFactory().getCurrentSession().createQuery(
                        "select distinct a from DifferentialExpressionAnalysis a "
                                + "join fetch a.experimentAnalyzed e "
                                + "where e.id in (:eeIds)" )
                .setParameterList( "eeIds", expressionExperimentIds )
                .setFirstResult( offset )
                .setMaxResults( limit > 0 ? limit : -1 )
                .list();

        // initialize result sets and hit list sizes
        // this is necessary because the DEA VO constructor will ignore uninitialized associations
        for ( DifferentialExpressionAnalysis hit : hits ) {
            Hibernate.initialize( hit.getResultSets() );
            for ( ExpressionAnalysisResultSet rs : hit.getResultSets() ) {
                Hibernate.initialize( rs.getHitListSizes() );
            }
        }

        Map<Long, Collection<FactorValue>> ee2fv = new HashMap<>();
        List<Object[]> fvs;

        if ( !hits.isEmpty() ) {
            // factor values for the experiments.
            //noinspection unchecked
            fvs = this.getSessionFactory().getCurrentSession().createQuery(
                    "select distinct ee.id, fv from " + "ExpressionExperiment"
                            + " ee join ee.bioAssays ba join ba.sampleUsed bm join bm.factorValues fv where ee.id in (:ees)" )
                    .setParameterList( "ees", expressionExperimentIds ).list();
            this.addFactorValues( ee2fv, fvs );

            // also get factor values for subsets - those not found yet.
            Collection<Long> used = new HashSet<>();
            for ( DifferentialExpressionAnalysis a : hits ) {
                used.add( a.getExperimentAnalyzed().getId() );
            }

            List probableSubSetIds = ListUtils.removeAll( used, ee2fv.keySet() );
            if ( !probableSubSetIds.isEmpty() ) {
                //noinspection unchecked
                fvs = this.getSessionFactory().getCurrentSession().createQuery(
                        "select distinct ee.id, fv from " + "ExpressionExperimentSubSet"
                                + " ee join ee.bioAssays ba join ba.sampleUsed bm join bm.factorValues fv where ee.id in (:ees)" )
                        .setParameterList( "ees", probableSubSetIds ).list();
                this.addFactorValues( ee2fv, fvs );
            }

        }

        /*
         * Subsets of those same experiments (there might not be any)
         */
        //noinspection unchecked
        List<DifferentialExpressionAnalysis> analysesOfSubsets = this.getSessionFactory().getCurrentSession()
                .createQuery( "select distinct a from " + "ExpressionExperimentSubSet"
                        + " ee, DifferentialExpressionAnalysis a" + " join ee.sourceExperiment see "
                        + " join fetch a.experimentAnalyzed eeanalyzed where see.id in (:eeids) and ee=eeanalyzed" )
                .setParameterList( "eeids", expressionExperimentIds ).list();

        if ( !analysesOfSubsets.isEmpty() ) {
            hits.addAll( analysesOfSubsets );

            Collection<Long> experimentSubsetIds = new HashSet<>();
            for ( DifferentialExpressionAnalysis a : analysesOfSubsets ) {
                ExpressionExperimentSubSet subset = ( ExpressionExperimentSubSet ) a.getExperimentAnalyzed();
                experimentSubsetIds.add( subset.getId() );
            }

            // factor value information for the subset. The key output is the ID of the subset, not of the source
            // experiment.
            //noinspection unchecked
            fvs = this.getSessionFactory().getCurrentSession().createQuery(
                    "select distinct ee.id, fv from " + "ExpressionExperimentSubSet"
                            + " ee join ee.bioAssays ba join ba.sampleUsed bm join bm.factorValues fv where ee.id in (:ees)" )
                    .setParameterList( "ees", experimentSubsetIds ).list();
            this.addFactorValues( ee2fv, fvs );
        }

        // postprocesss...
        if ( hits.isEmpty() ) {
            return r;
        }
        Collection<DifferentialExpressionAnalysisValueObject> summaries = this
                .convertToValueObjects( hits, arrayDesignsUsed, ee2fv );

        for ( DifferentialExpressionAnalysisValueObject an : summaries ) {

            Long bioAssaySetId;
            if ( an.getSourceExperiment() != null ) {
                bioAssaySetId = an.getSourceExperiment();
            } else {
                bioAssaySetId = an.getBioAssaySetId();
            }
            if ( !r.containsKey( bioAssaySetId ) ) {
                r.put( bioAssaySetId, new ArrayList<DifferentialExpressionAnalysisValueObject>() );
            }
            r.get( bioAssaySetId ).add( an );
        }

        return r;

    }

    @Override
    public void remove( DifferentialExpressionAnalysis analysis ) {
        if ( analysis == null ) {
            throw new IllegalArgumentException( "analysis cannot be null" );
        }

        Session session = this.getSessionFactory().getCurrentSession();

        super.remove( ( DifferentialExpressionAnalysis ) session.load( DifferentialExpressionAnalysis.class, analysis.getId() ) );

        session.flush();
        session.clear();
    }

    @Override
    public Collection<DifferentialExpressionAnalysis> findByInvestigation( Investigation investigation ) {
        Long id = investigation.getId();
        Collection<DifferentialExpressionAnalysis> results = new HashSet<>();

        //noinspection unchecked
        results.addAll( this.getSessionFactory().getCurrentSession().createQuery(
                        "select distinct a from DifferentialExpressionAnalysis a where a.experimentAnalyzed.id=:eeid" )
                .setParameter( "eeid", id ).list() );

        /*
         * Deal with the analyses of subsets of the investigation. User has to know this is possible.
         */
        //noinspection unchecked
        results.addAll( this.getSessionFactory().getCurrentSession().createQuery(
                "select distinct a from ExpressionExperimentSubSet eess, DifferentialExpressionAnalysis a "
                        + "join eess.sourceExperiment see "
                        + "join a.experimentAnalyzed eeanalyzed where see.id=:eeid and eess=eeanalyzed" )
                .setParameter( "eeid", id ).list() );

        return results;
    }

    private void addFactorValues( Map<Long, Collection<FactorValue>> ee2fv, List<Object[]> fvs ) {
        for ( Object[] oa : fvs ) {
            Long eeId = ( Long ) oa[0];
            if ( !ee2fv.containsKey( eeId ) ) {
                ee2fv.put( eeId, new HashSet<FactorValue>() );
            }
            ee2fv.get( eeId ).add( ( FactorValue ) oa[1] );
        }
    }

    private Collection<DifferentialExpressionAnalysisValueObject> convertToValueObjects(
            Collection<DifferentialExpressionAnalysis> analyses, Map<Long, Collection<Long>> arrayDesignsUsed,
            Map<Long, Collection<FactorValue>> ee2fv ) {
        Collection<DifferentialExpressionAnalysisValueObject> summaries = new HashSet<>();

        for ( DifferentialExpressionAnalysis analysis : analyses ) {

            Collection<ExpressionAnalysisResultSet> results = analysis.getResultSets();

            DifferentialExpressionAnalysisValueObject avo = new DifferentialExpressionAnalysisValueObject( analysis );

            BioAssaySet bioAssaySet = analysis.getExperimentAnalyzed();

            avo.setBioAssaySetId( bioAssaySet.getId() ); // might be a subset.

            if ( analysis.getSubsetFactorValue() != null ) {
                avo.setSubsetFactorValue( new FactorValueValueObject( analysis.getSubsetFactorValue() ) );
                avo.setSubsetFactor(
                        new ExperimentalFactorValueObject( analysis.getSubsetFactorValue().getExperimentalFactor() ) );
                assert bioAssaySet instanceof ExpressionExperimentSubSet;
                avo.setSourceExperiment( ( ( ExpressionExperimentSubSet ) bioAssaySet ).getSourceExperiment().getId() );
                if ( arrayDesignsUsed.containsKey( bioAssaySet.getId() ) ) {
                    avo.setArrayDesignsUsed( arrayDesignsUsed.get( bioAssaySet.getId() ) );
                } else {
                    assert arrayDesignsUsed.containsKey( avo.getSourceExperiment() );
                    avo.setArrayDesignsUsed( arrayDesignsUsed.get( avo.getSourceExperiment() ) );
                }
            } else {
                Collection<Long> adids = arrayDesignsUsed.get( bioAssaySet.getId() );
                avo.setArrayDesignsUsed( adids );
            }

            for ( ExpressionAnalysisResultSet resultSet : results ) {
                DiffExResultSetSummaryValueObject desvo = new DiffExResultSetSummaryValueObject( resultSet );
                desvo.setArrayDesignsUsed( avo.getArrayDesignsUsed() );
                desvo.setBioAssaySetAnalyzedId( bioAssaySet.getId() ); // might be a subset.
                desvo.setAnalysisId( analysis.getId() );
                avo.getResultSets().add( desvo );
                assert ee2fv.containsKey( bioAssaySet.getId() );
                this.populateWhichFactorValuesUsed( avo, ee2fv.get( bioAssaySet.getId() ) );
            }

            summaries.add( avo );
        }
        return summaries;
    }

    private void fetchExperimentsTestingGeneNativeQuery( Collection<CompositeSequence> probes,
            Collection<BioAssaySet> result, final String nativeQuery, Taxon taxon ) {

        if ( probes.isEmpty() )
            return;

        SQLQuery nativeQ = this.getSessionFactory().getCurrentSession().createSQLQuery( nativeQuery );
        nativeQ.setParameterList( "probes", EntityUtils.getIds( probes ) );
        nativeQ.setParameter( "taxon", taxon );
        List<?> list = nativeQ.list();
        Set<Long> ids = new HashSet<>();
        for ( Object o : list ) {
            ids.add( ( ( BigInteger ) o ).longValue() );
        }
        if ( !ids.isEmpty() ) {
            //noinspection unchecked
            result.addAll( this.getHibernateTemplate()
                    .findByNamedParam( "from ExpressionExperiment e where e.id in (:ids)", "ids", ids ) );
        }
    }

    /**
     * Figure out which factorValues were used for each of the experimental factors (excluding the subset factor)
     */
    private void populateWhichFactorValuesUsed( DifferentialExpressionAnalysisValueObject avo,
            Collection<FactorValue> fvs ) {
        if ( fvs == null || fvs.isEmpty() ) {
            return;
        }
        ExperimentalFactorValueObject subsetFactor = avo.getSubsetFactor();

        for ( FactorValue fv : fvs ) {

            Long experimentalFactorId = fv.getExperimentalFactor().getId();

            if ( subsetFactor != null && experimentalFactorId.equals( subsetFactor.getId() ) ) {
                continue;
            }

            if ( !avo.getFactorValuesUsed().containsKey( experimentalFactorId ) ) {
                avo.getFactorValuesUsed().put( experimentalFactorId, new HashSet<FactorValueValueObject>() );
            }

            avo.getFactorValuesUsed().get( experimentalFactorId ).add( new FactorValueValueObject( fv ) );

        }
    }
}