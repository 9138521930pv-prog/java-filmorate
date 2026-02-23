package ru.yandex.practicum.filmorate.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmRowMapper implements RowMapper<Film> {

    private final ObjectMapper objectMapper = new ObjectMapper(); // Один экземпляр на весь бин

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .duration(rs.getLong("duration"))
                .mpa(jsonToMpa(rs.getString("mpa")))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .genres(jsonToSetGenre(rs.getString("genre")))
                .likes(strToSetLike(rs.getString("likes")))
                .build();
    }

    public Mpa jsonToMpa(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, Mpa.class);
        } catch (Exception e) {
            log.error("Ошибка при преобразовании JSON в Mpa. JSON: '{}'", json, e);
            return null;
        }
    }

    public Set<Genre> jsonToSetGenre(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptySet();
        }

        try {
            List<Genre> genreList = objectMapper.readValue(
                    json,
                    new TypeReference<List<Genre>>() {}
            );
            return new HashSet<>(genreList);
        } catch (Exception e) {
            log.error("Ошибка десериализации JSON‑массива в Set<Genre>: '{}'", json, e);
            return Collections.emptySet();
        }
    }


    private Set<Long> strToSetLike(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptySet();
        }
        try {
            return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            log.error("Ошибка парсинга чисел из строки '{}'", input, e);
            return Collections.emptySet();
        }
    }

}