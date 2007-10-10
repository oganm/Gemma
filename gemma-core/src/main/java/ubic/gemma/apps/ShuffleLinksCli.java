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
package ubic.gemma.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import ubic.basecode.dataStructure.matrix.CompressedNamedBitMatrix;
import ubic.gemma.analysis.linkAnalysis.LinkAnalysisUtilService;
import ubic.gemma.model.association.coexpression.Probe2ProbeCoexpressionService;
import ubic.gemma.model.association.coexpression.Probe2ProbeCoexpressionDaoImpl.Link;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.GeneService;
import ubic.gemma.util.AbstractSpringAwareCLI;

import com.ibm.icu.text.NumberFormat;

/**
 * Used to generate the link(gene pair) background distribution, which could be used to estimate the false positive
 * rates under different levels of confirmation. There are two steps to finish this process. a) The first step is to
 * prepare the working table named with "CLEANED" as prefix. The reason to make this step separately is as follows; In
 * Gemma, the link tables store each links twice (the duplicate one with firstDesignElement and secondDesignElement
 * switched) to speed up the online co-expression query. Some huge expression experiments give rise to a large amount of
 * links more than 10M. However, the shuffling need to go through all expression experiments one by one to extract all
 * links for each expression experiment and this process is required to repeat many times (default is 100) to get better
 * estimation on the background distribution. Therefore, to speed up the shuffling process, the first step will create a
 * new table to save the links without redundancy. It could also do some filtering (only save links for known genes).
 * Then the next step will do the shuffling on the "CLEANED" tables, which could run much faster.
 * <p>
 * Note: The function GetProbeCoExpression in P2PService would need a third boolean parameter, whose true value
 * indicates the "CLEANED" table while the false value for original link tables;
 * </p>
 * 
 * <pre>
 * java -Xmx5000M -XX:+UseParallelGC  -jar ShuffleLinksCli.jar -s -f mouse_brain_dataset.txt  -t mouse -u administrator -p testing -v 3
 * </pre>
 * 
 * <p>
 * b) The second step is to do the shuffling on the "cleaned" table.
 * </p>
 * 
 * <pre>
 * java -Xmx5000M -XX:+UseParallelGC  -jar ShuffleLinksCli.jar -i 100 -f mouse_brain_dataset.txt  -t mouse -u administrator -p testing -v 3
 * </pre>
 * 
 * @author xwan
 * @version $Id$
 */
public class ShuffleLinksCli extends AbstractSpringAwareCLI {

    /**
     * Collect statistics for links replicated up to this number of times. (?)
     */
    private final static int LINK_MAXIMUM_COUNT = 100;

    public static void main( String[] args ) {
        ShuffleLinksCli shuffle = new ShuffleLinksCli();
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Exception ex = shuffle.doWork( args );
            if ( ex != null ) {
                ex.printStackTrace();
            }
            watch.stop();
            log.info( watch.getTime() / 1000 );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private String taxonName = "mouse";
    private boolean prepared = true;
    private String eeNameFile = null;
    private Probe2ProbeCoexpressionService p2pService = null;
    private GeneService geneService = null;
    private ExpressionExperimentService eeService = null;
    private LinkAnalysisUtilService linkAnalysisUtilService = null;
    private Map<Long, Integer> eeMap = null;
    private int numIterationsToDo = 100;
    private CompressedNamedBitMatrix linkCount = null;
    private CompressedNamedBitMatrix negativeLinkCount = null;
    private int[][] stats = null;
    private int currentIteration = 0;
    private int linkStringency = 0;
    private int totalLinks = 0;

    private Set<Long> geneCoverage = new HashSet<Long>();

    private void counting() {
        int rows = linkCount.rows();
        int cols = linkCount.columns();
        // The filling process only filled one item. So the matrix is not symetric
        for ( int i = 0; i < rows; i++ ) {
            int[] positiveBits = linkCount.getRowBitCount( i );
            int[] negativeBits = negativeLinkCount.getRowBitCount( i );
            for ( int j = 0; j < cols; j++ ) {
                int positiveBit = positiveBits[j];
                int negativeBit = negativeBits[j];
                if ( positiveBit > 0 ) {
                    stats[currentIteration][positiveBit]++;
                }
                if ( negativeBit > 0 ) {
                    stats[currentIteration][negativeBit]++;
                }
            }
        }

    }

    /**
     * @param ees
     */
    @SuppressWarnings("unchecked")
    private void doShuffling( Collection<ExpressionExperiment> ees ) {
        int total = 0;
        for ( ExpressionExperiment ee : ees ) {
            log.info( "Shuffling " + ee.getShortName() );
            Collection<Link> links = p2pService.getProbeCoExpression( ee, this.taxonName, true );
            if ( links == null || links.size() == 0 ) continue;
            if ( currentIteration != 0 ) {
                total = total + links.size();
                shuffleLinks( links );
            }
            fillingMatrix( links, ee );
        }
        counting();
        log.info( " Shuffled " + total + " links" );
    }

    /**
     * @param links
     * @param ee
     */
    @SuppressWarnings("unchecked")
    private void fillingMatrix( Collection<Link> links, ExpressionExperiment ee ) {
        Set<Long> csIds = new HashSet<Long>();
        for ( Link link : links ) {
            csIds.add( link.getFirst_design_element_fk() );
            csIds.add( link.getSecond_design_element_fk() );
        }
        Map<Long, Collection<Long>> cs2genes = geneService.getCS2GeneMap( csIds );
        int eeIndex = eeMap.get( ee.getId() );
        for ( Link link : links ) {
            Collection<Long> firstGeneIds = cs2genes.get( link.getFirst_design_element_fk() );
            Collection<Long> secondGeneIds = cs2genes.get( link.getSecond_design_element_fk() );
            if ( firstGeneIds == null || secondGeneIds == null ) {
                log.info( " Preparation is not correct (get null genes) " + link.getFirst_design_element_fk() + ","
                        + link.getSecond_design_element_fk() );
                continue;
            }
            // if(firstGeneIds.size() != 1 || secondGeneIds.size() != 1){
            // log.info(" Preparation is not correct (get non-specific genes)" + link.getFirst_design_element_fk() + ","
            // + link.getSecond_design_element_fk());
            // System.exit(0);
            // }
            for ( Long firstGeneId : firstGeneIds ) {
                for ( Long secondGeneId : secondGeneIds ) {
                    firstGeneId = firstGeneIds.iterator().next();
                    secondGeneId = secondGeneIds.iterator().next();
                    geneCoverage.add( firstGeneId );
                    geneCoverage.add( secondGeneId );
                    try {
                        int rowIndex = linkCount.getRowIndexByName( firstGeneId );
                        int colIndex = linkCount.getColIndexByName( secondGeneId );
                        if ( link.getScore() > 0 ) {
                            linkCount.set( rowIndex, colIndex, eeIndex );
                        } else {
                            negativeLinkCount.set( rowIndex, colIndex, eeIndex );
                        }
                    } catch ( Exception e ) {
                        log.info( " No Gene Definition " + firstGeneId + "," + secondGeneId );
                        // Aligned Region and Predicted Gene
                        continue;
                    }
                }
            }
            totalLinks++;
        }
    }

    /**
     * @param fileName Contains list of EE short names to use (essentially filters)
     * @param ees All the EEs for the chosen taxon.
     * @return
     */
    private Collection<ExpressionExperiment> loadExpressionExperiments( String fileName,
            Collection<ExpressionExperiment> ees ) {
        if ( fileName == null ) return ees;
        Collection<ExpressionExperiment> candidates = new HashSet<ExpressionExperiment>();
        Collection<String> eeNames = new HashSet<String>();
        try {
            InputStream is = new FileInputStream( fileName );
            BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            String shortName = null;
            while ( ( shortName = br.readLine() ) != null ) {
                if ( StringUtils.isBlank( shortName ) ) continue;
                eeNames.add( shortName.trim().toUpperCase() );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return candidates;
        }
        for ( ExpressionExperiment ee : ees ) {
            String shortName = ee.getShortName();
            if ( eeNames.contains( shortName.trim().toUpperCase() ) ) candidates.add( ee );
        }
        return candidates;
    }

    /**
     * @param ees
     * @param genes
     * @return
     */
    private CompressedNamedBitMatrix getMatrix( Collection<ExpressionExperiment> ees, Collection<Gene> genes ) {
        CompressedNamedBitMatrix linkCount = new CompressedNamedBitMatrix( genes.size(), genes.size(), ees.size() );
        for ( Gene geneIter : genes ) {
            linkCount.addRowName( geneIter.getId() );
        }
        for ( Gene geneIter : genes ) {
            linkCount.addColumnName( geneIter.getId() );
        }
        return linkCount;
    }

    /**
     * @param outFile
     * @param genes
     */
    private void saveMatrix( String outFile, Collection<Gene> genes ) {
        Map<Long, String> geneId2Name = new HashMap<Long, String>();
        for ( Gene gene : genes ) {
            geneId2Name.put( gene.getId(), gene.getName() );
        }
        try {
            FileWriter out = new FileWriter( new File( outFile ) );
            int rows = linkCount.rows();
            int cols = linkCount.columns();
            // The filling process only filled one item. So the matrix is not symetric
            for ( int i = 0; i < rows; i++ ) {
                int[] positiveBits = linkCount.getRowBitCount( i );
                int[] negativeBits = negativeLinkCount.getRowBitCount( i );

                for ( int j = 0; j < cols; j++ ) {
                    int positiveBit = positiveBits[j];
                    int negativeBit = negativeBits[j];
                    if ( this.linkStringency > 0 ) {
                        if ( positiveBit >= this.linkStringency ) {
                            out.write( geneId2Name.get( linkCount.getRowName( i ) ) + "\t"
                                    + geneId2Name.get( linkCount.getColName( j ) ) + "\t" + positiveBit + "\t" + "+"
                                    + "\n" );
                        }
                        if ( negativeBit >= this.linkStringency ) {
                            out.write( geneId2Name.get( linkCount.getRowName( i ) ) + "\t"
                                    + geneId2Name.get( linkCount.getColName( j ) ) + "\t" + negativeBit + "\t" + "-"
                                    + "\n" );
                        }
                    } else {
                        if ( positiveBit != 0 || negativeBit != 0 ) {
                            out.write( geneId2Name.get( linkCount.getRowName( i ) ) + "\t"
                                    + geneId2Name.get( linkCount.getColName( j ) ) + "\t" + positiveBit + "\t"
                                    + negativeBit + "\n" );
                        }
                    }
                }
            }
            out.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * @param outFile
     */
    private void saveStats( String outFile ) {
        try {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits( 3 );

            FileWriter out = new FileWriter( new File( outFile ) );
            for ( int i = 0; i < numIterationsToDo + 1; i++ ) {
                for ( int j = LINK_MAXIMUM_COUNT - 2; j >= 0; j-- ) {
                    stats[i][j] = stats[i][j] + stats[i][j + 1];
                }
            }
            int maxBits = 0;
            for ( int j = LINK_MAXIMUM_COUNT - 1; j >= 0; j-- ) {
                if ( stats[0][j] != 0 ) {
                    maxBits = j;
                    break;
                }
            }
            for ( int j = 1; j <= maxBits; j++ )
                out.write( "Link" + j + "\t" );
            out.write( "\n" );
            out.write( "Link Distribution:\n" );
            for ( int j = 1; j <= maxBits; j++ ) {
                out.write( stats[0][j] + "\t" );
            }
            out.write( "\n" );
            out.write( "Average False Positive Rate\n" );
            double[] falsePositiveRates = new double[maxBits + 1];
            for ( int i = 1; i < numIterationsToDo + 1; i++ ) {
                for ( int j = 1; j <= maxBits; j++ ) {
                    if ( stats[0][j] != 0 )
                        falsePositiveRates[j] = falsePositiveRates[j] + ( double ) stats[i][j] / ( double ) stats[0][j];
                }
            }
            for ( int j = 1; j < maxBits; j++ ) {
                out.write( nf.format( falsePositiveRates[j] / numIterationsToDo ) + "\t" );
            }
            out.write( "\n" );
            out.write( "All shuffled false positive rates:\n" );
            for ( int i = 1; i < numIterationsToDo + 1; i++ ) {
                for ( int j = 1; j <= maxBits; j++ ) {
                    if ( stats[0][j] == 0 )
                        out.write( "\t" );
                    else
                        out.write( nf.format( ( double ) stats[i][j] / ( double ) stats[0][j] ) + "\t" );
                }
                out.write( "\n" );
            }
            out.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Do shuffling
     * 
     * @param links
     */
    private void shuffleLinks( Collection<Link> links ) {
        Random random = new Random();
        Object[] linksInArray = links.toArray();
        for ( int i = linksInArray.length - 1; i >= 0; i-- ) {
            int pos = random.nextInt( i + 1 );
            Long tmpId = ( ( Link ) linksInArray[pos] ).getSecond_design_element_fk();
            ( ( Link ) linksInArray[pos] ).setSecond_design_element_fk( ( ( Link ) linksInArray[i] )
                    .getSecond_design_element_fk() );
            ( ( Link ) linksInArray[i] ).setSecond_design_element_fk( tmpId );
        }
    }

    @SuppressWarnings("static-access")
    @Override
    protected void buildOptions() {
        Option taxonOption = OptionBuilder.hasArg().withArgName( "Taxon" ).withDescription(
                "the taxon name (default=mouse)s" ).withLongOpt( "Taxon" ).create( 't' );
        addOption( taxonOption );

        Option eeNameFile = OptionBuilder.hasArg().withArgName( "File having Expression Experiment Names" )
                .withDescription( "File having Expression Experiment Names" ).withLongOpt( "eeFileName" ).create( 'f' );
        addOption( eeNameFile );
        Option startPreparing = OptionBuilder.withArgName( " Starting preparing " ).withDescription(
                " Starting preparing the temporary tables " ).withLongOpt( "startPreparing" ).create( 's' );
        addOption( startPreparing );

        Option iterationNum = OptionBuilder.hasArg().withArgName( " The number of iteration for shuffling " )
                .withDescription( " The number of iterations for shuffling (default = 100 " ).withLongOpt(
                        "iterationNum" ).create( 'i' );
        addOption( iterationNum );

        /*
         * Not sure what this does.
         */
        Option linkStringency = OptionBuilder.hasArg().withArgName( " The Link Stringency " ).withDescription(
                " The link Stringency " ).withLongOpt( "linkStringency" ).create( 'l' );
        addOption( linkStringency );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Exception doWork( String[] args ) {
        Exception err = processCommandLine( "Shuffle Links ", args );
        if ( err != null ) {
            return err;
        }
        Taxon taxon = linkAnalysisUtilService.getTaxon( taxonName );
        Collection<ExpressionExperiment> ees = eeService.findByTaxon( taxon );
        Collection<ExpressionExperiment> candidates = loadExpressionExperiments( this.eeNameFile, ees );
        if ( !prepared ) {
            log.info( " Create intermediate tables for shuffling " );
            StopWatch watch = new StopWatch();
            watch.start();
            p2pService.prepareForShuffling( candidates, taxonName );
            watch.stop();
            log.info( " Spent " + watch.getTime() / 1000 + " to finish the preparation " );
            System.exit( 0 );
        }
        Collection<Gene> genes = linkAnalysisUtilService.loadGenes( taxon );
        eeMap = new HashMap<Long, Integer>();
        int index = 0;
        for ( ExpressionExperiment eeIter : candidates ) {
            eeMap.put( eeIter.getId(), new Integer( index ) );
            index++;
        }
        if ( linkStringency != 0 ) {
            totalLinks = 0;
            linkCount = getMatrix( ees, genes );
            negativeLinkCount = getMatrix( ees, genes );
            System.gc();
            doShuffling( candidates );
            saveMatrix( "matrix_" + linkStringency + ".txt", genes );
            log.info( "Total Links " + totalLinks );
            log.info( "Covered Gene " + geneCoverage.size() );
        } else {
            // The first iteration doesn't do the shuffling and only read the real data and do the counting
            for ( currentIteration = 0; currentIteration < numIterationsToDo + 1; currentIteration++ ) {
                totalLinks = 0;
                linkCount = getMatrix( ees, genes );
                negativeLinkCount = getMatrix( ees, genes );
                System.gc();
                doShuffling( candidates );
                saveMatrix( "matrix" + currentIteration + ".txt", genes );
            }
            saveStats( "stats.txt" );
        }

        return null;
    }

    /**
     * 
     */
    protected void processOptions() {
        super.processOptions();
        if ( hasOption( 't' ) ) {
            this.taxonName = getOptionValue( 't' );
        }
        if ( hasOption( 'f' ) ) {
            this.eeNameFile = getOptionValue( 'f' );
        }
        if ( hasOption( 's' ) ) {
            this.prepared = false;
        }
        if ( hasOption( 'i' ) ) {
            this.numIterationsToDo = Integer.valueOf( getOptionValue( 'i' ) );
        }
        if ( hasOption( 'l' ) ) {
            this.linkStringency = Integer.valueOf( getOptionValue( 'l' ) );
        }

        p2pService = ( Probe2ProbeCoexpressionService ) this.getBean( "probe2ProbeCoexpressionService" );
        geneService = ( GeneService ) this.getBean( "geneService" );
        eeService = ( ExpressionExperimentService ) this.getBean( "expressionExperimentService" );
        linkAnalysisUtilService = ( LinkAnalysisUtilService ) this.getBean( "linkAnalysisUtilService" );
        stats = new int[numIterationsToDo + 1][LINK_MAXIMUM_COUNT];
    }

}
