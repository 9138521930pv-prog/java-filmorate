package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.response.MpaResponseDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaResponseDto>> getAllMpa() {
        List<MpaResponseDto> mpa = mpaService.getAllMpa();
        if (mpa.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(mpa);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaResponseDto> getMpaById(@PathVariable("id")
                                                     @NotNull(message = "id не может быть null")
                                                     @Min(value = 1, message = "id должен быть положительным целым числом")
                                                     @Valid Long id) {
        MpaResponseDto mpa = mpaService.getMpaById(id);
        if (mpa == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(mpa);
    }
}