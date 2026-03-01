package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
class TestMpaDbStorage {

    private final MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void seedDatabase() {
        jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (1, 'G')");
        jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (2, 'PG')");
    }

    @Test
    void findAll_and_findById_work() {
        Optional<List<Mpa>> allMpasOptional = mpaDbStorage.getRatings();
        List<Mpa> allMpas = allMpasOptional
                .orElseThrow(() -> new AssertionError("No MPA ratings found"));
        assertThat(allMpas).isNotEmpty();

        Mpa mpa = mpaDbStorage.getMpaById(1L)
                .orElseThrow(() -> new AssertionError("Mpa with ID 1 not found"));
        assertThat(mpa.getName()).isEqualTo("G");
    }
}
