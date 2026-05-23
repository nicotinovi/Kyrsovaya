package com.example.pokerclub.rating.dto;
//нужен когда админ начисляет очки
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RatingEntryRequest(
        @NotNull(message = "Укажите посетителя")
        Long visitorId,

        @NotNull(message = "Укажите очки")
        @PositiveOrZero(message = "Очки не могут быть отрицательными")
        Integer points
) {
}