package com.festquant.controller;

import com.festquant.exception.DataLoadException;
import com.festquant.exception.InvalidBidException;
import com.festquant.exception.InvalidEventException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public final class ApiExceptionHandler {
    @ExceptionHandler({InvalidBidException.class, InvalidEventException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> badRequest(RuntimeException exception) {
        return response(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler({DataLoadException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> serverError(RuntimeException exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, RuntimeException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
