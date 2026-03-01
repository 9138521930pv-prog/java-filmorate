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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
public class TestFilmDbStorage {
    private final FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void seedDictionaries() {
   //     jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (1, 'G')");
   //     jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (2, 'PG-13')");
   //     jdbc.update("MERGE INTO GENRE (ID, NAME) KEY(ID) VALUES (1, 'Комедия')");
   //     jdbc.update("MERGE INTO GENRE (ID, NAME) KEY(ID) VALUES (2, 'Драма')");

        // Пользователи для лайков (на случай внешних ключей)
        jdbc.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u1@x','u1','U1', DATE '1990-01-01')");
        jdbc.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u2@x','u2','U2', DATE '1991-01-01')");
        jdbc.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u3@x','u3','U3', DATE '1992-01-01')");
    }

    @Test
    void create_find_update_and_containsKey() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2001, 1, 1));
        film.setDuration(100L);
        film.setMpa(new Mpa(1L, null));
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1L, null));
        film.setGenres(genres);

        film = filmDbStorage.addFilm(film);
        assertThat(film.getId()).isPositive();
   //     assertThat(filmDbStorage.containsKey(film.getId())).isTrue();

        Optional<Film> foundFilm = filmDbStorage.getFilmById(film.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getMpa().getName()).isEqualTo("G"); // имя подхватилось из БД
        assertThat(foundFilm.get().getGenres()).extracting(Genre::getId).containsExactly(1L);

        // обновим жанры и длительность
        film.setDuration(120L);
        Set<Genre> updatedGenres = new LinkedHashSet<>();
        updatedGenres.add(new Genre(1L, null));
        updatedGenres.add(new Genre(2L, null));
        film.setGenres(updatedGenres);

        Film updatedFilm = filmDbStorage.updateFilm(film);
        assertThat(updatedFilm.getDuration()).isEqualTo(120);
        assertThat(updatedFilm.getGenres()).extracting(Genre::getId).containsExactly(1L, 2L);
    }

    @Test
    void findAll_and_popular() {
        // фильм 1
        Film filmA = new Film();
        filmA.setName("A");
        filmA.setDescription("d");
        filmA.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmA.setDuration(90L);
        filmA.setMpa(new Mpa(1L, null));
        filmA.setGenres(new LinkedHashSet<>(List.of(new Genre(1L, null))));
        filmA = filmDbStorage.addFilm(filmA);

        // фильм 2
        Film filmB = new Film();
        filmB.setName("B");
        filmB.setDescription("d");
        filmB.setReleaseDate(LocalDate.of(2000, 2, 1));
        filmB.setDuration(95L);
        filmB.setMpa(new Mpa(2L, null));
        filmB.setGenres(new LinkedHashSet<>(List.of(new Genre(2L, null))));
        filmB = filmDbStorage.addFilm(filmB);

        List<Film> allFilms = filmDbStorage.getAllFilm()
                .orElseThrow(() -> new AssertionError("No films found in getAllFilm()"));
        assertThat(allFilms.size()).isGreaterThanOrEqualTo(2);

        filmDbStorage.addLike(filmB.getId(), 1L);
        filmDbStorage.addLike(filmB.getId(), 2L);
        filmDbStorage.addLike(filmA.getId(), 1L);

        List<Film> popularFilms = filmDbStorage.getTopFilm(10)
                .orElseThrow(() -> new AssertionError("No popular films found in getTopFilm()"));
        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(filmB.getId());
    }
}
