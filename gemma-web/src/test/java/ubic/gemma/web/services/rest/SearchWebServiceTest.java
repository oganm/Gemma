package ubic.gemma.web.services.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import ubic.gemma.core.search.SearchResult;
import ubic.gemma.core.search.SearchService;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.GeneValueObject;
import ubic.gemma.persistence.service.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.persistence.service.genome.taxon.TaxonService;
import ubic.gemma.web.services.rest.util.ResponseDataObject;
import ubic.gemma.web.services.rest.util.args.PlatformArg;
import ubic.gemma.web.services.rest.util.args.TaxonArg;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebAppConfiguration
@ContextConfiguration
public class SearchWebServiceTest extends AbstractJUnit4SpringContextTests {

    @Configuration
    public static class SearchWebServiceTestContextConfiguration {

        @Bean
        public SearchWebService searchWebService() {
            return new SearchWebService();
        }

        @Bean
        public SearchService searchService() {
            return mock( SearchService.class );
        }

        @Bean
        public TaxonService taxonService() {
            return mock( TaxonService.class );
        }

        @Bean
        public ArrayDesignService arrayDesignService() {
            return mock( ArrayDesignService.class );
        }
    }

    @Autowired
    private SearchWebService searchWebService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private TaxonService taxonService;
    @Autowired
    private ArrayDesignService arrayDesignService;

    /* fixtures */
    private Gene gene;
    private Taxon taxon;
    private ArrayDesign arrayDesign;

    @Before
    public void setUp() {
        gene = new Gene();
        gene.setOfficialSymbol( "BRCA1" );
        taxon = new Taxon();
        gene.setTaxon( taxon );
        taxon.setNcbiId( 9606 );
        arrayDesign = new ArrayDesign();
        arrayDesign.setId( 1L );
        when( taxonService.findByNcbiId( 9606 ) ).thenReturn( taxon );
        when( arrayDesignService.load( 1L ) ).thenReturn( arrayDesign );
    }

    @After
    public void tearDown() {
        reset( searchService, taxonService, arrayDesignService );
    }

    @Test
    public void testSearchEverything() {
        when( searchService.search( any() ) ).thenReturn( Collections.singletonMap( Gene.class, Collections.singletonList( new SearchResult( gene ) ) ) );
        when( searchService.convertSearchResultObjectToValueObject( any() ) ).thenAnswer( args -> new GeneValueObject( ( Gene ) args.getArgument( 0, SearchResult.class ).getResultObject() ) );
        ResponseDataObject<List<SearchWebService.SearchResultValueObject>> searchResults = searchWebService.search( "BRCA1", null, null, null );
        assertThat( searchResults.getData() )
                .hasSize( 1 )
                .first()
                .hasFieldOrPropertyWithValue( "resultId", gene.getId() )
                .hasFieldOrPropertyWithValue( "resultType", gene.getClass().getName() )
                .extracting( "resultObject" )
                .hasFieldOrPropertyWithValue( "officialSymbol", gene.getOfficialSymbol() );
    }

    @Test
    public void testSearchByTaxon() {
        when( searchService.search( any() ) ).thenReturn( Collections.singletonMap( Gene.class, Collections.singletonList( new SearchResult( gene ) ) ) );
        when( searchService.convertSearchResultObjectToValueObject( any() ) ).thenAnswer( args -> new GeneValueObject( ( Gene ) args.getArgument( 0, SearchResult.class ).getResultObject() ) );
        searchWebService.search( "BRCA1", TaxonArg.valueOf( "9606" ), null, null );
        verify( taxonService ).findByNcbiId( 9606 );
    }

    @Test
    public void testSearchByArrayDesign() {
        when( searchService.search( any() ) ).thenReturn( Collections.singletonMap( Gene.class, Collections.singletonList( new SearchResult( gene ) ) ) );
        when( searchService.convertSearchResultObjectToValueObject( any() ) ).thenAnswer( args -> new GeneValueObject( ( Gene ) args.getArgument( 0, SearchResult.class ).getResultObject() ) );
        searchWebService.search( "BRCA1", null, PlatformArg.valueOf( "1" ), null );
        verify( arrayDesignService ).load( 1L );
    }

    @Test(expected = BadRequestException.class)
    public void testSearchWhenQueryIsMissing() {
        searchWebService.search( null, null, null, null );
    }

    @Test(expected = BadRequestException.class)
    public void testSearchWhenQueryIsEmpty() {
        searchWebService.search( null, null, null, null );
    }

    @Test(expected = NotFoundException.class)
    public void testSearchWhenUnknownTaxonIsProvided() {
        searchWebService.search( "brain", TaxonArg.valueOf( "9607" ), null, null );
    }

    @Test(expected = NotFoundException.class)
    public void testSearchWhenUnknownPlatformIsProvided() {
        searchWebService.search( "brain", null, PlatformArg.valueOf( "2" ), null );
    }

    @Test(expected = BadRequestException.class)
    public void testSearchWhenUnsupportedResultTypeIsProvided() {
        searchWebService.search( "brain", null, null, Arrays.asList( "ubic.gemma.model.expression.designElement.CompositeSequence2" ) );
    }
}