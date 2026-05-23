package com.example.pokerclub.rating.dto;
//нужен для отображения рейтинга фронтом
import com.example.pokerclub.rating.PlayerRating;
import com.example.pokerclub.user.UserResponse;

import java.time.LocalDateTime;

public record RatingResponse(
        Long id,
        UserResponse visitor,
        Integer totalPoints,
        Integer rankPosition,
        LocalDateTime lastUpdated
) {
    //from(PlayerRating rating) - превращает Entity в DTO
    public static RatingResponse from(PlayerRating rating) {
        return new RatingResponse(
                rating.getId(),
                UserResponse.from(rating.getVisitor()),
                rating.getTotalPoints(),
                rating.getRankPosition(),
                rating.getLastUpdated()
        );
    }
}