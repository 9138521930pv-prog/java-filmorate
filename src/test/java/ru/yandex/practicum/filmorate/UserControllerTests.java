package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController.clear(); // Сброс перед каждым тестом
    }

    // Добавление пользователя
    @Test
    void testCreateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"name\":\"Test Name\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@test.ru"))
                .andExpect(jsonPath("$.login").value("testlogin"));
    }

// Электронная почта не может быть пустой
    @Test
    void testReturnRequestWhenEmailIsEmpty() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Электронная почта не может быть пустой"));
    }

// Email должен быть корректным
    @Test
    void testReturnRequestWhenEmailIsIncorrect() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"testtest.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email должен быть корректным"));
    }

// Логин не может быть пустым
    @Test
    void testReturnRequestWhenLoginIsEmpty() throws Exception {
        mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"test@test.ru\",\"login\":\"\",\"birthday\":\"1990-01-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.login").value("Логин не может быть пустым"));
    }

// Логин не должен содержать пробелы
    @Test
    void testReturnRequestWhenLoginIsIncorrect() throws Exception {
        mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"test@test.ru\",\"login\":\"  \",\"birthday\":\"1990-01-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.login").value("Логин не должен содержать пробелы"));
    }

// Имя для отображения может быть пустым — в таком случае будет использован логин;
    @Test
    void testReturnRequestWhenNameIsIncorrect() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("testlogin"));
    }

// Дата рождения обязательна
    @Test
    void testReturnRequestWhenBirthdayIsEmpty() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthday").value("Дата рождения обязательна"));
    }

// дата рождения не может быть в будущем
    @Test
    void testReturnRequestWhenBirthdayIsIncorrect() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"2990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthday").value("Дата рождения не может быть в будущем"));
    }

// Вывод всех пользователей
    @Test
    void testReturnRequestWhenGetAllUsers() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1@test.ru\",\"login\":\"testlogin1\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Иванов"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("testlogin1"));
    }

// Запрос пользователя по не существующему ID
    @Test
    void testReturnRequestWhenGetOneBadUsers() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 999 не найден"));
    }

// Запрос пользователя по существующему ID
    @Test
    void testReturnRequestWhenGetOneUsers() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1@test.ru\",\"login\":\"testlogin1\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иванов"))
                .andExpect(jsonPath("$.login").value("testlogin"));
    }

// Редактирование пользователя по ID
    @Test
    void testReturnRequestWhenUpdateUsers() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"email\":\"test1@test.ru\",\"login\":\"testlogin1\",\"name\":\"Петров\",\"birthday\":\"2025-01-01\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Петров"))
                .andExpect(jsonPath("$.login").value("testlogin1"));
    }

}

