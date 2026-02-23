package ru.yandex.practicum.filmorate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserRequestDto {
    @Positive
    private Long id;
    private String name;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Email должен быть корректным")
    private String email;
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S*$", message = "Логин не может быть пустым и не должен содержать пробелы")
    private String login;
    @Past(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Дата рождения обязательна")
    private LocalDate birthday;
}