package com.example.pokerclub.rating;

import com.example.pokerclub.event.Event;
import com.example.pokerclub.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
//в отличии от PlayerRating, который хранит только место и колво очков пользователя
//этот класс хранит историю начисления очков: за какой турнир, кому начислились очки, сколько очков, кто начислил, когда начислил
@Entity
@Table(
    name = "rating_entries",
    //одному посетителю можно начислить очки за один турнир только один раз
    uniqueConstraints = @UniqueConstraint(name = "uq_rating_entry_visitor_tournament", columnNames = {"visitor_id", "tournament_id"})
)
public class RatingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) //один рейтинг может иметь много начислений
    @JoinColumn(name = "rating_id", nullable = false)
    private PlayerRating rating;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_id", nullable = false)
    private User visitor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Event tournament;

    @Column(nullable = false)
    private Integer points;

    //кто начислил очки
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_admin_id")
    private User assignedByAdmin;

    //когда начислили очки
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist//пере первым сохранением ставит дату начисления очков
    void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlayerRating getRating() {
        return rating;
    }

    public void setRating(PlayerRating rating) {
        this.rating = rating;
    }

    public User getVisitor() {
        return visitor;
    }

    public void setVisitor(User visitor) {
        this.visitor = visitor;
    }

    public Event getTournament() {
        return tournament;
    }

    public void setTournament(Event tournament) {
        this.tournament = tournament;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public User getAssignedByAdmin() {
        return assignedByAdmin;
    }

    public void setAssignedByAdmin(User assignedByAdmin) {
        this.assignedByAdmin = assignedByAdmin;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}

