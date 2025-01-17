package ubic.gemma.web.services.rest.util.args;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import ubic.gemma.persistence.service.FilteringService;
import ubic.gemma.persistence.util.Filters;
import ubic.gemma.persistence.util.ObjectFilter;
import ubic.gemma.web.services.rest.util.MalformedArgException;

import java.util.Iterator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FilterArgTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private FilteringService mockVoService;

    @Test
    public void testNullFilter() {
        Filters filters = FilterArg.valueOf( null ).getObjectFilters( mockVoService );
        assertThat( filters.isEmpty() ).isTrue();
    }

    @Test
    public void testEmptyFilter() {
        Filters filters = FilterArg.valueOf( "" ).getObjectFilters( mockVoService );
        assertThat( filters.isEmpty() ).isTrue();
    }

    @Before
    public void setUp() {
        when( mockVoService.getObjectFilter( any(), any(), any( String.class ) ) )
                .thenAnswer( arg -> ObjectFilter.parseObjectFilter( "alias", arg.getArgument( 0, String.class ),
                        String.class,
                        arg.getArgument( 1, ObjectFilter.Operator.class ),
                        arg.getArgument( 2, String.class ) ) );
    }

    @Test
    public void testSimpleEquality() {
        Filters filters = FilterArg.valueOf( "a = b" ).getObjectFilters( mockVoService );
        assertThat( filters )
                .extracting( of -> of[0] )
                .first()
                .hasFieldOrPropertyWithValue( "propertyName", "a" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "b" );
    }

    @Test
    public void testStringCannotContainSpace() {
        assertThatThrownBy( () -> FilterArg.valueOf( "a = bcd d" ) )
                .isInstanceOf( MalformedArgException.class )
                .hasCauseInstanceOf( FilterArgParseException.class )
                .extracting( "cause" )
                .hasFieldOrPropertyWithValue( "part", Optional.of( 3 ) );
    }

    @Test
    public void testParseInvalidOperator() {
        assertThatThrownBy( () -> FilterArg.valueOf( "a ~= bcd d" ) )
                .isInstanceOf( MalformedArgException.class )
                .hasCauseInstanceOf( FilterArgParseException.class )
                .extracting( "cause" )
                .hasFieldOrPropertyWithValue( "part", Optional.of( 1 ) );
    }

    @Test
    public void testInvalidFilter() {
        assertThatThrownBy( () -> FilterArg.valueOf( "a =b" ).getObjectFilters( mockVoService ) )
                .isInstanceOf( MalformedArgException.class )
                .hasCauseInstanceOf( FilterArgParseException.class )
                .extracting( "cause" )
                .hasFieldOrPropertyWithValue( "part", Optional.of( 0 ) );
    }

    @Test
    public void testConjunction() {
        Filters filters = FilterArg.valueOf( "a = b and c = d" ).getObjectFilters( mockVoService );
        assertThat( filters ).hasSize( 2 );
        assertThat( filters )
                .extracting( of -> of[0] )
                .first()
                .hasFieldOrPropertyWithValue( "propertyName", "a" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "b" );
        assertThat( filters )
                .extracting( of -> of[0] )
                .last()
                .hasFieldOrPropertyWithValue( "propertyName", "c" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "d" );
    }

    @Test
    public void testDisjunction() {
        Filters filters = FilterArg.valueOf( "a = b, c = d" ).getObjectFilters( mockVoService );
        assertThat( filters ).hasSize( 1 );
        assertThat( filters.iterator().next() )
                .hasSize( 2 );

        assertThat( filters.iterator().next()[0] )
                .hasFieldOrPropertyWithValue( "propertyName", "a" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "b" );

        assertThat( filters.iterator().next()[1] )
                .hasFieldOrPropertyWithValue( "propertyName", "c" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "d" );

        FilterArg.valueOf( "a = b or c = d" ).getObjectFilters( mockVoService );

        assertThat( filters.iterator().next()[0] )
                .hasFieldOrPropertyWithValue( "propertyName", "a" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "b" );

        assertThat( filters.iterator().next()[1] )
                .hasFieldOrPropertyWithValue( "propertyName", "c" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "d" );
    }

    @Test
    public void testConjunctionOfDisjunctions() {
        Filters filters = FilterArg.valueOf( "a = b or g = h and c = d or e = f" ).getObjectFilters( mockVoService );

        assertThat( filters ).hasSize( 2 );

        Iterator<ObjectFilter[]> iterator = filters.iterator();
        ObjectFilter[] of;

        of = iterator.next();
        assertThat( of )
                .hasSize( 2 );
        assertThat( of[0] )
                .hasFieldOrPropertyWithValue( "propertyName", "a" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "b" );

        assertThat( of[1] )
                .hasFieldOrPropertyWithValue( "propertyName", "g" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "h" );

        of = iterator.next();
        assertThat( of[0] )
                .hasFieldOrPropertyWithValue( "propertyName", "c" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "d" );

        assertThat( of[1] )
                .hasFieldOrPropertyWithValue( "propertyName", "e" )
                .hasFieldOrPropertyWithValue( "operator", ObjectFilter.Operator.eq )
                .hasFieldOrPropertyWithValue( "requiredValue", "f" );
    }
}