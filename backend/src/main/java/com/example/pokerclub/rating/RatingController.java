package com.example.pokerclub.rating;

import com.example.pokerclub.rating.dto.RatingEntryRequest;
import com.example.pokerclub.rating.dto.RatingEntryResponse;
import com.example.pokerclub.rating.dto.RatingResponse;
import com.example.pokerclub.user.User;
import com.example.pokerclub.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController // принимает запрос а возвращает JSON
@RequestMapping("/api")
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    public RatingController(RatingService ratingService, UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
    }

    @GetMapping("/ratings")
    //возвращает общий рейтинг
    public Page<RatingResponse> getRatings(
            //@PageableDefault задает значения
            //size = 20 - по 20 игроков на страницу
            //sort = "totalPoints" - сортировка по очкам
            //direction = DESC - по убыванию
            @PageableDefault(size = 20, sort = "totalPoints", direction = DESC) Pageable pageable
    ) {
        return ratingService.getRatings(pageable);
    }

    @GetMapping("/me/rating")
    //рейтинг текущего пользователя
    public RatingResponse getMyRating(Authentication authentication) { //тут получаем токен через Authentication authentication
        User visitor = userService.getCurrentUser(authentication); // берем пользователя из токена
        return ratingService.getMyRating(visitor); //передаа пользователя в сервичс
    }

    //нужен админу чтобы посмотреть результаты конкретного турнира
    @GetMapping("/admin/tournaments/{tournamentId}/rating-entries")
    public List<RatingEntryResponse> getTournamentEntries(@PathVariable Long tournamentId) {
        return ratingService.getTournamentEntries(tournamentId);
    }
    //начисление очков участникам турнира
    @PostMapping("/admin/tournaments/{tournamentId}/rating-entries")
    public List<RatingEntryResponse> assignTournamentPoints(
            @PathVariable Long tournamentId,
            //Берет JSON из тела запроса и превращает его в список RatingEntryRequest
            //@Valid включает проверку:
            //visitorId не null
            //points не null и не отрицательные
            @Valid @RequestBody List<@Valid RatingEntryRequest> requests,
            Authentication authentication // дает доступ к текущему админу
    ) {
        User admin = userService.getCurrentUser(authentication); //достаем админа из токена
        return ratingService.assignTournamentPoints(tournamentId, requests, admin); //передаем всё в сервис(там уже он сам решает начислять или нет очки)
    }

    //пересчет рейтинга
    @PostMapping("/admin/ratings/recalculate")
    public List<RatingResponse> recalculateRatings() {
        return ratingService.recalculateRanks();
    }
}