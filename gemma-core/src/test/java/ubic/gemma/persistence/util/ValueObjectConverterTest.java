package ubic.gemma.persistence.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;
import ubic.gemma.persistence.service.expression.experiment.ExpressionExperimentService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ContextConfiguration
public class ValueObjectConverterTest extends AbstractJUnit4SpringContextTests {

    @Configuration
    public static class VoConverterTestContextConfiguration {
        @Bean
        public ExpressionExperimentService expressionExperimentService() {
            return mock( ExpressionExperimentService.class );
        }
    }

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    /* fixtures */
    private ValueObjectConverter<ExpressionExperiment, ExpressionExperimentValueObject> eeConverter;

    private ExpressionExperiment ee;

    @Before
    public void setUp() {
        ee = new ExpressionExperiment();
        eeConverter = new ValueObjectConverter<>( expressionExperimentService, ExpressionExperiment.class, ExpressionExperimentValueObject.class );
        when( expressionExperimentService.loadValueObject( any( ExpressionExperiment.class ) ) )
                .thenAnswer( arg -> new ExpressionExperimentValueObject( arg.getArgument( 0, ExpressionExperiment.class ) ) );
        //noinspection unchecked
        when( expressionExperimentService.loadValueObjects( anyCollection() ) ).
                thenAnswer( arg -> ( ( Collection<ExpressionExperiment> ) arg.getArgument( 0, Collection.class ) ).stream().map( expressionExperimentService::loadValueObject ).collect( Collectors.toList() ) );
    }

    @After
    public void tearDown() {
        reset( expressionExperimentService );
    }

    @Test
    public void testConvertSingleEntity() {
        Object converted = eeConverter.convert( ee, TypeDescriptor.valueOf( ExpressionExperiment.class ), TypeDescriptor.valueOf( ExpressionExperimentValueObject.class ) );
        assertThat( converted )
                .isNotNull()
                .isInstanceOf( ExpressionExperimentValueObject.class );
        verify( expressionExperimentService ).loadValueObject( ee );
    }

    @Test
    public void testConvertSingleEntityToSuperType() {
        Object converted = eeConverter.convert( ee, TypeDescriptor.valueOf( ExpressionExperiment.class ), TypeDescriptor.valueOf( IdentifiableValueObject.class ) );
        assertThat( converted )
                .isNotNull()
                .isInstanceOf( ExpressionExperimentValueObject.class );
        verify( expressionExperimentService ).loadValueObject( ee );
    }

    @Test
    public void testConvertCollection() {
        Collection<ExpressionExperiment> ees = Collections.singleton( ee );
        Object converted = eeConverter.convert( ees,
                TypeDescriptor.collection( Collection.class, TypeDescriptor.valueOf( ExpressionExperiment.class ) ),
                TypeDescriptor.collection( List.class, TypeDescriptor.valueOf( ExpressionExperimentValueObject.class ) ) );
        assertThat( converted ).isInstanceOf( List.class );
        verify( expressionExperimentService ).loadValueObjects( ees );
    }

    @Test
    public void testConvertCollectionToSuperType() {
        Collection<ExpressionExperiment> ees = Collections.singleton( ee );
        Object converted = eeConverter.convert( ees,
                TypeDescriptor.collection( Collection.class, TypeDescriptor.valueOf( ExpressionExperiment.class ) ),
                TypeDescriptor.collection( List.class, TypeDescriptor.valueOf( IdentifiableValueObject.class ) ) );
        assertThat( converted ).isInstanceOf( List.class );
        verify( expressionExperimentService ).loadValueObjects( ees );
    }

    @Test
    public void testConvertNullEntity() {
        eeConverter.convert( null, TypeDescriptor.valueOf( ExpressionExperiment.class ), TypeDescriptor.valueOf( ExpressionExperimentValueObject.class ) );
        verify( expressionExperimentService ).loadValueObject( null );
    }

    @Test(expected = ConverterNotFoundException.class)
    public void testConvertUnknownType() {
        eeConverter.convert( null, TypeDescriptor.valueOf( ArrayDesign.class ), TypeDescriptor.valueOf( ArrayDesignValueObject.class ) );
    }
}