package edu.columbia.gemma.sequence.gene;

import edu.columbia.gemma.BaseDAOTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.springframework.dao.DataIntegrityViolationException;
import java.util.Collection;
/**
 * <HR>
 * <P>
 * Copyright (c) 2005 Columbia University
 * @author daq2101
 */
public class CandidateGeneDaoImplTest extends BaseDAOTestCase {

    private final Log log = LogFactory.getLog(CandidateGeneDaoImplTest.class);
    private CandidateGeneDao daoCG = null;
    private GeneDao daoGene = null;
    private TaxonDao daoTaxon = null;
    private Gene g = null;
    private CandidateGene cg = null;
	private Taxon t = null;
    protected void setUp() throws Exception {
		super.setUp();
	
		// create a taxon and a gene for that taxon
		daoCG = (CandidateGeneDao) ctx.getBean("candidateGeneDao");
		daoGene = (GeneDao) ctx.getBean("geneDao");
		daoTaxon = (TaxonDao) ctx.getBean("taxonDao");
		
		t = daoTaxon.findByCommonName("mouse");
		if( t==null){
		    t = Taxon.Factory.newInstance();
		    t.setCommonName("mouse");
			t.setName("mouse");
			daoTaxon.create(t);
		}
		Collection c = daoGene.findByOfficalName("testmygene");
		if( c.isEmpty() ){
		    g = Gene.Factory.newInstance();
			g.setName("testmygene");
			g.setSymbol("foo");
			g.setOfficialName("testmygene");
			g.setTaxon(t);
			daoGene.create(g);
		}
		else
		    g = (Gene)c.iterator().next();
	}


	protected void tearDown() throws Exception {
		super.tearDown();
	    daoCG.remove(cg);
		daoCG = null;
		daoGene.remove(g);
		daoGene = null;
		daoTaxon.remove(t);
		daoTaxon=null;
	}

	public void testSetCandidateGene() {
	  
	    cg = CandidateGene.Factory.newInstance();
	    cg.setRank(new Integer(1));
	    Gene gFromDB = (Gene) daoGene.findByOfficalName("testmygene").iterator().next();
	    cg.setGene(gFromDB);
	    daoCG.create(cg);
	    
	    assertTrue(cg.getGene().equals(gFromDB));
	    
	}

}
