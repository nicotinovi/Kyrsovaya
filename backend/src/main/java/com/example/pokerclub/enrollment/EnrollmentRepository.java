package com.example.pokerclub.enrollment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//слой доступа к данным для записи на мероприятие
//значит репозиторий работает с сущностью Enrollment у которой id типа long
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    //проверка повторной записи
    //существует ли запись этого посетителя на это мероприятие со статусом, не равным указанному
    boolean existsByVisitorIdAndEventIdAndStatusNot(Long visitorId, Long eventId, EnrollmentStatus status);

    //@EntityGraph - чтобы заранее загрузить связанные Event и User и избежать лишних запросов к БД.
    @EntityGraph(attributePaths = {"event", "visitor"})
    List<Enrollment> findByVisitorIdOrderByEventDateTimeAsc(Long visitorId); //получает все записи конкретного пользователя
    //OrderByEventDateTimeAsc - отсортировать записи по дате мероприятия от ближайших к поздним

    @EntityGraph(attributePaths = {"event", "visitor"})
    List<Enrollment> findByEventIdOrderByCreatedAtAsc(Long eventId); //записи по мероприятию

    //позволяет найти любую запись посетителя, даже отмененную
    @EntityGraph(attributePaths = {"event", "visitor"})
    Optional<Enrollment> findByVisitorIdAndEventId(Long visitorId, Long eventId);


    //найти запись по id и сразу загрузить event и visitor
    @EntityGraph(attributePaths = {"event", "visitor"})
    Optional<Enrollment> findWithEventById(Long id);

    @Query("""
        select count(e.id)
        from Enrollment e
        where e.event.id = :eventId and e.status <> 'CANCELLED'
    """)
    long countActiveByEventId(@Param("eventId") Long eventId);
}

