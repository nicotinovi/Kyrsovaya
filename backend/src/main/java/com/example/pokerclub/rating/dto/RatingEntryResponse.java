package com.example.pokerclub.rating.dto;
//возвращает инфу о начислении очков
import com.example.pokerclub.rating.RatingEntry;
import com.example.pokerclub.user.UserResponse;

import java.time.LocalDateTime;

public record RatingEntryResponse(
        Long id,
        UserResponse visitor,
        Long tournamentId,
        Integer points,
        UserResponse assignedByAdmin,
        LocalDateTime assignedAt
) {
    public static RatingEntryResponse from(RatingEntry entry) {
        return new RatingEntryResponse(
                entry.getId(),
                UserResponse.from(entry.getVisitor()),//сущность пользователя которму начислили очки
                entry.getTournament().getId(), //турнир за который начислили очки
                entry.getPoints(),
                //это если админа удалят из бд то должно остатться начисление очков, но assigned_by_admin_id будет NULL
                entry.getAssignedByAdmin() == null ? null : UserResponse.from(entry.getAssignedByAdmin()),
                entry.getAssignedAt()
        );
    }
}
