package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film addFilm(Film film);

    void  removeFilm(Long filmId);

    Film updateFilm(Film updatedFilm);

    Optional<List<Film>> getAllFilm();

    Optional<Film> getFilmById(Long filmId);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    void validateFilmExists(Long userId);

    void validateLikeExists(Long filmId, Long userId);

  //  void loadFilmDetails(Film film);
    Boolean emptyLike(Long filmId, Long userId);

    Optional<List<Film>> getTopFilm(Integer count);
}
