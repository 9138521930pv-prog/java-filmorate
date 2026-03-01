package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoContentException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Service
@Getter

public class MpaService {

    private final MpaDbStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaDbStorage mpaStorage) {

        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getRatings().orElseThrow(() -> new NoContentException("Список MPA пуст"));
    }

    public Mpa getMpaById(Long id) {
        return mpaStorage.getMpaById(id).orElseThrow(() -> new NotFoundException("MPA рейтинг с id= " + id + " не найден"));
    }

}