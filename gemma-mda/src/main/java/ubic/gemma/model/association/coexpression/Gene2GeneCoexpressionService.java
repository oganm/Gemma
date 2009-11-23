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
package ubic.gemma.model.association.coexpression;

import java.util.Collection;

import org.springframework.security.access.annotation.Secured;

import ubic.gemma.model.analysis.expression.coexpression.GeneCoexpressionAnalysis;
import ubic.gemma.model.genome.Gene;

/**
 * A service for CRUDding gene coexpression results
 * 
 * @author Gemma
 * @version $Id$
 */
public interface Gene2GeneCoexpressionService {

    /**
     * @param gene2geneCoexpression A collection of Gene2GeneCoexpression object to create
     */

    @Secured( { "GROUP_ADMIN" })
    public java.util.Collection<Gene2GeneCoexpression> create(
            java.util.Collection<Gene2GeneCoexpression> gene2geneCoexpressions );

    /**
     * @param Create the given gene2geneCoexpression object
     */
    @Secured( { "GROUP_ADMIN" })
    public ubic.gemma.model.association.coexpression.Gene2GeneCoexpression create(
            ubic.gemma.model.association.coexpression.Gene2GeneCoexpression gene2gene );

    /**
     * @param the gene2geneCoexpression object to remove from the DB
     */
    @Secured( { "GROUP_ADMIN" })
    public void delete( ubic.gemma.model.association.coexpression.Gene2GeneCoexpression toDelete );

    /**
     * <p>
     * Returns a map of genes to coexpression results.
     * </p>
     */
    public java.util.Map<Gene, Collection<Gene2GeneCoexpression>> findCoexpressionRelationships(
            java.util.Collection<Gene> genes, int stringency, int maxResults, GeneCoexpressionAnalysis sourceAnalysis );

    /**
     * 
     */
    public java.util.Collection<Gene2GeneCoexpression> findCoexpressionRelationships(
            ubic.gemma.model.genome.Gene gene, int stringency, int maxResults, GeneCoexpressionAnalysis sourceAnalysis );

    /**
     * <p>
     * Return coexpression relationships among the given genes, in a map of query gene to coexpression objects.
     * </p>
     */
    public java.util.Map<Gene, Collection<Gene2GeneCoexpression>> findInterCoexpressionRelationship(
            java.util.Collection<Gene> genes, int stringency, GeneCoexpressionAnalysis sourceAnalysis );

}
