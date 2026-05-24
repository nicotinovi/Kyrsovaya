package com.example.pokerclub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
//для входных данных регистрации их использует Controller, проверяет на валидацию и если норм то использует дальше
public record RegisterRequest(
    @NotBlank(message = "Введите имя")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё -]+$",
            message = "Имя может содержать только буквы, пробел и дефис"
    )
    String firstName,

    @NotBlank(message = "Введите фамилию")
    @Size(min = 2, max = 100, message = "Фамилия должна содержать от 2 до 100 символов")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё -]+$",
            message = "Фамилия может содержать только буквы, пробел и дефис"
    )
    String lastName,

    @NotBlank(message = "Введите email")
    @Email(message = "Введите корректный email")
    @Pattern(
            regexp = "^(?!.*xn--)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email должен быть в обычном латинском формате"
    )
    String email,

    @NotBlank(message = "Введите пароль")
    @Size(min = 8, max = 100, message = "Пароль должен содержать от 8 до 100 символов")
    String password
) {
}

