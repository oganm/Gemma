package ubic.gemma.core.search;

import net.sf.ehcache.CacheManager;
import org.compass.core.Compass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ubic.gemma.core.annotation.reference.BibliographicReferenceService;
import ubic.gemma.core.association.phenotype.PhenotypeAssociationManagerService;
import ubic.gemma.core.genome.gene.service.GeneSearchService;
import ubic.gemma.core.genome.gene.service.GeneService;
import ubic.gemma.core.genome.gene.service.GeneSetService;
import ubic.gemma.core.ontology.OntologyService;
import ubic.gemma.core.search.source.CompassSearchSource;
import ubic.gemma.core.search.source.DatabaseSearchSource;
import ubic.gemma.core.security.audit.AuditableUtil;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.analysis.expression.diff.ContrastResult;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.common.description.BibliographicReferenceValueObject;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.designElement.CompositeSequenceValueObject;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.DatabaseBackedGeneSetValueObject;
import ubic.gemma.model.genome.gene.GeneSet;
import ubic.gemma.model.genome.gene.phenotype.valueObject.CharacteristicValueObject;
import ubic.gemma.persistence.service.common.description.CharacteristicService;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.expression.designElement.CompositeSequenceService;
import ubic.gemma.persistence.service.expression.experiment.BlacklistedEntityDao;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentSetService;
import ubic.gemma.persistence.service.genome.biosequence.BioSequenceService;
import ubic.gemma.persistence.service.genome.gene.GeneProductService;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test conversion to VOs for search results.
 *
 * The conversion is typically performed with a {@link ubic.gemma.persistence.util.ValueObjectConverter} which in turn
 * relies upon specific {@link ubic.gemma.persistence.service.BaseVoEnabledService} logic for performing the VO
 * conversion. Thus, we want to make sure that the service will produce the expected VOs.
 *
 * @author poirigui
 */
@ContextConfiguration
public class SearchServiceVoConversionTest extends AbstractJUnit4SpringContextTests {

    @Configuration
    public static class SearchServiceVoConversionTestContextConfiguration {

        @Bean
        public SearchService searchService() {
            return new SearchServiceImpl();
        }

        @Bean
        public CacheManager cacheManager() {
            return mock( CacheManager.class );
        }

        @Bean
        public SearchSource compassSearchSource() {
            return mock( CompassSearchSource.class );
        }

        @Bean
        public Compass compassArray() {
            return mock( Compass.class );
        }

        @Bean
        public Compass compassBibliographic() {
            return mock( Compass.class );
        }

        @Bean
        public Compass compassBiosequence() {
            return mock( Compass.class );
        }

        @Bean
        public Compass compassExperimentSet() {
            return mock( Compass.class );
        }

        @Bean
        public Compass compassExpression() {
            return mock( Compass.class );
        }

        @Bean
        public Compass compassGene() {
            return mock( Compass.class );
        }

        @Bean
        public Compass compassGeneSet() {
            return mock( Compass.class );
        }

        @Bean
        public Compass compassProbe() {
            return mock( Compass.class );
        }

        @Bean
        public DatabaseSearchSource databaseSearchSource() {
            return mock( DatabaseSearchSource.class );
        }

        @Bean
        public GeneProductService geneProductService() {
            return mock( GeneProductService.class );
        }

        @Bean
        public ArrayDesignService arrayDesignService() {
            return mock( ArrayDesignService.class );
        }

        @Bean
        public BibliographicReferenceService bibliographicReferenceService() {
            return mock( BibliographicReferenceService.class );
        }

        @Bean
        public CharacteristicService characteristicService() {
            return mock( CharacteristicService.class );
        }

        @Bean
        public ExpressionExperimentService expressionExperimentService() {
            return mock( ExpressionExperimentService.class );
        }

        @Bean
        public ExpressionExperimentSetService experimentSetService() {
            return mock( ExpressionExperimentSetService.class );
        }

        @Bean
        public GeneSearchService geneSearchService() {
            return mock( GeneSearchService.class );
        }

        @Bean
        public GeneService geneService() {
            return mock( GeneService.class );
        }

        @Bean
        public GeneSetService geneSetService() {
            return mock( GeneSetService.class );
        }

        @Bean
        public OntologyService ontologyService() {
            return mock( OntologyService.class );
        }

        @Bean
        public PhenotypeAssociationManagerService phenotypeAssociationManagerService() {
            return mock( PhenotypeAssociationManagerService.class );
        }

        @Bean
        public BioSequenceService bioSequenceService() {
            return mock( BioSequenceService.class );
        }

        @Bean
        public CompositeSequenceService compositeSequenceService() {
            return mock( CompositeSequenceService.class );
        }

        @Bean
        public BlacklistedEntityDao blackListDao() {
            return mock( BlacklistedEntityDao.class );
        }

        @Bean
        public TaxonService taxonService() {
            return mock( TaxonService.class );
        }

        @Bean
        public AuditableUtil auditableUtil() {
            return mock( AuditableUtil.class );
        }
    }

    @Autowired
    private SearchService searchService;

    @Autowired
    private ArrayDesignService arrayDesignService;
    @Autowired
    private CompositeSequenceService compositeSequenceService;
    @Autowired
    private ExpressionExperimentService expressionExperimentService;
    @Autowired
    private GeneSetService geneSetService;
    @Autowired
    private BibliographicReferenceService bibliographicReferenceService;

    /* fixtures */
    private ArrayDesign ad;
    private CompositeSequence cs;
    private ExpressionExperiment ee;
    private GeneSet gs;
    private CharacteristicValueObject phenotypeAssociation;

    @Before
    public void setUp() {
        ad = new ArrayDesign();
        ad.setId( 11L );
        ad.setPrimaryTaxon( Taxon.Factory.newInstance( "Homo sapiens", "Human", 9606, false ) );
        cs = new CompositeSequence();
        cs.setId( 10L );
        cs.setArrayDesign( ad );
        ee = new ExpressionExperiment();
        ee.setId( 12L );
        gs = new GeneSet();
        gs.setId( 13L );
        phenotypeAssociation = new CharacteristicValueObject();
        phenotypeAssociation.setId( 14L );
        when( arrayDesignService.loadValueObject( any( ArrayDesign.class ) ) ).thenAnswer( a -> new ArrayDesignValueObject( a.getArgument( 0, ArrayDesign.class ) ) );
        //noinspection unchecked
        when( arrayDesignService.loadValueObjects( anyCollection() ) ).thenAnswer( a -> ( ( Collection<ArrayDesign> ) a.getArgument( 0, Collection.class ) )
                .stream()
                .map( ArrayDesignValueObject::new )
                .collect( Collectors.toList() ) );
        when( compositeSequenceService.loadValueObjectWithoutGeneMappingSummary( any( CompositeSequence.class ) ) ).thenAnswer( a -> new CompositeSequenceValueObject( a.getArgument( 0, CompositeSequence.class ) ) );
        when( expressionExperimentService.loadValueObject( any( ExpressionExperiment.class ) ) ).thenAnswer( a -> new ExpressionExperimentValueObject( a.getArgument( 0, ExpressionExperiment.class ) ) );
        when( geneSetService.loadValueObject( any( GeneSet.class ) ) ).thenAnswer( a -> {
            GeneSet geneSet = a.getArgument( 0, GeneSet.class );
            DatabaseBackedGeneSetValueObject geneSetVo = new DatabaseBackedGeneSetValueObject();
            geneSetVo.setId( geneSet.getId() );
            return geneSetVo;
        } );
    }

    @After
    public void tearDown() {
        reset( arrayDesignService, expressionExperimentService, geneSetService, compositeSequenceService, bibliographicReferenceService );
    }

    @Test
    public void testConvertArrayDesign() {
        searchService.loadValueObject( new SearchResult<>( ad ) );
        verify( arrayDesignService ).loadValueObject( ad );
    }

    @Test
    public void testConvertArrayDesignCollection() {
        searchService.loadValueObjects( Collections.singleton( new SearchResult<>( ad ) ) );
        verify( arrayDesignService ).loadValueObjects( Collections.singletonList( ad ) );
    }

    @Test
    public void testConvertBibliographicReference() {
        BibliographicReference br = new BibliographicReference();
        when( bibliographicReferenceService.loadValueObject( any( BibliographicReference.class ) ) )
                .thenAnswer( arg -> new BibliographicReferenceValueObject( arg.getArgument( 0, BibliographicReference.class ) ) );
        br.setId( 13L );
        searchService.loadValueObject( new SearchResult<>( br ) );
        verify( bibliographicReferenceService ).loadValueObject( br );
    }

    @Test
    public void testConvertCompositeSequence() {
        searchService.loadValueObject( new SearchResult<>( cs ) );
        verify( compositeSequenceService ).loadValueObjectWithoutGeneMappingSummary( cs );
    }

    @Test
    public void testConvertCompositeSequenceCollection() {
        // this is a special case because of how it's implemented
        searchService.loadValueObjects( Collections.singleton( new SearchResult<>( cs ) ) );
        verify( compositeSequenceService ).loadValueObjectWithoutGeneMappingSummary( cs );
    }

    @Test
    public void testConvertExpressionExperiment() {
        searchService.loadValueObject( new SearchResult<>( ee ) );
        verify( expressionExperimentService ).loadValueObject( ee );
    }

    @Test
    public void testConvertPhenotypeAssociation() {
        // this is a complicated one because
        assertThat( searchService.loadValueObject( new SearchResult<>( phenotypeAssociation ) ) )
                .extracting( "resultObject" )
                .isSameAs( phenotypeAssociation );
    }

    @Test
    public void testConvertGeneSet() {
        // this is another complicated one because GeneSetService does not implement BaseVoEnabledService
        searchService.loadValueObject( new SearchResult<>( gs ) );
        verify( geneSetService ).loadValueObject( gs );
    }

    @Test
    public void testConvertUninitializedResult() {
        SearchResult<IdentifiableValueObject<Identifiable>> sr = searchService.loadValueObject( new SearchResult<>( GeneSet.class, 12L, 1.0, null ) );
        verifyNoInteractions( geneSetService );
        assertThat( sr )
                .isNotNull()
                .hasFieldOrPropertyWithValue( "resultClass", GeneSet.class )
                .hasFieldOrPropertyWithValue( "resultId", 12L )
                .hasFieldOrPropertyWithValue( "score", 1.0 )
                .hasFieldOrPropertyWithValue( "highlightedText", null );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedResultTypeRaisesIllegalArgumentException() {
        searchService.loadValueObject( new SearchResult<>( new ContrastResult() ) );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedResultTypeInCollectionRaisesIllegalArgumentException() {
        searchService.loadValueObjects( Collections.singleton( new SearchResult<>( new ContrastResult() ) ) );
    }
}