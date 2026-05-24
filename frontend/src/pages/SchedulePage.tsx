//страница расписания
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiRequestError } from '../api/httpClient'; //ошибки бэка
import * as enrollmentsApi from '../api/enrollmentsApi'; //запись на меропр
import * as eventsApi from '../api/eventsApi'; //загрузка меропр
import { EventCard } from '../components/EventCard';
import { useAuth } from '../context/AuthContext';
import type { EventResponse, EventType } from '../types/event';

export function SchedulePage() {
  const { token, isAuthenticated, isAdmin } = useAuth();

  const [events, setEvents] = useState<EventResponse[]>([]); //список мероприятий
  const [enrolledEventIds, setEnrolledEventIds] = useState<Set<number>>(new Set());
  const [type, setType] = useState<EventType | ''>(''); //фильтр
  const [isLoading, setIsLoading] = useState(true); //идет ли загрузка расписания
  const [submittingEventId, setSubmittingEventId] = useState<number | null>(null); //id мероприятия на которое сейчас идет запись
  const [error, setError] = useState(''); //сообщение ошибки
  const [successMessage, setSuccessMessage] = useState(''); //сообщение усеха

  //загружает расписание с бэка

  
  async function loadEvents() {
    setIsLoading(true);
    setError('');

    try {
      const page = await eventsApi.getEvents({
        type,
        page: 0,
        size: 20,
      });

      const now = new Date();

      const visibleEvents = isAdmin
        ? page.content
        : page.content.filter((event) => new Date(event.dateTime) > now);

      setEvents(visibleEvents);
    } catch {
      setError('Не удалось загрузить расписание');
    } finally {
      setIsLoading(false);
    }
  }
//выхывается при клике на кнопку записаться
  async function loadMyEnrollments() {
    if (!token || isAdmin) {
      setEnrolledEventIds(new Set());
      return;
    }

    try {
      const enrollments = await enrollmentsApi.getMyEnrollments(token);
      const activeEventIds = enrollments
        .filter((enrollment) => enrollment.status !== 'CANCELLED')
        .map((enrollment) => enrollment.event.id);

      setEnrolledEventIds(new Set(activeEventIds));
    } catch {
      setEnrolledEventIds(new Set());
    }
  }

  useEffect(() => {
    loadEvents();
  }, [type, isAdmin]);

  useEffect(() => {
    loadMyEnrollments();
  }, [token, isAdmin]);

  async function handleEnroll(eventId: number) {
    if (!token) {
      setError('Для записи нужно войти в систему');
      return;
    }

    setError('');
    setSuccessMessage('');
    setSubmittingEventId(eventId);

    try {
      await enrollmentsApi.enrollToEvent(eventId, token);
      setSuccessMessage('Вы успешно записались на мероприятие');
      setEnrolledEventIds((current) => new Set(current).add(eventId));
      await loadEvents();
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError('Не удалось записаться на мероприятие');
      }
    } finally {
      setSubmittingEventId(null);
    }
  }

  const canVisitorEnroll = isAuthenticated && !isAdmin;

  return (
    <section className="content-section">
      <div className="section-header">
        <div>
          <h2>Расписание</h2>
          <p>Турниры и тренировки клуба</p>
        </div>

        <select value={type} onChange={(event) => setType(event.target.value as EventType | '')}>
          <option value="">Все мероприятия</option>
          <option value="TOURNAMENT">Турниры</option>
          <option value="TRAINING">Тренировки</option>
        </select>
      </div>

      {!isAuthenticated && (
            <div className="signin-callout">
                <div>
                    <strong>Хотите записаться на мероприятие?</strong>
                    <span>Войдите в систему или создайте аккаунт посетителя.</span>
                </div>
            </div>
        )}

      {successMessage && <div className="success">{successMessage}</div>}

      {isLoading && <div className="state-message">Загрузка расписания...</div>}

      {error && <div className="error">{error}</div>}

      {!isLoading && !error && events.length === 0 && (
        <div className="state-message">Мероприятий пока нет</div>
      )}

      <div className="event-grid">
        {events.map((event) => (
          <EventCard
            key={event.id}
            event={event}
            canEnroll={canVisitorEnroll}
            isAlreadyEnrolled={enrolledEventIds.has(event.id)}
            isSubmitting={submittingEventId === event.id}
            onEnroll={handleEnroll}
          />
        ))}
      </div>
    </section>
  );
}