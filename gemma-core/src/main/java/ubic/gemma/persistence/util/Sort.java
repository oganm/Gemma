package ubic.gemma.persistence.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a directed sort by a property.
 */
@Getter
@EqualsAndHashCode
public class Sort {

    public enum Direction {
        ASC, DESC
    }

    private final String orderBy;
    private final Direction direction;

    public Sort( String orderBy, Direction direction ) {
        this.orderBy = orderBy;
        this.direction = direction;
    }
}
