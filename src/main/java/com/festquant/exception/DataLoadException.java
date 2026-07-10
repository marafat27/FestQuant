package com.festquant.exception;

public class DataLoadException extends RuntimeException {
    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataLoadException(String message) {
        super(message);
    }
}
