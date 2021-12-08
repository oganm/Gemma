/*
 * The Gemma project
 *
 * Copyright (c) 2012 University of British Columbia
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
package ubic.gemma.core.security.audit;

import ubic.gemma.model.common.auditAndSecurity.curation.AbstractCuratableValueObject;
import ubic.gemma.model.common.auditAndSecurity.curation.Curatable;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

import java.util.Collection;
import java.util.Map;

/**
 * A few utility methods to filter collections of {@link Curatable} and {@link AbstractCuratableValueObject}.
 * @author paul
 */
public interface AuditableUtil {

    void removeTroubledCuratableEntities( Collection<? extends Curatable> entities );

    void removeTroubledCuratableValueObjects( Collection<? extends AbstractCuratableValueObject<? extends Curatable>> valueObjects );
}