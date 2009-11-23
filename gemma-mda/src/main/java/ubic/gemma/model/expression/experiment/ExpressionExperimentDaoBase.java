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
package ubic.gemma.model.expression.experiment;

import java.util.Map;

import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.designElement.DesignElement;
import ubic.gemma.model.genome.Taxon;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>ubic.gemma.model.expression.experiment.ExpressionExperiment</code>.
 * </p>
 * 
 * @see ubic.gemma.model.expression.experiment.ExpressionExperiment
 * @version $Id$
 * @author paul based on generated code
 */
public abstract class ExpressionExperimentDaoBase extends
        ubic.gemma.model.expression.experiment.BioAssaySetDaoImpl<ExpressionExperiment> implements
        ubic.gemma.model.expression.experiment.ExpressionExperimentDao {

    /**
     * This anonymous transformer is designed to transform entities or report query results (which result in an array of
     * objects) to {@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject} using the Jakarta
     * Commons-Collections Transformation API.
     */
    private org.apache.commons.collections.Transformer EXPRESSIONEXPERIMENTVALUEOBJECT_TRANSFORMER = new org.apache.commons.collections.Transformer() {
        public Object transform( Object input ) {
            Object result = null;
            if ( input instanceof ubic.gemma.model.expression.experiment.ExpressionExperiment ) {
                result = toExpressionExperimentValueObject( ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) input );
            } else if ( input instanceof Object[] ) {
                result = toExpressionExperimentValueObject( ( Object[] ) input );
            }
            return result;
        }
    };

    private final org.apache.commons.collections.Transformer ExpressionExperimentValueObjectToEntityTransformer = new org.apache.commons.collections.Transformer() {
        public Object transform( Object input ) {
            return expressionExperimentValueObjectToEntity( ( ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject ) input );
        }
    };

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#countAll()
     */
    public java.lang.Integer countAll() {
        try {
            return this.handleCountAll();
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.countAll()' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#create(int, java.util.Collection)
     */
    public java.util.Collection<ExpressionExperiment> create( final int transform,
            final java.util.Collection<ExpressionExperiment> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.create - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNewSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator entityIterator = entities.iterator(); entityIterator.hasNext(); ) {
                            create( transform,
                                    ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) entityIterator
                                            .next() );
                        }
                        return null;
                    }
                } );
        return entities;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#create(int transform,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public Object create( final int transform,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        if ( expressionExperiment == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.create - 'expressionExperiment' can not be null" );
        }
        this.getHibernateTemplate().save( expressionExperiment );
        return this.transformEntity( transform, expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#create(java.util.Collection)
     */

    public java.util.Collection create( final java.util.Collection entities ) {
        return create( TRANSFORM_NONE, entities );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#create(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ExpressionExperiment create( ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        return ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) this.create( TRANSFORM_NONE,
                expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#expressionExperimentValueObjectToEntity(ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public void expressionExperimentValueObjectToEntity(
            ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject source,
            ubic.gemma.model.expression.experiment.ExpressionExperiment target, boolean copyIfNull ) {
        if ( copyIfNull || source.getSource() != null ) {
            target.setSource( source.getSource() );
        }
        if ( copyIfNull || source.getShortName() != null ) {
            target.setShortName( source.getShortName() );
        }
        if ( copyIfNull || source.getName() != null ) {
            target.setName( source.getName() );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#expressionExperimentValueObjectToEntityCollection(java.util.Collection)
     */
    public final void expressionExperimentValueObjectToEntityCollection( java.util.Collection instances ) {
        if ( instances != null ) {
            for ( final java.util.Iterator<? extends Object> iterator = instances.iterator(); iterator.hasNext(); ) {
                // - remove an objects that are null or not of the correct instance
                if ( !( iterator.next() instanceof ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject ) ) {
                    iterator.remove();
                }
            }
            org.apache.commons.collections.CollectionUtils.transform( instances,
                    ExpressionExperimentValueObjectToEntityTransformer );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#find(int, java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */

    public ExpressionExperiment find( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( expressionExperiment );
        argNames.add( "expressionExperiment" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        ExpressionExperiment result = null;
        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.experiment.ExpressionExperiment"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) results.iterator().next();
        }

        result = ( ExpressionExperiment ) transformEntity( transform, result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#find(int,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ExpressionExperiment find( final int transform,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        return this
                .find(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExpressionExperiment as expressionExperiment where expressionExperiment.expressionExperiment = :expressionExperiment",
                        expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#find(java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment find( final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        return this.find( TRANSFORM_NONE, queryString, expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#find(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment find(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        return this.find( TRANSFORM_NONE, expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByAccession(int, java.lang.String,
     *      ubic.gemma.model.common.description.DatabaseEntry)
     */

    public ExpressionExperiment findByAccession( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.common.description.DatabaseEntry accession ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( accession );
        argNames.add( "accession" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        ExpressionExperiment result = null;

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.experiment.ExpressionExperiment"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) results.iterator().next();
        }

        result = ( ExpressionExperiment ) transformEntity( transform, result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByAccession(int,
     *      ubic.gemma.model.common.description.DatabaseEntry)
     */
    public ExpressionExperiment findByAccession( final int transform,
            final ubic.gemma.model.common.description.DatabaseEntry accession ) {
        return this.findByAccession( transform, "from ExpressionExperiment where accession=:accession", accession );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByAccession(java.lang.String,
     *      ubic.gemma.model.common.description.DatabaseEntry)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByAccession(
            final java.lang.String queryString, final ubic.gemma.model.common.description.DatabaseEntry accession ) {
        return this.findByAccession( TRANSFORM_NONE, queryString, accession );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByAccession(ubic.gemma.model.common.description.DatabaseEntry)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByAccession(
            ubic.gemma.model.common.description.DatabaseEntry accession ) {
        return this.findByAccession( TRANSFORM_NONE, accession );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByBibliographicReference(java.lang.Long)
     */
    public java.util.Collection<ExpressionExperiment> findByBibliographicReference( final java.lang.Long bibRefID ) {
        try {
            return this.handleFindByBibliographicReference( bibRefID );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByBibliographicReference(java.lang.Long bibRefID)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByBioMaterial(ubic.gemma.model.expression.biomaterial.BioMaterial)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByBioMaterial(
            final ubic.gemma.model.expression.biomaterial.BioMaterial bm ) {
        try {
            return this.handleFindByBioMaterial( bm );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByBioMaterial(ubic.gemma.model.expression.biomaterial.BioMaterial bm)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByBioMaterials(java.util.Collection)
     */
    public java.util.Collection<ExpressionExperiment> findByBioMaterials(
            final java.util.Collection<BioMaterial> bioMaterials ) {
        try {
            return this.handleFindByBioMaterials( bioMaterials );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByBioMaterials(java.util.Collection bioMaterials)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByExpressedGene(ubic.gemma.model.genome.Gene,
     *      java.lang.Double)
     */
    public java.util.Collection<ExpressionExperiment> findByExpressedGene( final ubic.gemma.model.genome.Gene gene,
            final java.lang.Double rank ) {
        try {
            return this.handleFindByExpressedGene( gene, rank );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByExpressedGene(ubic.gemma.model.genome.Gene gene, java.lang.Double rank)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByFactorValue(ubic.gemma.model.expression.experiment.FactorValue)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByFactorValue(
            final ubic.gemma.model.expression.experiment.FactorValue factorValue ) {
        try {
            return this.handleFindByFactorValue( factorValue );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByFactorValue(ubic.gemma.model.expression.experiment.FactorValue factorValue)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByFactorValues(java.util.Collection)
     */
    public java.util.Collection<ExpressionExperiment> findByFactorValues(
            final java.util.Collection<FactorValue> factorValues ) {
        try {
            return this.handleFindByFactorValues( factorValues );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByFactorValues(java.util.Collection factorValues)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByGene(ubic.gemma.model.genome.Gene)
     */
    public java.util.Collection<ExpressionExperiment> findByGene( final ubic.gemma.model.genome.Gene gene ) {
        try {
            return this.handleFindByGene( gene );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByGene(ubic.gemma.model.genome.Gene gene)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByInvestigator(int, java.lang.String,
     *      ubic.gemma.model.common.auditAndSecurity.Contact)
     */

    public java.util.Collection findByInvestigator( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.common.auditAndSecurity.Contact investigator ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( investigator );
        argNames.add( "investigator" );
        java.util.List results = this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() );
        transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByInvestigator(int,
     *      ubic.gemma.model.common.auditAndSecurity.Contact)
     */

    public java.util.Collection findByInvestigator( final int transform,
            final ubic.gemma.model.common.auditAndSecurity.Contact investigator ) {
        return this
                .findByInvestigator(
                        transform,
                        "from InvestigationImpl i inner join Contact c on c in elements(i.investigators) or c == i.owner where c == :investigator",
                        investigator );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByInvestigator(java.lang.String,
     *      ubic.gemma.model.common.auditAndSecurity.Contact)
     */

    public java.util.Collection findByInvestigator( final java.lang.String queryString,
            final ubic.gemma.model.common.auditAndSecurity.Contact investigator ) {
        return this.findByInvestigator( TRANSFORM_NONE, queryString, investigator );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByInvestigator(ubic.gemma.model.common.auditAndSecurity.Contact)
     */

    public java.util.Collection<ExpressionExperiment> findByInvestigator(
            ubic.gemma.model.common.auditAndSecurity.Contact investigator ) {
        return this.findByInvestigator( TRANSFORM_NONE, investigator );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByName(int, java.lang.String)
     */
    public ExpressionExperiment findByName( final int transform, final java.lang.String name ) {
        return this.findByName( transform, "from ExpressionExperimentImpl a where a.name=:name", name );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByName(int, java.lang.String,
     *      java.lang.String)
     */

    public ExpressionExperiment findByName( final int transform, final java.lang.String queryString,
            final java.lang.String name ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( name );
        argNames.add( "name" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        ExpressionExperiment result = null;

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.experiment.ExpressionExperiment"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) results.iterator().next();
        }

        result = ( ExpressionExperiment ) transformEntity( transform, result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByName(java.lang.String)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByName( java.lang.String name ) {
        return this.findByName( TRANSFORM_NONE, name );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByName(java.lang.String,
     *      java.lang.String)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByName( final java.lang.String queryString,
            final java.lang.String name ) {
        return this.findByName( TRANSFORM_NONE, queryString, name );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByParentTaxon(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection<ExpressionExperiment> findByParentTaxon( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleFindByParentTaxon( taxon );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByByTaxon(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /*
     * 
     */
    public ExpressionExperiment findByQuantitationType( QuantitationType quantitationType ) {
        try {
            return this.handleFindByQuantitationType( quantitationType );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByQuantitationType  --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByShortName(int, java.lang.String)
     */
    public ExpressionExperiment findByShortName( final int transform, final java.lang.String shortName ) {
        return this.findByShortName( transform, "from ExpressionExperimentImpl a where a.shortName=:shortName",
                shortName );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByShortName(int, java.lang.String,
     *      java.lang.String)
     */

    public ExpressionExperiment findByShortName( final int transform, final java.lang.String queryString,
            final java.lang.String shortName ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( shortName );
        argNames.add( "shortName" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        ExpressionExperiment result = null;

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.experiment.ExpressionExperiment"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) results.iterator().next();
        }

        result = ( ExpressionExperiment ) transformEntity( transform, result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByShortName(java.lang.String)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByShortName( java.lang.String shortName ) {
        return this.findByShortName( TRANSFORM_NONE, shortName );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByShortName(java.lang.String,
     *      java.lang.String)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByShortName(
            final java.lang.String queryString, final java.lang.String shortName ) {
        return this.findByShortName( TRANSFORM_NONE, queryString, shortName );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findByTaxon(ubic.gemma.model.genome.Taxon)
     */
    public java.util.Collection<ExpressionExperiment> findByTaxon( final ubic.gemma.model.genome.Taxon taxon ) {
        try {
            return this.handleFindByTaxon( taxon );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.findByTaxon(ubic.gemma.model.genome.Taxon taxon)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findOrCreate(int, java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */

    public ExpressionExperiment findOrCreate( final int transform, final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( expressionExperiment );
        argNames.add( "expressionExperiment" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        ExpressionExperiment result = null;
        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'ubic.gemma.model.expression.experiment.ExpressionExperiment"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) results.iterator().next();
        }

        result = ( ExpressionExperiment ) transformEntity( transform, result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findOrCreate(int,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ExpressionExperiment findOrCreate( final int transform,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        return this
                .findOrCreate(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExpressionExperiment as expressionExperiment where expressionExperiment.expressionExperiment = :expressionExperiment",
                        expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findOrCreate(java.lang.String,
     *      ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findOrCreate(
            final java.lang.String queryString,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        return this.findOrCreate( TRANSFORM_NONE, queryString, expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#findOrCreate(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findOrCreate(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        return this.findOrCreate( TRANSFORM_NONE, expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getAnnotationCounts(java.util.Collection)
     */
    public java.util.Map getAnnotationCounts( final java.util.Collection<Long> ids ) {
        try {
            return this.handleGetAnnotationCounts( ids );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getAnnotationCounts(java.util.Collection ids)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getArrayDesignAuditEvents(java.util.Collection)
     */
    public java.util.Map getArrayDesignAuditEvents( final java.util.Collection<Long> ids ) {
        try {
            return this.handleGetArrayDesignAuditEvents( ids );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getArrayDesignAuditEvents(java.util.Collection ids)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getAuditEvents(java.util.Collection)
     */
    public java.util.Map getAuditEvents( final java.util.Collection<Long> ids ) {
        try {
            return this.handleGetAuditEvents( ids );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getAuditEvents(java.util.Collection ids)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getBioAssayCountById(long)
     */
    public long getBioAssayCountById( final long id ) {
        try {
            return this.handleGetBioAssayCountById( id );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getBioAssayCountById(long id)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getBioMaterialCount(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public long getBioMaterialCount(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        try {
            return this.handleGetBioMaterialCount( expressionExperiment );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getBioMaterialCount(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getDesignElementDataVectorCountById(long)
     */
    public long getDesignElementDataVectorCountById( final long id ) {
        try {
            return this.handleGetDesignElementDataVectorCountById( id );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getDesignElementDataVectorCountById(long id)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getDesignElementDataVectors(java.util.Collection,
     *      ubic.gemma.model.common.quantitationtype.QuantitationType)
     */
    public java.util.Collection<DesignElementDataVector> getDesignElementDataVectors(
            final java.util.Collection<? extends DesignElement> designElements,
            final ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType ) {
        try {
            return this.handleGetDesignElementDataVectors( designElements, quantitationType );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getDesignElementDataVectors(java.util.Collection designElements, ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getDesignElementDataVectors(java.util.Collection)
     */
    public java.util.Collection<DesignElementDataVector> getDesignElementDataVectors(
            final java.util.Collection<QuantitationType> quantitationTypes ) {
        try {
            return this.handleGetDesignElementDataVectors( quantitationTypes );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getDesignElementDataVectors(java.util.Collection quantitationTypes)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getLastArrayDesignUpdate(java.util.Collection,
     *      java.lang.Class)
     */
    public java.util.Map getLastArrayDesignUpdate(
            final java.util.Collection<ExpressionExperiment> expressionExperiments, final java.lang.Class type ) {
        try {
            return this.handleGetLastArrayDesignUpdate( expressionExperiments, type );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getLastArrayDesignUpdate(java.util.Collection expressionExperiments, java.lang.Class type)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getLastArrayDesignUpdate(ubic.gemma.model.expression.experiment.ExpressionExperiment,
     *      java.lang.Class)
     */
    public ubic.gemma.model.common.auditAndSecurity.AuditEvent getLastArrayDesignUpdate(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment ee,
            final java.lang.Class<? extends AuditEventType> eventType ) {
        try {
            return this.handleGetLastArrayDesignUpdate( ee, eventType );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getLastArrayDesignUpdate(ubic.gemma.model.expression.experiment.ExpressionExperiment ee, java.lang.Class eventType)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getMaskedPreferredQuantitationType(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ubic.gemma.model.common.quantitationtype.QuantitationType getMaskedPreferredQuantitationType(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        try {
            return this.handleGetMaskedPreferredQuantitationType( expressionExperiment );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getMaskedPreferredQuantitationType(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getPerTaxonCount()
     */
    public java.util.Map<Taxon, Long> getPerTaxonCount() {
        try {
            return this.handleGetPerTaxonCount();
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getPerTaxonCount()' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getPopulatedFactorCounts(java.util.Collection)
     */
    public java.util.Map getPopulatedFactorCounts( final java.util.Collection<Long> ids ) {
        try {
            return this.handleGetPopulatedFactorCounts( ids );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getPopulatedFactorCounts(java.util.Collection ids)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getPreferredDesignElementDataVectorCount(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public long getProcessedExpressionVectorCount(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        try {
            return this.handleGetProcessedExpressionVectorCount( expressionExperiment );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getPreferredDesignElementDataVectorCount(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getQuantitationTypeCountById(java.lang.Long)
     */
    public java.util.Map getQuantitationTypeCountById( final java.lang.Long Id ) {
        try {
            return this.handleGetQuantitationTypeCountById( Id );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getQuantitationTypeCountById(java.lang.Long Id)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getQuantitationTypes(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public java.util.Collection<QuantitationType> getQuantitationTypes(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        try {
            return this.handleGetQuantitationTypes( expressionExperiment );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getQuantitationTypes(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getQuantitationTypes(ubic.gemma.model.expression.experiment.ExpressionExperiment,
     *      ubic.gemma.model.expression.arrayDesign.ArrayDesign)
     */
    public java.util.Collection<QuantitationType> getQuantitationTypes(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment,
            final ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign ) {
        try {
            return this.handleGetQuantitationTypes( expressionExperiment, arrayDesign );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getQuantitationTypes(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment, ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getRecipient(int, java.lang.Long)
     */
    public Object getRecipient( final int transform, final java.lang.Long id ) {
        return this
                .getRecipient(
                        transform,
                        "from ubic.gemma.model.expression.experiment.ExpressionExperiment as expressionExperiment where expressionExperiment.id = :id",
                        id );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getRecipient(int, java.lang.String,
     *      java.lang.Long)
     */

    public Object getRecipient( final int transform, final java.lang.String queryString, final java.lang.Long id ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( id );
        argNames.add( "id" );
        java.util.Set results = new java.util.LinkedHashSet( this.getHibernateTemplate().findByNamedParam( queryString,
                argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;

        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'java.lang.String" + "' was found when executing query --> '"
                            + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        result = transformEntity( transform, ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) result );
        return result;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getSampleRemovalEvents(java.util.Collection)
     */
    public java.util.Map getSampleRemovalEvents( final java.util.Collection<ExpressionExperiment> expressionExperiments ) {
        try {
            return this.handleGetSampleRemovalEvents( expressionExperiments );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getSampleRemovalEvents(java.util.Collection expressionExperiments)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getSamplingOfVectors(ubic.gemma.model.common.quantitationtype.QuantitationType,
     *      java.lang.Integer)
     */
    public java.util.Collection<DesignElementDataVector> getSamplingOfVectors(
            final ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType,
            final java.lang.Integer limit ) {
        try {
            return this.handleGetSamplingOfVectors( quantitationType, limit );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getSamplingOfVectors(ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType, java.lang.Integer limit)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getSubSets(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public java.util.Collection<ExpressionExperimentSubSet> getSubSets(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        try {
            return this.handleGetSubSets( expressionExperiment );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getSubSets(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#getTaxon(java.lang.Long)
     */
    public ubic.gemma.model.genome.Taxon getTaxon( final java.lang.Long ExpressionExperimentID ) {
        try {
            return this.handleGetTaxon( ExpressionExperimentID );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.getTaxon(java.lang.Long ExpressionExperimentID)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#load(int, java.lang.Long)
     */

    public ExpressionExperiment load( final int transform, final java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.load - 'id' can not be null" );
        }
        final Object entity = this.getHibernateTemplate().get(
                ubic.gemma.model.expression.experiment.ExpressionExperimentImpl.class, id );
        return ( ExpressionExperiment ) transformEntity( transform,
                ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) entity );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#load(java.lang.Long)
     */

    public ExpressionExperiment load( java.lang.Long id ) {
        return this.load( TRANSFORM_NONE, id );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#load(java.util.Collection)
     */
    public java.util.Collection<ExpressionExperiment> load( final java.util.Collection<Long> ids ) {
        try {
            return this.handleLoad( ids );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.load(java.util.Collection ids)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#loadAll()
     */

    public java.util.Collection loadAll() {
        return this.loadAll( TRANSFORM_NONE );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#loadAll(int)
     */

    public java.util.Collection<ExpressionExperiment> loadAll( final int transform ) {
        final java.util.Collection results = this.getHibernateTemplate().loadAll(
                ubic.gemma.model.expression.experiment.ExpressionExperimentImpl.class );
        this.transformEntities( transform, results );
        return results;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#loadAllValueObjects()
     */
    public java.util.Collection<ExpressionExperimentValueObject> loadAllValueObjects() {
        try {
            return this.handleLoadAllValueObjects();
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.loadAllValueObjects()' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#loadValueObjects(java.util.Collection)
     */
    public java.util.Collection<ExpressionExperimentValueObject> loadValueObjects( final java.util.Collection<Long> ids ) {
        try {
            return this.handleLoadValueObjects( ids );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.loadValueObjects(java.util.Collection ids)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#remove(java.lang.Long)
     */

    public void remove( java.lang.Long id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.remove - 'id' can not be null" );
        }
        ubic.gemma.model.expression.experiment.ExpressionExperiment entity = this.load( id );
        if ( entity != null ) {
            this.remove( entity );
        }
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#remove(java.util.Collection)
     */

    public void remove( java.util.Collection<? extends ExpressionExperiment> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.remove - 'entities' can not be null" );
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#remove(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public void remove( ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        if ( expressionExperiment == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.remove - 'expressionExperiment' can not be null" );
        }
        this.getHibernateTemplate().delete( expressionExperiment );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#thaw(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public void thaw( final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        try {
            this.handleThaw( expressionExperiment, true );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.thaw(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#thawBioAssays(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public void thawBioAssays( final ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        try {
            this.handleThaw( expressionExperiment, false );
        } catch ( Throwable th ) {
            throw new java.lang.RuntimeException(
                    "Error performing 'ubic.gemma.model.expression.experiment.ExpressionExperimentDao.thawBioAssays(ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment)' --> "
                            + th, th );
        }
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#toExpressionExperimentValueObject(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject toExpressionExperimentValueObject(
            final ubic.gemma.model.expression.experiment.ExpressionExperiment entity ) {
        final ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject target = new ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject();
        this.toExpressionExperimentValueObject( entity, target );
        return target;
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#toExpressionExperimentValueObject(ubic.gemma.model.expression.experiment.ExpressionExperiment,
     *      ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject)
     */
    public void toExpressionExperimentValueObject( ubic.gemma.model.expression.experiment.ExpressionExperiment source,
            ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject target ) {
        target.setId( source.getId() );
        target.setName( source.getName() );
        target.setSource( source.getSource() );
        // No conversion for target.accession (can't convert
        // source.getAccession():ubic.gemma.model.common.description.DatabaseEntry to java.lang.String)
        target.setShortName( source.getShortName() );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#toExpressionExperimentValueObjectCollection(java.util.Collection)
     */
    public final void toExpressionExperimentValueObjectCollection( java.util.Collection<ExpressionExperiment> entities ) {
        if ( entities != null ) {
            org.apache.commons.collections.CollectionUtils.transform( entities,
                    EXPRESSIONEXPERIMENTVALUEOBJECT_TRANSFORMER );
        }
    }

    /**
     * @see ubic.gemma.model.common.SecurableDao#update(java.util.Collection)
     */
    public void update( final java.util.Collection<? extends ExpressionExperiment> entities ) {
        if ( entities == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.update - 'entities' can not be null" );
        }
        this.getHibernateTemplate().executeWithNewSession(
                new org.springframework.orm.hibernate3.HibernateCallback<Object>() {
                    public Object doInHibernate( org.hibernate.Session session )
                            throws org.hibernate.HibernateException {
                        for ( java.util.Iterator<? extends ExpressionExperiment> entityIterator = entities.iterator(); entityIterator
                                .hasNext(); ) {
                            update( entityIterator.next() );
                        }
                        return null;
                    }
                } );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#update(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    public void update( ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment ) {
        if ( expressionExperiment == null ) {
            throw new IllegalArgumentException( "ExpressionExperiment.update - 'expressionExperiment' can not be null" );
        }
        this.getHibernateTemplate().update( expressionExperiment );
    }

    /**
     * Performs the core logic for {@link #countAll()}
     */
    protected abstract java.lang.Integer handleCountAll() throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByBibliographicReference(java.lang.Long)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleFindByBibliographicReference(
            java.lang.Long bibRefID ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByBioMaterial(ubic.gemma.model.expression.biomaterial.BioMaterial)}
     */
    protected abstract ubic.gemma.model.expression.experiment.ExpressionExperiment handleFindByBioMaterial(
            ubic.gemma.model.expression.biomaterial.BioMaterial bm ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByBioMaterials(java.util.Collection)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleFindByBioMaterials(
            java.util.Collection<BioMaterial> bioMaterials ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByExpressedGene(ubic.gemma.model.genome.Gene, java.lang.Double)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleFindByExpressedGene(
            ubic.gemma.model.genome.Gene gene, java.lang.Double rank ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByFactorValue(ubic.gemma.model.expression.experiment.FactorValue)}
     */
    protected abstract ubic.gemma.model.expression.experiment.ExpressionExperiment handleFindByFactorValue(
            ubic.gemma.model.expression.experiment.FactorValue factorValue ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByFactorValues(java.util.Collection)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleFindByFactorValues(
            java.util.Collection<FactorValue> factorValues ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByGene(ubic.gemma.model.genome.Gene)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleFindByGene( ubic.gemma.model.genome.Gene gene )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #findByParentTaxon(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleFindByParentTaxon(
            ubic.gemma.model.genome.Taxon taxon ) throws java.lang.Exception;

    protected abstract ExpressionExperiment handleFindByQuantitationType( QuantitationType quantitationType )
            throws Exception;

    /**
     * Performs the core logic for {@link #findByTaxon(ubic.gemma.model.genome.Taxon)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleFindByTaxon( ubic.gemma.model.genome.Taxon taxon )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getAnnotationCounts(java.util.Collection)}
     */
    protected abstract java.util.Map handleGetAnnotationCounts( java.util.Collection<Long> ids )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getArrayDesignAuditEvents(java.util.Collection)}
     */
    protected abstract java.util.Map handleGetArrayDesignAuditEvents( java.util.Collection<Long> ids )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getAuditEvents(java.util.Collection)}
     */
    protected abstract java.util.Map handleGetAuditEvents( java.util.Collection<Long> ids ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getBioAssayCountById(long)}
     */
    protected abstract long handleGetBioAssayCountById( long id ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getBioMaterialCount(ubic.gemma.model.expression.experiment.ExpressionExperiment)}
     */
    protected abstract long handleGetBioMaterialCount(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getDesignElementDataVectorCountById(long)}
     */
    protected abstract long handleGetDesignElementDataVectorCountById( long id ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getDesignElementDataVectors(java.util.Collection, ubic.gemma.model.common.quantitationtype.QuantitationType)}
     */
    protected abstract java.util.Collection<DesignElementDataVector> handleGetDesignElementDataVectors(
            java.util.Collection<? extends DesignElement> designElements,
            ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getDesignElementDataVectors(java.util.Collection)}
     */
    protected abstract java.util.Collection<DesignElementDataVector> handleGetDesignElementDataVectors(
            java.util.Collection<QuantitationType> quantitationTypes ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getLastArrayDesignUpdate(java.util.Collection, java.lang.Class)}
     */
    protected abstract java.util.Map handleGetLastArrayDesignUpdate(
            java.util.Collection<ExpressionExperiment> expressionExperiments,
            java.lang.Class<? extends AuditEventType> type ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getLastArrayDesignUpdate(ubic.gemma.model.expression.experiment.ExpressionExperiment, java.lang.Class)}
     */
    protected abstract ubic.gemma.model.common.auditAndSecurity.AuditEvent handleGetLastArrayDesignUpdate(
            ubic.gemma.model.expression.experiment.ExpressionExperiment ee,
            java.lang.Class<? extends AuditEventType> eventType ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getMaskedPreferredQuantitationType(ubic.gemma.model.expression.experiment.ExpressionExperiment)}
     */
    protected abstract ubic.gemma.model.common.quantitationtype.QuantitationType handleGetMaskedPreferredQuantitationType(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getPerTaxonCount()}
     */
    protected abstract Map<Taxon, Long> handleGetPerTaxonCount() throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getPopulatedFactorCounts(java.util.Collection)}
     */
    protected abstract java.util.Map handleGetPopulatedFactorCounts( java.util.Collection<Long> ids )
            throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getProcessedExpressionVectorCount(ubic.gemma.model.expression.experiment.ExpressionExperiment)}
     */
    protected abstract long handleGetProcessedExpressionVectorCount(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getQuantitationTypeCountById(java.lang.Long)}
     */
    protected abstract java.util.Map handleGetQuantitationTypeCountById( java.lang.Long Id ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getQuantitationTypes(ubic.gemma.model.expression.experiment.ExpressionExperiment)}
     */
    protected abstract java.util.Collection<QuantitationType> handleGetQuantitationTypes(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment )
            throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getQuantitationTypes(ubic.gemma.model.expression.experiment.ExpressionExperiment, ubic.gemma.model.expression.arrayDesign.ArrayDesign)}
     */
    protected abstract java.util.Collection<QuantitationType> handleGetQuantitationTypes(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment,
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getSampleRemovalEvents(java.util.Collection)}
     */
    protected abstract java.util.Map handleGetSampleRemovalEvents(
            java.util.Collection<ExpressionExperiment> expressionExperiments ) throws java.lang.Exception;

    /**
     * Performs the core logic for
     * {@link #getSamplingOfVectors(ubic.gemma.model.common.quantitationtype.QuantitationType, java.lang.Integer)}
     */
    protected abstract java.util.Collection<DesignElementDataVector> handleGetSamplingOfVectors(
            ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType, java.lang.Integer limit )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getSubSets(ubic.gemma.model.expression.experiment.ExpressionExperiment)}
     */
    protected abstract java.util.Collection<ExpressionExperimentSubSet> handleGetSubSets(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #getTaxon(java.lang.Long)}
     */
    protected abstract ubic.gemma.model.genome.Taxon handleGetTaxon( java.lang.Long ExpressionExperimentID )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #load(java.util.Collection)}
     */
    protected abstract java.util.Collection<ExpressionExperiment> handleLoad( java.util.Collection<Long> ids )
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadAllValueObjects()}
     */
    protected abstract java.util.Collection<ExpressionExperimentValueObject> handleLoadAllValueObjects()
            throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #loadValueObjects(java.util.Collection)}
     */
    protected abstract java.util.Collection<ExpressionExperimentValueObject> handleLoadValueObjects(
            java.util.Collection<Long> ids ) throws java.lang.Exception;

    /**
     * Performs the core logic for {@link #thaw(ubic.gemma.model.expression.experiment.ExpressionExperiment)}
     */
    protected abstract void handleThaw(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment, boolean thawVectors )
            throws java.lang.Exception;

    /**
     * Default implementation for transforming the results of a report query into a value object. This implementation
     * exists for convenience reasons only. It needs only be overridden in the {@link ExpressionExperimentDaoImpl} class
     * if you intend to use reporting queries.
     * 
     * @see ubic.gemma.model.expression.experiment.ExpressionExperimentDao#toExpressionExperimentValueObject(ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    protected ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject toExpressionExperimentValueObject(
            Object[] row ) {
        ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject target = null;
        if ( row != null ) {
            final int numberOfObjects = row.length;
            for ( int ctr = 0; ctr < numberOfObjects; ctr++ ) {
                final Object object = row[ctr];
                if ( object instanceof ubic.gemma.model.expression.experiment.ExpressionExperiment ) {
                    target = this
                            .toExpressionExperimentValueObject( ( ubic.gemma.model.expression.experiment.ExpressionExperiment ) object );
                    break;
                }
            }
        }
        return target;
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,ubic.gemma.model.expression.experiment.ExpressionExperiment)} method. This method
     * does not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     * 
     * @param transform one of the constants declared in
     *        <code>ubic.gemma.model.expression.experiment.ExpressionExperimentDao</code>
     * @param entities the collection of entities to transform
     * @return the same collection as the argument, but this time containing the transformed entities
     * @see #transformEntity(int,ubic.gemma.model.expression.experiment.ExpressionExperiment)
     */
    protected void transformEntities( final int transform, final java.util.Collection<ExpressionExperiment> entities ) {
        switch ( transform ) {
            case ubic.gemma.model.expression.experiment.ExpressionExperimentDao.TRANSFORM_EXPRESSIONEXPERIMENTVALUEOBJECT:
                toExpressionExperimentValueObjectCollection( entities );
                break;
            case TRANSFORM_NONE: // fall-through
            default:
                // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter), when the
     * <code>transform</code> flag is set to one of the constants defined in
     * <code>ubic.gemma.model.expression.experiment.ExpressionExperimentDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be returned.
     * <p/>
     * This method will return instances of these types:
     * <ul>
     * <li>{@link ubic.gemma.model.expression.experiment.ExpressionExperiment} - {@link #TRANSFORM_NONE}</li>
     * <li>{@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject} -
     * {@link TRANSFORM_EXPRESSIONEXPERIMENTVALUEOBJECT}</li>
     * </ul>
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     * 
     * @param transform one of the constants declared in
     *        {@link ubic.gemma.model.expression.experiment.ExpressionExperimentDao}
     * @param entity an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity( final int transform,
            final ubic.gemma.model.expression.experiment.ExpressionExperiment entity ) {
        Object target = null;
        if ( entity != null ) {
            switch ( transform ) {
                case ubic.gemma.model.expression.experiment.ExpressionExperimentDao.TRANSFORM_EXPRESSIONEXPERIMENTVALUEOBJECT:
                    target = toExpressionExperimentValueObject( entity );
                    break;
                case TRANSFORM_NONE: // fall-through
                default:
                    target = entity;
            }
        }
        return target;
    }

}