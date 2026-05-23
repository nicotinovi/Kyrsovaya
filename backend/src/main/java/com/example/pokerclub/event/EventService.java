package com.example.pokerclub.event;

import com.example.pokerclub.common.BadRequestException;
import com.example.pokerclub.common.NotFoundException;
import com.example.pokerclub.event.dto.EventRequest;
import com.example.pokerclub.event.dto.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Service
public class EventService {

    private final EventRepository eventRepository; //репозиторий для доступа к таблице events

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
    //вохвращает расписание
    public Page<EventResponse> getSchedule(EventType type, EventStatus status, Pageable pageable) {
        Page<Event> events;
        //если передали тип и статус то фильтруем по двум полям
        if (type != null && status != null) {
            events = eventRepository.findByEventTypeAndStatus(type, status, pageable);
        } else if (type != null) { //если только тип то по типу фильтруем
            events = eventRepository.findByEventType(type, pageable);
        } else if (status != null) { //если только статус то по статусу
            events = eventRepository.findByStatus(status, pageable);
        } else { //если нет фильтров то возвращаем всё
            events = eventRepository.findAll(pageable);
        }
        //возвращщает каждое событие Event в EventResponse
        return events.map(this::toResponse);
    }

    public EventResponse getEventById(Long id) {
        Event event = findEvent(id);
        return toResponse(event);
    }

    @Transactional //все действия в одной транзакции
    public EventResponse createEvent(EventRequest request) {
        Event event = new Event(); //создаем сущность
        applyRequest(event, request); //перенос данных из dto
        event.setStatus(EventStatus.OPEN_FOR_REGISTRATION); //автоматически ставится статус

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) { //обновление мероприятия
        Event event = findEvent(id); //ищем меропр

        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new BadRequestException("Нельзя редактировать завершенное или отмененное мероприятие");
        }

        applyRequest(event, request);

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    @Transactional
    public EventResponse cancelEvent(Long id) { //отменяет мероприятие
        Event event = findEvent(id);

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BadRequestException("Нельзя отменить завершенное мероприятие");
        }

        event.setStatus(EventStatus.CANCELLED);
        Event saved = eventRepository.save(event);

        return toResponse(saved);
    }

    @Transactional
    public void deleteEvent(Long id) { //удаляеи меропр
        Event event = findEvent(id);
        long activeEnrollments = eventRepository.countActiveEnrollments(id);

        if (activeEnrollments > 0) {
            throw new BadRequestException("Нельзя удалить мероприятие с активными записями");
        }

        eventRepository.delete(event);
    }

    public Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));
    }

    public boolean hasTimeConflict(Long visitorId, Long eventId, LocalDateTime fromTime, LocalDateTime toTime) {
        return eventRepository.existsTimeConflict(visitorId, eventId, fromTime, toTime);
    }

    private EventResponse toResponse(Event event) { //считает кол-во участнико и создает dto
        long enrolledCount = eventRepository.countActiveEnrollments(event.getId());
        return EventResponse.from(event, enrolledCount);
    }
    //переносит данные из dto в сущность
    private void applyRequest(Event event, EventRequest request) {
        event.setEventType(request.eventType());
        event.setTitle(request.title().trim());
        event.setDescription(request.description() == null ? null : request.description().trim());
        event.setDateTime(request.dateTime());
        event.setMaxParticipants(request.maxParticipants());
    }
}