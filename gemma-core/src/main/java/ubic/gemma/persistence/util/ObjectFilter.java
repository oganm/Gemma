/*
 * The gemma-core project
 *
 * Copyright (c) 2019 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.gemma.persistence.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.GenericConversionService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by tesarst on 14/07/17.
 * Provides necessary information to filter a database query by a value of a specific object property.
 */
@Getter
@EqualsAndHashCode
@ToString
public class ObjectFilter {

    /**
     * This is only the date part of the ISO 8601 standard.
     */
    private static DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );

    /**
     * Provide all the supported type conversion for parsing required values.
     */
    private static final ConfigurableConversionService conversionService = new GenericConversionService();

    private static <T> void addConverter( Class<?> targetClass, Converter<String, T> converter ) {
        conversionService.addConverter( String.class, targetClass, converter );
    }

    static {
        addConverter( String.class, s -> s );
        addConverter( Boolean.class, Boolean::parseBoolean );
        addConverter( Double.class, Double::parseDouble );
        addConverter( Float.class, Float::parseFloat );
        addConverter( Long.class, Long::parseLong );
        addConverter( Integer.class, Integer::parseInt );
        addConverter( Date.class, s -> {
            try {
                return DATE_FORMAT.parse( s );
            } catch ( ParseException e ) {
                throw new ConversionFailedException( TypeDescriptor.valueOf( Date.class ), TypeDescriptor.valueOf( String.class ), s, e );
            }
        } );
    }

    /**
     * Creates a new ObjectFilter with a value parsed from a String into a given propertyType.
     *
     * @param propertyName  property name of the object
     * @param propertyType  the type of the property that will be checked, you can use the {@link EntityUtils#getDeclaredFieldType(String, Class)}
     *                      utility to obtain that type conveniently
     * @param operator      operator for this filter, see {@link Operator} for more details about available operations
     * @param requiredValue required value
     * @throws IllegalArgumentException if
     */
    public static ObjectFilter parseObjectFilter( String alias, String propertyName, Class<?> propertyType, Operator operator, String requiredValue ) throws IllegalArgumentException {
        return new ObjectFilter( alias, propertyName, propertyType, operator, parseRequiredValue( requiredValue, propertyType ) );
    }


    /**
     * Creates a new ObjectFilter with a value of type that the given requiredValue object is.
     *
     * @param  propertyName             the name of a property that will be checked.
     * @param  operator                 the operator that will be used to compare the value of the object. The
     *                                  requiredValue will be the right operand of the given operator.
     *                                  Demonstration in pseudo-code: if( object.value lessThan requiredValue) return
     *                                  object.
     *                                  The {@link Operator#in} operator means that the given requiredValue is
     *                                  expected to be a {@link Collection}, and the checked property has to be equal to
     *                                  at least one of the values contained within that <i>List</i>.
     *                                  The {@link Operator#like} operator must be provided a {@link String} as
     *                                  requiredValue
     * @param  requiredValues           the value that the property will be checked for. Null objects are not allowed
     *                                  for operators "lessThan", "greaterThan" and "in".
     * @throws IllegalArgumentException if the given operator is the "in" operator but the
     *                                  given requiredValue is not an instance of Iterable.
     */
    public static ObjectFilter parseObjectFilter( String alias, String propertyName, Class<?> propertyType, Operator operator, Collection<String> requiredValues ) throws IllegalArgumentException {
        return new ObjectFilter( alias, propertyName, propertyType, operator, parseRequiredValues( requiredValues, propertyType ) );
    }

    public enum Operator {
        /**
         * Note that in the case of a null requiredValue, the {@link #sqlToken} of this operator must be ignored and 'is'
         * must be used instead.
         */
        eq( "=", false, null ),
        /**
         * Same remark for {@link #eq} applies, but with the 'is not' operator.
         */
        notEq( "!=", false, null ),
        like( "like", true, String.class ),
        lessThan( "<", true, null ),
        greaterThan( ">", true, null ),
        lessOrEq( "<=", true, null ),
        greaterOrEq( ">=", true, null ),
        in( "in", true, Collection.class );

        /**
         * Token used when parsing object filter input.
         */
        private final String token;

        /**
         * Token used in SQL/HQL query.
         */
        private final String sqlToken;

        /**
         * THe required value must not be null.
         */
        private final boolean nonNullRequired;

        /**
         * The required value must satisfy this type.
         */
        private final Class<?> requiredType;

        Operator( String operator, boolean isNonNullRequired, Class<?> requiredType ) {
            this.token = operator;
            this.sqlToken = operator;
            this.nonNullRequired = isNonNullRequired;
            this.requiredType = requiredType;
        }

        public String getToken() {
            return token;
        }

        /**
         * This is package-private on purpose and is only meant for{@link ObjectFilterQueryUtils#formRestrictionClause(Filters)}.
         */
        String getSqlToken() {
            return sqlToken;
        }

        public boolean isNonNullRequired() {
            return nonNullRequired;
        }

        public Class<?> getRequiredType() {
            return requiredType;
        }
    }

    private final String objectAlias;
    private final String propertyName;
    private final Class<?> propertyType;
    private final Operator operator;
    private final Object requiredValue;

    public ObjectFilter( String objectAlias, String propertyName, Class<?> propertyType, Operator operator, Object requiredValue ) throws IllegalArgumentException {
        this.objectAlias = objectAlias;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.operator = operator;
        this.requiredValue = requiredValue;
        this.checkTypeCorrect();
    }

    private void checkTypeCorrect() throws IllegalArgumentException {
        if ( operator.isNonNullRequired() && requiredValue == null ) {
            throw new IllegalArgumentException( "requiredValue for operator " + operator + " cannot be null." );
        }

        // if the operator does not have a specific type requirement, then consider that the propertyType is the type requirement
        Class<?> requiredType = operator.getRequiredType() != null ? operator.getRequiredType() : propertyType;

        // if the required type is a primitive, convert it to the corresponding wrapper type because required value can
        // never be a primitive
        if ( requiredType.isPrimitive() ) {
            requiredType = ClassUtils.primitiveToWrapper( requiredType );
        }

        if ( requiredValue != null && !requiredType.isAssignableFrom( requiredValue.getClass() ) ) {
            throw new IllegalArgumentException( "requiredValue " + requiredValue + " of type " + requiredValue.getClass().getName() + " for operator " + operator + " must be assignable from " + requiredType.getName() + "." );
        }

        // if an operator expects a collection as RHS, then the type of the elements in that collection must absolutely
        // match the propertyType
        // for example, ad.id in ("a", 1, NULL) must be invalid if id is of type Long
        if ( requiredValue != null && operator.getRequiredType() != null && Collection.class.isAssignableFrom( operator.getRequiredType() ) ) {
            Collection<?> requiredCollection = ( Collection<?> ) requiredValue;
            if ( !requiredCollection.stream().allMatch( rv -> rv.getClass().isAssignableFrom( propertyType ) ) ) {
                throw new IllegalArgumentException( "All elements in requiredValue " + requiredType + " must be assignable from " + propertyType.getName() + "." );
            }
        }
    }

    /**
     * Converts the given value to be of the given property type. For primitive number types, the wrapper class is used.
     *
     * @param  rv the Object to be converted into the desired type.
     * @param  pt  the type that the given value should be converted to.
     * @return and Object of requested type, containing the given value converted to the new type.
     */
    private static Object parseRequiredValue( String rv, Class<?> pt ) throws IllegalArgumentException {
        if ( isCollection( rv ) ) {
            // convert individual elements
            return parseCollection( rv ).stream()
                    .map( item -> parseItem( item, pt ) )
                    .collect( Collectors.toList() );
        } else {
            return parseItem( rv, pt );
        }
    }

    private static Object parseRequiredValues( Collection<String> requiredValues, Class<?> pt ) throws IllegalArgumentException {
        return requiredValues.stream()
                .map( item -> parseItem( item, pt ) )
                .collect( Collectors.toList() );
    }

    private static boolean isCollection( String value ) {
        return value.trim().matches( "^\\((.+,)*.+\\)$" );
    }

    /**
     * Tries to parse the given string value into a collection of strings.
     * @param value the value to be parsed into a collection of strings. This should be a bracketed comma separated list
     *              of strings.
     * @return a collection of strings.
     */
    private static Collection<String> parseCollection( String value ) {
        return Arrays.asList( value
                .trim()
                .substring( 1, value.length() - 1 ) // these are the parenthesis
                .split( "\\s*,\\s*" ) );
    }

    private static Object parseItem( String rv, Class<?> pt ) throws IllegalArgumentException {
        try {
            return conversionService.convert( rv, pt );
        } catch ( ConversionException e ) {
            throw new IllegalArgumentException( e );
        }
    }
}
