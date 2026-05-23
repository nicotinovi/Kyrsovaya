package com.example.pokerclub.auth.dto;

import com.example.pokerclub.user.UserRole;
//JSON-ответ который backend возвращает после успешной регистрации или входа
public record AuthResponse(
    String token,
    Long userId,
    String firstName,
    String lastName,
    String email,
    UserRole role
) {
}

