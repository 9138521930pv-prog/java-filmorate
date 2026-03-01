package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Override
    public Optional<List<Film>> getAllFilm() {
        String query = """
                       SELECT f.id,
                              f.name,
                              f.description,
                              f.release_date,
                              f.duration,
                              f.mpa_id,
                              mpa.name AS mpa_name
                         FROM films AS f
                        INNER JOIN mpa AS mpa ON f.mpa_id = mpa.id
                       """;
        try {
            List<Film> filmList = jdbc.query(query, filmRowMapper);
            Map<Long, Film> filmsById = filmList.stream()
                    .collect(Collectors.toMap(Film::getId, film -> film));

            loadGenresForFilms(filmsById);
            loadLikesForFilms(filmsById);
            return Optional.of(filmList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String query = """
                       SELECT f.id,
                              f.name,
                              f.description,
                              f.release_date,
                              f.duration,
                              f.mpa_id,
                              mpa.name AS mpa_name
                         FROM films AS f
                        INNER JOIN mpa AS mpa ON f.mpa_id = mpa.id
                        WHERE f.id = ?
                       """;
        try {
            Film film = jdbc.queryForObject(query, filmRowMapper, id);
            Map<Long, Film> filmsById = Map.of(film.getId(), film);
            loadGenresForFilms(filmsById);
            loadLikesForFilms(filmsById);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<Film>> getTopFilm(Integer count) {
        final String query = """
                             SELECT F.ID,
                                    F.NAME,
                                    F.DESCRIPTION,
                                    F.RELEASE_DATE,
                                    F.DURATION,
                                    F.MPA_ID,
                                    M.NAME AS MPA_NAME,
                                    COUNT(L.USER_ID) AS LIKES_COUNT
                               FROM FILMS AS F
                               JOIN MPA AS M ON F.MPA_ID = M.ID
                               LEFT JOIN LIKES AS L ON F.ID = L.FILM_ID
                              GROUP BY F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME
                              ORDER BY LIKES_COUNT DESC
                             LIMIT ?
                             """;
        try {
            List<Film> filmList = jdbc.query(query, filmRowMapper, count);
            Map<Long, Film> filmsById = filmList.stream()
                    .collect(Collectors.toMap(Film::getId, film -> film));
            loadGenresForFilms(filmsById);
            loadLikesForFilms(filmsById);
            return Optional.of(filmList);
        } catch (EmptyResultDataAccessException e) {
             return Optional.empty();
        }
    }

    @Override
    public void validateFilmExists(Long userId) {
        String query = """
                       SELECT 1 FROM
                       films WHERE ID = ?
                       """;
        try {
            jdbc.queryForObject(query, Integer.class, userId);
            log.info("Фильм с ID: {} найден.", userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с ID: {} не найден.", userId);
            throw new NotFoundException("Фильм с ID " + userId + " не найден");
        }
    }

    @Override
    public void validateLikeExists(Long filmId, Long userId) {
        String query = """
                       SELECT 1
                         FROM likes
                        WHERE film_id = ?
                          and user_id = ?
                       """;
        try {
            jdbc.queryForObject(query, Integer.class, filmId, userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с ID: {} не ставил лайк фильму с ID {} найден.", userId, filmId);
            throw new ValidationException("Пользователь с ID: " + userId + " не ставил лайк фильму с ID " + filmId + " найден.");
        }
    }

    @Override
    public Boolean emptyLike(Long filmId, Long userId) {
        String query = """
                       SELECT count(*)
                         FROM likes
                        WHERE film_id = ?
                          and user_id = ?
                       """;

        Integer cnt = jdbc.queryForObject(query, Integer.class, filmId, userId);
        return cnt == 0;
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = """
                       INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)
                                  VALUES (?, ?, ?, ?, ?)
                       """;
        validateFilm(film);
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);
        Long generatedId = keyHolder.getKey().longValue();
        film.setId(generatedId);
        saveFilmGenre(film);
        log.info("Создан фильм с ID: {}", film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Запрос на добавление фильма поступил с пустым телом");
        }

        // Проверка MPA
        if (film.getMpa() == null) {
            throw new ValidationException("Поле 'mpa' не может быть пустым");
       }

        if (film.getMpa().getId() == null) {
            throw new ValidationException("ID рейтинга MPA не может быть пустым");
        }
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaStorage.validateMpaExists(film.getMpa().getId());
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            throw new ValidationException(
                    String.format("Дата релиза не может быть раньше %s", minReleaseDate)
            );
        }

        List<Long> validGenreIds = genreStorage.getGenreIds();
        film.getGenres().forEach(genre -> {
            if (!validGenreIds.contains(genre.getId())) {
                throw new NotFoundException("Жанр с ID " + genre.getId() + " отсутствует в системе");
            }
        });
    }

    @Override
    public Film updateFilm(Film film) {
        String query = """
                        UPDATE FILMS SET name = ?,
                          	             description = ?,
                          	             release_date = ?,
                          	             duration = ?,
                          	             mpa_id = ?
                         WHERE id =?;
                        """;
        validateFilm(film);
        int updated = jdbc.update(query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        if (updated == 0) {
            throw new NotFoundException("there is no such film: " + film.getId());
        }
        return film;

    }

    @Override
    public void removeFilm(Long filmId) {
        String checkQuery = """
                            SELECT COUNT(*)
                              FROM films
                             WHERE film_id = ?
                            """;
        int count = jdbc.queryForObject(checkQuery, Integer.class, filmId);

        if (count == 0) {
            throw new NotFoundException("Фильм с ID " + filmId + " не существует!");
        }

        try {
            String deleteGenreQuery = "DELETE FROM film_genre WHERE film_id = ?";
            int genreDeleted = jdbc.update(deleteGenreQuery, filmId);
            log.debug("Удалено записей из film_genre: {}", genreDeleted);

            String deleteMpaQuery = "DELETE FROM mpa WHERE film_id = ?";
            int mpaDeleted = jdbc.update(deleteMpaQuery, filmId);
            log.debug("Удалено записей из mpa: {}", mpaDeleted);

            String deleteFilmQuery = "DELETE FROM films WHERE film_id = ?";
            int filmDeleted = jdbc.update(deleteFilmQuery, filmId);

            if (filmDeleted == 0) {
                throw new NotFoundException("Фильм с ID " + filmId + " не найден при удалении из основной таблицы!");
            }

            log.info("Фильм с ID {} и связанные записи успешно удалены", filmId);
        } catch (DataAccessException e) {
            log.error("Ошибка при каскадном удалении фильма с ID {}", filmId, e);
            throw new ValidationException("Ошибка при удалении фильма и связанных данных");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String query = """
                       INSERT INTO likes (film_id, user_id)
                       VALUES (?,?)
                       """;
        int updated = jdbc.update(query, filmId, userId);
        if (updated == 0) {
            throw new NotFoundException("there is no such film: " + filmId + "or user: " + userId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        String query = """
                       DELETE FROM likes
                       WHERE film_id = ? AND
                       user_id = ?
                       """;
        int updated = jdbc.update(query, filmId, userId);
        if (updated == 0) {
            throw new NotFoundException("there is no such film: " + filmId + "or user: " + userId);
        }
    }

    private void loadGenresForFilms(Map<Long, Film> filmsById) {
        if (filmsById.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(filmsById.size(), "?"));
        String query = String.format(
                "SELECT fg.FILM_ID, g.ID, g.NAME " +
                        "FROM FILM_GENRES fg " +
                        "JOIN GENRES g ON g.ID = fg.GENRE_ID " +
                        "WHERE fg.FILM_ID IN (%s) ",
                placeholders
        );
        jdbc.query(query, resultSet -> {
            long filmId = resultSet.getLong("FILM_ID");
            long genreId = resultSet.getInt("ID");
            String genreName = resultSet.getString("NAME");
            Genre genre = new Genre(genreId, genreName);
            filmsById.get(filmId).getGenres().add(genre);
        }, filmsById.keySet().toArray());
    }

    private void loadLikesForFilms(Map<Long, Film> filmsById) {
        if (filmsById.isEmpty()) {
            return;
        }
        String addStr = String.join(",", Collections.nCopies(filmsById.size(), "?"));
        final String query = String.format(
                "SELECT FILM_ID, USER_ID FROM LIKES WHERE FILM_ID IN (%s)",
                addStr
        );
        jdbc.query(query, resultSet -> {
            long filmId = resultSet.getLong("FILM_ID");
            long userId = resultSet.getLong("USER_ID");
            filmsById.get(filmId).getLikes().add(userId);
        }, filmsById.keySet().toArray());
    }

    private void saveFilmGenre(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String query = "DELETE FROM film_genres WHERE film_id = ?";
        int deleted = jdbc.update(query, film.getId());

        log.debug("Deleted {} existing genre records for film ID {}", deleted, film.getId());

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        log.info(sql);

        jdbc.batchUpdate(sql,
                film.getGenres(),
                film.getGenres().size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                }
        );
    }
}