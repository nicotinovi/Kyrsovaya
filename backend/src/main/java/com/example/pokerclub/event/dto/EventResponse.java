package com.example.pokerclub.event.dto;

import com.example.pokerclub.event.Event;
import com.example.pokerclub.event.EventStatus;
import com.example.pokerclub.event.EventType;

import java.time.LocalDateTime;
//описывает файл JSON, который бэк будет возвращать фронту при запросе расписания
//нельзя напрямую вернуть Event, потому что фронту нужны доп поля, которых нет в events
//например: enrolledCount и availablePlaces
public record EventResponse(
        Long id,
        EventType eventType,
        String title,
        String description,
        LocalDateTime dateTime,
        Integer maxParticipants,
        Long enrolledCount, //сколько участников уже записано
        Integer availablePlaces, // сколько свободных мест осталось
        EventStatus status,
        LocalDateTime createdAt //дата создания в системе
) {
    public static EventResponse from(Event event, long enrolledCount) {
        return new EventResponse(
                event.getId(),
                event.getEventType(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getMaxParticipants(),
                enrolledCount,
                Math.max(0, event.getMaxParticipants() - (int) enrolledCount),//для защиты от отрицательного числа(всегда вернет минимум 0, если получится максимум -2)
                event.getStatus(),
                event.getCreatedAt()
        );
    }
}
