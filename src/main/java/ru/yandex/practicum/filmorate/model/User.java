package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class User {
    private final Set<Long> friendsId = new HashSet<>();
    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Email должен быть корректным")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S*$", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Дата рождения обязательна")
    private LocalDate birthday;

    public void addFriend(Long addedFriendsId) {
        friendsId.add(addedFriendsId);
    }

    public void removeFriend(Long removedFriendsId) {
        friendsId.remove(removedFriendsId);
    }
}
