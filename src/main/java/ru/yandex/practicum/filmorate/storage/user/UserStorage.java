package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User removeUser(Long id);

    User updateUser(User updatedUser);

    Optional<List<User>> getAllUsers();

    Optional<User> getUserById(Long userId);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    void validateUserExists(Long userId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long friendId);
}
