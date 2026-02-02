package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addLike(Long likedFilmId, Long userId) {
        userService.getUserStorage().getUserById(userId);
        Film film = filmStorage.getFilmById(likedFilmId);
        film.addLike(userId);
        return film;
    }

    public Film removeLike(Long unlikedFilmId, Long userId) {
        userService.getUserStorage().getUserById(userId);
        Film film = filmStorage.getFilmById(unlikedFilmId);
        film.removeLike(userId);
        return film;
    }

    public List<Film> getMostPopularFilms(Long mostPopularFilmCount) {
        if (mostPopularFilmCount == null || mostPopularFilmCount <= 0) {
            throw new IllegalArgumentException("count должен быть больше 0");
        }
        return filmStorage.getAllFilm().stream()
                .filter(film -> !film.getFilmLikedUsersId().isEmpty())
                .sorted(Comparator.comparing(
                        film -> film.getFilmLikedUsersId().size(),
                        Comparator.reverseOrder()
                ))
                .limit(mostPopularFilmCount)
                .collect(Collectors.toList());
    }

    public void clearAllFilms() {
        filmStorage.clear();
    }
}
