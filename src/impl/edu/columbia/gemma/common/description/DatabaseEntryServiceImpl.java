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

package edu.columbia.gemma.common.description;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 - 2006 University of British Columbia
 * 
 * @author keshav
 * @version $Id$
 * @see edu.columbia.gemma.common.description.DatabaseEntryService
 */
public class DatabaseEntryServiceImpl extends edu.columbia.gemma.common.description.DatabaseEntryServiceBase {

    /**
     * @see edu.columbia.gemma.common.description.DatabaseEntryService#find(edu.columbia.gemma.common.description.DatabaseEntry)
     */
    protected edu.columbia.gemma.common.description.DatabaseEntry handleFind(
            edu.columbia.gemma.common.description.DatabaseEntry databaseEntry ) throws java.lang.Exception {
        return this.getDatabaseEntryDao().find( databaseEntry );
    }

    /**
     * @see edu.columbia.gemma.common.description.DatabaseEntryService#create(edu.columbia.gemma.common.description.DatabaseEntry)
     */
    protected edu.columbia.gemma.common.description.DatabaseEntry handleCreate(
            edu.columbia.gemma.common.description.DatabaseEntry databaseEntry ) throws java.lang.Exception {
        return this.getDatabaseEntryDao().create( databaseEntry );
    }

    /**
     * @see edu.columbia.gemma.common.description.DatabaseEntryService#update(edu.columbia.gemma.common.description.DatabaseEntry)
     */
    protected void handleUpdate( edu.columbia.gemma.common.description.DatabaseEntry databaseEntry )
            throws java.lang.Exception {
        this.getDatabaseEntryDao().update( databaseEntry );
    }

    /**
     * @see edu.columbia.gemma.common.description.DatabaseEntryService#remove(edu.columbia.gemma.common.description.DatabaseEntry)
     */
    protected void handleRemove( edu.columbia.gemma.common.description.DatabaseEntry databaseEntry )
            throws java.lang.Exception {
        this.getDatabaseEntryDao().remove( databaseEntry );
    }

    @Override
    protected DatabaseEntry handleFindOrCreate( DatabaseEntry databaseEntry ) throws Exception {
        return this.getDatabaseEntryDao().findOrCreate( databaseEntry );
    }

}