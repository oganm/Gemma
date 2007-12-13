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
package ubic.gemma.loader.entrez;

import junit.framework.TestCase;

/**
 * @author paul
 * @version $Id$
 */
public class EutilFetchTest extends TestCase {

    final public void testFetch() throws Exception {
        String result = EutilFetch.fetch( "gds", "GSE4595", 2 );
        assertNotNull( result );
        assertTrue( "Got " + result, result.startsWith( "1: GSE4595 record:" ) );
    }

}
