package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    void  removeFilm(Long filmId);

    Film updateFilm(Film updatedFilm);

    List<Film> getAllFilm();

    Film getFilmById(Long filmId);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    List<Film> getTopFilm(Integer count);
}
