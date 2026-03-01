package ru.yandex.practicum.filmorate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc

public class UserControllerTests {
    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private long ind;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @Autowired
    private UserStorage userStorage;

    // Добавление пользователя
    @Test
    void testCreateUserSuccessfully() throws Exception {

        long ind1 = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind1 + "@test.ru\",\"login\":\"testlogin" + ind1 + "\",\"name\":\"Test Name\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test" + ind1 + "@test.ru"))
                .andExpect(jsonPath("$.login").value("testlogin" + ind1));
    }

    // Электронная почта не может быть пустой
    @Test
    void testReturnRequestWhenEmailIsEmpty() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Электронная почта не может быть пустой"));
    }

    // Email должен быть корректным
    @Test
    void testReturnRequestWhenEmailIsIncorrect() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"testtest.ru\",\"login\":\"testlogin\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email должен быть корректным"));
    }

    // Логин не может быть пустым
    @Test
    void testReturnRequestWhenLoginIsEmpty() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Логин не может быть пустым"));
    }

    // Имя для отображения может быть пустым — в таком случае будет использован логин;
    @Test
    void testReturnRequestWhenNameIsIncorrect() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("testlogin" + ind));
    }

    // Дата рождения обязательна
    @Test
    void testReturnRequestWhenBirthdayIsEmpty() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Дата рождения обязательна"));
    }

    // дата рождения не может быть в будущем
    @Test
    void testReturnRequestWhenBirthdayIsIncorrect() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"2990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Дата рождения не может быть в будущем"));
    }

    // Вывод всех пользователей
    @Test
    void testReturnRequestWhenGetAllUsers() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind  + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated());
        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Запрос пользователя по не существующему ID
    @Test
    void testReturnRequestWhenGetOneBadUsers() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь не найден"));
    }

    // Запрос пользователя по существующему ID
    @Test
    void testReturnRequestWhenGetOneUsers() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult0 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long userId0 = getId(responseBody0);
        Long ind1 = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind1 + "@test.ru\",\"login\":\"testlogin" + ind1 + "\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/users/" + userId0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId0))
                .andExpect(jsonPath("$.name").value("Иванов"))
                .andExpect(jsonPath("$.login").value("testlogin" + ind));
    }

    // Редактирование пользователя по ID
    @Test
    void testReturnRequestWhenUpdateUsers() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult0 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long userId0 = getId(responseBody0);

        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + userId0 + "\",\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"name\":\"Петров\",\"birthday\":\"2025-01-01\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + userId0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId0))
                .andExpect(jsonPath("$.name").value("Петров"))
                .andExpect(jsonPath("$.login").value("testlogin" + ind));
    }

    // Добавление друга
    @Test
    void testReturnRequestWhenAddFriend() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult0 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long userId0 = getId(responseBody0);
        String responseBody1 = mvcResult1.getResponse().getContentAsString();
        Long userId1 = getId(responseBody1);
        mockMvc.perform(put("/users/" + userId0 + "/friends/" + userId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // Добавление друга - Ошибка - сам себе
    @Test
    void testReturnRequestWhenAddFriend1() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult0 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long userId0 = getId(responseBody0);
        String responseBody1 = mvcResult1.getResponse().getContentAsString();
        Long userId1 = getId(responseBody1);
        mockMvc.perform(put("/users/" + userId0 + "/friends/" + userId0)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Нельзя добавить себя в друзья: userId=" + userId0));
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

    // Удаление друга
    @Test
    void testReturnRequestWhenDelFriend() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult0 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long userId0 = getId(responseBody0);
        String responseBody1 = mvcResult1.getResponse().getContentAsString();
        Long userId1 = getId(responseBody1);
        mockMvc.perform(put("/users/" + userId0 + "/friends/" + userId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/users/" + userId0 + "/friends/" + userId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    // Получение списка друзей
    @Test
    void testReturnRequestWhenListFriend() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult0 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult1 =  mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1991-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long userId0 = getId(responseBody0);
        String responseBody1 = mvcResult1.getResponse().getContentAsString();
        Long userId1 = getId(responseBody1);

        mockMvc.perform(put("/users/" + userId0 + "/friends/" + userId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/" + userId0 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId1));
    }

    // Получение списка общих друзей
    @Test
    void testReturnRequestWhenListCommonFriend() throws Exception {
        long ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult0 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1990-01-01\",\"name\":\"Иванов\"}"))
                        .andExpect(status().isCreated())
                        .andReturn();
        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1991-01-01\"}"))
                        .andExpect(status().isCreated())
                        .andReturn();
        ind = Math.abs(UUID.randomUUID().getMostSignificantBits());
        MvcResult mvcResult2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test" + ind + "@test.ru\",\"login\":\"testlogin" + ind + "\",\"birthday\":\"1991-01-01\"}"))
                        .andExpect(status().isCreated())
                        .andReturn();
        String responseBody0 = mvcResult0.getResponse().getContentAsString();
        Long userId0 = getId(responseBody0);
        String responseBody1 = mvcResult1.getResponse().getContentAsString();
        Long userId1 = getId(responseBody1);
        String responseBody2 = mvcResult2.getResponse().getContentAsString();
        Long userId2 = getId(responseBody2);

        mockMvc.perform(put("/users/" + userId0 + "/friends/" + userId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/users/" + userId2 + "/friends/" + userId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/" + userId0 + "/friends/common/" + userId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId1));
    }

}
