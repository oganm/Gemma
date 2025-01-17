/*
 * The Gemma project
 *
 * Copyright (c) 2008 University of British Columbia
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
package ubic.gemma.persistence.util;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Methods to create Spring contexts for Gemma manually. This is meant to be used by CLIs only.
 *
 * @author pavlidis
 */
public class SpringContextUtil {

    private static final Log log = LogFactory.getLog( SpringContextUtil.class.getName() );

    /**
     * Obtain an application context for Gemma.
     *
     * @param testing                           If true, it will get a test-configured application context
     * @param isWebApp                          If true, a {@link UnsupportedOperationException} will be raised since
     *                                          retrieving the web application context is not supported from here. Use
     *                                          WebApplicationContextUtils.getWebApplicationContext() instead. This is
     *                                          only kept for backward-compatibility with external scripts.
     * @param additionalConfigurationLocations, like "classpath*:/myproject/applicationContext-mine.xml"
     * @return a fully initialized {@link ApplicationContext}
     * @throws org.springframework.beans.BeansException if the creation of the context fails
     */
    public static ApplicationContext getApplicationContext( boolean testing, boolean isWebApp, String[] additionalConfigurationLocations ) throws BeansException {
        if ( isWebApp ) {
            throw new UnsupportedOperationException( "The Web app context cannot be retrieved from here, use WebApplicationContextUtils.getWebApplicationContext() instead." );
        }

        List<String> paths = new ArrayList<>();

        paths.add( "classpath*:gemma/gsec/applicationContext-*.xml" );
        paths.add( "classpath*:ubic/gemma/applicationContext-*.xml" );

        if ( testing ) {
            paths.add( "classpath:ubic/gemma/testDataSource.xml" );
        } else {
            paths.add( "classpath:ubic/gemma/dataSource.xml" );
        }

        if ( additionalConfigurationLocations != null ) {
            paths.addAll( Arrays.asList( additionalConfigurationLocations ) );
        }

        StopWatch timer = StopWatch.createStarted();
        try {
            return new ClassPathXmlApplicationContext( paths.toArray( new String[0] ) );
        } finally {
            SpringContextUtil.log.info( "Got context in " + timer.getTime() + "ms" );
        }
    }
}