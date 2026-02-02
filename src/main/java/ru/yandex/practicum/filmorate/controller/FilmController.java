package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.model.Film;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        List<Film> films = filmService.getFilmStorage().getAllFilm();
        if (films.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(films);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        Film savedFilm = filmService.getFilmStorage().addFilm(film);
        URI location = URI.create("/films/" + savedFilm.getId());
        return ResponseEntity.created(location).body(savedFilm);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        return filmService.getFilmStorage().updateFilm(updatedFilm);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long filmId) {
        return filmService.getFilmStorage().getFilmById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable("id")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long likedFilmId,
                        @PathVariable("userId")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long userId) {
        return filmService.addLike(likedFilmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable("id")
                           @NotNull(message = "id не может быть null")
                           @Min(value = 1, message = "id должен быть положительным целым числом")
                           @Valid Long likedFilmId,
                           @PathVariable("userId")
                           @NotNull(message = "id не может быть null")
                           @Min(value = 1, message = "id должен быть положительным целым числом")
                           @Valid Long userId) {
        return filmService.removeLike(likedFilmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(name = "count", defaultValue = "10")
                                          @Positive(message = "count должен быть больше 0")
                                          @Valid Long mostPopularFilmCount) {
        return filmService.getMostPopularFilms(mostPopularFilmCount);
    }
}