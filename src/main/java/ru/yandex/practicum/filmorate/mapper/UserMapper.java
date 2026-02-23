package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.request.UserRequestDto;
import ru.yandex.practicum.filmorate.dto.response.UserResponseDto;
import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {
    public static UserResponseDto convertToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .email(user.getEmail())
                .birthday(user.getBirthday())
                .build();
    }

    public static User convertToEntity(UserRequestDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .login(dto.getLogin())
                .email(dto.getEmail())
                .birthday(dto.getBirthday())
                .build();
    }
}