package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.response.GenreResponseDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;


@RestController
@RequestMapping("/genres")
@Slf4j
@Validated
@RequiredArgsConstructor

public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<List<GenreResponseDto>> getGenres() {
        List<GenreResponseDto> genre = genreService.getGenre();
        if (genre.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(genre);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponseDto> getGenreById(@PathVariable("id")
                                                         @NotNull(message = "id не может быть null")
                                                         @Min(value = 1, message = "id должен быть положительным целым числом")
                                                         @Valid Long id) {
        GenreResponseDto genre = genreService.getGenreById(id);
        if (genre == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(genre);
    }

}