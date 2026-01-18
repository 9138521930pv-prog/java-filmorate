package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    @Slf4j
    @RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
    public static class ValidationHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<Map<String, String>> handleValidationExceptions(
                MethodArgumentNotValidException ex) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                String field = error.getField();
                String message = error.getDefaultMessage();

                errors.put(field, message);
                log.warn("VALIDATION_ERROR: field '{}' — {}", field, message);
            }

            return ResponseEntity
                    .badRequest()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(errors);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());

            log.error("ILLEGAL_ARGUMENT_ERROR: source '{}', message: {}",
                    errorResponse.get("source"), ex.getMessage(), ex);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(errorResponse);
        }
    }
}