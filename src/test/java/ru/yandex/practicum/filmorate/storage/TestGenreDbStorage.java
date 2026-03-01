package ru.yandex.practicum.filmorate.storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(GenreDbStorage.class)
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
public class TestGenreDbStorage {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GenreDbStorage genreDbStorage;

    @BeforeEach
    void setUpDatabase() {
    /*    jdbcTemplate.update("DELETE FROM GENRE");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (1,'Комедия')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (2,'Драма')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (3,'Мультфильм')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (4,'Триллер')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (5,'Документальный')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (6,'Боевик')");

     */
    }

    @Test
    void findAll_returnsAllGenres_sortedByIdAsc() {
        List<Genre> genreList = genreDbStorage.getGenre()
                .orElseThrow(() -> new AssertionError("No genres found"));

        assertThat(genreList).hasSize(6);
        assertThat(genreList.stream().map(Genre::getId))
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(genreList.get(0).getName()).isEqualTo("Комедия");
        assertThat(genreList.get(2).getName()).isEqualTo("Мультфильм");
    }

    @Test
    void findById_existing_returnsGenre() {
        Optional<Genre> genre = genreDbStorage.getGenreById(3L);
        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(3);
        assertThat(genre.get().getName()).isEqualTo("Мультфильм");
    }

    @Test
    void findById_missing_returnsEmpty() {
        Optional<Genre> genre = genreDbStorage.getGenreById(999L);
        assertThat(genre).isEmpty();
    }
}
