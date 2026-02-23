package ru.yandex.practicum.filmorate.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

@Data
@Builder
public class FilmResponseDto {
    private Long id;
    private String name;
    private String description;
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;
    private Long duration;
    @Builder.Default
    private Set<Genre> genres = Collections.emptySet();
    private MpaResponseDto mpa;
    @Builder.Default
    private Set<Long> likes = Collections.emptySet();
}