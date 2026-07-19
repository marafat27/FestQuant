/**
 * Contains the data load exception implementation used by FestQuant.
 */
package com.festquant.exception;

/**
 * Signals a problem related to data load.
 */
public class DataLoadException extends RuntimeException {
    /**
     * Creates a DataLoadException with the values needed by this component.
     */
    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a DataLoadException with the values needed by this component.
     */
    public DataLoadException(String message) {
        super(message);
    }
}
