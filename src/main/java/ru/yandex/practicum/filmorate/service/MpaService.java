package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.dto.response.MpaResponseDto;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Service
@Getter

public class MpaService {

    private final MpaDbStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaDbStorage mpaStorage) {

        this.mpaStorage = mpaStorage;
    }

    public List<MpaResponseDto> getAllMpa() {
        return mpaStorage.getRatings().stream().map(MpaMapper::convertToDto).toList();
    }

    public MpaResponseDto getMpaById(Long id) {
        return MpaMapper.convertToDto(mpaStorage.getMpaById(id));
    }

}