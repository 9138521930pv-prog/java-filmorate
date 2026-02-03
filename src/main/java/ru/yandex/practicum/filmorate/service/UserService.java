package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Getter
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья: userId=" + userId);
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.addFriend(friendId);
        friend.addFriend(userId);

        log.info("Дружба между {} и {} установлена", userId, friendId);
        return user; // Возвращаем инициатора операции
    }

    public User removeFriend(Long userId, Long removedFriendsId) {
        if (userId.equals(removedFriendsId)) {
            throw new ValidationException("ID=" + userId + " пользователя и ID= "
                    + removedFriendsId + " друга для добавления совпадают");
        }
        User user = userStorage.getUserById(userId);
        User removedFriend = userStorage.getUserById(removedFriendsId);
        user.removeFriend(removedFriendsId);
        removedFriend.removeFriend(userId);
        log.info("Дружба между {} и {} удалена", userId, removedFriendsId);
        return removedFriend;
    }

    public List<User> getFriendsListOfUser(Long userId) {
        User user = userStorage.getUserById(userId);
        Set<Long> friendsIds = user.getFriendsId();
        List<User> friends = userStorage.getUsersByIds(friendsIds);
        log.info("Загружено {} друзей для пользователя {}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(Long firstUserId, Long secondUserId) {
        if (firstUserId.equals(secondUserId)) {
            throw new ValidationException("ID обоих пользователей совпадают");
        }
        User firstUser = userStorage.getUserById(firstUserId);
        User secondUser = userStorage.getUserById(secondUserId);
        Set<Long> mutualFriendsId = new HashSet<>(firstUser.getFriendsId());
        mutualFriendsId.retainAll(secondUser.getFriendsId());

        if (mutualFriendsId.isEmpty()) {
            log.info("У пользователей {} и {} нет общих друзей", firstUserId, secondUserId);
            return List.of();
        }

        List<User> commonFriends = userStorage.getUsersByIds(mutualFriendsId);

        log.info("Создан список из {} общих друзей пользователей с ID = {} и ID = {}",
                commonFriends.size(), firstUserId, secondUserId);

        return commonFriends;
    }


    public void clearAllUsers() {
        userStorage.clear();
    }

}
