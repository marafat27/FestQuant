/**
 * Contains the invalid bid exception implementation used by FestQuant.
 */
package com.festquant.exception;

/**
 * Signals a problem related to invalid bid.
 */
public class InvalidBidException extends RuntimeException {
    /**
     * Creates a InvalidBidException with the values needed by this component.
     */
    public InvalidBidException(String message) {
        super(message);
    }
}
