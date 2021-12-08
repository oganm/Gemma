package ubic.gemma.persistence.util;

import com.google.common.collect.Sets;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;
import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.persistence.service.BaseVoEnabledService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Provide a {@link GenericConverter} that performs conversion according to a {@link BaseVoEnabledService}.
 *
 * This is used along with a {@link org.springframework.core.convert.ConversionService} to automate the conversion
 * process of objects to their value objects counterparts.
 *
 * The converter recognize two cases: converting {@link O} -> {@link VO} and converting {@link Collection} of {@link O}
 * to {@link List} of {@link VO} by calling respectively {@link BaseVoEnabledService#loadValueObject(Identifiable)} and
 * {@link BaseVoEnabledService#loadValueObjects(Collection)}.
 *
 * This implementation also work with supertypes of the designated {@link VO}. For example, you can perform generic
 * conversion to {@link IdentifiableValueObject} without having to mention the specific type of value object you
 * ultimately want.
 *
 * @author poirigui
 */
public class ValueObjectConverter<O extends Identifiable, VO extends IdentifiableValueObject<O>> implements ConditionalGenericConverter {

    /**
     * We need a service that can produce something can can be cast into a {@link VO}.
     */
    private final BaseVoEnabledService<O, ? extends VO> service;

    private final Set<ConvertiblePair> convertibleTypes;

    /* enables very efficient type checks */
    private final TypeDescriptor sourceType;
    private final TypeDescriptor targetType;
    private final TypeDescriptor sourceCollectionType;
    private final TypeDescriptor targetListType;

    public ValueObjectConverter( BaseVoEnabledService<O, ? extends VO> service, Class<O> sourceType, Class<? super VO> targetType ) {
        this.service = service;
        this.convertibleTypes = Sets.newHashSet(
                new ConvertiblePair( sourceType, targetType ),
                new ConvertiblePair( Collection.class, List.class ) );
        this.sourceType = TypeDescriptor.valueOf( sourceType );
        this.targetType = TypeDescriptor.valueOf( targetType );
        this.sourceCollectionType = TypeDescriptor.collection( Collection.class, this.sourceType );
        this.targetListType = TypeDescriptor.collection( List.class, this.targetType );
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return convertibleTypes;
    }

    @Override
    public boolean matches( TypeDescriptor sourceType, TypeDescriptor targetType ) {
        // this is necessary because Collection and List types are too broad and will result in conflicts with
        // converters for other VOs
        return sourceType.isAssignableTo( this.sourceType ) && this.targetType.isAssignableTo( targetType ) ||
                sourceType.isAssignableTo( sourceCollectionType ) && targetListType.isAssignableTo( targetType );
    }

    @Override
    public Object convert( Object object, TypeDescriptor sourceType, TypeDescriptor targetType ) {
        if ( sourceType.isAssignableTo( this.sourceType ) && this.targetType.isAssignableTo( targetType ) ) {
            //noinspection unchecked
            return service.loadValueObject( ( O ) object );
        }

        if ( sourceType.isAssignableTo( sourceCollectionType ) && targetListType.isAssignableTo( targetType ) ) {
            //noinspection unchecked
            return service.loadValueObjects( ( Collection<O> ) object );
        }

        throw new ConverterNotFoundException( sourceType, targetType );
    }
}
