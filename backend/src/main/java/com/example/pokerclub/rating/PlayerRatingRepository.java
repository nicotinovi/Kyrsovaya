package com.example.pokerclub.rating;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
//репозиторий для работы с таблицей player_ratings
//репозиторий работает с сущностью у которой id типо long
public interface PlayerRatingRepository extends JpaRepository<PlayerRating, Long> {

    @EntityGraph(attributePaths = "visitor")//Загрузть рейтинг сразу вместе с visitor.
    Optional<PlayerRating> findByVisitorId(Long visitorId); //ищет рейтинг по id пользователя

    @EntityGraph(attributePaths = "visitor")
    //DESC - по убыванию
    //если у двух пользователей одинаковое коолво очков, то выше тот у кого ретинг обновлен раньше
    //Page - потому что постраничный вывод
    Page<PlayerRating> findAllByOrderByTotalPointsDescLastUpdatedAsc(Pageable pageable);
}

