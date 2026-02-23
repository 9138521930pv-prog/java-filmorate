package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.dto.request.FilmRequestDto;
import ru.yandex.practicum.filmorate.dto.response.FilmResponseDto;

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
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<FilmResponseDto>> getAllFilms() {
        List<FilmResponseDto> films =  filmService.getFilmAll();
        if (films.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmResponseDto> getFilmById(@PathVariable("id")
                                                       @NotNull(message = "id не может быть null")
                                                       @Min(value = 1, message = "id должен быть положительным целым числом")
                                                       @Valid Long filmId) {
        FilmResponseDto films =  filmService.getFilmById(filmId);
        if (films == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(films);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FilmResponseDto> addFilm(@Valid @RequestBody FilmRequestDto film) {
        FilmResponseDto savedFilm = filmService.addFilm(film);
        URI location = URI.create("/flims/" + savedFilm.getId());
        return ResponseEntity.created(location).body(savedFilm);
    }

    @PutMapping
    public ResponseEntity<FilmResponseDto>  updateUser(@Valid @RequestBody FilmRequestDto film) {

        return ResponseEntity.ok(filmService.updateFilm(film));
    }

    @PutMapping("/{id}/like/{friendId}")
    public ResponseEntity<FilmResponseDto> addLike(@PathVariable("id")
                                                   @NotNull(message = "id не может быть null")
                                                   @Min(value = 1, message = "id должен быть положительным целым числом")
                                                   @Valid Long filmId,
                                                   @PathVariable("friendId")
                                                   @NotNull(message = "id не может быть null")
                                                   @Min(value = 1, message = "id должен быть положительным целым числом")
                                                   @Valid Long userId) {
        filmService.addLike(filmId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like/{friendId}")
    public ResponseEntity<FilmResponseDto> delLike(@PathVariable("id")
                                                   @NotNull(message = "id не может быть null")
                                                   @Min(value = 1, message = "id должен быть положительным целым числом")
                                                   @Valid Long filmId,
                                                   @PathVariable("friendId")
                                                   @NotNull(message = "id не может быть null")
                                                   @Min(value = 1, message = "id должен быть положительным целым числом")
                                                   @Valid Long userId) {
        filmService.delLike(filmId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public List<FilmResponseDto> firstTenFilms(@RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
        log.info("film get popular " + "count: " + count);
        return filmService.getTopFilms(count);
    }
}