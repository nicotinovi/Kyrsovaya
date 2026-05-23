package com.example.pokerclub.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "events") //Hubernate будет связывать объекты Event с таблицей бд
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //вид мероприятия
    @Enumerated(EnumType.STRING)//только строка
    @Column(name = "event_type", nullable = false, length = 30) //name="" пишется если именя и тут и в бд не совпадают
    private EventType eventType;
    //название мероприятия
    @Column(nullable = false, length = 200)
    private String title;
    //описание мероприятия
    @Column(columnDefinition = "TEXT") //TEXT используется потому что описание может быть длиннее обичноого VARCHAR(255)
    private String description;
    //дата и время проведения мероприятия
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime; //LocalDateTime - это тип с pgSQL - TIMESTAMP
    //максимальное количество участников
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;
    //статут мероприятия
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EventStatus status;
    //для создания записис в системе(дата создания в бд)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist //выполняется перед первым сохранением сущности в бд
    void onCreate() {
        //если время создания нет то ставим настоязее
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        } //если нет статуса то ставим OPEN_FOR_REGISTRATION
        if (status == null) {
            status = EventStatus.OPEN_FOR_REGISTRATION;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

