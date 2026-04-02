package com.sobready.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Catches all exceptions and returns clean JSON error responses.
 *
 * NestJS equivalent:
 *
 *   @Catch()
 *   export class AllExceptionsFilter implements ExceptionFilter {
 *     catch(exception: Error, host: ArgumentsHost) {
 *       const response = host.switchToHttp().getResponse();
 *       response.status(500).json({ message: exception.message });
 *     }
 *   }
 *
 * Without this, Spring returns ugly HTML error pages.
 * With this, all errors return clean JSON: { "message": "Something went wrong" }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Internal server error"));
    }
}
