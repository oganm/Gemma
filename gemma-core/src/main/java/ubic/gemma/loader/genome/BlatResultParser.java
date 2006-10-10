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
package ubic.gemma.loader.genome;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.loader.util.parser.BasicLineParser;
import ubic.gemma.model.genome.Chromosome;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.sequenceAnalysis.BlatResult;

/**
 * Loader to handle results generated by Jim Kent's Blat.
 * <p>
 * The PSL file format is described at {@link http://genome.ucsc.edu/FAQ/FAQformat#format2}. Blank lines are skipped as
 * are valid PSL headers.
 * <p>
 * Target sequences are assumed to be chromosomes. If a chromosome name (chr10 or chr10.fa) is detected, the name is
 * stripped to be a chromosome number only (e.g. 10). Otherwise, the value is used as is. If the query name starts with
 * "target:", this is removed.
 * <p>
 * Results can be filtered by setting the scoreThreshold parameter.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class BlatResultParser extends BasicLineParser {
    protected static final Log log = LogFactory.getLog( BlatResultParser.class );
    private static final int NUM_BLAT_FIELDS = 21;

    private static final int MATCHES_FIELD = 0;
    private static final int MISMATCHES_FIELD = 1;
    private static final int REPMATCHES_FIELD = 2;
    private static final int NS_FIELD = 3;
    private static final int QGAPCOUNT_FIELD = 4;
    private static final int QGAPBASES_FIELD = 5;
    private static final int TGAPCOUNT_FIELD = 6;
    private static final int TGAPBASES_FIELD = 7;
    private static final int STRAND_FIELD = 8;
    private static final int QNAME_FIELD = 9;
    private static final int QSIZE_FIELD = 10;
    private static final int QSTART_FIELD = 11;
    private static final int QEND_FIELD = 12;
    private static final int TNAME_FIELD = 13;
    private static final int TSIZE_FIELD = 14;
    private static final int TSTART_FIELD = 15;
    private static final int TEND_FIELD = 16;
    private static final int BLOCKCOUNT_FIELD = 17;
    private static final int BLOCKSIZES_FIELD = 18;
    private static final int QSTARTS_FIELD = 19;
    private static final int TSTARTS_FIELD = 20;

    private Taxon taxon;

    private Collection<BlatResult> results = new HashSet<BlatResult>();
    private double scoreThreshold = 0.0;

    /**
     * Define a threshold, below which results are ignored. By default all results are read in.
     * 
     * @param score
     */
    public void setScoreThreshold( double score ) {
        this.scoreThreshold = score;
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.io.reader.BasicParser#parseOneLine(java.lang.String)
     */
    public Object parseOneLine( String line ) {

        if ( StringUtils.isBlank( line ) ) return null;

        try {
            // header format (starts of lines only are shown:
            // psLayout version 3
            //
            // match mis- rep. N's Q gap Q gap T gap T gap strand Q Q Q Q T T T T block blockSizes qStarts tStarts
            // (spaces)match match
            // ------------------
            // check if it is a header line.
            if ( line.startsWith( "psLayout" ) || line.startsWith( "match" ) || line.startsWith( "    " )
                    || line.startsWith( "-----------------------" ) ) {
                return null;
            }

            String[] f = line.split( "\t" );
            if ( f.length == 0 ) return null;
            if ( f.length != NUM_BLAT_FIELDS )
                throw new IllegalArgumentException( "Only" + f.length + " fields in line (starts with "
                        + line.substring( 0, Math.max( line.length(), 25 ) ) );

            BlatResult result = BlatResult.Factory.newInstance();

            String name = ( "BlatResult:" + f[QNAME_FIELD] + ":" + f[QSTART_FIELD] + ":" + f[TSTART_FIELD] + ":" + f[TNAME_FIELD] );
            result.setId( new Long( name.hashCode() ) );
            result.setQuerySequence( BioSequence.Factory.newInstance() );
            result.getQuerySequence().setLength( Long.parseLong( f[QSIZE_FIELD] ) );

            result.setMatches( Integer.parseInt( f[MATCHES_FIELD] ) );
            result.setMismatches( Integer.parseInt( f[MISMATCHES_FIELD] ) );
            result.setRepMatches( Integer.parseInt( f[REPMATCHES_FIELD] ) );
            result.setNs( Integer.parseInt( f[NS_FIELD] ) );
            result.setQueryGapCount( Integer.parseInt( f[QGAPCOUNT_FIELD] ) );
            result.setQueryGapBases( Integer.parseInt( f[QGAPBASES_FIELD] ) );
            result.setTargetGapBases( Integer.parseInt( f[TGAPBASES_FIELD] ) );
            result.setTargetGapCount( Integer.parseInt( f[TGAPCOUNT_FIELD] ) );
            result.setStrand( f[STRAND_FIELD] );
            result.setTargetChromosome( Chromosome.Factory.newInstance() );
            result.setQueryStart( Integer.parseInt( f[QSTART_FIELD] ) );
            result.setQueryEnd( Integer.parseInt( f[QEND_FIELD] ) );
            result.setTargetStart( Long.parseLong( f[TSTART_FIELD] ) );
            result.setTargetEnd( Long.parseLong( f[TEND_FIELD] ) );
            result.setBlockCount( Integer.parseInt( f[BLOCKCOUNT_FIELD] ) );
            result.setBlockSizes( f[BLOCKSIZES_FIELD] ); // FIXME - there should be an aligned regions
            // association.
            result.setQueryStarts( f[QSTARTS_FIELD] );
            result.setTargetStarts( f[TSTARTS_FIELD] );

            String queryName = f[QNAME_FIELD];
            queryName = cleanUpQueryName( queryName );
            result.getQuerySequence().setName( queryName );

            String chrom = f[TNAME_FIELD];
            if ( chrom.startsWith( "chr" ) ) {
                chrom = chrom.substring( chrom.indexOf( "chr" ) + 3 );
                if ( chrom.endsWith( ".fa" ) ) {
                    chrom = chrom.substring( 0, chrom.indexOf( ".fa" ) );
                }
            }
            result.getTargetChromosome().setName( chrom );
            result.getTargetChromosome().setSequence( BioSequence.Factory.newInstance() );
            result.getTargetChromosome().getSequence().setName( chrom );
            result.getTargetChromosome().getSequence().setLength( Long.parseLong( f[TSIZE_FIELD] ) );

            if ( taxon != null ) {
                result.getTargetChromosome().setTaxon( taxon );
                result.getTargetChromosome().getSequence().setTaxon( taxon );
            }

            if ( scoreThreshold > 0.0 && result.score() < scoreThreshold ) {
                return null;
            }

            return result;
        } catch ( NumberFormatException e ) {
            log.error( "Invalid number format", e );
            return null;
        } catch ( IllegalArgumentException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * @param queryName
     * @return
     */
    private String cleanUpQueryName( String queryName ) {
        queryName = queryName.replace( "target:", "" );
        queryName = queryName.replaceFirst( ";$", "" );
        return queryName;
    }

    @Override
    protected void addResult( Object obj ) {
        results.add( ( BlatResult ) obj );

    }

    @Override
    public Collection<BlatResult> getResults() {
        return results;
    }

    /**
     * @return the taxon
     */
    public Taxon getTaxon() {
        return this.taxon;
    }

    /**
     * @param taxon the taxon to set
     */
    public void setTaxon( Taxon taxon ) {
        this.taxon = taxon;
    }
}
