package com.example.pokerclub.enrollment;

import com.example.pokerclub.common.BadRequestException;
import com.example.pokerclub.common.NotFoundException;
import com.example.pokerclub.enrollment.dto.EnrollmentResponse;
import com.example.pokerclub.event.Event;
import com.example.pokerclub.event.EventService;
import com.example.pokerclub.event.EventStatus;
import com.example.pokerclub.user.User;
import com.example.pokerclub.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private static final long TIME_CONFLICT_WINDOW_HOURS = 3; //конфликт мероприятия в окне +-3 часа

    private final EnrollmentRepository enrollmentRepository;
    private final EventService eventService;
    private final UserService userService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            EventService eventService,
            UserService userService
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Transactional //запись посетителя на мероприятие
    public EnrollmentResponse enrollVisitor(Long eventId, User visitor) {
        Event event = eventService.findEvent(eventId);
        //метод проверки описан ниже
        validateEnrollmentAvailable(event, visitor);
// реактивация записи Cancelled - если ее не было то создадим новую, а если была то переделаем в Registered
        Enrollment enrollment = enrollmentRepository.findByVisitorIdAndEventId(visitor.getId(), eventId)
                .orElseGet(Enrollment::new);

        enrollment.setVisitor(visitor);
        enrollment.setEvent(event);
        enrollment.setStatus(EnrollmentStatus.REGISTERED);

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Transactional //добавление участника вручную(для админа)
    public EnrollmentResponse addVisitorManually(Long eventId, Long visitorId) {
        User visitor = userService.getVisitorById(visitorId); //нужен именно посетитель а не админ
        return enrollVisitor(eventId, visitor);
    }
    // возвращает записи текущего пользователя
    public List<EnrollmentResponse> getMyEnrollments(User visitor) {
        return enrollmentRepository.findByVisitorIdOrderByEventDateTimeAsc(visitor.getId()) //ищет все записи пользователя и сортирует их по дате
                .stream() //список в потток
                .map(this::toResponse)//превращает каждую сущность в dto чтобы фронт получил данные в безоп формате
                .toList();
    }
    //показывает участников конкретного мероприятия(нужен админу)
    public List<EnrollmentResponse> getEnrollmentsByEvent(Long eventId) {
        eventService.findEvent(eventId); //проверка что мероприятие существует

        return enrollmentRepository.findByEventIdOrderByCreatedAtAsc(eventId) //ищет все записи на мероприятие и сортирует их: кто раньше записался тот выше
                .stream() //список в потток
                .map(this::toResponse) //превращает каждую сущность в dto чтобы фронт получил данные в безоп формате
                .toList();
    }

    @Transactional
    // позволяет посетителю отменить свою запись
    public EnrollmentResponse cancelEnrollment(Long enrollmentId, User visitor) {
        Enrollment enrollment = findEnrollment(enrollmentId); //ищем запись

        if (!enrollment.getVisitor().getId().equals(visitor.getId())) {
            throw new BadRequestException("Нельзя отменить чужую запись");
        }

        if (enrollment.getStatus() != EnrollmentStatus.REGISTERED) {
            throw new BadRequestException("Можно отменить только активную запись");
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED); //не удаляет а именно меняет статус чтобы созранить в истории

        return toResponse(enrollmentRepository.save(enrollment)); //сохранение обновленной записи
    }

    @Transactional
    //используется админом
    //для подтверждения присутствия посетителя
    public EnrollmentResponse confirmPresence(Long enrollmentId) {
        Enrollment enrollment = findEnrollment(enrollmentId); //ищем запись

        if (enrollment.getStatus() != EnrollmentStatus.REGISTERED) {
            throw new BadRequestException("Присутствие можно подтвердить только для активной записи");
        }

        enrollment.setStatus(EnrollmentStatus.PRESENT); //меняем статус

        return toResponse(enrollmentRepository.save(enrollment)); //сохраняем изменение
    }

    private void validateEnrollmentAvailable(Event event, User visitor) {
        //мероприятие закрыто для записи
        if (event.getStatus() != EventStatus.OPEN_FOR_REGISTRATION) {
            throw new BadRequestException("Мероприятие недоступно для записи");
        }
        //проверка есть ли у пользоваеля уже актиная запись на это мероприятие
        boolean hasActiveEnrollment = enrollmentRepository.existsByVisitorIdAndEventIdAndStatusNot(
                visitor.getId(),
                event.getId(),
                EnrollmentStatus.CANCELLED
        );
        //чтобы не записаться еще раз
        if (hasActiveEnrollment) {
            throw new BadRequestException("Пользователь уже записан на мероприятие");
        }

        long activeCount = enrollmentRepository.countActiveByEventId(event.getId());

        if (activeCount >= event.getMaxParticipants()) {
            throw new BadRequestException("На мероприятие нет свободных мест");
        }
        //проверка конфликта времени
        boolean hasTimeConflict = eventService.hasTimeConflict(
                visitor.getId(),
                event.getId(),
                event.getDateTime().minusHours(TIME_CONFLICT_WINDOW_HOURS),
                event.getDateTime().plusHours(TIME_CONFLICT_WINDOW_HOURS)
        );

        if (hasTimeConflict) {
            throw new BadRequestException("Вы уже записаны на мероприятие в это время");
        }
    }

    private Enrollment findEnrollment(Long id) {
        return enrollmentRepository.findWithEventById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        long enrolledCount = enrollmentRepository.countActiveByEventId(enrollment.getEvent().getId()); //кол-во активных участников мероприятия
        return EnrollmentResponse.from(enrollment, enrolledCount);  //создает dto ответа
    }
}