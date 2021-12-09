package ubic.gemma.core.search;

import org.junit.Before;
import org.junit.Test;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SearchResultTest {

    private ArrayDesign ad;

    @Before
    public void setUp() {
        ad = ArrayDesign.Factory.newInstance();
        ad.setId( 11L );
    }

    @Test
    public void test() {
        assertThat( new SearchResult<>( ad ) )
                .hasFieldOrPropertyWithValue( "score", 0.0 );
    }

    @Test(expected = NullPointerException.class)
    public void testNullResultObjectRaisesException() {
        //noinspection ConstantConditions
        new SearchResult<>( null );
    }

    @Test
    public void testUninitializedSearchResult() {
        assertThat( new SearchResult<>( ad.getClass(), ad.getId() ) )
                .hasFieldOrPropertyWithValue( "score", 0.0 );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUninitializedSearchResultWithoutIdRaisesException() {
        assertThat( new SearchResult<>( ArrayDesign.Factory.newInstance() ) )
                .hasFieldOrPropertyWithValue( "score", 0.0 );
    }

    @Test
    public void testUninitializedSearchResultSetToANullResultObjectRaisesException() {
        SearchResult<ArrayDesign> sr = new SearchResult<>( ad.getClass(), ad.getId() );
        assertThatThrownBy( () -> sr.setResultObject( null ) )
                .isInstanceOf( NullPointerException.class );
    }

    @Test
    public void testUninitializedSearchResultSetToAResultObjectWithDifferentIdRaisesException() {
        SearchResult<ArrayDesign> sr = new SearchResult<>( ad.getClass(), ad.getId() );

        ArrayDesign ad2 = ArrayDesign.Factory.newInstance();
        ad2.setId( 13L );

        assertThatThrownBy( () -> sr.setResultObject( ad2 ) )
                .isInstanceOf( IllegalArgumentException.class );
    }
}