package com.example.pokerclub.rating;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingEntryRepository extends JpaRepository<RatingEntry, Long> {
    //ищет, есть ли уже начисление очков этому игроку за этот турнир
    Optional<RatingEntry> findByVisitorIdAndTournamentId(Long visitorId, Long tournamentId);

    @EntityGraph(attributePaths = {"visitor", "tournament", "assignedByAdmin"}) // загружает эти сущности сразу(уменьшение колва SQL запросов
    List<RatingEntry> findByTournamentIdOrderByPointsDesc(Long tournamentId); // все начисления по конкретному турниру
}

