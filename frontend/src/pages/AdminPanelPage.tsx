//админ-страница
import { useCallback, useEffect, useState } from 'react';
import { ApiRequestError } from '../api/httpClient';
import * as enrollmentsApi from '../api/enrollmentsApi';
import type { EnrollmentResponse } from '../types/enrollment';
import * as eventsApi from '../api/eventsApi';
import { EventForm } from '../components/EventForm';
import { useAuth } from '../context/AuthContext';
import type { EventRequest, EventResponse } from '../types/event';
import * as usersApi from '../api/usersApi';
import type { UserResponse } from '../types/auth';
import * as ratingsApi from '../api/ratingsApi';
import * as reportsApi from '../api/reportsApi';
import type { RatingEntryRequest } from '../types/rating';

const eventTypeLabels = {
  TOURNAMENT: 'Турнир',
  TRAINING: 'Тренировка',
};

const statusLabels = {
  OPEN_FOR_REGISTRATION: 'Открыта запись',
  IN_PROGRESS: 'Идет',
  COMPLETED: 'Завершено',
  CANCELLED: 'Отменено',
};

//статус записей
const enrollmentStatusLabels = {
  REGISTERED: 'Записан',
  PRESENT: 'Присутствует',
  CANCELLED: 'Отменено',
};

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('ru-RU', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function AdminPanelPage() {
  const { token, isAdmin } = useAuth();

  const [events, setEvents] = useState<EventResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [actionEventId, setActionEventId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [selectedEvent, setSelectedEvent] = useState<EventResponse | null>(null); //мероприятие для которого смотрим участников
  const [eventEnrollments, setEventEnrollments] = useState<EnrollmentResponse[]>([]); //спискок участников этого мероприятия
  const [isLoadingEnrollments, setIsLoadingEnrollments] = useState(false); //идет ли загрузка участников
  const [visitors, setVisitors] = useState<UserResponse[]>([]);
  const [selectedVisitorId, setSelectedVisitorId] = useState(''); //id посетителя для ручного добавления
  const [manualVisitorError, setManualVisitorError] = useState('');
  //состояние для очков
  const [pointsByEnrollmentId, setPointsByEnrollmentId] = useState<Record<number, string>>({});
  const [isAssigningPoints, setIsAssigningPoints] = useState(false);
  const [isDownloadingReport, setIsDownloadingReport] = useState(false);
  const [editingEvent, setEditingEvent] = useState<EventResponse | null>(null); //состояние редактирования

  const loadEvents = useCallback(async function loadEvents() {
    setIsLoading(true);
    setError('');

    try {
      const page = await eventsApi.getEvents({
        page: 0,
        size: 50,
      });
      setEvents(page.content);
    } catch {
      setError('Не удалось загрузить мероприятия');
    } finally {
      setIsLoading(false);
    }
  }, []);
  //функция загрузки пользователей
  const loadVisitors = useCallback(async function loadVisitors() {
    if (!token) {
      return;
    }

    try {
      const data = await usersApi.getVisitors(token);
      setVisitors(data);
    } catch {
      setVisitors([]);
    }
  }, [token]);

  useEffect(() => {
    if (isAdmin) {
      loadEvents();
      loadVisitors();
    }
  }, [isAdmin, loadEvents, loadVisitors]);

  async function handleCreateEvent(request: EventRequest) {
    if (!token) {
      return;
    }

    setError('');
    setSuccessMessage('');
    setValidationErrors({});
    setIsCreating(true);

    try {
      await eventsApi.createEvent(request, token);
      setSuccessMessage('Мероприятие создано');
      await loadEvents();
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
        setValidationErrors(err.validationErrors);
      } else {
        setError('Не удалось создать мероприятие');
      }
    } finally {
      setIsCreating(false);
    }
  }

  async function handleSaveEvent(request: EventRequest) {
  if (!token) {
    return;
  }

  if (!editingEvent) {
    await handleCreateEvent(request);
    return;
  }

  setError('');
  setSuccessMessage('');
  setValidationErrors({});
  setIsCreating(true);

  try {
    await eventsApi.updateEvent(editingEvent.id, request, token);
    setSuccessMessage('Мероприятие обновлено');
    setEditingEvent(null);
    await loadEvents();
  } catch (err) {
    if (err instanceof ApiRequestError) {
      setError(err.message);
      setValidationErrors(err.validationErrors);
    } else {
      setError('Не удалось обновить мероприятие');
    }
  } finally {
    setIsCreating(false);
  }
}

  async function handleCancelEvent(eventId: number) {
    if (!token) {
      return;
    }

    setError('');
    setSuccessMessage('');
    setActionEventId(eventId);

    try {
      await eventsApi.cancelEvent(eventId, token);
      setSuccessMessage('Мероприятие отменено');
      await loadEvents();
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError('Не удалось отменить мероприятие');
      }
    } finally {
      setActionEventId(null);
    }
  }

  async function handleDeleteEvent(eventId: number) {
    if (!token) {
      return;
    }

    const confirmed = window.confirm('Удалить мероприятие?');
    if (!confirmed) {
      return;
    }

    setError('');
    setSuccessMessage('');
    setActionEventId(eventId);

    try {
      await eventsApi.deleteEvent(eventId, token);
      setSuccessMessage('Мероприятие удалено');
      await loadEvents();
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError('Не удалось удалить мероприятие');
      }
    } finally {
      setActionEventId(null);
    }
  }

  async function loadEnrollmentsForEvent(event: EventResponse) {
  if (!token) {
    return;
  }

  setSelectedEvent(event);
  setIsLoadingEnrollments(true);
  setError('');

  try {
    const data = await enrollmentsApi.getEnrollmentsByEvent(event.id, token);
    setEventEnrollments(data);

    let pointsByVisitorId: Record<number, number> = {};

    if (event.eventType === 'TOURNAMENT') {
      const entries = await ratingsApi.getTournamentRatingEntries(event.id, token);

      pointsByVisitorId = entries.reduce<Record<number, number>>((acc, entry) => {
        acc[entry.visitor.id] = entry.points;
        return acc;
      }, {});
    }

    const initialPoints = data.reduce<Record<number, string>>((acc, enrollment) => {
      const existingPoints = pointsByVisitorId[enrollment.visitor.id];
      acc[enrollment.id] = existingPoints !== undefined ? String(existingPoints) : '';
      return acc;
    }, {});

    setPointsByEnrollmentId(initialPoints);
  } catch (err) {
    if (err instanceof ApiRequestError) {
      setError(err.message);
    } else {
      setError('Не удалось загрузить участников');
    }
  } finally {
    setIsLoadingEnrollments(false);
  }
}

async function handleConfirmPresence(enrollmentId: number) {
  if (!token || !selectedEvent) {
    return;
  }

  setError('');
  setSuccessMessage('');
  setActionEventId(selectedEvent.id);

  try {
    await enrollmentsApi.confirmPresence(enrollmentId, token);
    setSuccessMessage('Присутствие подтверждено');
    await loadEnrollmentsForEvent(selectedEvent);
  } catch (err) {
    if (err instanceof ApiRequestError) {
      setError(err.message);
    } else {
      setError('Не удалось подтвердить присутствие');
    }
  } finally {
    setActionEventId(null);
  }
}

async function handleAddVisitorManually() {
  if (!token || !selectedEvent) {
    return;
  }

  setManualVisitorError('');

  const visitorId = Number(selectedVisitorId);

  if (!visitorId || visitorId <= 0) {
    setManualVisitorError('Выберите посетителя');
    return;
  }

  setError('');
  setSuccessMessage('');
  setActionEventId(selectedEvent.id);

  try {
    await enrollmentsApi.addVisitorManually(selectedEvent.id, visitorId, token);
    setSuccessMessage('Участник добавлен');
    setSelectedVisitorId('');
    await loadEnrollmentsForEvent(selectedEvent);
    await loadEvents();
  } catch (err) {
    if (err instanceof ApiRequestError) {
      setManualVisitorError(err.message);
    } else {
      setManualVisitorError('Не удалось добавить участника');
    }
  } finally {
    setActionEventId(null);
  }
}

async function handleAssignPoints() {
  if (!token || !selectedEvent) {
    return;
  }

  if (selectedEvent.eventType !== 'TOURNAMENT') {
    setError('Очки можно начислять только за турниры');
    return;
  }

  const requests: RatingEntryRequest[] = eventEnrollments
    .filter((enrollment) => enrollment.status === 'PRESENT')
    .filter((enrollment) => pointsByEnrollmentId[enrollment.id]?.trim() !== '')
    .map((enrollment) => ({
      visitorId: enrollment.visitor.id,
      points: Number(pointsByEnrollmentId[enrollment.id]),
    }));

  const hasInvalidPoints = requests.some(
    (request) => Number.isNaN(request.points) || request.points < 0
  );

  if (hasInvalidPoints) {
    setError('Очки должны быть неотрицательным числом');
    return;
  }

  if (requests.length === 0) {
    setError('Укажите очки хотя бы одному присутствующему участнику');
    return;
  }

  setError('');
  setSuccessMessage('');
  setIsAssigningPoints(true);

  try {
    await ratingsApi.assignTournamentPoints(selectedEvent.id, requests, token);
    setSuccessMessage('Очки начислены');
  } catch (err) {
    if (err instanceof ApiRequestError) {
      setError(err.message);
    } else {
      setError('Не удалось начислить очки');
    }
  } finally {
    setIsAssigningPoints(false);
  }
}

async function handleDownloadReport(format: 'csv' | 'pdf') {
  if (!token || !selectedEvent) {
    return;
  }

  if (selectedEvent.eventType !== 'TOURNAMENT') {
    setError('Отчеты доступны только для турниров');
    return;
  }

  setError('');
  setSuccessMessage('');
  setIsDownloadingReport(true);

  try {
    if (format === 'csv') {
      await reportsApi.downloadTournamentCsvReport(selectedEvent.id, token);
    } else {
      await reportsApi.downloadTournamentPdfReport(selectedEvent.id, token);
    }
  } catch {
    setError('Не удалось скачать отчет');
  } finally {
    setIsDownloadingReport(false);
  }
}

  if (!isAdmin) {
    return (
      <section className="content-section">
        <div className="state-message">Админ-панель доступна только администратору.</div>
      </section>
    );
  }

  return (
    <section className="content-section admin-layout">
      <div>
        <div className="section-header">
          <div>
            <h2>Админ-панель</h2>
            <p>Создание и управление мероприятиями</p>
          </div>
        </div>

        {successMessage && <div className="success">{successMessage}</div>}

        {error && <div className="error">{error}</div>}

        <div className="panel admin-form-panel">
          <h3>{editingEvent ? 'Редактирование мероприятия' : 'Новое мероприятие'}</h3>
          <EventForm
            initialEvent={editingEvent}
            isSubmitting={isCreating}
            validationErrors={validationErrors}
            submitLabel={editingEvent ? 'Сохранить изменения' : 'Создать мероприятие'}
            onSubmit={handleSaveEvent}
            onCancelEdit={() => {
              setEditingEvent(null);
              setValidationErrors({});
              setError('');
            }}
          />
        </div>
      </div>

      <div>
        <div className="section-header compact">
          <div>
            <h2>Мероприятия</h2>
            <p>Список всех событий клуба</p>
          </div>
        </div>

        {isLoading && <div className="state-message">Загрузка мероприятий...</div>}

        {!isLoading && events.length === 0 && (
          <div className="state-message">Мероприятий пока нет</div>
        )}

        {!isLoading && events.length > 0 && (
          <div className="admin-event-list">
            {events.map((event) => (
              <article className="admin-event-item" key={event.id}>
                <div>
                  <span className="event-type">{eventTypeLabels[event.eventType]}</span>
                  <h3>{event.title}</h3>
                  <p>{formatDateTime(event.dateTime)}</p>
                  <p>
                    {statusLabels[event.status]} · {event.enrolledCount}/
                    {event.maxParticipants} участников
                  </p>
                </div>

                <div className="admin-actions">
                  
                  <button
                    className="button secondary"
                    type="button"
                    onClick={() => {
                      setEditingEvent(event);
                      setValidationErrors({});
                      setError('');
                      window.scrollTo({ top: 0, behavior: 'smooth' });
                    }}
                  >
                    Редактировать
                  </button>

                  <button
                    className="button"
                    type="button"
                    onClick={() => loadEnrollmentsForEvent(event)}
                  >
                    Участники
                  </button>

                  {event.status !== 'CANCELLED' && (
                    <button
                      className="button secondary"
                      type="button"
                      disabled={actionEventId === event.id}
                      onClick={() => handleCancelEvent(event.id)}
                    >
                      Отменить
                    </button>
                  )}

                  <button
                    className="button danger"
                    type="button"
                    disabled={actionEventId === event.id}
                    onClick={() => handleDeleteEvent(event.id)}
                  >
                    Удалить
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
        {selectedEvent && (
                  <div className="participants-panel">
          <div className="section-header compact participants-header">
            <div>
              <h2>Участники</h2>
              <p>{selectedEvent.title}</p>
            </div>

            {selectedEvent.eventType === 'TOURNAMENT' && eventEnrollments.length > 0 && (
              <div className="rating-actions">
                <button
                  className="button"
                  type="button"
                  disabled={isAssigningPoints}
                  onClick={handleAssignPoints}
                >
                  {isAssigningPoints ? 'Начисление...' : 'Начислить очки'}
                </button>

                <button
                  className="button secondary"
                  type="button"
                  disabled={isDownloadingReport}
                  onClick={() => handleDownloadReport('csv')}
                >
                  CSV
                </button>

                <button
                  className="button secondary"
                  type="button"
                  disabled={isDownloadingReport}
                  onClick={() => handleDownloadReport('pdf')}
                >
                  PDF
                </button>
              </div>
            )}
        </div>

            <div className="manual-add-row">
              <div className="manual-add-field">
                <select
                  value={selectedVisitorId}
                  onChange={(event) => {
                    setSelectedVisitorId(event.target.value);
                    setManualVisitorError('');
                  }}
                >
                  <option value="">Выберите посетителя</option>
                  {visitors.map((visitor) => (
                    <option key={visitor.id} value={visitor.id}>
                      {visitor.lastName} {visitor.firstName} — {visitor.email}
                    </option>
                  ))}
                </select>

                {manualVisitorError && (
                  <div className="inline-error">
                    <span aria-hidden="true">!</span>
                    {manualVisitorError}
                  </div>
                )}
              </div>

              <button
                className="button"
                type="button"
                disabled={actionEventId === selectedEvent.id}
                onClick={handleAddVisitorManually}
              >
                Добавить
              </button>
            </div>
            {isLoadingEnrollments && (
              <div className="state-message">Загрузка участников...</div>
            )}

            {!isLoadingEnrollments && eventEnrollments.length === 0 && (
              <div className="state-message">Участников пока нет</div>
            )}

            {!isLoadingEnrollments && eventEnrollments.length > 0 && (
              <div className="table-wrap participants-table-wrap">
                <table
                  className={
                    selectedEvent.eventType === 'TOURNAMENT'
                      ? 'data-table participants-rating-table'
                      : 'data-table'
                  }
                >
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Участник</th>
                      <th>Email</th>
                      <th>Статус</th>
                      {selectedEvent.eventType === 'TOURNAMENT' && <th>Очки</th>}
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {eventEnrollments.map((enrollment) => (
                      <tr key={enrollment.id}>
                        <td>{enrollment.id}</td>
                        <td>
                          {enrollment.visitor.firstName} {enrollment.visitor.lastName}
                        </td>
                        <td>{enrollment.visitor.email}</td>
                        <td>{enrollmentStatusLabels[enrollment.status]}</td>
                        {selectedEvent.eventType === 'TOURNAMENT' && (
                          <td>
                            {enrollment.status === 'PRESENT' ? (
                              <input
                                className="points-input"
                                value={pointsByEnrollmentId[enrollment.id] ?? ''}
                                onChange={(event) =>
                                  setPointsByEnrollmentId((current) => ({
                                    ...current,
                                    [enrollment.id]: event.target.value,
                                  }))
                                }
                                type="number"
                                min={0}
                                placeholder="Не задано"
                              />
                            ) : (
                              <span className="muted">Только после присутствия</span>
                            )}
                          </td>
                        )}
                        <td className="table-actions">
                          {enrollment.status === 'REGISTERED' && (
                            <button
                              className="button secondary"
                              type="button"
                              disabled={actionEventId === selectedEvent.id}
                              onClick={() => handleConfirmPresence(enrollment.id)}
                            >
                              Подтвердить
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

          </div>
        )}
      </div>
    </section>
  );
}
