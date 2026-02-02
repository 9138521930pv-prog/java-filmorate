package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTests {
    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected static String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка преобразования объекта в JSON", e);
        }
    }

    @Autowired
    private FilmController filmController;

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage.clear();
        userStorage.clear();
    }

// Добавление фильма
    @Test
    void testCreateFilmSuccessfully() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/films/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.releaseDate").value("1990-01-01"))
                .andExpect(jsonPath("$.duration").value("120"))
                .andExpect(jsonPath("$.description").value("Хороший фильм"))
                .andExpect(jsonPath("$.name").value("Новый фильм"));
    }

// Название не может быть пустым;
    @Test
    void testReturnRequestWhenNameIsEmpty() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"));
    }

// Максимальная длина описания — 200 символов;
    @Test
    void testReturnRequestWhenDescriptionIsMaxLength() throws Exception {
        String maxLengthDescription = "A".repeat(255);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"" + maxLengthDescription + "\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Описание не должно превышать 200 символов"));
    }

// Дата релиза — не раньше 28 декабря 1895 года;
    @Test
    void testReturnRequestWhenReleaseDateInCorrect() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"2027-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.releaseDate").value("Дата релиза не может быть в будущем"));
    }

// Продолжительность фильма должна быть положительным числом.
    @Test
    void testReturnRequestWhenDurationInCorrect() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"-120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration").value("Продолжительность фильма должна быть положительным числом"));
    }

// Вывод всех пользователей
    @Test
    void testReturnRequestWhenGetAllFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Очень новый фильм\",\"duration\":\"125\",\"description\":\"Хороший фильм\",\"releaseDate\":\"2025-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Новый фильм"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Очень новый фильм"));
    }

// Запрос фильма по не существующему ID
    @Test
    void testReturnRequestWhenGetOneBadIdFilm() throws Exception {
        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с ID: 999 не найден"));
    }

// Запрос фильма по существующему ID
    @Test
    void testReturnRequestWhenGetOneFilms() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Очень новый фильм\",\"duration\":\"125\",\"description\":\"Хороший фильм\",\"releaseDate\":\"2025-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/films/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Новый фильм"));
    }

    // Запрос добавление фильма с ID
    @Test
    void testReturnRequestWhenGetOneFilmsID() throws Exception {
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isNotFound());
    }

// Редактирование пользователя по ID
    @Test
    void testReturnRequestWhenUpdateFilms() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"Очень новый фильм\",\"duration\":\"125\",\"description\":\"Очень хороший фильм\",\"releaseDate\":\"2025-01-01\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Очень новый фильм"))
                .andExpect(jsonPath("$.description").value("Очень хороший фильм"))
                .andExpect(jsonPath("$.duration").value("125"));
    }

// Добавление лайка все параметры валидные
@Test
void testReturnRequestWhenAddLike() throws Exception {
    mockMvc.perform(post("/films")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
            .andExpect(status().isCreated());
    mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
            .andExpect(status().isCreated());
    mockMvc.perform(put("/films/1/like/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
            .andExpect(status().isOk());
}

// Добавление лайка все параметры валидные
    @Test
    void testReturnRequestWhenDelLike() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/films/1/like/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/films/1/like/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());
    }

//Возвращает список из первых count фильмов по количеству лайков. popular?count=0
    @Test
    void testReturnRequestWhenPopularCount0() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/films/1/like/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular?count=0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("getMostPopularFilms.mostPopularFilmCount: count должен быть больше 0"));

    }

//Возвращает список из первых count фильмов по количеству лайков. popular?count=2
    @Test
    void testReturnRequestWhenPopularCount1() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1@test.ru\",\"login\":\"testlogin1\",\"birthday\":\"1990-01-01\",\"name\":\"Петров\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/films/1/like/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/1/like/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular?count=2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

}
