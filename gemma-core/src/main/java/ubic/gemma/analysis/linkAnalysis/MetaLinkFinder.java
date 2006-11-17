/**
 * 
 */
package ubic.gemma.analysis.linkAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.CompressedNamedBitMatrix;
import ubic.gemma.model.association.coexpression.Probe2ProbeCoexpression;
import ubic.gemma.model.association.coexpression.Probe2ProbeCoexpressionService;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVector;
import ubic.gemma.model.expression.bioAssayData.DesignElementDataVectorService;
import ubic.gemma.model.expression.designElement.DesignElement;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.GeneService;

/**
 * @author xwan
 * This finder does the query on the Probe2ProbeCoexpression and outputs the meta links between genes
 *
 */
public class MetaLinkFinder {
    private Probe2ProbeCoexpressionService ppService = null;
    private DesignElementDataVectorService deService = null;
    private GeneService geneService = null;
    private ExpressionExperimentService eeService = null;
    private CompressedNamedBitMatrix linkCount = null;
    private HashMap<Long, Integer> eeMap = null;
    private Vector allEE = null;
    protected static final Log log = LogFactory.getLog( MetaLinkFinder.class );
    
    public MetaLinkFinder(Probe2ProbeCoexpressionService ppService, DesignElementDataVectorService deService, ExpressionExperimentService eeService, GeneService geneService){
    	assert(ppService != null);
    	assert(deService != null);
    	assert(geneService != null);
    	assert(eeService != null);
    	this.ppService = ppService;
    	this.deService = deService;
    	this.geneService = geneService;
    	this.eeService = eeService;
    }
    
    private Collection getExpressionExperiment(Taxon taxon){
    	Collection<ExpressionExperiment> all_ee = this.eeService.loadAll();
    	Collection<ExpressionExperiment> ees = new HashSet<ExpressionExperiment>();
    	if(all_ee == null || all_ee.size() == 0) return null;
    	
    	for(ExpressionExperiment ee:all_ee){
    		if(this.eeService.getTaxon(ee.getId()).equals(taxon))
    			ees.add(ee);
    	}
    	return ees;
    }
    public void find(Taxon taxon, QuantitationType qt){
    	Collection <Gene> genes = geneService.getGenesByTaxon(taxon);
    	if(genes == null || genes.size() == 0) return;
    	this.find(genes, qt);
    }
    public void find(Collection<Gene> genes, QuantitationType qt){
    	if(genes == null || genes.size() == 0) return;
    	Taxon taxon = genes.iterator().next().getTaxon();
    	Collection <ExpressionExperiment> ees = this.getExpressionExperiment(taxon);
    	if(ees == null || ees.size() == 0) return;
    	this.find(genes, ees, qt);
    }
    
    public void find(Collection<Gene> genes, Collection <ExpressionExperiment> ees, QuantitationType qt){
    	if(genes == null || ees == null || qt == null || genes.size() == 0 || ees.size() == 0) return;
    	Collection <Gene> genesInTaxon = this.geneService.getGenesByTaxon(genes.iterator().next().getTaxon());
    	if(genesInTaxon == null || genesInTaxon.size() == 0) return;
    	
    	this.init(genes, ees, genesInTaxon);
    	
    	for(Gene gene:genes){
    		Collection<DesignElementDataVector> p2plinks = ppService.findCoexpressionRelationships(gene,ees,qt);
    		if(p2plinks == null || p2plinks.size() == 0) continue;
    		log.info("Get "+ p2plinks.size() + " links");
    		this.count(gene, p2plinks);
    	}

    }
    public Gene getRowGene(int i){
    	Object geneId = this.linkCount.getRowName(i);
    	Gene gene = this.geneService.load(((Long)geneId).longValue());
    	return gene;
    }
    
    public Gene getColGene(int i){
    	Object geneId = this.linkCount.getColName(i);
    	Gene gene = this.geneService.load(((Long)geneId).longValue());
    	return gene;
    }
    
    public ExpressionExperiment getEE(int i){
    	Object eeId = this.allEE.elementAt(i);
    	ExpressionExperiment ee = this.eeService.findById((Long)eeId);
    	return ee;
    }

    public void output(int num){
    	int count = 0;
    	for(int i = 0; i < this.linkCount.rows(); i++)
    		for(int j = 0; j < this.linkCount.columns(); j++){
    			if(this.linkCount.bitCount(i,j) >= num){
    				System.err.println(this.getRowGene(i).getName() + "  " + this.getColGene(j).getName());
    				count++;
    			}
    		}
    	System.err.println("Total Links " + count);
    }
    private void count(Gene rowGene, Collection<DesignElementDataVector> p2plinks){
    	int rowIndex = -1, colIndex = -1, eeIndex = -1;
    	ExpressionExperiment ee = null;
    	
    	rowIndex = this.linkCount.getRowIndexByName(rowGene.getId());

    	Integer index = null;
    	
    	HashMap<Object, Set> probeToGenes = (HashMap)this.deService.getGenes(p2plinks);
    	for(DesignElementDataVector p2pIter:p2plinks){
    		ee = p2pIter.getExpressionExperiment();
    		index = this.eeMap.get(ee.getId());
    		if(index == null){
    			log.info("Couldn't find the ee index for ee " + ee.getId());
    			continue;
    		}
    		eeIndex = index.intValue();
    		
    		HashSet <Gene> pairedGenes = (HashSet)probeToGenes.get(p2pIter);
    		if(pairedGenes == null || pairedGenes.size() == 0){
    			continue;
    		}
    		if(pairedGenes.contains(rowGene)){
    			continue;
    		}
    		for(Gene colGene:pairedGenes){
        		colIndex = this.linkCount.getColIndexByName(colGene.getId());
        		if(colIndex >= 0 && colIndex < this.linkCount.columns())
        			this.linkCount.set(rowIndex,colIndex,eeIndex);
    		}
    	}
    }
    private void init(Collection<Gene> genes, Collection <ExpressionExperiment> ees, Collection<Gene> genesInTaxon){
    	int index = 0;
    	
    	this.linkCount = new CompressedNamedBitMatrix(genes.size(), genesInTaxon.size(), ees.size());
    	
    	for(Gene geneIter:genes){
    		this.linkCount.addRowName(geneIter.getId());
    	}
    	
    	for(Gene geneIter:genesInTaxon){
    		this.linkCount.addColumnName(geneIter.getId());
    	}
    	
    	this.eeMap = new HashMap();
    	this.allEE = new Vector();
    	for(ExpressionExperiment eeIter:ees){
    		eeMap.put(eeIter.getId(), new Integer(index));
    		this.allEE.add(eeIter.getId());
    		index++;
    	}
    	
    }
}
