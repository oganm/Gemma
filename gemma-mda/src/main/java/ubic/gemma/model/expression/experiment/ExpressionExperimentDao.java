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

import java.util.Collection;
import java.util.Map;

import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimension;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.designElement.DesignElement;
import ubic.gemma.model.genome.Taxon;

/**
 * @see ubic.gemma.model.expression.experiment.ExpressionExperiment
 */
public interface ExpressionExperimentDao extends
        ubic.gemma.model.expression.experiment.BioAssaySetDao<ExpressionExperiment> {
    /**
     * This constant is used as a transformation flag; entities can be converted automatically into value objects or
     * other types, different methods in a class implementing this interface support this feature: look for an
     * <code>int</code> parameter called <code>transform</code>.
     * <p/>
     * This specific flag denotes entities must be transformed into objects of type
     * {@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject}.
     */
    public final static int TRANSFORM_EXPRESSIONEXPERIMENTVALUEOBJECT = 1;

    /**
     * 
     */
    public java.lang.Integer countAll();

    /**
     * Converts an instance of type {@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject} to
     * this DAO's entity.
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperimentValueObjectToEntity(
            ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject expressionExperimentValueObject );

    /**
     * Copies the fields of {@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject} to the
     * specified entity.
     * 
     * @param copyIfNull If FALSE, the value object's field will not be copied to the entity if the value is NULL. If
     *        TRUE, it will be copied regardless of its value.
     */
    public void expressionExperimentValueObjectToEntity(
            ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject sourceVO,
            ubic.gemma.model.expression.experiment.ExpressionExperiment targetEntity, boolean copyIfNull );

    /**
     * Converts a Collection of instances of type
     * {@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject} to this DAO's entity.
     */
    public void expressionExperimentValueObjectToEntityCollection( java.util.Collection<ExpressionExperiment> instances );

    /**
     * 
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment find(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * 
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByAccession(
            ubic.gemma.model.common.description.DatabaseEntry accession );

    /**
     * <p>
     * Finds all the EE's that reference the given bibliographicReference id
     * </p>
     */
    public java.util.Collection<ExpressionExperiment> findByBibliographicReference( java.lang.Long bibRefID );

    /**
     * 
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByBioMaterial(
            ubic.gemma.model.expression.biomaterial.BioMaterial bm );

    /**
     * 
     */
    public java.util.Collection<ExpressionExperiment> findByBioMaterials( java.util.Collection<BioMaterial> bioMaterials );

    /**
     * <p>
     * Returns a collection of expression experiments that detected the given gene at a level greater than the given
     * rank (percentile)
     * </p>
     */
    public java.util.Collection<ExpressionExperiment> findByExpressedGene( ubic.gemma.model.genome.Gene gene,
            java.lang.Double rank );

    /**
     * 
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByFactorValue(
            ubic.gemma.model.expression.experiment.FactorValue factorValue );

    /**
     * 
     */
    public java.util.Collection<ExpressionExperiment> findByFactorValues( java.util.Collection<FactorValue> factorValues );

    /**
     * <p>
     * returns a collection of expression experiments that have an AD that assays for the given gene
     * </p>
     */
    public java.util.Collection<ExpressionExperiment> findByGene( ubic.gemma.model.genome.Gene gene );

    /**
     * 
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByName( java.lang.String name );

    /**
     * 
     */
    public java.util.Collection<ExpressionExperiment> findByParentTaxon( ubic.gemma.model.genome.Taxon taxon );

    public ExpressionExperiment findByQuantitationType( QuantitationType quantitationType );

    /**
     * 
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findByShortName( java.lang.String shortName );

    /**
     * 
     */
    public java.util.Collection<ExpressionExperiment> findByTaxon( ubic.gemma.model.genome.Taxon taxon );

    /**
     * 
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperiment findOrCreate(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * <p>
     * Get the map of ids to number of terms associated with each expression experiment.
     * </p>
     */
    public java.util.Map<Long, Integer> getAnnotationCounts( java.util.Collection<Long> ids );

    /**
     * 
     */
    public Map<Long, Map<Long, Collection<AuditEvent>>> getArrayDesignAuditEvents( java.util.Collection<Long> ids );

    public Collection<ArrayDesign> getArrayDesignsUsed( ExpressionExperiment expressionExperiment );

    /**
     * <p>
     * Gets the AuditEvents of the specified expression experiment ids. This returns a map of id -> AuditEvent. If the
     * events do not exist, the map entry will point to null.
     * </p>
     */
    public java.util.Map<Long, Collection<AuditEvent>> getAuditEvents( java.util.Collection<Long> ids );

    /**
     * 
     */
    public long getBioAssayCountById( long id );

    /**
     * Retrieve the BioAssayDimensions for the study.
     * 
     * @param expressionExperiment
     * @return
     */
    public Collection<BioAssayDimension> getBioAssayDimensions( ExpressionExperiment expressionExperiment );

    /**
     * 
     */
    public long getBioMaterialCount( ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * 
     */
    public long getDesignElementDataVectorCountById( long id );

    /**
     * 
     */
    public java.util.Collection<DesignElementDataVector> getDesignElementDataVectors(
            java.util.Collection<? extends DesignElement> designElements,
            ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType );

    /**
     * 
     */
    public java.util.Collection<DesignElementDataVector> getDesignElementDataVectors(
            java.util.Collection<QuantitationType> quantitationTypes );

    /**
     * 
     */
    public Map<ExpressionExperiment, AuditEvent> getLastArrayDesignUpdate(
            java.util.Collection<ExpressionExperiment> expressionExperiments, java.lang.Class type );

    /**
     * <p>
     * Gets the last audit event for the AD's associated with the given EE.
     * </p>
     */
    public ubic.gemma.model.common.auditAndSecurity.AuditEvent getLastArrayDesignUpdate(
            ubic.gemma.model.expression.experiment.ExpressionExperiment ee,
            java.lang.Class<? extends AuditEventType> eventType );

    /**
     * <p>
     * Returns the missing-value-masked preferred quantitation type for the experiment, if it exists, or null otherwise.
     * </p>
     */
    public ubic.gemma.model.common.quantitationtype.QuantitationType getMaskedPreferredQuantitationType(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * <p>
     * Function to get a count of expression experiments, grouped by Taxon
     * </p>
     */
    public java.util.Map<Taxon, Long> getPerTaxonCount();

    /**
     * <p>
     * Get map of ids to how many factor values the experiment has, counting only factor values which are associated
     * with biomaterials.
     * </p>
     */
    public java.util.Map getPopulatedFactorCounts( java.util.Collection<Long> ids );

    public Collection<ProcessedExpressionDataVector> getProcessedDataVectors( ExpressionExperiment ee );

    /**
     * 
     */
    public long getProcessedExpressionVectorCount(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * <p>
     * Function to get a count of an expressionExperiment's designelementdatavectors, grouped by quantitation type.
     * </p>
     */
    public java.util.Map<Long, Integer> getQuantitationTypeCountById( java.lang.Long Id );

    /**
     * 
     */
    public java.util.Collection<QuantitationType> getQuantitationTypes(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * <p>
     * Get the quantitation types for the expression experiment, for the array design specified. This is really only
     * useful for expression experiments that use more than one array design.
     * </p>
     */
    public java.util.Collection<QuantitationType> getQuantitationTypes(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment,
            ubic.gemma.model.expression.arrayDesign.ArrayDesign arrayDesign );

    /**
     * 
     */
    public java.util.Map<ExpressionExperiment, Collection<AuditEvent>> getSampleRemovalEvents(
            java.util.Collection<ExpressionExperiment> expressionExperiments );

    /**
     * 
     */
    public java.util.Collection<DesignElementDataVector> getSamplingOfVectors(
            ubic.gemma.model.common.quantitationtype.QuantitationType quantitationType, java.lang.Integer limit );

    /**
     * <p>
     * Return any ExpressionExperimentSubSets the given Experiment might have.
     * </p>
     */
    public java.util.Collection<ExpressionExperimentSubSet> getSubSets(
            ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * <p>
     * Gets the taxon for the given expressionExperiment
     * </p>
     */
    public ubic.gemma.model.genome.Taxon getTaxon( java.lang.Long ExpressionExperimentID );

    /**
     * 
     */
    public java.util.Collection<ExpressionExperimentValueObject> loadAllValueObjects();

    /**
     * 
     */
    public java.util.Collection<ExpressionExperimentValueObject> loadValueObjects( java.util.Collection<Long> ids );

    /**
     * 
     */
    public void thaw( ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * <p>
     * Thaws the BioAssays associated with the given ExpressionExperiment and their associations, but not the
     * DesignElementDataVectors.
     * </p>
     */
    public void thawBioAssays( ubic.gemma.model.expression.experiment.ExpressionExperiment expressionExperiment );

    /**
     * Converts this DAO's entity to an object of type
     * {@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject}.
     */
    public ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject toExpressionExperimentValueObject(
            ubic.gemma.model.expression.experiment.ExpressionExperiment entity );

    /**
     * Copies the fields of the specified entity to the target value object. This method is similar to
     * toExpressionExperimentValueObject(), but it does not handle any attributes in the target value object that are
     * "read-only" (as those do not have setter methods exposed).
     */
    public void toExpressionExperimentValueObject(
            ubic.gemma.model.expression.experiment.ExpressionExperiment sourceEntity,
            ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject targetVO );

    /**
     * Converts this DAO's entity to a Collection of instances of type
     * {@link ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject}.
     */
    public void toExpressionExperimentValueObjectCollection( java.util.Collection<ExpressionExperiment> entities );

}
