package ubic.gemma.persistence.service;

import ubic.gemma.model.common.Identifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

/**
 * Interface that supports basic CRUD operations.
 *
 * @param <O> the Object type that this service is handling.
 * @author tesarst
 */
@ParametersAreNonnullByDefault
public interface BaseService<O extends Identifiable> {

    /**
     * Does a search for the entity in the persistent storage
     *
     * @param entity the entity to be searched for
     * @return the version of entity retrieved from the persistent storage, if found, otherwise null.
     */
    @Nullable
    @CheckReturnValue
    O find( O entity );

    /**
     * Does a search for the entity in the persistent storage, and if not found, creates it.
     *
     * @param entity the entity to look for, and create if not found.
     * @return the entity retrieved from the persistent storage, either found or created.
     */
    @CheckReturnValue
    O findOrCreate( O entity );

    /**
     * Creates all the given entities in a persistent storage
     *
     * @param entities the entities to be created.
     * @return collection of objects referencing the persistent instances of given entities.
     */
    @SuppressWarnings("unused") // Consistency
    @CheckReturnValue
    Collection<O> create( Collection<O> entities );

    /**
     * Creates the given entity in the persistent storage.
     *
     * @param entity the entity to be created.
     * @return object referencing the persistent instance of the given entity.
     */
    @CheckReturnValue
    O create( O entity );

    /**
     * Loads objects with given ids.
     *
     * @param ids the ids of objects to be loaded.
     * @return collection containing object with given IDs.
     */
    Collection<O> load( Collection<Long> ids );

    /**
     * Loads object with given ID.
     *
     * @param id the ID of entity to be loaded.
     * @return the entity with matching ID, or null if the entity does not exist or if the passed ID was null
     */
    @Nullable
    O load( @Nullable Long id );

    /**
     * Loads all the entities of specific type.
     *
     * @return collection of all entities currently available in the persistent storage.
     */
    Collection<O> loadAll();

    long countAll();

    /**
     * Removes all the given entities from persistent storage.
     *
     * @param entities the entities to be removed.
     */
    void remove( Collection<O> entities );

    /**
     * Removes the entity with given ID from the persistent storage.
     *
     * @param id the ID of entity to be removed.
     */
    void remove( Long id );

    /**
     * Removes the given entity from the persistent storage.
     *
     * @param entity the entity to be removed.
     */
    void remove( O entity );

    /**
     * Remove all entities from the persistent storage.
     */
    void removeAll();

    /**
     * Updates all entities in the given collection in the persistent storage.
     *
     * @param entities the entities to be updated.
     */
    void update( Collection<O> entities );

    /**
     * Updates the given entity in the persistent storage.
     *
     * @param entity the entity to be updated.
     */
    void update( O entity );

}