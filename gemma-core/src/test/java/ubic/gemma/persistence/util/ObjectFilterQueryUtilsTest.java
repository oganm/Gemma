package ubic.gemma.persistence.util;

import org.hibernate.Query;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static ubic.gemma.persistence.util.ObjectFilterQueryUtils.*;

public class ObjectFilterQueryUtilsTest {

    @Test
    public void testFormParamName() {
        assertThat( formParamName( "ee", "id" ) ).isEqualTo( "ee_id" );
        assertThat( formParamName( "ee", "curationDetails.troubled" ) ).isEqualTo( "ee_curationDetails_troubled" );
        assertThat( formParamName( null, "id" ) ).isEqualTo( "id" );
    }

    @Test
    public void testComplexClause() {
        Filters filters = new Filters();
        filters.add( ObjectFilter.parseObjectFilter( "ee", "shortName", String.class, ObjectFilter.Operator.like, "GSE" ) );
        filters.add( ObjectFilter.parseObjectFilter( "ee", "id", Long.class, ObjectFilter.Operator.in, "(1,2,3,4)" ) );
        filters.add( ObjectFilter.parseObjectFilter( "ad", "taxonId", Long.class, ObjectFilter.Operator.eq, "9606" ) );
        filters.add( ObjectFilter.parseObjectFilter( "ee", "id", Long.class, ObjectFilter.Operator.in, "(1,2,3,4)" ),
                ObjectFilter.parseObjectFilter( "ee", "id", Long.class, ObjectFilter.Operator.in, "(5,6,7,8)" ) );
        assertThat( formRestrictionClause( filters ) )
                .isEqualTo( " and (ee.shortName like :ee_shortName1) and (ee.id in (:ee_id2)) and (ad.taxonId = :ad_taxonId3) and (ee.id in (:ee_id4) or ee.id in (:ee_id5))" );

        Query mockedQuery = mock( Query.class );
        addRestrictionParameters( mockedQuery, filters );
        verify( mockedQuery ).setParameter( "ee_shortName1", "%GSE%" );
        verify( mockedQuery ).setParameterList( "ee_id2", Arrays.asList( 1L, 2L, 3L, 4L ) );
        verify( mockedQuery ).setParameter( "ad_taxonId3", 9606L );
        verify( mockedQuery ).setParameterList( "ee_id4", Arrays.asList( 1L, 2L, 3L, 4L ) );
        verify( mockedQuery ).setParameterList( "ee_id5", Arrays.asList( 5L, 6L, 7L, 8L ) );
    }

    @Test
    public void testRestrictionClauseWithCollection() {
        Filters filters = Filters.singleFilter( ObjectFilter.parseObjectFilter( "ee", "id", Long.class, ObjectFilter.Operator.in, "(1,2,3,4)" ) );
        assertThat( formRestrictionClause( filters ) )
                .isEqualTo( " and (ee.id in (:ee_id1))" );

        Query mockedQuery = mock( Query.class );
        addRestrictionParameters( mockedQuery, filters );
        verify( mockedQuery ).setParameterList( "ee_id1", Arrays.asList( 1L, 2L, 3L, 4L ) );
    }

    @Test
    public void testRestrictionClauseWithNullRequiredValue() {
        Filters filters = new Filters();
        filters.add( new ObjectFilter( "ee", "id", Long.class, ObjectFilter.Operator.eq, null ),
                new ObjectFilter( "ee", "id", Long.class, ObjectFilter.Operator.notEq, null ) );
        assertThat( formRestrictionClause( filters ) )
                .isEqualTo( " and (ee.id is :ee_id1 or ee.id is not :ee_id2)" );

        Query mockedQuery = mock( Query.class );
        addRestrictionParameters( mockedQuery, filters );
        verify( mockedQuery ).setParameter( "ee_id1", null );
        verify( mockedQuery ).setParameter( "ee_id2", null );
    }

    @Test
    public void testRestrictionClauseWithNullObjectAlias() {
        Filters filters = Filters.singleFilter( new ObjectFilter( null, "id", Long.class, ObjectFilter.Operator.eq, 12L ) );
        assertThat( formRestrictionClause( filters ) )
                .isEqualTo( " and (id = :id1)" );

        Query mockedQuery = mock( Query.class );
        addRestrictionParameters( mockedQuery, filters );
        verify( mockedQuery ).setParameter( "id1", 12L );
    }
}