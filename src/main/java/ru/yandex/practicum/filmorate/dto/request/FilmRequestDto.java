package ru.yandex.practicum.filmorate.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.response.MpaResponseDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilmRequestDto {
    @Positive
    private Long id;
    @NotBlank
    @NotNull(message = "Описание не может быть null")
    private String name;
    @NotNull(message = "Описание не может быть null")
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Long duration;
    private Set<Genre> genres = Collections.emptySet();
    private MpaResponseDto mpa;
}