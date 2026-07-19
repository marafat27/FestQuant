/**
 * Contains the repository implementation used by FestQuant.
 */
package com.festquant.repository;

import java.util.List;
import java.util.Optional;

/**
 * Defines the operations required for repository.
 */
public interface Repository<T> {
    /**
     * Finds all.
     */
    List<T> findAll();
    /**
     * Finds by id.
     */
    Optional<T> findById(String id);
}
