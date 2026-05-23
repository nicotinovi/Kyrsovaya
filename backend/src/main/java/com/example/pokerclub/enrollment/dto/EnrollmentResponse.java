package com.example.pokerclub.enrollment.dto;

import com.example.pokerclub.enrollment.Enrollment;
import com.example.pokerclub.enrollment.EnrollmentStatus;
import com.example.pokerclub.event.dto.EventResponse;
import com.example.pokerclub.user.UserResponse;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        EnrollmentStatus status,
        LocalDateTime createdAt,
        UserResponse visitor,
        EventResponse event
) { //превращаем сущность в dto
    public static EnrollmentResponse from(Enrollment enrollment, long enrolledCount) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStatus(),
                enrollment.getCreatedAt(),
                UserResponse.from(enrollment.getVisitor()),
                EventResponse.from(enrollment.getEvent(), enrolledCount)
        );
    }
}