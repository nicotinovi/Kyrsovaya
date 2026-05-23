package com.example.pokerclub.enrollment.dto;

import jakarta.validation.constraints.NotNull;
//dto для ручного добавления участника админом
public record AdminEnrollmentRequest(
        @NotNull(message = "Укажите посетителя")
        Long visitorId
) {
}