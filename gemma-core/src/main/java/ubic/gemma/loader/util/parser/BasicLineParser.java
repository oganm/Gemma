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
package ubic.gemma.loader.util.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.gemma.util.progress.ProgressData;
import ubic.gemma.util.progress.ProgressManager;

/**
 * A simple LineParser implementation that doesn't do anything. Subclass this and implement the "parseOneLine" method.
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class BasicLineParser implements LineParser {

    private static final String COMMENTMARK = "#";

    protected Log log = LogFactory.getLog( getClass() );

    protected int linesParsed = 0;

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.io.reader.LineParser#parse(java.io.InputStream)
     */
    public void parse( InputStream is ) throws IOException {

        if ( is == null ) {
            throw new IllegalArgumentException( "Inputstream null" );
        }

        if ( is.available() == 0 ) {
            throw new IOException( "No bytes available to read from inputStream" );
        }

        linesParsed = 0;
        int nullLines = 0;
        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

        String line = null;

        while ( ( line = br.readLine() ) != null ) {

            if ( line.startsWith( COMMENTMARK ) ) {
                continue;
            }

            Object newItem = parseOneLine( line );

            if ( newItem != null ) {
                addResult( newItem );
            } else {
                // if ( log.isDebugEnabled() ) log.debug( "Got null parse from " + line );
                nullLines++;
            }
            
            if ( ++linesParsed % PARSE_ALERT_FREQUENCY == 0 ) {
                String message = "Parsed " + linesParsed + " lines...";
                ProgressManager.updateCurrentThreadsProgressJob( new ProgressData( 0, message ) );
                log.debug( message );
            }

        }
        log.info( "Parsed " + linesParsed + " lines. "
                + ( nullLines > 0 ? nullLines + " yielded no parse result (they may have been filtered)." : "" ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.io.reader.LineParser#parse(java.io.File)
     */
    public void parse( File file ) throws IOException {
        if ( file == null ) {
            throw new IllegalArgumentException( "File cannot be null" );
        }
        if ( !file.exists() || !file.canRead() ) {
            throw new IOException( "Could not read from file " + file.getPath() );
        }
        InputStream stream = FileTools.getInputStreamFromPlainOrCompressedFile( file.getAbsolutePath() );
        parse( stream );
        stream.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.io.reader.LineParser#pasre(java.lang.String)
     */
    public void parse( String filename ) throws IOException {
        if ( StringUtils.isBlank( filename ) ) {
            throw new IllegalArgumentException( "No filename provided" );
        }
        log.info( "Parsing " + filename );
        File infile = new File( filename );
        parse( infile );
    }

    /**
     * Add an object to the results collection.
     * 
     * @param obj
     */
    protected abstract void addResult( Object obj );

    /**
     * 
     */
    public abstract Collection getResults();

}
