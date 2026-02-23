package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.request.FilmRequestDto;
import ru.yandex.practicum.filmorate.dto.response.FilmResponseDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
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
                       UserDbStorage userStorage)
            {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.userStorage = userStorage;
    }

    public List<FilmResponseDto> getFilmAll() {
        return filmStorage.getAllFilm().stream().map(FilmMapper::convertToDto).toList();
    }

    public FilmResponseDto getFilmById(Long filmId) {
        return FilmMapper.convertToDto(filmStorage.getFilmById(filmId));
    }

    public FilmResponseDto addFilm(FilmRequestDto filmRequestDto) {
        Film film = FilmMapper.convertToEntity(filmRequestDto);
        return FilmMapper.convertToDto(filmStorage.addFilm(film));
    }

    public FilmResponseDto updateFilm(FilmRequestDto film) {
        return FilmMapper.convertToDto(filmStorage.updateFilm(FilmMapper.convertToEntity(film)));
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: фильм {}, пользователь {}", filmId, userId);

        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        filmStorage.addLike(filmId, userId);
        log.info("Лайк успешно добавлен: фильм {}, пользователь {}", filmId, userId);
    }

    public void delLike(Long filmId, Long userId) {
        log.info("Удаление лайка: фильм {}, пользователь {}", filmId, userId);

        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }

        filmStorage.deleteLike(filmId, userId);
        log.info("Лайк успешно удалён: фильм {}, пользователь {}", filmId, userId);
    }

    public List<FilmResponseDto> getTopFilms(Integer count) {
        return filmStorage.getTopFilm(count).stream().map(FilmMapper::convertToDto).toList();
    }
}
