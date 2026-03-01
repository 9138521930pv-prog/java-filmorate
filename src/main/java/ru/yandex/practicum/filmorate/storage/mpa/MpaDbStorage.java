package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Qualifier("MpaDbStorage")
@Slf4j
public class MpaDbStorage implements MpaStorage {

    private final MpaRowMapper mpaRowMapper;
    private final JdbcTemplate jdbc;

    @Override
    public Optional<List<Mpa>> getRatings() {
        String query = """
                       SELECT *
                         FROM mpa
                        order by id
                       """;
        try {
            return Optional.of(jdbc.query(query, mpaRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Mpa> getMpaById(Long id) {
        String query = """
                       SELECT *
                         FROM mpa
                        WHERE id = ?
                       """;
        try {
            return Optional.of(jdbc.queryForObject(query, mpaRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void validateMpaExists(Long mpaId) {
        String query = """
                       SELECT 1
                         FROM mpa
                        WHERE ID = ?
                       """;
        try {
            jdbc.queryForObject(query, Integer.class, mpaId);
            log.info("MPA с ID: {} найден.", mpaId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA с ID: {} не найден. Выбрасываем исключение.", mpaId);
            throw new NotFoundException("Пользователь с ID " + mpaId + " не найден");
        }
    }
}
