/*
 * The Gemma project
 *
 * Copyright (c) 2009 University of British Columbia
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
package ubic.gemma.persistence.service.association.phenotype;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ubic.gemma.model.association.phenotype.ExperimentalEvidence;
import ubic.gemma.model.genome.gene.phenotype.valueObject.ExperimentalEvidenceValueObject;
import ubic.gemma.persistence.service.AbstractDao;
import ubic.gemma.persistence.service.AbstractVoEnabledDao;
import ubic.gemma.persistence.util.ObjectFilter;

import java.util.List;

@Repository
public class ExperimentalEvidenceDaoImpl extends AbstractDao<ExperimentalEvidence> implements ExperimentalEvidenceDao {

    @Autowired
    public ExperimentalEvidenceDaoImpl( SessionFactory sessionFactory ) {
        super( ExperimentalEvidence.class, sessionFactory );
    }

    public ExperimentalEvidenceValueObject loadValueObject( ExperimentalEvidence entity ) {
        return new ExperimentalEvidenceValueObject( entity );
    }

}
