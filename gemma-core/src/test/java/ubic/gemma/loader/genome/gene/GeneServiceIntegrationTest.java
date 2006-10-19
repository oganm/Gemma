package ubic.gemma.loader.genome.gene;

import java.io.InputStream;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import ubic.gemma.apps.Blat;
import ubic.gemma.loader.expression.arrayDesign.ArrayDesignProbeMapperService;
import ubic.gemma.loader.expression.arrayDesign.ArrayDesignProbeMapperServiceIntegrationTest;
import ubic.gemma.loader.expression.arrayDesign.ArrayDesignSequenceAlignmentService;
import ubic.gemma.loader.expression.arrayDesign.ArrayDesignSequenceProcessingService;
import ubic.gemma.loader.expression.geo.GeoDomainObjectGenerator;
import ubic.gemma.loader.expression.geo.service.AbstractGeoService;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.TaxonService;
import ubic.gemma.model.genome.biosequence.SequenceType;
import ubic.gemma.model.genome.gene.GeneService;
import ubic.gemma.model.genome.sequenceAnalysis.BlatResult;
import ubic.gemma.testing.BaseSpringContextTest;

public class GeneServiceIntegrationTest extends BaseSpringContextTest {
    ArrayDesignProbeMapperServiceIntegrationTest pTest = new ArrayDesignProbeMapperServiceIntegrationTest();

    String officialName = "PPARA";
    String accession = "GPL140";
    
    @SuppressWarnings("unchecked")
    public void testGetCompositeSequenceCountById() throws Exception {

        // get geneService
        GeneService geneService = ( GeneService ) this.getBean( "geneService" );
        // get a gene to get the id
        Collection<Gene> geneCollection = geneService.findByOfficialSymbol( officialName );
        Gene g = geneCollection.iterator().next();
        long count = geneService.getCompositeSequenceCountById( g.getId() );
        assert ( count != 0 );
    }
    
    @SuppressWarnings("unchecked")
    public void testGetCompositeSequencesById() throws Exception {

        // get geneService
        GeneService geneService = ( GeneService ) this.getBean( "geneService" );
        // get a gene to get the id
        Collection<Gene> geneCollection = geneService.findByOfficialSymbol( officialName );
        Gene g = geneCollection.iterator().next();
        Collection<CompositeSequence> compSequences = geneService.getCompositeSequencesById( g.getId() );
        assert ( compSequences.size() != 0 );
    }
    
    
    // preloads GPL140. See ArrayDesignProbeMapperServiceIntegrationTest
    @SuppressWarnings("unchecked")
    protected void onSetUp() throws Exception {
            super.onSetUp();
            
            ArrayDesign ad;
            // first load small twoc-color
            AbstractGeoService geoService = ( AbstractGeoService ) this.getBean( "geoDatasetService" );
            geoService.setGeoDomainObjectGenerator( new GeoDomainObjectGenerator() );
            geoService.setLoadPlatformOnly( true );
            final Collection<ArrayDesign> ads = ( Collection<ArrayDesign> ) geoService.fetchAndLoad( accession );
            ad = ads.iterator().next();

            ArrayDesignService arrayDesignService = ( ArrayDesignService ) this.getBean( "arrayDesignService" );

            arrayDesignService.thaw( ad );
            
            Taxon taxon = ( ( TaxonService ) getBean( "taxonService" ) ).findByScientificName( "Homo sapiens" );

            // needed to fill in the sequence information for blat scoring.
            InputStream sequenceFile = this.getClass().getResourceAsStream( "/data/loader/genome/gpl140.sequences.fasta" );
            ArrayDesignSequenceProcessingService app = ( ArrayDesignSequenceProcessingService ) getBean( "arrayDesignSequenceProcessingService" );
            app.processArrayDesign( ad, sequenceFile, SequenceType.EST, taxon );

            // fill in the blat results. Note that each time you run this test you get the results loaded again (so they
            // pile up)
            ArrayDesignSequenceAlignmentService aligner = ( ArrayDesignSequenceAlignmentService ) getBean( "arrayDesignSequenceAlignmentService" );

            InputStream blatResultInputStream = new GZIPInputStream( this.getClass().getResourceAsStream(
                    "/data/loader/genome/gpl140.blatresults.psl.gz" ) );
            
            Blat blat = new Blat();
            Collection<BlatResult> results = blat.processPsl( blatResultInputStream, taxon );

            aligner.processArrayDesign( ad, results, taxon );

            // real stuff.
            ArrayDesignProbeMapperService arrayDesignProbeMapperService = ( ArrayDesignProbeMapperService ) this
                    .getBean( "arrayDesignProbeMapperService" );
            arrayDesignProbeMapperService.processArrayDesign( ad, taxon );
    }
}
