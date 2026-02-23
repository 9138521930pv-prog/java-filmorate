package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Component("genreDbStorage")
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final GenreRowMapper genreRowMapper;
    private final JdbcTemplate jdbc;

    @Override
    public List<Genre> getGenre() {
        String query = "SELECT * FROM genres order by id";
        return jdbc.query(query, genreRowMapper);
    }

    @Override
    public Genre getGenreById(Long id) {
        String query = "SELECT * FROM genres WHERE id = ?";
        Genre genre;
        try {
            genre =  jdbc.queryForObject(query, genreRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с ID= " + id + " не найден");
        }
        return genre;
    }

    @Override
    public Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) return Map.of();

        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String query = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genre fg
                JOIN film_genres g ON g.id = fg.genre_id
                WHERE fg.film_id IN (""" + inSql + ") ORDER BY fg.film_id, g.id";

        Object[] params = filmIds.toArray();
        return jdbc.query(query, rs -> {
            Map<Long, List<Genre>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Genre g = new Genre(rs.getLong("id"), rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new ArrayList<>()).add(g);
            }
            return map;
        }, params);
    }

    @Override
    public void setGenreToFilm(Long genreId, Long filmId) {
        String query = "INSERT INTO film_genres (genre_id, film_id) VALUES (?,?)";
        jdbc.update(query, genreId, filmId);
    }

    @Override
    public Set<Genre> getGenresByFilmId(Long id) {
        String query = "SELECT * FROM film_genres WHERE id IN (SELECT genre_id FROM film_genre WHERE film_id = ?)";
        List<Genre> genres = jdbc.query(query, genreRowMapper, id);
        return new HashSet<>(genres);
    }


    @Override
    public List<Long> getGenreIds() {
        List<Genre> genres = getGenre();
        return genres.stream()
                .map(Genre::getId).filter(Objects::nonNull).toList();
    }


}