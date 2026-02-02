package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Запрос на добавление фильма поступил с пустым телом");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Успешно добавлен новый фильм с ID: {}", film.getId());
        return film;
    }

    @Override
    public Film removeFilm(Long filmId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new ValidationException("Попытка удаления фильма. Фильм с ID: " + filmId + " не найден");
        }
        log.info("Фильм с ID: {} успешно удален.", filmId);
        return films.remove(filmId);
    }

    @Override
    public List<Film> getAllFilm() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Long filmId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID: " + filmId + " не найден");
        }
        log.info("Фильм с ID: {} найден и успешно предоставлен в ответ на запрос.", filmId);
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        if (updatedFilm == null) {
            throw new ValidationException("Запрос на обновление данных фильма поступил с пустым телом");
        }

        Long filmId = updatedFilm.getId();
        if (filmId == null) {
            throw new ValidationException("ID фильма должен быть указан");
        }

        Film film = films.get(filmId);
        if (film == null) {
            throw new ValidationException("Фильм с ID: " + filmId + " не найден");
        }

        if (updatedFilm.getReleaseDate() != null
                && updatedFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        film.setName(updatedFilm.getName());
        film.setDescription(updatedFilm.getDescription());
        film.setReleaseDate(updatedFilm.getReleaseDate());
        film.setDuration(updatedFilm.getDuration());
        log.info("Данные фильма с ID: {} успешно обновлены", film.getId());
        return film;
    }

    @Override
    public void clear() {
        films.clear();
        log.info("Хранилище films очищено. Текущий размер: {}", films.size());
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
