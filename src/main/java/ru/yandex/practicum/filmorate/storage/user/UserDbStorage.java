package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Qualifier("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    @Override
    public Optional<List<User>> getAllUsers() {
        String query = """
                       SELECT *
                         FROM users
                       """;
        try {
            return Optional.of(jdbc.query(query, userRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String query = """
                       SELECT *
                         FROM USERS
                        WHERE ID = ?
                       """;
        try {
            return Optional.of(jdbc.queryForObject(query, userRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void validateUserExists(Long userId) {
        String query = """
                       SELECT 1
                         FROM USERS
                        WHERE ID = ?
                       """;
        try {
            jdbc.queryForObject(query, Integer.class, userId);
            log.info("Пользователь с ID: {} найден.", userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с ID: {} не найден. Выбрасываем исключение.", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }


    @Override
    public User addUser(User user) {
        String query = """
                       INSERT INTO users (name, email, login, birthday)
                                  VALUES (?,?,?,?)
                       """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (user == null) {
            throw new ValidationException("Запрос на добавление пользователя поступил с пустым телом");
        }

        if (user.getEmail() == null) {
            throw new ValidationException("Указанный E-mail: " + user.getEmail() + " уже используется");
        }

        String checkQuery = """
                            SELECT COUNT(*)
                              FROM users
                             WHERE email = ?
                            """;
        int count = jdbc.queryForObject(checkQuery, Integer.class, user.getEmail());

        if (count > 0) {
            throw new ValidationException("Указанный E-mail: " + user.getEmail() + " уже используется");
        }

        boolean nameIsNull = user.getName() == null || user.getName().isBlank();
        if (nameIsNull) {
            user.setName(user.getLogin());
            log.info("Имя для отображения может быть пустым — в таком случае будет использован логин");
        }

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        user.setId(generatedId);
        log.info("Создан пользователь с ID: {}", user.getId());
        return user;
    }

    @Override
    public User removeUser(Long id) {
        String checkQuery = """
                            SELECT COUNT(*)
                              FROM users
                             WHERE id = ?
                            """;
        int count = jdbc.queryForObject(checkQuery, Integer.class, id);

        if (count == 0) {
            throw new ValidationException("Попытка удаления пользователя. Пользователь с ID: " + id + " не найден");
        }

        String getEmailQuery = """
                               SELECT email
                                 FROM users
                                WHERE id = ?
                               """;
        String email = jdbc.queryForObject(getEmailQuery, String.class, id);

        String deleteQuery = """
                             DELETE FROM users
                                   WHERE id = ?
                             """;
        int rowsAffected = jdbc.update(deleteQuery, id);

        if (rowsAffected == 0) {
            throw new ValidationException("Пользователь с ID: " + id + " не был удалён — возможно, уже удалён другим процессом");
        }

        log.info("Пользователь с ID: {} и email: {} успешно удалён.", id, email);
        return null;
    }

    @Override
    public User updateUser(User user) {
        String checkUserQuery = """
                                SELECT COUNT(*)
                                  FROM users
                                 WHERE id = ?
                                """;
        int userExists = jdbc.queryForObject(checkUserQuery, Integer.class, user.getId());

        if (userExists == 0) {
            throw new ValidationException("Пользователь с ID: " + user.getId() + " не найден");
        }

        String checkEmailQuery = """
                                 SELECT COUNT(*)
                                   FROM users
                                  WHERE email = ?
                                    AND id != ?
                                 """;
        int emailExists = jdbc.queryForObject(checkEmailQuery, Integer.class, user.getEmail(), user.getId());

        if (emailExists > 0) {
            throw new ValidationException("Email '" + user.getEmail() + "' уже используется другим пользователем");
        }
        String updateQuery = """
                             UPDATE users
                                SET name = ?,
                                    email = ?,
                                    login = ?,
                                    birthday = ?
                              WHERE id = ?
                             """;

        int rowsAffected = jdbc.update(updateQuery,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId());

        if (rowsAffected == 0) {
            throw new ValidationException("Не удалось обновить пользователя с ID: " + user.getId());
        }
        log.info("Пользователь с ID: {} успешно обновлён", user.getId());
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья: userId=" + userId);
        }

        try {
            String query = """
                           INSERT INTO friends (user_id, friend_id, status)
                                        VALUES (?,?,?)
                           """;
            jdbc.update(query, userId, friendId, true);
        } catch (DataAccessException e) {
            throw new RuntimeException("Cannot Add To friends: " + e.getMessage(), e);
        }
        log.info("Дружба между {} и {} установлена", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long removedFriendsId) {
        if (userId.equals(removedFriendsId)) {
            throw new ValidationException("ID=" + userId + " пользователя и ID= "
                    + removedFriendsId + " друга для добавления совпадают");
        }
        String query = """
                       DELETE FROM friends
                             WHERE user_id = ?
                               and friend_id = ?
                       """;
        jdbc.update(query, userId, removedFriendsId);
        log.info("Дружба между {} и {} удалена", userId, removedFriendsId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String query = """
                       SELECT u.* FROM users u
                       JOIN friends f ON u.id = f.friend_id
                       WHERE f.user_id = ?
                       ORDER BY u.id
                       """;
        return jdbc.query(query, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        String query = """
                       SELECT u.id, u.email, u.login, u.name, u.birthday
                       FROM friends f1
                       INNER JOIN friends f2 ON f1.friend_id = f2.friend_id
                       INNER JOIN users u ON u.id = f1.friend_id
                       WHERE f1.user_id = ? AND f2.user_id = ?
                       AND f1.status = true AND f2.status = true
                       """;
        return jdbc.query(query, userRowMapper, userId, friendId);
    }
}