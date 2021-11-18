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
package ubic.gemma.persistence.service.expression.experiment;

import org.springframework.security.access.annotation.Secured;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExperimentalFactorValueObject;
import ubic.gemma.persistence.service.BaseVoEnabledService;

import java.util.Collection;
import java.util.List;

/**
 * @author paul
 */
public interface ExperimentalFactorService
        extends BaseVoEnabledService<ExperimentalFactor, ExperimentalFactorValueObject> {

    String BATCH_FACTOR_NAME_PREFIX = "Batch_";

    String BATCH_FACTOR_CATEGORY_URI = "http://www.ebi.ac.uk/efo/EFO_0005067"; // block aka batch

    String BATCH_FACTOR_CATEGORY_NAME = "block";

    String BATCH_FACTOR_NAME = "batch";
    String FACTOR_VALUE_RNAME_PREFIX = "fv_";

    ExperimentalFactor loadAndThaw( Long id );

    /**
     * Delete the factor, its associated factor values and all differential expression analyses in which it is used.
     *
     * @param experimentalFactor the factor to be deleted
     */
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    void delete( ExperimentalFactor experimentalFactor );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExperimentalFactor find( ExperimentalFactor experimentalFactor );

    @Override
    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    ExperimentalFactor findOrCreate( ExperimentalFactor experimentalFactor );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    List<ExperimentalFactor> load( Collection<Long> ids );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_READ" })
    ExperimentalFactor load( java.lang.Long id );

    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "AFTER_ACL_COLLECTION_READ" })
    List<ExperimentalFactor> loadAll();

    @Override
    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    void update( ExperimentalFactor experimentalFactor );

    /**
     * @deprecated use {@link #loadAndThaw(Long)} instead
     * @param ef
     * @return
     */
    @Deprecated
    ExperimentalFactor thaw( ExperimentalFactor ef );
}
