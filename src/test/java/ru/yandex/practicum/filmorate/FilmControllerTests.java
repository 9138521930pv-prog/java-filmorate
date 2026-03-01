package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.io.IOException;
import java.util.Map;

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

    @Autowired
    private JdbcTemplate jdbc;


    // Добавление фильма
    @Test
    void testCreateFilmSuccessfully() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String locationHeader = mvcResult.getResponse().getHeader("Location");
        String responseBody = mvcResult.getResponse().getContentAsString();
        Long filmId = getId(responseBody);
        mockMvc.perform(get(locationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.releaseDate").value("1990-01-01"))
                .andExpect(jsonPath("$.duration").value(120)) // число без кавычек
                .andExpect(jsonPath("$.description").value("Хороший фильм"))
                .andExpect(jsonPath("$.name").value("Новый фильм"));

    }

    // Название не может быть пустым;
    @Test
    void testReturnRequestWhenNameIsEmpty() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Название фильма не может быть пустым"));
    }

    // Максимальная длина описания — 200 символов;
    @Test
    void testReturnRequestWhenDescriptionIsMaxLength() throws Exception {
        String maxLengthDescription = "A".repeat(255);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"" + maxLengthDescription + "\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Описание не должно превышать 200 символов"));
    }

    // Дата релиза — не раньше 28 декабря 1895 года;
    @Test
    void testReturnRequestWhenReleaseDateInCorrect() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"2027-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Дата выхода не может быть в будущем"));
    }

    // Продолжительность фильма должна быть положительным числом.
    @Test
    void testReturnRequestWhenDurationInCorrect() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"-120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Продолжительность фильма должна быть положительным числом"));
    }

    // Вывод всех пользователей
    @Test
    void testReturnRequestWhenGetAllFilm() throws Exception {
        MvcResult mvcResult0 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcResult1 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Очень новый фильм\",\"duration\":\"125\",\"description\":\"Хороший фильм\",\"releaseDate\":\"2025-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long filmId0 = getId(responseBody0);
        String responseBody1 = mvcResult1.getResponse().getContentAsString();
        Long filmId1 = getId(responseBody1);
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Запрос фильма по не существующему ID
    @Test
    void testReturnRequestWhenGetOneBadIdFilm() throws Exception {
        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Фильм не найден."));
    }


    private Long getId(String responseBody) throws IOException {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        //Long id = (Long) responseMap.get("id");
        Object idObject = responseMap.get("id");
        if (idObject instanceof Long) {
            return (Long) idObject;
        } else if (idObject instanceof Integer) {
            return ((Integer) idObject).longValue();
        } else {
            throw new IllegalArgumentException(
                    "Поле 'id' имеет неподдерживаемый тип: " + idObject.getClass()
            );
        }

    }

    // Запрос фильма по существующему ID
    @Test
    void testReturnRequestWhenGetOneFilms() throws Exception {

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated());
        MvcResult mvcResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Очень новый фильм\",\"duration\":\"125\",\"description\":\"Хороший фильм\",\"releaseDate\":\"2025-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        Long filmId = getId(responseBody);

        mockMvc.perform(get("/films/" + filmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value("Очень новый фильм"));
    }

    // Запрос добавление фильма с ID
    @Test
    void testReturnRequestWhenGetOneFilmsID() throws Exception {
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"id\":\"9999\",\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isNotFound());
    }

    // Редактирование пользователя по ID
    @Test
    void testReturnRequestWhenUpdateFilms() throws Exception {
        MvcResult mvcResult0 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody = mvcResult0.getResponse().getContentAsString();
        Long filmId = getId(responseBody);
        MvcResult mvcResult1 = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"id\":\"" + filmId + "\",\"name\":\"Очень новый фильм\",\"duration\":\"125\",\"description\":\"Очень хороший фильм\",\"releaseDate\":\"2025-01-01\"}"))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/films/" + filmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value("Очень новый фильм"))
                .andExpect(jsonPath("$.description").value("Очень хороший фильм"))
                .andExpect(jsonPath("$.duration").value("125"));
    }

    // Добавление лайка все параметры валидные
    @Test
    void testReturnRequestWhenAddLike() throws Exception {
        MvcResult mvcResult0 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1112@test.ru\",\"login\":\"testlogin1\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long filmId = getId(responseBody0);
        String responseBody1 = mvcResult0.getResponse().getContentAsString();
        Long userId = getId(responseBody1);


        mockMvc.perform(put("/films/" + filmId + "/like/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isNoContent());
    }

    // Добавление лайка все параметры валидные
    @Test
    void testReturnRequestWhenDelLike() throws Exception {
        MvcResult mvcResult0 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}")
                                        )
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1113@test.ru\",\"login\":\"testlogin2\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long filmId = getId(responseBody0);
        String responseBody1 = mvcResult0.getResponse().getContentAsString();
        Long userId = getId(responseBody1);
        mockMvc.perform(put("/films/" + filmId + "/like/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/films/" + filmId + "/like/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());
    }

    //Возвращает список из первых count фильмов по количеству лайков. popular?count=0
    @Test
    void testReturnRequestWhenPopularCount0() throws Exception {
        MvcResult mvcResult0 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test111@test.ru\",\"login\":\"testlogin3\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long filmId = getId(responseBody0);
        String responseBody1 = mvcResult0.getResponse().getContentAsString();
        Long userId = getId(responseBody1);
        mockMvc.perform(put("/films/" + filmId + "/like/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/popular?count=0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка базы данных: firstTenFilms.count: count должен быть положительным числом и > 0"));

    }

    //Возвращает список из первых count фильмов по количеству лайков. popular?count=2
    @Test
    void testReturnRequestWhenPopularCount1() throws Exception {
        MvcResult mvcResult0 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mpa\":{\"id\":1},\"genres\":[{\"id\":1}],\"name\":\"Новый фильм\",\"duration\":\"120\",\"description\":\"Хороший фильм\",\"releaseDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test2221@test.ru\",\"login\":\"testlogin4\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult mvcResult2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1@test.ru\",\"login\":\"testlogin5\",\"birthday\":\"1990-01-01\",\"name\":\"Петров\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long filmId = getId(responseBody0);
        String responseBody1 = mvcResult0.getResponse().getContentAsString();
        Long userId1 = getId(responseBody1);
        String responseBody2 = mvcResult0.getResponse().getContentAsString();
        Long userId2 = getId(responseBody2);
        mockMvc.perform(put("/films/" + filmId + "/like/" + userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/films/" + filmId + "/like/" + userId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/popular?count=2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

}