package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersList = new ArrayList<>(users.values());
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(usersList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable @Valid Integer id) {
        User user = users.get(id);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь с ID " + id + " не найден");
        }
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        // Если имя пустое — используем логин
        boolean nameIsNull = user.getName() == null || user.getName().isBlank();
        if (nameIsNull) {
            user.setName(user.getLogin());
            log.info("Имя для отображения может быть пустым — в таком случае будет использован логин");
        }

        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Создан пользователь с ID: {}", user.getId());

        return ResponseEntity
                .created(URI.create("/users/" + user.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(user);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody @Valid User updatedUser) {
        if (updatedUser.getId() == null || !users.containsKey(updatedUser.getId())) {
            throw new IllegalArgumentException("Пользователь с указанным ID не найден");
        }

        if (updatedUser.getName() == null || updatedUser.getName().isBlank()) {
            updatedUser.setName(updatedUser.getLogin());
        }
        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновлён пользователь с ID: {}", updatedUser.getId());
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(updatedUser);
    }
}
