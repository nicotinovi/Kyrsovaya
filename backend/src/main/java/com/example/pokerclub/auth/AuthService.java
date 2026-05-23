package com.example.pokerclub.auth;

import com.example.pokerclub.auth.dto.AuthResponse;
import com.example.pokerclub.auth.dto.LoginRequest;
import com.example.pokerclub.auth.dto.RegisterRequest;
import com.example.pokerclub.common.BadRequestException;
import com.example.pokerclub.config.JwtService;
import com.example.pokerclub.rating.PlayerRating;
import com.example.pokerclub.rating.PlayerRatingRepository;
import com.example.pokerclub.user.User;
import com.example.pokerclub.user.UserRepository;
import com.example.pokerclub.user.UserRole;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//тут описана логика авторизации и входа( а приемом запросов и возвратами ответов занимается контроллер

@Service //это реквисный компонент(бизнес-логика)
public class AuthService {

    private final UserRepository userRepository; //для проверки почты, сохраннения и поиска пользователя
    private final PlayerRatingRepository playerRatingRepository; //таблица player_ratings, всегда показывает рейтинг(даже для нового пльзователя)
    private final PasswordEncoder passwordEncoder; //для хэширования пароля
    private final AuthenticationManager authenticationManager; //для провеки логина  пароля при входе
    private final JwtService jwtService; //токен при успешной регистрации или входе
    //внедрение зависимостей
    public AuthService(
        UserRepository userRepository,
        PlayerRatingRepository playerRatingRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.playerRatingRepository = playerRatingRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional //все действия внутри метода выполняюются в однй транзакции
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email()); //нормализируем email
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Пользователь с таким email уже существует");
        }
        //есди пользователь новый то идем дальше

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.VISITOR);
        User saved = userRepository.save(user);

        //создание рейтинга
        PlayerRating rating = new PlayerRating();
        rating.setVisitor(saved);
        rating.setTotalPoints(0);
        rating.setRankPosition(null);
        playerRatingRepository.save(rating);

        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(
            //ищет пользователя по email, берет его пароль, сравнивает его с хэшем и если пароль верный то вход разрешен
            //иначе ошибка
            new UsernamePasswordAuthenticationToken(email, request.password())
        );
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new BadRequestException("Неверный email или пароль"));
        //также данные которые имеются уже у пользователя при входе в аккаунт
        //также данные которые имеются уже у пользователя при входе в аккаунт
        return toResponse(user);
    }

    //для возврата данных которые мы введем при регистрации
    private AuthResponse toResponse(User user) {
        return new AuthResponse(
            jwtService.generateToken(user),
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole()
        );
    }
    //переводит в нижний регистр и убирает пробелы в начале и в конце
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

