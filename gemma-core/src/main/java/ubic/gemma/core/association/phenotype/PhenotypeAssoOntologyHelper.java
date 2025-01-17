/*
 * The gemma project
 *
 * Copyright (c) 2013 University of British Columbia
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
package ubic.gemma.core.association.phenotype;

import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.search.OntologySearchException;
import ubic.gemma.model.common.description.Characteristic;
import ubic.gemma.model.genome.gene.phenotype.valueObject.CharacteristicValueObject;

import java.util.Collection;
import java.util.Set;

/**
 * @author nicolas
 */
public interface PhenotypeAssoOntologyHelper {

    /**
     * @return Gemma might be ready but the ontology thread not finish loading
     */
    boolean areOntologiesAllLoaded();

    /**
     * CharacteristicValueObject to Characteristic with no valueUri given
     *
     * @param  characteristicValueObject characteristic VO
     * @return                           vocab characteristic
     */
    Characteristic characteristicValueObject2Characteristic( CharacteristicValueObject characteristicValueObject );

    /**
     * Giving some Ontology terms return all valueUri of Ontology Terms + children
     *
     * @param  ontologyTerms ontology terms
     * @return               all valueUri of Ontology Terms + children
     */
    Set<String> findAllChildrenAndParent( Collection<OntologyTerm> ontologyTerms );

    /**
     * For a valueUri return the OntologyTerm found
     *
     * @param  valueUri value uri
     * @return          ontology term
     */
    OntologyTerm findOntologyTermByUri( String valueUri ) throws EntityNotFoundException;

    /**
     * search the disease,hp and mp ontology for a searchQuery and return an ordered set of CharacteristicVO
     *
     * @param  searchQuery query
     * @return             characteristic VOs
     */
    Set<CharacteristicValueObject> findPhenotypesInOntology( String searchQuery ) throws OntologySearchException;

    /**
     * search the disease, hp and mp ontology for OntologyTerm
     *
     * @param  searchQuery free text query?
     * @return             terms
     */
    Collection<OntologyTerm> findValueUriInOntology( String searchQuery ) throws OntologySearchException;

    /**
     * Helper method. For a valueUri return the Characteristic (represents a phenotype)
     *
     * @param  valueUri value uri
     * @return          Characteristic
     */
    Characteristic valueUri2Characteristic( String valueUri );

}