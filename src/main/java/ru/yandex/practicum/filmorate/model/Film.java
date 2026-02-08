package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class Film {
    private final Set<Long> filmLikedUsersId = new HashSet<>();
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @NotNull(message = "Описание не может быть null")
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Long duration;

    public void addLike(Long userId) {
        filmLikedUsersId.add(userId);
    }

    public void removeLike(Long userId) {
        filmLikedUsersId.remove(userId);
    }

}
