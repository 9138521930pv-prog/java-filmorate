package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.dto.request.UserRequestDto;
import ru.yandex.practicum.filmorate.dto.response.UserResponseDto;
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
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsersAll() {
        List<UserResponseDto> users = userService.getUserAll();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(users);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserId(@PathVariable("id")
                                                     @NotNull(message = "id не может быть null")
                                                     @Min(value = 1, message = "id должен быть положительным целым числом")
                                                     @Valid Long userId) {
        UserResponseDto user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> addUser(@Valid @RequestBody UserRequestDto user) {
        UserResponseDto savedUser = userService.addUsers(user);
        URI location = URI.create("/user/" + savedUser.getId());

        return ResponseEntity.created(location).body(savedUser);
    }

    @PutMapping
    public ResponseEntity<UserResponseDto>  updateUser(@Valid @RequestBody UserRequestDto user) {
        return ResponseEntity.ok(userService.updateUsers(user));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserResponseDto> addFriend(@PathVariable("id")
                                                     @NotNull(message = "id не может быть null")
                                                     @Min(value = 1, message = "id должен быть положительным целым числом")
                                                     @Valid Long userId,
                                                     @PathVariable("friendId")
                                                     @NotNull(message = "id не может быть null")
                                                     @Min(value = 1, message = "id должен быть положительным целым числом")
                                                     @Valid Long addedFriendsId) {
        userService.addFriends(userId, addedFriendsId);
        return ResponseEntity.noContent().build();
        }



    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable("id")
                                             @NotNull(message = "id не может быть null")
                                             @Min(value = 1, message = "id должен быть положительным целым числом")
                                             @Valid Long userId,
                                             @PathVariable("friendId")
                                             @NotNull(message = "id не может быть null")
                                             @Min(value = 1, message = "id должен быть положительным целым числом")
                                             @Valid Long removedFriendsId) {
        userService.removeFriends(userId, removedFriendsId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<UserResponseDto>> getFriendsListOfUser(@PathVariable("id")
                                                                      @NotNull(message = "id не может быть null")
                                                                      @Min(value = 1, message = "id должен быть положительным целым числом")
                                                                      @Valid Long userId) {
        List<UserResponseDto> friends = userService.getFriendsList(userId);

        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<UserResponseDto>> getCommonFriends(@PathVariable("id")
                                                                  @NotNull(message = "id не может быть null")
                                                                  @Min(value = 1, message = "id должен быть положительным целым числом")
                                                                  @Valid Long userId,
                                                                  @PathVariable("otherId")
                                                                  @NotNull(message = "id не может быть null")
                                                                  @Min(value = 1, message = "id должен быть положительным целым числом")
                                                                  @Valid Long anotherUserId) {
        return ResponseEntity.ok(userService.getCommonFriends(userId, anotherUserId));
    }
}
