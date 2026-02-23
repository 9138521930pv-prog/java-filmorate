package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.response.GenreResponseDto;
import ru.yandex.practicum.filmorate.model.Genre;

public class GenreMapper {
    public static GenreResponseDto convertToDto(Genre genre) {
        return GenreResponseDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public static Genre convertToEntity(GenreResponseDto genreDto) {
        return Genre.builder()
                .id(genreDto.getId())
                .name(genreDto.getName())
                .build();
    }
}