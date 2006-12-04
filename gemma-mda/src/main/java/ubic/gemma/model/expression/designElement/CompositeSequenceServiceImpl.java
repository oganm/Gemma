/*
 * The Gemma project.
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package ubic.gemma.model.expression.designElement;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.model.expression.arrayDesign.ArrayDesign;

/**
 * @author keshav
 * @author pavlidis
 * @version $Id$
 * @see ubic.gemma.model.expression.designElement.CompositeSequenceService
 */
public class CompositeSequenceServiceImpl extends
        ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase {

    Log log = LogFactory.getLog( this.getClass() );

    /**
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceService#saveCompositeSequence(ubic.gemma.model.expression.designElement.CompositeSequence)
     */
    protected void handleSaveCompositeSequence(
            ubic.gemma.model.expression.designElement.CompositeSequence compositeSequence ) throws java.lang.Exception {
        this.getCompositeSequenceDao().create( compositeSequence );
    }

    @Override
    protected Integer handleCountAll() throws Exception {
        return this.getCompositeSequenceDao().countAll();
    }

    @Override
    protected CompositeSequence handleFindOrCreate( CompositeSequence compositeSequence ) throws Exception {
        return this.getCompositeSequenceDao().findOrCreate( compositeSequence );
    }

    @Override
    protected void handleRemove( CompositeSequence compositeSequence ) throws Exception {
        this.getCompositeSequenceDao().remove( compositeSequence );

    }

    @Override
    protected CompositeSequence handleFind( CompositeSequence compositeSequence ) throws Exception {
        return this.getCompositeSequenceDao().find( compositeSequence );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase#handleCreate(ubic.gemma.model.expression.designElement.CompositeSequence)
     */
    @Override
    protected CompositeSequence handleCreate( CompositeSequence compositeSequence ) throws Exception {
        return ( CompositeSequence ) this.getCompositeSequenceDao().create( compositeSequence );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase#handleCreate(java.util.Collection)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Collection<CompositeSequence> handleCreate( Collection compositeSequences ) throws Exception {
        return this.getCompositeSequenceDao().create( compositeSequences );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase#handleFindByName(java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Collection<CompositeSequence> handleFindByName( String name ) throws Exception {
        return this.getCompositeSequenceDao().findByName( name );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase#handleFindByName(ubic.gemma.model.expression.arrayDesign.ArrayDesign,
     *      java.lang.String)
     */
    @Override
    protected CompositeSequence handleFindByName( ArrayDesign arrayDesign, String name ) throws Exception {
        return this.getCompositeSequenceDao().findByName( arrayDesign, name );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase#handleRemove(Collection)
     */
    @Override
    protected void handleRemove( java.util.Collection sequencesToDelete ) throws java.lang.Exception {

        // check the collection to make sure it contains no transitive entities (just check the id and make sure its
        // non-null

        Collection<CompositeSequence> filteredSequence = new Vector<CompositeSequence>();
        for ( Object sequence : sequencesToDelete ) {
            if ( ( ( CompositeSequence ) sequence ).getId() != null )
                filteredSequence.add( ( CompositeSequence ) sequence );
        }

        this.getCompositeSequenceDao().remove( filteredSequence );
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase#handleUpdate(ubic.gemma.model.expression.designElement.CompositeSequence)
     */
    @Override
    protected void handleUpdate( CompositeSequence compositeSequence ) throws Exception {
        this.getCompositeSequenceDao().update( compositeSequence );
    }

    /*
     * Checks to see if the CompositeSequence exists in any of the array designs. If so, it is internally stored in the
     * collection of composite sequences as a {@link LinkedHashSet), preserving order based on insertion.   
     * (non-Javadoc)
     * 
     * @see ubic.gemma.model.expression.designElement.CompositeSequenceServiceBase#handleGetMatchingCompositeSequences(java.lang.String[],
     *      java.util.Collection)
     */
    @Override
    protected Collection handleFindByNamesInArrayDesigns( Collection compositeSequenceNames, Collection arrayDesigns )
            throws Exception {
        LinkedHashMap<String, CompositeSequence> compositeSequencesMap = new LinkedHashMap<String, CompositeSequence>();

        Iterator iter = arrayDesigns.iterator();

        while ( iter.hasNext() ) {
            ArrayDesign arrayDesign = ( ArrayDesign ) iter.next();

            for ( Object obj : compositeSequenceNames ) {
                String name = ( String ) obj;
                name = StringUtils.trim( name );
                log.debug( "entered: " + name );
                CompositeSequence cs = this.findByName( arrayDesign, name );
                if ( cs != null && !compositeSequencesMap.containsKey( cs.getName() ) ) {
                    compositeSequencesMap.put( cs.getName(), cs );
                } else {
                    log.warn( "Composite sequence " + name + " does not exist." );
                }
            }
        }

        if ( compositeSequencesMap.isEmpty() ) return null;

        return compositeSequencesMap.values();
    }

}