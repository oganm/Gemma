/*
 * The Gemma project.
 * 
 * Copyright (c) 2006 University of British Columbia
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
package ubic.gemma.model.association;

import org.springframework.stereotype.Service;

/**
 * @see ubic.gemma.model.association.BioSequence2GeneProductService
 */
@Service
public abstract class BioSequence2GeneProductServiceImpl extends
        ubic.gemma.model.association.BioSequence2GeneProductServiceBase {

    /**
     * @see ubic.gemma.model.association.BioSequence2GeneProductService#create(ubic.gemma.model.association.BioSequence2GeneProduct)
     */
    @Override
    protected ubic.gemma.model.association.BioSequence2GeneProduct handleCreate(
            ubic.gemma.model.association.BioSequence2GeneProduct bioSequence2GeneProduct ) throws java.lang.Exception {
        // @todo implement protected ubic.gemma.model.association.BioSequence2GeneProduct
        // handleCreate(ubic.gemma.model.association.BioSequence2GeneProduct bioSequence2GeneProduct)
        return null;
    }

}