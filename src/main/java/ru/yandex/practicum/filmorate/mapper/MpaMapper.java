package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.response.MpaResponseDto;
import ru.yandex.practicum.filmorate.model.Mpa;

public class MpaMapper {
    public static MpaResponseDto convertToDto(Mpa mpa) {
        return MpaResponseDto.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }

    public static Mpa convertToEntity(MpaResponseDto mpaDto) {
        return Mpa.builder()
                .id(mpaDto.getId())
                .name(mpaDto.getName())
                .build();
    }
}
