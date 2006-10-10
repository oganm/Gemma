/*
 * The Gemma project
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
package ubic.gemma.loader.util.concurrent;

import java.io.IOException;
import java.io.InputStream;

import ubic.gemma.loader.util.parser.Parser;

/**
 * See http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * 
 * @author pavlidis
 * @version $Id$
 */
public class ParsingStreamConsumer extends Thread {
    InputStream is;
    Parser parser;

    public ParsingStreamConsumer( Parser parser, InputStream is ) {
        this.is = is;
        this.parser = parser;
    }

    public void run() {
        try {
            parser.parse( is );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
