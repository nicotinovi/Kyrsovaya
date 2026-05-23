package com.example.pokerclub.event.dto;
//dto входного запроса для мероприятия, нужен когда админ создает или редактирует мероприятие
//фронт отправляет бэку
import com.example.pokerclub.event.EventType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
//record потмоу что это dto, record сам создает поля, конструктор и тд и тп
public record EventRequest(
        @NotNull(message = "Выберите тип мероприятия")//не NotBlank - потому что это enum а не строка, поэтому надо чтобы проверялся на NULL
        EventType eventType,

        @NotBlank(message = "Введите название")
        @Size(max = 200, message = "Название должно быть не длиннее 200 символов")
        String title,

        @Size(max = 2000, message = "Описание должно быть не длиннее 2000 символов")
        String description,

        @NotNull(message = "Укажите дату и время")
        @Future(message = "Дата проведения должна быть позже текущей даты")//дата должна быть в будущем
        LocalDateTime dateTime,

        @NotNull(message = "Укажите количество участников")
        @Positive(message = "Количество участников должно быть больше нуля")//проверяет неотрицательность числа
        Integer maxParticipants
        //нет поля Status потому что при создании автоматически ставится OPEN_FOR_REGISTRATION
        //это в Event.java прописал
) {
}
