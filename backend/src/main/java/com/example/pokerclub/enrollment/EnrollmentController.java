package com.example.pokerclub.enrollment;

import com.example.pokerclub.enrollment.dto.AdminEnrollmentRequest;
import com.example.pokerclub.enrollment.dto.EnrollmentResponse;
import com.example.pokerclub.user.User;
import com.example.pokerclub.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController //принимает http-запросы и возвращает JSON
public class EnrollmentController {

    private final EnrollmentService enrollmentService; //для выполнения логии
    private final UserService userService; //для получения текущего пользщователя

    public EnrollmentController(EnrollmentService enrollmentService, UserService userService) {
        this.enrollmentService = enrollmentService;
        this.userService = userService;
    }

    //запись на мероприятие
    @PostMapping("/api/events/{eventId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enrollToEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        User visitor = userService.getCurrentUser(authentication); //берем пользователя ищ токена
        return enrollmentService.enrollVisitor(eventId, visitor); //передаем в сервис id мероприятия и текущего посетителя
    }

    //возвращает записис текущего пользователя
    @GetMapping("/api/me/enrollments")
    public List<EnrollmentResponse> getMyEnrollments(Authentication authentication) {
        User visitor = userService.getCurrentUser(authentication);
        return enrollmentService.getMyEnrollments(visitor);
    }
// отмена записи
    @PatchMapping("/api/enrollments/{id}/cancel")
    public EnrollmentResponse cancelEnrollment(
            @PathVariable Long id, //id записи
            Authentication authentication
    ) {
        User visitor = userService.getCurrentUser(authentication);
        return enrollmentService.cancelEnrollment(id, visitor);
    }
    //список участников мероприятия
    @GetMapping("/api/admin/events/{eventId}/enrollments")
    public List<EnrollmentResponse> getEnrollmentsByEvent(@PathVariable Long eventId) {
        return enrollmentService.getEnrollmentsByEvent(eventId);
    }
    //ручное добавление участников
    @PostMapping("/api/admin/events/{eventId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse addVisitorManually(
            @PathVariable Long eventId,
            @Valid @RequestBody AdminEnrollmentRequest request
    ) {
        return enrollmentService.addVisitorManually(eventId, request.visitorId());
    }
// подтверждение присутствия
    @PatchMapping("/api/admin/enrollments/{id}/presence")
    public EnrollmentResponse confirmPresence(@PathVariable Long id) {
        return enrollmentService.confirmPresence(id);
    }
}