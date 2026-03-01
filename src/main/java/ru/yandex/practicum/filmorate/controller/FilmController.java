package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getFilmAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable("id")
                            @NotNull(message = "id не может быть null")
                            @Min(value = 1, message = "id должен быть положительным целым числом")
                            @Valid Long filmId) {

        return filmService.getFilmById(filmId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        Film savedFilm = filmService.addFilm(film);
        URI location = URI.create("/films/" + savedFilm.getId());
        return ResponseEntity.created(location).body(savedFilm);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable("id")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long filmId,
                        @PathVariable("friendId")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void delLike(@PathVariable("id")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long filmId,
                        @PathVariable("friendId")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long userId) {
        filmService.delLike(filmId, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delFilm(@PathVariable("id")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long filmId) {
        filmService.removeFilm(filmId);
    }

    @GetMapping("/popular")
    public List<Film> firstTenFilms(@RequestParam(value = "count", required = false, defaultValue = "10")
                                    @Min(value = 1, message = "count должен быть положительным числом и > 0")
                                    Integer count) {
        return filmService.getTopFilms(count);
    }
}