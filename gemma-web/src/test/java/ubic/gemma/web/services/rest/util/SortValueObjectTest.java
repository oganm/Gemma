package ubic.gemma.web.services.rest.util;

import org.junit.Test;
import ubic.gemma.persistence.util.Sort;

import static org.assertj.core.api.Assertions.assertThat;

public class SortValueObjectTest {

    @Test
    public void test() {
        assertThat( new SortValueObject( Sort.by( "ee", "id", Sort.Direction.DESC ) ) )
                .hasFieldOrPropertyWithValue( "orderBy", "ee.id" )
                .hasFieldOrPropertyWithValue( "direction", "DESC" );
        assertThat( new SortValueObject( Sort.by( "ee", "id" ) ) )
                .hasFieldOrPropertyWithValue( "orderBy", "ee.id" )
                .hasFieldOrPropertyWithValue( "direction", null );
        assertThat( new SortValueObject( Sort.by( null, "id" ) ) )
                .hasFieldOrPropertyWithValue( "orderBy", "id" )
                .hasFieldOrPropertyWithValue( "direction", null );
    }

}