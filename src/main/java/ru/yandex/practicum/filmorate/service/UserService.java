package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.request.UserRequestDto;
import ru.yandex.practicum.filmorate.dto.response.UserResponseDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;


@Service
@Getter
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<UserResponseDto> getUserAll() {
        return userStorage.getAllUsers().stream().map(UserMapper::convertToDto).toList();
    }

    public UserResponseDto getUserById(Long userId) {
        return UserMapper.convertToDto(userStorage.getUserById(userId));
    }

    public UserResponseDto addUsers(UserRequestDto user) {
        User users = UserMapper.convertToEntity(user);
        return UserMapper.convertToDto(userStorage.addUser(users));
    }

    public UserResponseDto updateUsers(UserRequestDto user) {
        userIdIsValid(user.getId());
        return UserMapper.convertToDto(userStorage.updateUser(UserMapper.convertToEntity(user)));
    }

    public void addFriends(Long userId, Long friendId) {
        userIdIsValid(userId);
        userIdIsValid(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriends(Long userId, Long friendId) {
        userIdIsValid(userId);
        userIdIsValid(friendId);
        userStorage.removeFriend(userId, friendId);
    }
    public List<UserResponseDto> getFriendsList(Long userId) {
        userIdIsValid(userId);
        Collection<User> friends = userStorage.getFriends(userId);

        return friends.stream()
                .map(UserMapper::convertToDto)
                .toList();
    }

    public List<UserResponseDto> getCommonFriends(Long userId, Long friendId) {
        List<User> friends = userStorage.getCommonFriends(userId, friendId);
        if (friends.isEmpty()) {
            throw new NotFoundException("Пользователя с ID: " + userId + " или: " + friendId);
        }
        return friends.stream().map(UserMapper::convertToDto).toList();
    }

    public void userIdIsValid(Long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID: " + id + " не найден в базе данных");
        }
    }
}
