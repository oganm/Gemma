package ubic.gemma.persistence.service;

import ubic.gemma.model.IdentifiableValueObject;
import ubic.gemma.model.common.Identifiable;
import ubic.gemma.persistence.util.Filters;
import ubic.gemma.persistence.util.Slice;
import ubic.gemma.persistence.util.Sort;

import java.util.List;

/**
 * Interface VO-enabled service with filtering capabilities.
 *
 * @param <O>
 * @param <VO>
 */
public interface FilteringVoEnabledService<O extends Identifiable, VO extends IdentifiableValueObject<O>>
        extends FilteringService<O>, BaseVoEnabledService<O, VO> {

    /**
     * @see FilteringVoEnabledDao#loadValueObjectsPreFilter(Filters, Sort, int, int)
     */
    Slice<VO> loadValueObjectsPreFilter( Filters filters, Sort sort, int offset, int limit );

    /**
     * @see FilteringVoEnabledDao#loadValueObjectsPreFilter(Filters, Sort)
     */
    List<VO> loadValueObjectsPreFilter( Filters filters, Sort sort );
}