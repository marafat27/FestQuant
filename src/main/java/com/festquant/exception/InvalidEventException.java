/**
 * Contains the invalid event exception implementation used by FestQuant.
 */
package com.festquant.exception;

/**
 * Signals a problem related to invalid event.
 */
public class InvalidEventException extends RuntimeException {
    /**
     * Creates a InvalidEventException with the values needed by this component.
     */
    public InvalidEventException(String message) {
        super(message);
    }
}
