package com.example.pokerclub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
//используется DTO потому что нам не нужны все данные user а надо именно почту и пароль
public record LoginRequest(
    @NotBlank(message = "Введите email")
    @Email(message = "Введите корректный email")
    String email,

    @NotBlank(message = "Введите пароль")
    String password
) {
}

