package com.example.pokerclub.enrollment;

import com.example.pokerclub.event.Event;
import com.example.pokerclub.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
//отвечает за факт того что конкретный пользователь записался на мероприятие
@Entity //сущность и хранится в бд
@Table(
    name = "enrollments", //сущность связана с таблицей enrollments
    uniqueConstraints = @UniqueConstraint(name = "uq_enrollment_visitor_event", columnNames = {"visitor_id", "event_id"}) //уникальные значения "visitor_id", "event_id" для предоствращения дублирования
        //то есть один польщователь не может записаться на одно и то же мероприятие дважды
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //многие записис принадлежат одному пользователю
    //fetch = FetchType.LAZY - не использовать пока он реально не вызовется
    //optional = false - у записи обязательно должен быть посетитель
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_id", nullable = false)
    private User visitor;
//тоже самое что и с посетителем
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnrollmentStatus status;

    //расчет времени когда пользователь записался
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    //перед первым созранением(автоматически следующие значения ставятся)
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EnrollmentStatus.REGISTERED;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getVisitor() {
        return visitor;
    }

    public void setVisitor(User visitor) {
        this.visitor = visitor;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

