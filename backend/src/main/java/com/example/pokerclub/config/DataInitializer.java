package com.example.pokerclub.config;

import com.example.pokerclub.event.Event;
import com.example.pokerclub.event.EventRepository;
import com.example.pokerclub.event.EventStatus;
import com.example.pokerclub.event.EventType;
import com.example.pokerclub.rating.PlayerRating;
import com.example.pokerclub.rating.PlayerRatingRepository;
import com.example.pokerclub.user.User;
import com.example.pokerclub.user.UserRepository;
import com.example.pokerclub.user.UserRole;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
//нужен для сартовых демонстрационных данных
//то есть после первого запуска приложения у нас уже есть: администратор, посетитель, несколько мероприятий, начальный рейтинг посетителя
@Configuration
public class DataInitializer {

    @Bean
    //CommandLineRunner код внутри него выполнятеся после запуска приложения
    CommandLineRunner seedData(
        UserRepository userRepository, //для создания пользователей
        PlayerRatingRepository playerRatingRepository, //для создания рейтинга пользователей
        EventRepository eventRepository, //для создания мероприятий
        PasswordEncoder passwordEncoder //для хэширования паролей
    ) {
        return args -> {
            User admin = ensureUser(
                userRepository,
                passwordEncoder,
                "admin@pokerclub.local",
                "Администратор",
                "Клуба",
                "admin12345",
                UserRole.ADMINISTRATOR
            );

            User visitor = ensureUser(
                userRepository,
                passwordEncoder,
                "visitor@pokerclub.local",
                "Семен",
                "Басманов",
                "visitor12345",
                UserRole.VISITOR
            );
            //создание рейтинга для пользователя
            ensureRating(playerRatingRepository, visitor);
            //если в таблице еще нет мероприятий, то создаем новые
            if (eventRepository.count() == 0) {
                createEvent(
                    eventRepository,
                    EventType.TOURNAMENT,
                    "Friday Poker Cup",
                    "Еженедельный турнир клуба с начислением рейтинговых очков.",
                    LocalDateTime.now().plusDays(2).withHour(19).withMinute(0),
                    24
                );
                createEvent(
                    eventRepository,
                    EventType.TRAINING,
                    "Тренировка по турнирной стратегии",
                    "Разбор позиций, стеков и решений на поздних стадиях турнира.",
                    LocalDateTime.now().plusDays(1).withHour(18).withMinute(30),
                    12
                );
            }
        };
    }

    private User ensureUser(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        String email,
        String firstName,
        String lastName,
        String password,
        UserRole role
    ) { //если пользователь с таким email уже есть то возвращаем его, если нет, то создаем нового
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            return userRepository.save(user);
        });
    }
    //проверка есть ли рейтинг, если его нет то создаем новый
    private void ensureRating(PlayerRatingRepository playerRatingRepository, User user) {
        playerRatingRepository.findByVisitorId(user.getId()).orElseGet(() -> {
            PlayerRating rating = new PlayerRating();
            rating.setVisitor(user);
            rating.setTotalPoints(0);
            return playerRatingRepository.save(rating);
        });
    }
    //создание мероприятия
    private void createEvent(
        EventRepository eventRepository,
        EventType type,
        String title,
        String description,
        LocalDateTime dateTime,
        int maxParticipants
    ) {
        Event event = new Event();
        event.setEventType(type);
        event.setTitle(title);
        event.setDescription(description);
        event.setDateTime(dateTime);
        event.setMaxParticipants(maxParticipants);
        event.setStatus(EventStatus.OPEN_FOR_REGISTRATION); //статовые мероприятия имеют статус доступен для записи
        eventRepository.save(event);
    }
}
