package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.model.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public User getUser(@PathVariable("id")
                        @NotNull(message = "id не может быть null")
                        @Min(value = 1, message = "id должен быть положительным целым числом")
                        @Valid Long userId) {
        return userService.getUserStorage().getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getUserStorage().getAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsListOfUser(@PathVariable("id")
                                           @NotNull(message = "id не может быть null")
                                           @Min(value = 1, message = "id должен быть положительным целым числом")
                                           @Valid Long userId) {
        return userService.getFriendsListOfUser(userId);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> addUsers(@Valid @RequestBody User user) {
        User savedUser = userService.getUserStorage().addUser(user);
        URI location = URI.create("/user/" + savedUser.getId());
        return ResponseEntity.created(location).body(savedUser);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User updateUser) {
        return userService.getUserStorage().updateUser(updateUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id")
                          @NotNull(message = "id не может быть null")
                          @Min(value = 1, message = "id должен быть положительным целым числом")
                          @Valid Long userId,
                          @PathVariable("friendId")
                          @NotNull(message = "id не может быть null")
                          @Min(value = 1, message = "id должен быть положительным целым числом")
                          @Valid Long addedFriendsId) {
        return userService.addFriend(userId, addedFriendsId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable("id")
                             @NotNull(message = "id не может быть null")
                             @Min(value = 1, message = "id должен быть положительным целым числом")
                             @Valid Long userId,
                             @PathVariable("friendId")
                             @NotNull(message = "id не может быть null")
                             @Min(value = 1, message = "id должен быть положительным целым числом")
                             @Valid Long removedFriendsId) {
        return userService.removeFriend(userId, removedFriendsId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id")
                                       @NotNull(message = "id не может быть null")
                                       @Min(value = 1, message = "id должен быть положительным целым числом")
                                       @Valid Long userId,
                                       @PathVariable("otherId")
                                       @NotNull(message = "id не может быть null")
                                       @Min(value = 1, message = "id должен быть положительным целым числом")
                                       @Valid Long anotherUserId) {
        return userService.getCommonFriends(userId, anotherUserId);
    }

}
