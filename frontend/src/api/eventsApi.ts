//API для мероприятий
import { apiRequest } from './httpClient';
import type { PageResponse } from '../types/api';
import type { EventRequest, EventResponse, EventStatus, EventType } from '../types/event';

type GetEventsParams = {
  type?: EventType | '';
  status?: EventStatus | '';
  page?: number;
  size?: number;
};

/**toQueryString нужен, чтобы из объекта:
{
  type: 'TOURNAMENT',
  page: 0,
  size: 10
}
сделать строку: ?type=TOURNAMENT&page=0&size=10
 */

function toQueryString(params: GetEventsParams) {
  const searchParams = new URLSearchParams();

  if (params.type) {
    searchParams.set('type', params.type);
  }

  if (params.status) {
    searchParams.set('status', params.status);
  }

  if (params.page !== undefined) {
    searchParams.set('page', String(params.page));
  }

  if (params.size !== undefined) {
    searchParams.set('size', String(params.size));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

//получить расписание с фильтрами
export function getEvents(params: GetEventsParams = {}) {
  return apiRequest<PageResponse<EventResponse>>(`/events${toQueryString(params)}`);
}

//получить одно мероприятие
export function getEventById(id: number) {
  return apiRequest<EventResponse>(`/events/${id}`);
}

//создать турнир/тренировку
export function createEvent(request: EventRequest, token: string) {
  return apiRequest<EventResponse>('/admin/events', {
    method: 'POST',
    body: request,
    token,
  });
}

//редактировать мероприятие
export function updateEvent(id: number, request: EventRequest, token: string) {
  return apiRequest<EventResponse>(`/admin/events/${id}`, {
    method: 'PUT',
    body: request,
    token,
  });
}

//отменя мероприятия
export function cancelEvent(id: number, token: string) {
  return apiRequest<EventResponse>(`/admin/events/${id}/cancel`, {
    method: 'PATCH',
    token,
  });
}

//удаление мероприятия
export function deleteEvent(id: number, token: string) {
  return apiRequest<void>(`/admin/events/${id}`, {
    method: 'DELETE',
    token,
  });
}