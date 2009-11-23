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
package ubic.gemma.model.expression.experiment;

import java.util.Collection;

import org.springframework.stereotype.Service;

/**
 * @author pavlidis
 * @version $Id$
 * @see ubic.gemma.model.expression.experiment.ExperimentalFactorService
 */
@Service
public class ExperimentalFactorServiceImpl extends ubic.gemma.model.expression.experiment.ExperimentalFactorServiceBase {

    public Collection<ExperimentalFactor> create( Collection<ExperimentalFactor> factors ) {
        return ( Collection<ExperimentalFactor> ) this.getExperimentalFactorDao().create( factors );
    }

    /*
     * (non-Javadoc)
     * @see
     * ubic.gemma.model.expression.experiment.ExperimentalFactorServiceBase#handleCreate(ubic.gemma.model.expression
     * .experiment.ExperimentalFactor)
     */
    @Override
    protected ExperimentalFactor handleCreate( ExperimentalFactor experimentalFactor ) throws Exception {
        return this.getExperimentalFactorDao().create( experimentalFactor );
    }

    @Override
    protected void handleDelete( ExperimentalFactor experimentalFactor ) throws Exception {
        this.getExperimentalFactorDao().remove( experimentalFactor );
    }

    /*
     * (non-Javadoc)
     * @see
     * ubic.gemma.model.expression.experiment.ExperimentalFactorServiceBase#handleFindOrcreate(ubic.gemma.model.expression
     * .experiment.ExperimentalFactor)
     */
    @Override
    protected ExperimentalFactor handleFind( ExperimentalFactor experimentalFactor ) throws Exception {
        return this.getExperimentalFactorDao().find( experimentalFactor );
    }

    /*
     * (non-Javadoc)
     * @see
     * ubic.gemma.model.expression.experiment.ExperimentalFactorServiceBase#handleFindOrcreate(ubic.gemma.model.expression
     * .experiment.ExperimentalFactor)
     */
    @Override
    protected ExperimentalFactor handleFindOrCreate( ExperimentalFactor experimentalFactor ) throws Exception {
        return this.getExperimentalFactorDao().findOrCreate( experimentalFactor );
    }

    /*
     * (non-Javadoc)
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorServiceBase#handleFindByName(java.lang.String)
     */
    @Override
    protected ExperimentalFactor handleLoad( Long id ) throws Exception {
        return this.getExperimentalFactorDao().load( id );
    }

    /**
     * @see ubic.gemma.model.expression.experiment.ExperimentalFactorService#getAllExperimentalFactors()
     */
    @Override
    protected java.util.Collection<ExperimentalFactor> handleLoadAll() throws java.lang.Exception {
        return ( Collection<ExperimentalFactor> ) this.getExperimentalFactorDao().loadAll();
    }

    @Override
    protected void handleUpdate( ExperimentalFactor experimentalFactor ) throws Exception {
        this.getExperimentalFactorDao().update( experimentalFactor );
    }

}