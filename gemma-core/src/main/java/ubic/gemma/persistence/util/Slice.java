package ubic.gemma.persistence.util;

import ubic.gemma.model.expression.experiment.ExpressionExperimentDetailsValueObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a slice of {@link List}.
 */
public class Slice<O> extends AbstractList<O> implements List<O> {

    public static <O> Slice<O> fromList( List<O> list ) {
        return new Slice<>( list, null, null, null, ( long ) list.size() );
    }

    private final List<O> data;

    private final Sort sort;
    public final Integer offset;
    public final Integer limit;
    private final Long totalElements;

    public Slice( List<O> elements, Sort sort, Integer offset, Integer limit, Long totalElements ) {
        this.data = elements;
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
        this.totalElements = totalElements;
    }

    /**
     * Creates an empty, unsorted slice.
     */
    public Slice() {
        this( new ArrayList<>(), null, 0, 0, 0L );
    }

    @Override
    public O get( int i ) {
        return data.get( i );
    }

    @Override
    public int size() {
        return data.size();
    }

    /**
     * This is unfortunately necessary because it it blindly casted.
     * This is necessary
     * @param elem
     * @return
     */
    @Override
    @Deprecated
    public boolean add( O elem ) {
        return data.add( elem );
    }

    /**
     * Unfortunately, we need to implement this because gsec explicitly remove items that are not accessible by the
     * current user in {@link gemma.gsec.acl.afterinvocation.AclAfterFilterValueObjectCollectionProvider}.
     */
    @Override
    @Deprecated
    public boolean remove( Object elem ) {
        return data.remove( elem );
    }

    /**
     * @return a sort, or null if unspecified
     */
    public Sort getSort() {
        return this.sort;
    }

    /**
     * @return an offset, or null if unspecified
     */
    public Integer getOffset() {
        return this.offset;
    }

    /**
     * @return a limit, or null if unspecified
     */
    public Integer getLimit() {
        return this.limit;
    }

    /**
     *
     * @return the total number
     */
    public Long getTotalElements() {
        return this.totalElements;
    }

    public <S> Slice<S> map( Function<? super O, ? extends S> converter ) {
        return new Slice( this.stream().map( converter ).collect( Collectors.toList() ), sort, offset, limit, totalElements );
    }
}
