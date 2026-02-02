package ru.yandex.practicum.filmorate.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .peek(error -> log.warn("VALIDATION_ERROR: field '{}' — {}", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("ILLEGAL_ARGUMENT_ERROR: source '{}', message: {}", ex.getMessage(), ex);
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(ValidationException ex) {
        log.warn("VALIDATION_ERROR: {}", ex.getMessage());
        return Map.of("error", ex.getMessage(), "details", "НЕКОРРЕКТНЫЕ ПАРАМЕТРЫ.");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException ex) {
        log.warn("NOT_FOUND_ID: {}", ex.getMessage());
        return Map.of("error", ex.getMessage(), "details", "ИСКОМЫЙ ОБЪЕКТ НЕ НАЙДЕН.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolation(final ConstraintViolationException ex) {
        log.warn("Constraint_Violation: {}", ex.getMessage());
        return Map.of("error", ex.getMessage(), "details", "ОШИБКА ВАЛИДАЦИИ ТЕЛА ЗАПРОСА");
    }
}
