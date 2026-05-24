package com.example.pokerclub.event;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//отвечает за запросы к бд
//EventController → EventService → EventRepository → PostgreSQL
public interface EventRepository extends JpaRepository<Event, Long> { //репозиторий работает с сущностью event у которой id типа Long
    //поиск по типу и статусу
    //Pageable pageable потому что нам надо выводить не все мероприятия разом а по несколько мероприятий(10 например)
    Page<Event> findByEventTypeAndStatus(EventType eventType, EventStatus status, Pageable pageable);
    //поиск по типу
    Page<Event> findByEventType(EventType eventType, Pageable pageable);
    //поиск по статусу
    Page<Event> findByStatus(EventStatus status, Pageable pageable);
    //получить все мероприятия
    Page<Event> findAll(Pageable pageable);

    @Query("""
        select count(en.id)
        from Enrollment en
        where en.event.id = :eventId and en.status <> com.example.pokerclub.enrollment.EnrollmentStatus.CANCELLED
    """) //посчет активных записей
    long countActiveEnrollments(@Param("eventId") Long eventId);

    @Query("""
        select count(en.id) > 0
        from Enrollment en
        where en.visitor.id = :visitorId 
          and en.status <> com.example.pokerclub.enrollment.EnrollmentStatus.CANCELLED
          and en.event.id <> :eventId
          and en.event.dateTime between :fromTime and :toTime
    """) //есть ли у пользователя другая активная запись
    boolean existsTimeConflict(
        @Param("visitorId") Long visitorId,
        @Param("eventId") Long eventId,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime
    );
}
