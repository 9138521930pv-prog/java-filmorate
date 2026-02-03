package ru.yandex.practicum.filmorate.storage.user;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emailSet = new HashSet<>();

    @PostConstruct
    public void initEmailSet() {
        users.values().forEach(user -> emailSet.add(user.getEmail()));
        log.info("Инициализирован emailSet с {} существующими email", emailSet.size());
    }

    @Override
    public User addUser(User user) {
        if (user == null) {
            throw new ValidationException("Запрос на добавление пользователя поступил с пустым телом");
        }

        if (emailSet.contains(user.getEmail())) {
            throw new ValidationException("Указанный E-mail: " + user.getEmail() + " уже используется");
        }

        boolean nameIsNull = user.getName() == null || user.getName().isBlank();
        if (nameIsNull) {
            user.setName(user.getLogin());
            log.info("Имя для отображения может быть пустым — в таком случае будет использован логин");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        emailSet.add(user.getEmail());
        log.info("Создан пользователь с ID: {}", user.getId());
        return user;
    }

    @Override
    public User removeUser(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new ValidationException("Попытка удаления фильма. Фильм с ID: " + id + " не найден");
        }
        emailSet.remove(user.getEmail());
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
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }

        String newEmail = updatedUser.getEmail();
        String oldEmail = user.getEmail();

        if (!newEmail.equals(oldEmail)) {
            if (emailSet.contains(newEmail)) {
                throw new ValidationException("Обновляемый E‑mail: " + newEmail + " уже используется");
            }

            emailSet.remove(oldEmail);
            emailSet.add(newEmail);
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
        emailSet.clear();
        log.info("Хранилище films очищено. Текущий размер: {}",  users.size());
    }

    @Override
    public List<User> getUsersByIds(Collection<Long> userIds) {
        return userIds.stream()
                .map(id -> users.get(id))
                .filter(Objects::nonNull)
                .toList();
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
