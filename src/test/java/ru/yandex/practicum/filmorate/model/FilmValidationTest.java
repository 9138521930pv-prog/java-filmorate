package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Описание");
        film.setDuration(100L);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("a".repeat(201));
        film.setDuration(100L);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setDuration(-90L);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenReleaseDateBefore1895() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setDuration(120L);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWithCorrectFilm() {
        Film film = new Film();
        film.setName("Тест");
        film.setDescription("Описание");
        film.setDuration(90L);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }
}
