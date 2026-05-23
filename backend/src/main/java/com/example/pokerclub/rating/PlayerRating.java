package com.example.pokerclub.rating;

import com.example.pokerclub.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
//хранит текущий рейтинг пользователя
@Entity //является сущностью и хранится в бд
@Table(name = "player_ratings") //соотв таблице player_ratings в бд
public class PlayerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //unique = true - один visitor_id может встретиться в player_ratings только один раз
    //fetch = FetchType.LAZY - пользователь загружается только тогда когда понядобистся
    @OneToOne(fetch = FetchType.LAZY, optional = false) // один посетитель = один рейтинг
    @JoinColumn(name = "visitor_id", nullable = false, unique = true)
    private User visitor;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    //место в рэйтинге
    //может быть NULL потому что только что зареганный пользователь не имеет позиции в рейтинге
    @Column(name = "rank_position")
    private Integer rankPosition;

    //дата последнего обновления рейтинга(когда админ начислит очки или выполнит пересчет, это понадобится)
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist //перед первым сохраненнием рейтинга
    void onCreate() {
        if (totalPoints == null) {
            totalPoints = 0;
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
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

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

