package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GenreStorage {
    List<Genre> getGenre();

    Genre getGenreById(Long id);

    void setGenreToFilm(Long genreId, Long filmId);

    Set<Genre> getGenresByFilmId(Long id);

    List<Long> getGenreIds();

    Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds);
}