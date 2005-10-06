/*
 * The Gemma project.
 * 
 * Copyright (c) 2005 Columbia University
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
package edu.columbia.gemma.expression.designElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 * @see edu.columbia.gemma.expression.designElement.Reporter
 */
public class ReporterDaoImpl extends edu.columbia.gemma.expression.designElement.ReporterDaoBase {

    private static Log log = LogFactory.getLog( ReporterDaoImpl.class.getName() );

    /*
     * (non-Javadoc)
     * 
     * @see edu.columbia.gemma.expression.designElement.ReporterDaoBase#findOrCreate(edu.columbia.gemma.expression.designElement.Reporter)
     */
    @Override
    public Reporter findOrCreate( Reporter reporter ) {
        if ( reporter.getName() == null || reporter.getArrayDesign() == null ) {
            log.debug( "reporter must name and arrayDesign." );
            return null;
        }
        Reporter newreporter = ( Reporter ) this.find( reporter );
        if ( newreporter != null ) {
            return newreporter;
        }
        if ( log.isDebugEnabled() ) log.debug( "Creating new reporter: " + reporter.getName() );
        return ( Reporter ) create( reporter );

    }

}