package com.example.pokerclub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
//для входных данных регистрации их использует Controller, проверяет на валидацию и если норм то использует дальше
public record RegisterRequest(
    @NotBlank(message = "Введите имя")
    @Size(max = 100, message = "Имя должно быть не длиннее 100 символов")
    String firstName,

    @NotBlank(message = "Введите фамилию")
    @Size(max = 100, message = "Фамилия должна быть не длиннее 100 символов")
    String lastName,

    @NotBlank(message = "Введите email")
    @Email(message = "Введите корректный email")
    String email,

    @NotBlank(message = "Введите пароль")
    @Size(min = 8, max = 100, message = "Пароль должен содержать от 8 до 100 символов")
    String password
) {
}

