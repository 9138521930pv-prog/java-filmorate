package ru.yandex.practicum.filmorate.storage.film;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
//import ru.yandex.practicum.filmorate.model.Film;


//import java.sql.Statement;
//import java.util.*;
//import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
//-!-----Нужен---проверен
    @Override
    public List<Film> getAllFilm() {
        String query = """
                SELECT f.id, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID,
                       (SELECT '[' ||
                                                        STRING_AGG(
                                                            '{"id": ' || b.ID || ', "name": "' ||
                                                            b.NAME || '"}',
                                                            ', ' ORDER BY b.ID
                                                        ) || ']' AS genres_json
                                                    FROM film_genres a
                                                    LEFT JOIN genres b ON a.genre_id = b.id
                                            
                       ) AS genre,
                       (SELECT LISTAGG(l.user_id , ',')
                          FROM likes l 
                         WHERE l.film_id = f.id
                       ) AS likes,
                      '{"id": ' || c.ID || ', "name": "' || c.NAME || '"}' AS mpa
                  FROM films f
                  LEFT JOIN mpa c ON f.mpa_id = c.id
        """;
        return jdbc.query(query, filmRowMapper);
    }
    //-!-----Нужен---проверен
    @Override
    public Film getFilmById(Long id) {
        String query = """
                SELECT f.id, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID,
                       (SELECT '[' ||
                                                                STRING_AGG(
                                                                    '{"id": ' || b.ID || ', "name": "' ||
                                                                    b.NAME || '"}',
                                                                    ', ' ORDER BY b.ID
                                                                ) || ']' AS genres_json
                                                            FROM film_genres a
                                                            LEFT JOIN genres b ON a.genre_id = b.id
                                                            WHERE a.film_id = f.id                       
                       ) AS genre,
                       (SELECT LISTAGG(l.user_id , ',')
                          FROM likes l 
                         WHERE l.film_id = f.id
                       ) AS likes,
                      '{"id": ' || c.ID || ', "name": "' || c.NAME || '"}' AS mpa
                  FROM films f
                  LEFT JOIN mpa c ON f.mpa_id = c.id
                  WHERE f.id = ?
        """;

        return  jdbc.queryForObject(query, filmRowMapper, id);
    }
//-!-----Нужен---проверен
    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = """
                INSERT INTO FILMS (
                    NAME,
                    DESCRIPTION,
                    RELEASE_DATE,
                    DURATION,
                    MPA_ID
                ) VALUES (?, ?, ?, ?, ?)
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
        // Сохраняем жанры
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

        try {
            mpaStorage.getMpaById(film.getMpa().getId());
        } catch (Exception e) {
            throw new NotFoundException(
                    String.format("Рейтинг MPA с ID=%d не найден", film.getMpa().getId()));
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
//-!-----Нужен---проверен
    @Override
    public Film updateFilm(Film film) {
        String query = """
                UPDATE FILMS SET
                	name = ?,
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
//!!!!!!!!!!!!!!!!!!!!!
    public void removeFilm(Long filmId) {
        String query = """    
                select ?
                """;
        int updated = jdbc.update(query, filmId);
        if (updated == 0) {
            throw new NotFoundException("Фильм с : " + filmId + " не сушествует!");
        }
    }

    //!!!!!!!!!!!!!!!!!!!!!
    public Set<Long> getLikesByFilmId(Long id) {
        String query = "SELECT user_id FROM likes WHERE film_id = ?";
        return Set.copyOf(jdbc.queryForList(query, Long.class, id));
    }
    //!!!!!!!!!!!!!!!!!!!!!
    @Override
    public Map<Long, List<Long>> getLikesByFilmId(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) return Map.of();

        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = """
                SELECT film_id, user_id
                FROM likes
                WHERE film_id IN (""" + inSql + ") ORDER BY film_id, user_id";

        Object[] params = filmIds.toArray();
        return jdbc.query(sql, rs -> {
            Map<Long, List<Long>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                long userId = rs.getLong("user_id");
                map.computeIfAbsent(filmId, k -> new ArrayList<>()).add(userId);
            }
            return map;
        }, params);
    }
//-!-----Нужен---проверен
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
    //-!-----Нужен---проверен
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
    //-!-----Нужен---проверен
    @Override
    public List<Film> getTopFilm(Integer count) {
        String query = """
                SELECT f.id, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID,
                       (SELECT '[' || LISTAGG('{"id": ' || b.ID || ', "name": "' || b.NAME || '"}', ', ') || ']'
                          FROM film_genres a 
                          LEFT JOIN genres b ON a.genre_id = b.id
                         WHERE a.film_id = f.id
                       ) AS genre,
                       (SELECT LISTAGG(l.user_id , ',')
                          FROM likes l 
                         WHERE l.film_id = f.id
                       ) AS likes,
                      '{"id": ' || c.ID || ', "name": "' || c.NAME || '"}' AS mpa
                  FROM films f
                  JOIN (SELECT film_id,
                               count(user_id) AS likes_count
                	      FROM likes
                	     GROUP BY film_id
                	     ORDER BY likes_count DESC
                	     LIMIT ?) AS lc ON f.id = lc.film_id                  
                  LEFT JOIN mpa c ON f.mpa_id = c.id
                 ORDER BY lc.likes_count DESC
                """;
        return jdbc.query(query, filmRowMapper, count);
    }


    private void saveFilmGenre(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        int deleted = jdbc.update(deleteSql, film.getId());

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