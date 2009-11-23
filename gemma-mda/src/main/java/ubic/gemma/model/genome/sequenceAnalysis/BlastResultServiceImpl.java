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
package ubic.gemma.model.genome.sequenceAnalysis;

import org.springframework.stereotype.Service;

/**
 * @see ubic.gemma.model.genome.sequenceAnalysis.BlastResultService
 */
@Service
public class BlastResultServiceImpl extends ubic.gemma.model.genome.sequenceAnalysis.BlastResultServiceBase {

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlastResultService#create(ubic.gemma.model.genome.sequenceAnalysis.BlastResult)
     */
    @Override
    protected ubic.gemma.model.genome.sequenceAnalysis.BlastResult handleCreate(
            ubic.gemma.model.genome.sequenceAnalysis.BlastResult blastResult ) throws java.lang.Exception {
        return this.getBlastResultDao().create( blastResult );
    }

    /*
     * (non-Javadoc)
     * @see
     * ubic.gemma.model.genome.sequenceAnalysis.BlastResultServiceBase#handleFind(ubic.gemma.model.genome.sequenceAnalysis
     * .BlastResult)
     */
    @Override
    protected ubic.gemma.model.genome.sequenceAnalysis.BlastResult handleFind(
            ubic.gemma.model.genome.sequenceAnalysis.BlastResult resultToFind ) {

        return this.getBlastResultDao().find( resultToFind );

    }

    /*
     * (non-Javadoc)
     * @seeubic.gemma.model.genome.sequenceAnalysis.BlastResultServiceBase#handleFindOrCreate(ubic.gemma.model.genome.
     * sequenceAnalysis.BlastResult)
     */
    @Override
    protected ubic.gemma.model.genome.sequenceAnalysis.BlastResult handleFindOrCreate(
            ubic.gemma.model.genome.sequenceAnalysis.BlastResult resultToFindOrCreate ) throws java.lang.Exception {

        return this.getBlastResultDao().findOrCreate( resultToFindOrCreate );
    }

    /**
     * @see ubic.gemma.model.genome.sequenceAnalysis.BlastResultService#remove(ubic.gemma.model.genome.sequenceAnalysis.BlastResult)
     */
    @Override
    protected void handleRemove( ubic.gemma.model.genome.sequenceAnalysis.BlastResult blastResult )
            throws java.lang.Exception {
        this.getBlastResultDao().remove( blastResult );
    }

}