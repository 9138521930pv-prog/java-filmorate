package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
       List<Film> filmsList = new ArrayList<>(films.values());
       return ResponseEntity
                .ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(filmsList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getUserById(@PathVariable @Valid Integer id) {
        Film film = films.get(id);
        if (film == null) {
            throw new IllegalArgumentException("Фильм с ID " + id + " не найден");
        }
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(film);
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@RequestBody @Valid Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм с ID: {}", film.getId());

        return ResponseEntity
                .created(URI.create("/films/" + film.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(film);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@RequestBody @Valid Film updatedFilm) {
        if (updatedFilm.getId() == null || !films.containsKey(updatedFilm.getId())) {
            throw new IllegalArgumentException("Фильм с указанным ID не найден");
        }

        if (updatedFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Обновлён фильм с ID: {}", updatedFilm.getId());
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(updatedFilm);

    }

    public void clear() {
        films.clear();
        nextId = 1;
        log.info("Коллекция фильмов очищена, nextId обнулён.");
    }
}