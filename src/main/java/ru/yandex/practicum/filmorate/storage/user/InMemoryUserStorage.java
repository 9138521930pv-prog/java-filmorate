package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        if (user == null) {
            throw new ValidationException("Запрос на добавление пользователя поступил с пустым телом");
        }

        for (User u : users.values()) {
            if (user.getEmail().equals(u.getEmail())) {
                throw new ValidationException("Указанный E-mail: " + user.getEmail() + " уже используется");
            }
        }

        boolean nameIsNull = user.getName() == null || user.getName().isBlank();
        if (nameIsNull) {
            user.setName(user.getLogin());
            log.info("Имя для отображения может быть пустым — в таком случае будет использован логин");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь с ID: {}", user.getId());
        return user;
    }

    @Override
    public User removeUser(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new ValidationException("Попытка удаления фильма. Фильм с ID: " + id + " не найден");
        }

        log.info("Пользователь с ID: {} успешно удален.", id);
        return users.remove(id);
    }

    @Override
    public User updateUser(User updatedUser) {
        if (updatedUser == null) {
            throw new ValidationException("Запрос на обновление данных пользователя поступил с пустым телом");
        }

        Long userId = updatedUser.getId();
        if (userId == null) {
            throw new ValidationException("ID пользователя должен быть указан");
        }

        User user = users.get(userId);
        if (user == null) {
            throw new ValidationException("Пользователь с ID: " + userId + " не найден");
        }

        for (User u : users.values()) {
            if (updatedUser.getEmail().equals(u.getEmail())) {
                throw new ValidationException("Обновляемый E-mail: " + updatedUser.getEmail()
                        + " уже используется");
            }
        }
            user.setEmail(updatedUser.getEmail());
            user.setLogin(updatedUser.getLogin());
            user.setName(updatedUser.getName());
            user.setBirthday(updatedUser.getBirthday());
            return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long userId) {
        log.info("Пользователь с ID: {} найден и успешно предоставлен в ответ на запрос.", userId);

        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        log.info("Пользователь с ID: {} найден и успешно предоставлен в ответ на запрос.", userId);
        return user;
    }

    @Override
    public void clear() {
        users.clear();
        log.info("Хранилище films очищено. Текущий размер: {}",  users.size());
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
