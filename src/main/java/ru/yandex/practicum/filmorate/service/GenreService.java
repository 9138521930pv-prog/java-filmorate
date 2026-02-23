package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.GenreResponseDto;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Set;

@Service
@Getter
public class GenreService {

    private final GenreDbStorage genreStorage;

    public GenreService(@Qualifier("genreDbStorage") GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<GenreResponseDto> getGenre() {
        return genreStorage.getGenre().stream().map(GenreMapper::convertToDto).toList();
    }

    public GenreResponseDto getGenreById(Long id) {
        Genre genre = genreStorage.getGenreById(id);
        return GenreMapper.convertToDto(genreStorage.getGenreById(id));
    }

    public void setGenreToFilm(Long genreId, Long filmId) {
        checkGenreExists(genreId);
        genreStorage.setGenreToFilm(genreId, filmId);
    }

    public Set<Genre> getGenresByFilmId(Long filmId) {
        return genreStorage.getGenresByFilmId(filmId); // Исправлено имя поля
    }

    public boolean checkGenreExists(Long id) {
        return genreStorage.getGenreById(id) != null; // Исправлено имя поля и логика
    }
}