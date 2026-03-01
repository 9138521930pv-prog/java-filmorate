package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoContentException;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Getter
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmDbStorage filmStorage,
                                                   MpaDbStorage mpaStorage,
                                                   GenreDbStorage genreStorage,
                                                   UserDbStorage userStorage) {

        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getFilmAll() {
        return filmStorage.getAllFilm().orElseThrow(() -> new NoContentException("Фильм не найден."));
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id).orElseThrow(() -> new NotFoundException("Фильм не найден."));
    }

    public List<Film> getTopFilms(Integer count) {
        return filmStorage.getTopFilm(count).orElseThrow(() -> new NoContentException("Фильм не найден."));
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: фильм {}, пользователь {}", filmId, userId);
        filmStorage.validateFilmExists(filmId);
        userStorage.validateUserExists(userId);
        if (filmStorage.emptyLike(filmId, userId)) {
            filmStorage.addLike(filmId, userId);
            log.info("Лайк успешно добавлен: фильм {}, пользователь {}", filmId, userId);
        }
    }

    public void delLike(Long filmId, Long userId) {
        log.info("Удаление лайка: фильм {}, пользователь {}", filmId, userId);

        filmStorage.validateFilmExists(filmId);
        userStorage.validateUserExists(userId);
        if (!filmStorage.emptyLike(filmId, userId)) {
            filmStorage.deleteLike(filmId, userId);
            log.info("Лайк успешно удалён: фильм {}, пользователь {}", filmId, userId);
        }
    }

    public void removeFilm(Long filmId) {
        log.info("Удаление фильмаЖ {}", filmId);
        filmStorage.validateFilmExists(filmId);
        filmStorage.removeFilm(filmId);
        log.info("Фильм {} успешно удалён", filmId);
    }
}
