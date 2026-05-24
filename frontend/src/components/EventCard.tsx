//компонент карточки мероприятия
import type { EventResponse } from '../types/event';

//отображение карточки мероприятия
type EventCardProps = {
  event: EventResponse; //данные мероприятия
  canEnroll?: boolean; //показывать ли кнопку записаться
  isAlreadyEnrolled?: boolean;
  isSubmitting?: boolean; //идет ли сейчас запрос записи?
  onEnroll?: (eventId: number) => void; //функция которая вызывается при клике
};
type DisplayStatus = EventResponse['status'] | 'REGISTRATION_CLOSED';

//словари
//бэк возвращает то что слува а мы меняем на то что справа
const eventTypeLabels = {
  TOURNAMENT: 'Турнир',
  TRAINING: 'Тренировка',
};

//то же самое со статусами
const statusLabels: Record<DisplayStatus, string> = {
  OPEN_FOR_REGISTRATION: 'Открыта запись',
  REGISTRATION_CLOSED: 'Запись закрыта',
  IN_PROGRESS: 'Идет',
  COMPLETED: 'Завершено',
  CANCELLED: 'Отменено',
};

//для нормального показа даты
function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('ru-RU', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function EventCard({
  event,
  canEnroll = false,
  isAlreadyEnrolled = false,
  isSubmitting = false,
  onEnroll,
}: EventCardProps) {
  const isEnrollmentAvailable =
    event.status === 'OPEN_FOR_REGISTRATION' && event.availablePlaces > 0;

  const displayStatus: DisplayStatus =
    event.status === 'OPEN_FOR_REGISTRATION' && event.availablePlaces <= 0
      ? 'REGISTRATION_CLOSED'
      : event.status;

  return (
    <article className="event-card">
      <div className="event-card-header">
        <div>
          <span className="event-type">{eventTypeLabels[event.eventType]}</span>
          <h3>{event.title}</h3>
        </div>
        <span className={`status status-${displayStatus.toLowerCase()}`}>
            {statusLabels[displayStatus]}
        </span>
      </div>

      <p className="event-description">{event.description}</p>

      <dl className="event-meta">
        <div>
          <dt>Дата</dt>
          <dd>{formatDateTime(event.dateTime)}</dd>
        </div>
        <div>
          <dt>Участники</dt>
          <dd>
            {event.enrolledCount} / {event.maxParticipants}
          </dd>
        </div>
        <div>
          <dt>Свободно мест</dt>
          <dd>{event.availablePlaces}</dd>
        </div>
      </dl>

      {canEnroll && isAlreadyEnrolled && (
        <div className="enrolled-note">Вы уже записаны</div>
      )}

      {canEnroll && !isAlreadyEnrolled && (
        <button
          className="button event-action"
          type="button"
          disabled={!isEnrollmentAvailable || isSubmitting}
          onClick={() => onEnroll?.(event.id)}
        >
          {isSubmitting ? 'Запись...' : 'Записаться'}
        </button>
      )}
    </article>
  );
}