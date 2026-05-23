package com.example.pokerclub.event;

import com.example.pokerclub.event.dto.EventRequest;
import com.example.pokerclub.event.dto.EventResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController //принимает запросы и возвращает JSON
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/api/events") //доступен без токена
    public Page<EventResponse> getSchedule(
            @RequestParam(required = false) EventType type, //позволяет фильтровать по типу
            @RequestParam(required = false) EventStatus status, //фильтр по статусу
            @PageableDefault(size = 10, sort = "dateTime", direction = Sort.Direction.ASC) Pageable pageable
            //если фронт не передал page, size,sort то бэк по умолчанию вернет 10 элементов и сортировку по dataTime ASC
            //отсротировать мероприятие по дате и времени от  ближайших к более поздним
    ) {
        return eventService.getSchedule(type, status, pageable);
    }

    @GetMapping("/api/events/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping("/api/admin/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventRequest request) {
        //@RequestBody - превращает http-запрос в Java-объект
        return eventService.createEvent(request);
    }

    @PutMapping("/api/admin/events/{id}") //Put для обновления ресурса
    public EventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request
    ) {
        return eventService.updateEvent(id, request);
    }

    @PatchMapping("/api/admin/events/{id}/cancel")
    public EventResponse cancelEvent(@PathVariable Long id) {
        return eventService.cancelEvent(id);
    }

    @DeleteMapping("/api/admin/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}