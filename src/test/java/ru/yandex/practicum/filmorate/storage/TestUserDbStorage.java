package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ru.yandex.practicum.filmorate.storage.user.UserDbStorage.class})
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
class TestUserDbStorage {

    private final ru.yandex.practicum.filmorate.storage.user.UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        // ничего не делаем
    }

    @Test
    void create_and_findById_and_containsKey() {
        User u = new User(0L, "user@mail.ru", "login", "Имя", LocalDate.of(1990, 1, 1));
        u = userStorage.addUser(u);

        assertThat(u.getId()).isPositive();
   //    assertThat(userStorage.containsKey(u.getId())).isTrue();

        Optional<User> byId = userStorage.getUserById(u.getId());
        assertThat(byId).isPresent();
        assertThat(byId.get().getEmail()).isEqualTo("user@mail.ru");
    }

    @Test
    void update_updates_fields() {
        User u = userStorage.addUser(new User(0L, "a@a", "a", "A", LocalDate.of(2000, 1, 1)));
        u.setName("B");
        u.setEmail("b@b");
        userStorage.updateUser(u);
        User actual = userStorage.getUserById(u.getId())
                .orElseThrow(() -> new AssertionError("User not found after update"));

        assertThat(actual.getName()).isEqualTo("B");
        assertThat(actual.getEmail()).isEqualTo("b@b");
    }

    @Test
    void findAll_returns_list() {
        userStorage.addUser(new User(0L, "1@x", "l1", "N1", LocalDate.of(1991, 1, 1)));
        userStorage.addUser(new User(0L, "2@x", "l2", "N2", LocalDate.of(1992, 2, 2)));
        Optional<List<User>> allUsersOptional = userStorage.getAllUsers();

        // Проверяем, что Optional не пуст
        assertThat(allUsersOptional).isNotEmpty();

        // Извлекаем список из Optional и проверяем его размер
        List<User> allUsers = allUsersOptional.get();
        assertThat(allUsers.size()).isGreaterThanOrEqualTo(2);

    }
}
