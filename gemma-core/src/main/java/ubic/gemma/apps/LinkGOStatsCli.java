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
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.directwebremoting.convert.ArrayConverter;

import ubic.basecode.dataStructure.matrix.CompressedNamedBitMatrix;
import ubic.gemma.analysis.linkAnalysis.LinkAnalysisUtilService;
import ubic.gemma.analysis.linkAnalysis.LinkBitMatrixUtil;
import ubic.gemma.model.association.coexpression.Probe2ProbeCoexpressionService;
import ubic.gemma.model.association.coexpression.Probe2ProbeCoexpressionDaoImpl.Link;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.PredictedGeneImpl;
import ubic.gemma.model.genome.ProbeAlignedRegionImpl;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.TaxonService;
import ubic.gemma.model.genome.gene.GeneService;
import ubic.gemma.util.AbstractSpringAwareCLI;

public class LinkGOStatsCli extends AbstractSpringAwareCLI {
	
	private final static int GO_MAXIMUM_COUNT = 200;
	private final static int MAXIMUM_LINK_NUM = 100;
	private final static int ITERATION_NUM = 100;
	private final static int CHUNK_NUM = 20;
	private Probe2ProbeCoexpressionService p2pService = null;
	private LinkAnalysisUtilService linkAnalysisUtilService = null;
	private ExpressionExperimentService eeService = null;
	private GeneService geneService = null;
    private CompressedNamedBitMatrix linkCount = null;
	private int[][] realStats = null;
	private int[][] simulatedStats = null;
	private String taxonName = "mouse";
	private String eeNameFile = null;
	private Map<Long, Integer> eeIndexMap = new HashMap<Long, Integer>();
	private int[] goTermsDistribution = null;
	@Override
	protected void buildOptions() {
		// TODO Auto-generated method stub
        Option taxonOption = OptionBuilder.hasArg().withArgName( "Taxon" ).withDescription(
        "the taxon name" ).withLongOpt( "Taxon" ).create( 't' );
        addOption( taxonOption );
        Option eeNameFile = OptionBuilder.hasArg().withArgName( "File having Expression Experiment Names" ).withDescription(
        "File having Expression Experiment Names" ).withLongOpt( "eeFileName" ).create( 'f' );
        addOption( eeNameFile );

	}
    protected void processOptions() {
        super.processOptions();
        if ( hasOption( 't' ) ) {
            this.taxonName = getOptionValue( 't' );
        }
        if ( hasOption( 'f' ) ) {
            this.eeNameFile = getOptionValue( 'f' );
        }

        p2pService = (Probe2ProbeCoexpressionService) this.getBean ( "probe2ProbeCoexpressionService" );
        linkAnalysisUtilService = (LinkAnalysisUtilService) this.getBean( "linkAnalysisUtilService" );
        eeService = (ExpressionExperimentService) this.getBean( "expressionExperimentService" );
        geneService = (GeneService) this.getBean( "geneService" );
        realStats= new int[MAXIMUM_LINK_NUM][GO_MAXIMUM_COUNT];
        simulatedStats= new int[ITERATION_NUM/CHUNK_NUM + 1][GO_MAXIMUM_COUNT];
        goTermsDistribution = new int[GO_MAXIMUM_COUNT];
    }
    private Collection<ExpressionExperiment> getCandidateEE(String fileName, Collection<ExpressionExperiment> ees){
        if(fileName == null) return ees;
        Collection<ExpressionExperiment> candidates = new HashSet<ExpressionExperiment>();
        Collection<String> eeNames = new HashSet<String>();
        try {
            InputStream is = new FileInputStream( fileName );
            BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            String shortName = null;
            while ( ( shortName = br.readLine() ) != null ) {
                if ( StringUtils.isBlank( shortName ) ) continue;
                eeNames.add( shortName.trim().toUpperCase());
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return candidates;
        }
        for(ExpressionExperiment ee:ees){
            String shortName = ee.getShortName();
            if(eeNames.contains( shortName.trim().toUpperCase() )) candidates.add( ee );
        }
        return candidates;
    }
    private void fillingMatrix(Collection<Link> links, ExpressionExperiment ee){
        Set<Long> csIds = new HashSet<Long>();
        for(Link link:links){
            csIds.add(link.getFirst_design_element_fk());
            csIds.add(link.getSecond_design_element_fk());
        }
        Map<Long, Collection<Long>> cs2genes = geneService.getCS2GeneMap( csIds );
        int eeIndex = eeIndexMap.get(ee.getId());
        for(Link link:links){
       		Collection<Long> firstGeneIds =cs2genes.get( link.getFirst_design_element_fk() );
       		Collection<Long> secondGeneIds =cs2genes.get( link.getSecond_design_element_fk() );
            if(firstGeneIds == null || secondGeneIds == null){
            	log.info(" Preparation is not correct (get null genes) " + link.getFirst_design_element_fk() + "," + link.getSecond_design_element_fk());
            	continue;
            }
//            if(firstGeneIds.size() != 1 || secondGeneIds.size() != 1){
//            	log.info(" Preparation is not correct (get non-specific genes)" + link.getFirst_design_element_fk() + "," + link.getSecond_design_element_fk());
//            	System.exit(0);
//            }
            for(Long firstGeneId:firstGeneIds){
            	for(Long secondGeneId:secondGeneIds){
                    firstGeneId = firstGeneIds.iterator().next();
                    secondGeneId = secondGeneIds.iterator().next();
                    try{
                    	int rowIndex = linkCount.getRowIndexByName(firstGeneId);
                    	int colIndex = linkCount.getColIndexByName(secondGeneId);
                        linkCount.set( rowIndex, colIndex, eeIndex );
                        linkCount.set( colIndex, rowIndex, eeIndex );
                    }catch(Exception e){
                    	log.info(" No Gene Definition " + firstGeneId + "," + secondGeneId);
                    	//Aligned Region and Predicted Gene
                    	continue;
                    }
            	}
            }
        }
    }
    private void counting(){
        int rows = linkCount.rows();
        int cols = linkCount.columns();
        for(int i = 0; i < rows; i++){
        	int[] bits = new int[cols];
        	bits = linkCount.getRowBits(i, bits);
        	for(int j = i+1; j < cols; j++){
                int bit = bits[j];
                if(bit > 0){
                	int goOverlap = linkAnalysisUtilService.computeGOOverlap((Long)linkCount.getRowName(i),(Long)linkCount.getColName(j));
                    realStats[bit][goOverlap]++;
                }
        	}
        }

    }
    private Gene[] shuffling(Gene[] genes){
    	Gene[] shuffledGenes = new Gene[genes.length];
    	System.arraycopy(genes, 0, shuffledGenes, 0, genes.length);
        Random random = new Random();
        for(int i = genes.length - 1; i >= 0; i--){
            int pos = random.nextInt(i+1);
            Gene tmp = shuffledGenes[pos];
            shuffledGenes[pos] = shuffledGenes[i];
            shuffledGenes[i] = tmp;
        }
        return shuffledGenes;
    }
    private void counting(Gene[] genes, Gene[] shuffledGenes, int iterationIndex){
    	for(int i = 0; i < genes.length; i++){
    		if(genes[i].getId() == shuffledGenes[i].getId()) continue;
        	int goOverlap = linkAnalysisUtilService.computeGOOverlap(genes[i], shuffledGenes[i]);
            simulatedStats[iterationIndex][goOverlap]++;
            simulatedStats[ITERATION_NUM/CHUNK_NUM][goOverlap]++;
    	}
    }
    private void output(){
        int totalSimulatedLinks[] = new int[ITERATION_NUM/CHUNK_NUM+1], totalRealLinks[] = new int[MAXIMUM_LINK_NUM];
        for(int i = 0; i < ITERATION_NUM/CHUNK_NUM + 1; i++){
        	for(int j = 0; j < GO_MAXIMUM_COUNT; j++){
        	totalSimulatedLinks[i] = totalSimulatedLinks[i] + simulatedStats[i][j];
        	}
        }
        for(int i = 1; i < MAXIMUM_LINK_NUM; i++){
        	for(int j = 0; j < GO_MAXIMUM_COUNT; j++){
        		totalRealLinks[i] = totalRealLinks[i] + realStats[i][j];
        	}
        }
        try{
            FileWriter out = new FileWriter( new File( "goSimilarity.txt" ) );
            double culmulative = 0.0;
            for(int i = ITERATION_NUM/CHUNK_NUM; i < ITERATION_NUM/CHUNK_NUM + 1; i++){
            	for(int j = 0; j < GO_MAXIMUM_COUNT; j++){
            		culmulative = culmulative + (double)simulatedStats[i][j]/(double)totalSimulatedLinks[i];
            		out.write(culmulative+"\t");
            	}
            }
            out.write("\n");
            for(int i = 1; i < 30; i++){
            	culmulative = 0.0;
            	for(int j = 0; j < GO_MAXIMUM_COUNT; j++){
            		culmulative = culmulative + (double)realStats[i][j]/(double)totalRealLinks[i];
            		out.write(culmulative+"\t");
            	}
            	out.write("\n");
            }
            out.write("\nRandom Pair Generation Distribution:\n");
            for(int i = 0; i < ITERATION_NUM/CHUNK_NUM + 1; i++){
            	culmulative = 0.0;
            	for(int j = 0; j < GO_MAXIMUM_COUNT; j++){
            		culmulative = culmulative + (double)simulatedStats[i][j]/(double)totalSimulatedLinks[i];
            		out.write(culmulative+"\t");
            	}
            	out.write("\n");
            }
            
            int total = 0;
            for(int i = 0; i < GO_MAXIMUM_COUNT; i++)
            	total = total + goTermsDistribution[i];
            out.write("Go Terms Distribution:\n");
            for(int i = 0; i < GO_MAXIMUM_COUNT; i++){
            	out.write((double)goTermsDistribution[i]/(double)total + "\t");
            }
            out.write("\n");
            out.close();
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
	@Override
	protected Exception doWork(String[] args) {
		// TODO Auto-generated method stub
        Exception err = processCommandLine( "Shuffle Links ", args );
        if ( err != null ) {
            return err;
        }
        Taxon taxon = linkAnalysisUtilService.getTaxon( taxonName );
        Collection <ExpressionExperiment> ees = eeService.findByTaxon(taxon);
        Collection <ExpressionExperiment> eeCandidates = getCandidateEE(this.eeNameFile, ees);
        Collection<Gene> allGenes = linkAnalysisUtilService.loadGenes(taxon);

        
        Gene[] genes = new Gene[allGenes.size()];
        int index = 0;
        for(Gene gene:allGenes){
        	System.err.println(index + "/" + allGenes.size());
        	genes[index++] = gene;
        	int goTermsNum = linkAnalysisUtilService.getGoTerms(gene).size();
        	goTermsDistribution[goTermsNum]++;
        }
        for(int i = 0; i < ITERATION_NUM; i++){
        	log.info("Current Iteration: " + i);
        	Gene[] shuffledGenes = shuffling(genes);
        	counting(genes, shuffledGenes, (int)(i/CHUNK_NUM));
        }
        index = 0;
        for(ExpressionExperiment ee:eeCandidates){
        	eeIndexMap.put(ee.getId(), index);
        	index++;
        }
        linkCount = new CompressedNamedBitMatrix(allGenes.size(), allGenes.size(), eeCandidates.size());
        for(Gene geneIter:allGenes){
            linkCount.addRowName(geneIter.getId());
            linkCount.addColumnName(geneIter.getId());
        }
        for(ExpressionExperiment ee:eeCandidates){
        	log.info("Shuffling " + ee.getShortName() );
            Collection<Link> links = p2pService.getProbeCoExpression( ee, this.taxonName, true );
            if(links == null || links.size() == 0) continue;
            fillingMatrix(links,ee);
        }
        log.info("Counting the GO terms for real links");
        counting();
        log.info("Output");
        output();

		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkGOStatsCli goStats = new LinkGOStatsCli();
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Exception ex = goStats.doWork( args );
            if ( ex != null ) {
                ex.printStackTrace();
            }
            watch.stop();
            log.info( watch.getTime()/1000 );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
	}

}
