package com.example.pokerclub.event;
//здесь просто статус мероприятия описывается
//enum это перечисление фиксированного набора значений(то есть EventType может быть только одним из
//двух признаков
public enum EventStatus {
    OPEN_FOR_REGISTRATION,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

