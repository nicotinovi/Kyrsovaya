package com.example.pokerclub.rating;

import com.example.pokerclub.common.BadRequestException;
import com.example.pokerclub.enrollment.Enrollment;
import com.example.pokerclub.enrollment.EnrollmentRepository;
import com.example.pokerclub.enrollment.EnrollmentStatus;
import com.example.pokerclub.event.Event;
import com.example.pokerclub.event.EventService;
import com.example.pokerclub.event.EventType;
import com.example.pokerclub.rating.dto.RatingEntryRequest;
import com.example.pokerclub.rating.dto.RatingEntryResponse;
import com.example.pokerclub.rating.dto.RatingResponse;
import com.example.pokerclub.user.User;
import com.example.pokerclub.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RatingService {

    private final PlayerRatingRepository playerRatingRepository; //работает с общей таблицей рейтинга очков
    private final RatingEntryRepository ratingEntryRepository; // работает с отдельными начислениями очков за турниры
    private final EventService eventService; // нужен чтобы получить турнир по id
    private final UserService userService; //нужен чтобы получить пользователя по id
    private final EnrollmentRepository enrollmentRepository; //нужен чтобы проверить был ли пользователь записан на турнир и был ли он отмечен как присутствующий

    public RatingService(PlayerRatingRepository playerRatingRepository,
                         RatingEntryRepository ratingEntryRepository,
                         EventService eventService,
                         UserService userService,
                         EnrollmentRepository enrollmentRepository) {
        this.playerRatingRepository = playerRatingRepository;
        this.ratingEntryRepository = ratingEntryRepository;
        this.eventService = eventService;
        this.userService = userService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true) //значит изменять бд тут не надо, просто читать данные
    //Pageable pageable содержит параметры пагинации: номер страницы, размер страницы.
    public Page<RatingResponse> getRatings(Pageable pageable) {
        //читаем рейтинги
        //сначала игроки с большим колвом очков
        //если одинаковое колво, то выше тот кто раньше получил это колво
        return playerRatingRepository.findAllByOrderByTotalPointsDescLastUpdatedAsc(pageable)
                .map(RatingResponse::from); //превращает сущность PlayerRating в dto RatingRasponse
    }
    //рейтинг текущего пользователя
    @Transactional
    public RatingResponse getMyRating(User visitor) {
        PlayerRating rating = getOrCreateRating(visitor);//берет или создает новый рейтинг для  пользователя

        // если NULL то запусткаем пересчет рейтинга
        if (rating.getRankPosition() == null) {
            recalculateRanks();
            rating = playerRatingRepository.findByVisitorId(visitor.getId())
                    .orElseThrow();
        }

        return RatingResponse.from(rating);
    }


    @Transactional
    //метод начисления очков участникам турнира
    //List<RatingEntryRequest> requests - список начислений
    public List<RatingEntryResponse> assignTournamentPoints(Long tournamentId,
                                                            List<RatingEntryRequest> requests,
                                                            User admin) {
        //если админ отправит пустой список, то система выдаст ошибку(просто от бессмысленного запроса)
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Передайте результаты турнира");
        }

        Event tournament = eventService.findEvent(tournamentId); //ищем мероприятие по id

        //проверка что тип ТУРНИР а не ТРЕНИРОВКА, потому что только за турнир можно начислить очки
        if (tournament.getEventType() != EventType.TOURNAMENT) {
            throw new BadRequestException("Очки можно начислять только за турниры");
        }

        List<RatingEntryResponse> responses = new ArrayList<>(); //список ответов
        //в нем хранится результат по каждому начислению

        //админ передал несколько игроков, сервис обрабатывает их через цикл по очереди
        for (RatingEntryRequest request : requests) {
            User visitor = userService.getVisitorById(request.visitorId()); //находим постетителя

            Enrollment enrollment = enrollmentRepository.findByVisitorIdAndEventId(visitor.getId(), tournamentId)
                    .orElseThrow(() -> new BadRequestException("Посетитель не зарегистрирован на турнир"));
            //если не присутствовал игрок, то нельзя ему начислть очки
            if (enrollment.getStatus() != EnrollmentStatus.PRESENT) {
                throw new BadRequestException("Очки можно начислять только присутствующим участникам");
            }

            PlayerRating rating = getOrCreateRating(visitor); //рейтинг игрока получили

            //это надо для того чтобы если админ оибся и поставил 10 очков вместо 15, то при изменении он исправил результат и не прибавил еще 15
            RatingEntry entry = ratingEntryRepository
                    .findByVisitorIdAndTournamentId(visitor.getId(), tournamentId)//получаем запись очков этого игрока за конкретный турнир
                    .orElseGet(() -> { //если нету записи то тогда создаем новую
                        RatingEntry newEntry = new RatingEntry();
                        newEntry.setVisitor(visitor);
                        newEntry.setTournament(tournament);
                        newEntry.setRating(rating);
                        return newEntry;
                    });
            //расчет разницы при изменении
            int oldPoints = entry.getPoints() == null ? 0 : entry.getPoints(); //старые очки
            int newPoints = request.points(); //новые начисленные очки
            int delta = newPoints - oldPoints; // колво очков которы прибавтся

            //обновление записи начисления
            entry.setPoints(newPoints); // стоклко начсилено
            entry.setAssignedByAdmin(admin); // кто начислил
            entry.setAssignedAt(LocalDateTime.now()); //когда начислил

            //rating.getTotalPoints() + delta - новое общее колво очков
            //Math.max(0, rating.getTotalPoints() + delta) - не дает рейтингу стать отрицательным
            rating.setTotalPoints(Math.max(0, rating.getTotalPoints() + delta));
            rating.setLastUpdated(LocalDateTime.now()); //обновляется чтобы понять когда он менялся последний раз

            playerRatingRepository.save(rating); //соххраняем общий рейтинг
            RatingEntry savedEntry = ratingEntryRepository.save(entry); //потом сохраняем конкретную запись начисления

            responses.add(RatingEntryResponse.from(savedEntry));  //из сущности в dto и в список ответов
        }

        recalculateRanks(); //пересчитывание позиций игроков

        return responses; //возвращает список начислений(созданных и обновленных)
    }

    @Transactional
    public List<RatingResponse> recalculateRanks() {
        List<PlayerRating> ratings = playerRatingRepository.findAll(
                Sort.by(
                        Sort.Order.desc("totalPoints"), //сначала больше очков
                        Sort.Order.asc("lastUpdated") //потом если одинаковое колво очко то выше тот кто раньше получил эти очки
                )
        );
        //приссваиваем места
        for (int i = 0; i < ratings.size(); i++) {
            ratings.get(i).setRankPosition(i + 1);
        }
        //сохр все обновленные места и превращение и в dto для фронта
        return playerRatingRepository.saveAll(ratings)
                .stream()
                .map(RatingResponse::from)
                .toList();
    }

    //результат конкретного турнира
    @Transactional(readOnly = true)
    public List<RatingEntryResponse> getTournamentEntries(Long tournamentId) {
        Event tournament = eventService.findEvent(tournamentId);//нахождение меропр

        if (tournament.getEventType() != EventType.TOURNAMENT) {
            throw new BadRequestException("Результаты можно смотреть только для турниров");
        }
//все начисления по турниру сортируем по очкам по убыванию и возвращаем dto
        return ratingEntryRepository.findByTournamentIdOrderByPointsDesc(tournamentId)
                .stream()
                .map(RatingEntryResponse::from)
                .toList();
    }

    private PlayerRating getOrCreateRating(User visitor) {
        //если рейтинг уже есть - вернуть его
        //если нету рейтинга - создать запись с 0 очков
        return playerRatingRepository.findByVisitorId(visitor.getId())
                .orElseGet(() -> {
                    PlayerRating rating = new PlayerRating();
                    rating.setVisitor(visitor);
                    rating.setTotalPoints(0);
                    rating.setRankPosition(0);
                    rating.setLastUpdated(LocalDateTime.now());
                    return playerRatingRepository.save(rating);
                });
    }
}