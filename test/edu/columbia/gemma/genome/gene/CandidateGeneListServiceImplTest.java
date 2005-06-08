package edu.columbia.gemma.genome.gene;

import edu.columbia.gemma.BaseDAOTestCase;
import edu.columbia.gemma.genome.Gene;
import edu.columbia.gemma.genome.GeneDao;
import edu.columbia.gemma.genome.Taxon;
import edu.columbia.gemma.genome.TaxonDao;
import edu.columbia.gemma.common.auditAndSecurity.Person;
import edu.columbia.gemma.common.auditAndSecurity.PersonDao;
import edu.columbia.gemma.common.auditAndSecurity.Security;
import edu.columbia.gemma.common.auditAndSecurity.SecurityDao;
import edu.columbia.gemma.genome.gene.CandidateGeneList;

/**
 *
 *
 * <hr>
 * <p>Copyright (c) 2004, 2005 Columbia University
 * @author daq2101
 * @version $Id$
 */
public class CandidateGeneListServiceImplTest extends BaseDAOTestCase {

    private TaxonDao daoTaxon = null;
    private int foo=2;
    private GeneDao daoGene= null;
    private Gene g, g2, g3, g4 = null;;
    private Person p = null;
    private PersonDao daoPerson= null;
    private Taxon t = null;
    private Security s = null;
    private SecurityDao daoSecurity = null;
    
    protected void setUp() throws Exception {
        daoTaxon = (TaxonDao)ctx.getBean("taxonDao");
        daoGene = (GeneDao)ctx.getBean("geneDao");
        daoPerson = (PersonDao)ctx.getBean("personDao");
        daoSecurity = (SecurityDao)ctx.getBean("securityDao");
        t = Taxon.Factory.newInstance();
        t.setScientificName("test testicus");
        t.setCommonName("common test");
        daoTaxon.create(t);
        
        g = Gene.Factory.newInstance();
        g.setName("test gene one");
        g.setOfficialName("test gene one");
        g.setTaxon(t);
        
        g2 = Gene.Factory.newInstance();
        g2.setName("test gene two");
        g2.setOfficialName("test gene two");
        g2.setTaxon(t);
        
        g3 = Gene.Factory.newInstance();
        g3.setName("test gene three");
        g3.setOfficialName("test gene three");
        g3.setTaxon(t);
        
        g4 = Gene.Factory.newInstance();
        g4.setName("test gene four");
        g4.setOfficialName("test gene four");
        g4.setTaxon(t);
        
        p = Person.Factory.newInstance();
        
        p.setFirstName("David");
        p.setLastName("Quigley");
        p.setEmail("daq2101@columbia.edu");
        daoPerson.create(p);

        daoGene.create(g);
        daoGene.create(g2);
        daoGene.create(g3);
        daoGene.create(g4);
    }
    
    protected void tearDown() throws Exception {
        
        daoGene.remove(g);
        daoGene.remove(g2);
        daoGene.remove(g3);
        daoGene.remove(g4);
        daoTaxon.remove(t);
        daoPerson.remove(p);
        /*
         * daoSecurity.remove(s);
        */
    }
    public void testCandidateGeneListServiceImpl(){
        
        // test create CandidateGeneList
        CandidateGeneListService svc = (CandidateGeneListService)ctx.getBean("candidateGeneListService");
        svc.setActor(p);
        
        CandidateGeneList cgl = svc.createCandidateGeneList("New Candidate List from service test");
        Long cgl_id = cgl.getId();
        
        // test add/remove candidates
        CandidateGene cg = svc.addCandidateToCandidateGeneList(cgl, g);
        CandidateGene cg2 = svc.addCandidateToCandidateGeneList(cgl, g2);
        CandidateGene cg3 = svc.addCandidateToCandidateGeneList(cgl, g3);
        // test Add by ID; I already know the ID of G4 since I just created it.
        CandidateGene cg4 = svc.addCandidateToCandidateGeneList(cgl, g4.getId().longValue());
        
        cg2.setOwner(p);
        svc.saveCandidateGeneList(cgl);
        svc.removeCandidateFromCandidateGeneList(cgl, cg);
        
        // test rank manipulation
        svc.decreaseCandidateRanking(cgl, cg2);
        svc.increaseCandidateRanking(cgl, cg2);
        svc.saveCandidateGeneList(cgl);
        // test finders
        java.util.Collection cByName =null;
        java.util.Collection cByContributer =null;
        java.util.Collection cAll = null;
        
        try{
        cByName = svc.findByGeneOfficialName("test gene two");
        cByContributer = svc.findByContributer(p);
        System.out.println("By Name: " + cByName.size());
        System.out.println("By Contributor: " + cByContributer.size());
        cAll = svc.getAll();
        System.out.println("All: " + cAll.size() + " candidate lists.");
        cgl = svc.findByID(cgl_id.longValue());
        
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        assertTrue( cByName!= null && cByName.size()==1);
        assertTrue( cByContributer != null && cByContributer.size()>=1);
        assertTrue( cAll != null && cAll.size()>=1);
        assertTrue( cgl != null );
        assertTrue( cg4 != null );
        // test remove CandidateGeneList
        svc.removeCandidateGeneList(cgl);
        
    }
}