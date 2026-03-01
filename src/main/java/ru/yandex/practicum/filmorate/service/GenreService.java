package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoContentException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Set;

@Service
@Getter
@Slf4j
public class GenreService {

    private final GenreDbStorage genreStorage;

    public GenreService(@Qualifier("genreDbStorage") GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getGenre() {
        return genreStorage.getGenre().orElseThrow(() -> new NoContentException("Жанр не найден."));
    }

    public Genre getGenreById(Long id) {
        return genreStorage.getGenreById(id).orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден."));
    }

    public void setGenreToFilm(Long genreId, Long filmId) {
        checkGenreExists(genreId);
        genreStorage.setGenreToFilm(genreId, filmId);
    }

    public Set<Genre> getGenresByFilmId(Long filmId) {
        return genreStorage.getGenresByFilmId(filmId); // Исправлено имя поля
    }

    public boolean checkGenreExists(Long id) {
        return genreStorage.getGenreById(id) != null; // Исправлено имя поля и логика
    }
}