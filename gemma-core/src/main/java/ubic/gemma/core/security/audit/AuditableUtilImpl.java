/*
 * The Gemma project
 *
 * Copyright (c) 2007 University of British Columbia
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
package ubic.gemma.core.security.audit;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ubic.gemma.model.common.auditAndSecurity.curation.AbstractCuratableValueObject;
import ubic.gemma.model.common.auditAndSecurity.curation.Curatable;

import java.util.Collection;

@Component
public class AuditableUtilImpl implements AuditableUtil {

    @Override
    public void removeTroubledCuratableEntities( Collection<? extends Curatable> entities ) {
        CollectionUtils.filter( entities, o -> !o.getCurationDetails().getTroubled() );
    }

    @Override
    public void removeTroubledCuratableValueObjects( Collection<? extends AbstractCuratableValueObject<? extends Curatable>> valueObjects ) {
        if ( valueObjects == null || valueObjects.size() == 0 ) {
            return;
        }
        CollectionUtils.filter( valueObjects, vo -> !vo.getTroubled() );
    }

}
