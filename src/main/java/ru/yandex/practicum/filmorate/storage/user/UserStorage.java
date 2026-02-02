package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User addUser(User user);

    User removeUser(Long id);

    User updateUser(User updatedUser);

    List<User> getAllUsers();

    User getUserById(Long userId);

    void clear();
}
