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
package ubic.gemma.persistence.service.common.description;

import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.common.description.DatabaseEntryValueObject;
import ubic.gemma.persistence.service.AbstractQueryFilteringVoEnabledDao;
import ubic.gemma.persistence.service.genome.taxon.TaxonDao;
import ubic.gemma.persistence.util.Filters;
import ubic.gemma.persistence.util.Sort;

import java.util.EnumSet;

/**
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>DatabaseEntry</code>.
 *
 * @see DatabaseEntry
 */
@Repository
public class DatabaseEntryDaoImpl extends AbstractQueryFilteringVoEnabledDao<DatabaseEntry, DatabaseEntryValueObject>
        implements DatabaseEntryDao {

    @Autowired
    public DatabaseEntryDaoImpl( SessionFactory sessionFactory ) {
        super( DatabaseEntryDao.OBJECT_ALIAS, DatabaseEntry.class, sessionFactory );
    }

    @Override
    public DatabaseEntry findByAccession( String accession ) {
        return this.findOneByProperty( "accession", accession );
    }

    @Override
    public DatabaseEntryValueObject loadValueObject( DatabaseEntry entity ) {
        return new DatabaseEntryValueObject( entity );
    }

    @Override
    protected Query getLoadValueObjectsQuery( Filters filters, Sort sort, EnumSet<QueryHint> hints ) {
        throw new NotImplementedException( "This is not implemented yet!" );
    }
}