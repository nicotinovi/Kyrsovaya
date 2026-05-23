package com.example.pokerclub.config;

import com.example.pokerclub.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
//нужен чтобы после входа пользователь мог обращаться к защищенным endpoint ббез повторного пароля
@Service
public class JwtService {

    private final SecretKey secretKey; //секретный ключ, которым backend подписывает токен.
    private final long expirationMinutes; //время жизни токена в минутах(из application.yml у нас 720 минут)

    public JwtService(
        @Value("${app.jwt.secret}") String secret, //берет значение secret из application.yml
        @Value("${app.jwt.expiration-minutes}") long expirationMinutes // берет время жизни токена из application.yml
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // криптографический ключ которым подписываются токены
        this.expirationMinutes = expirationMinutes;
    }
    //создаем новый токен для пользователя входа или регистрации
    public String generateToken(User user) {
        Instant now = Instant.now(); //текущее время
        return Jwts.builder() //сборка JWT
            .subject(user.getEmail())
            .claim("role", user.getRole().name())
            .claim("userId", user.getId())
            .issuedAt(Date.from(now)) //дата выпуска токена
            .expiration(Date.from(now.plusSeconds(expirationMinutes * 60))) // дата окончания действия ктокен
            .signWith(secretKey) //подписывает токен секретным ключом
            .compact(); // собирает всё в строку
    }
    //достает email(getSubject()) - для того чтобы понять, кто делает запрос
    public String extractUsername(String token) {
        return claims(token).getSubject();
    }
    //Проверяет подходит ли токен пользователю и еще действителен? - это  после того как достали почту
    public boolean isValid(String token, User user) {
        Claims claims = claims(token);
        //user.getEmail().equals(claims.getSubject())
        //Проверяет, что email пользователя совпадает с subject токена.
        //claims.getExpiration().after(new Date())
        //Проверяет, что срок действия токена еще не закончился.
        return user.getEmail().equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

