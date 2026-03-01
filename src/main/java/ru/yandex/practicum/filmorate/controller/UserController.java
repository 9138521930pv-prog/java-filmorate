package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.dto.request.UserRequestDto;
import ru.yandex.practicum.filmorate.dto.response.UserResponseDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDto> getUsersAll() {
        return userService.getUserAll();
    }


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getUserId(@PathVariable("id")
                                     @NotNull(message = "id не может быть null")
                                     @Min(value = 1, message = "id должен быть положительным целым числом")
                                     @Valid Long userId) {
        return userService.getUserById(userId);
}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto addUser(@Valid @RequestBody UserRequestDto user) {
        UserResponseDto savedUser = userService.addUsers(user);
        URI location = URI.create("/user/" + savedUser.getId());
        return savedUser;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto  updateUser(@Valid @RequestBody UserRequestDto user) {
        return userService.updateUsers(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(@PathVariable("id")
                          @NotNull(message = "id не может быть null")
                          @Min(value = 1, message = "id должен быть положительным целым числом")
                          @Valid Long userId,
                          @PathVariable("friendId")
                          @NotNull(message = "id не может быть null")
                          @Min(value = 1, message = "id должен быть положительным целым числом")
                          @Valid Long addedFriendsId) {
        userService.addFriends(userId, addedFriendsId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFriend(@PathVariable("id")
                             @NotNull(message = "id не может быть null")
                             @Min(value = 1, message = "id должен быть положительным целым числом")
                             @Valid Long userId,
                             @PathVariable("friendId")
                             @NotNull(message = "id не может быть null")
                             @Min(value = 1, message = "id должен быть положительным целым числом")
                             @Valid Long removedFriendsId) {
        userService.removeFriends(userId, removedFriendsId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDto> getFriendsListOfUser(@PathVariable("id")
                                                      @NotNull(message = "id не может быть null")
                                                      @Min(value = 1, message = "id должен быть положительным целым числом")
                                                      @Valid Long userId) {
        return userService.getFriendsList(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDto> getCommonFriends(@PathVariable("id")
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
