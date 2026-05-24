//API для записей
import { apiRequest } from './httpClient';
import type { EnrollmentResponse } from '../types/enrollment'; //typeScript тета бэка

//посетитель записывается на мероприятие
export function enrollToEvent(eventId: number, token: string) {
  return apiRequest<EnrollmentResponse>(`/events/${eventId}/enrollments`, {
    method: 'POST',
    token,
  });
}

//записи текущего пользователя
export function getMyEnrollments(token: string) {
  return apiRequest<EnrollmentResponse[]>('/me/enrollments', {
    token,
  });
}

//посетитель отменяет свою запись
export function cancelEnrollment(enrollmentId: number, token: string) {
  return apiRequest<EnrollmentResponse>(`/enrollments/${enrollmentId}/cancel`, {
    method: 'PATCH',
    token,
  });
}

//админ смотри участников
export function getEnrollmentsByEvent(eventId: number, token: string) {
  return apiRequest<EnrollmentResponse[]>(`/admin/events/${eventId}/enrollments`, {
    token,
  });
}

//админ добавляет участников вручную
export function addVisitorManually(eventId: number, visitorId: number, token: string) {
  return apiRequest<EnrollmentResponse>(`/admin/events/${eventId}/enrollments`, {
    method: 'POST',
    token,
    body: { visitorId },
  });
}

//админ подтверждает присутствие
export function confirmPresence(enrollmentId: number, token: string) {
  return apiRequest<EnrollmentResponse>(`/admin/enrollments/${enrollmentId}/presence`, {
    method: 'PATCH',
    token,
  });
}