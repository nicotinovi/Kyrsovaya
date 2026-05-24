//форма мероприятия
import { useEffect, useState, type FormEvent } from 'react';
import type { EventRequest, EventResponse, EventType } from '../types/event';

type EventFormProps = {
  initialEvent?: EventResponse | null;
  isSubmitting?: boolean;
  validationErrors?: Record<string, string>;
  submitLabel?: string;
  onSubmit: (request: EventRequest) => Promise<void>;
  onCancelEdit?: () => void;
};

function getDefaultDateTime() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  date.setHours(19, 0, 0, 0);
  return date.toISOString().slice(0, 16);
}

function toDateTimeLocalValue(value: string) {
  return value.slice(0, 16);
}

export function EventForm({
  initialEvent = null,
  isSubmitting = false,
  validationErrors = {},
  submitLabel,
  onSubmit,
  onCancelEdit,
}: EventFormProps) {
  const [eventType, setEventType] = useState<EventType>('TOURNAMENT');
  const [title, setTitle] = useState('Новый турнир');
  const [description, setDescription] = useState('Описание мероприятия');
  const [dateTime, setDateTime] = useState(getDefaultDateTime());
  const [maxParticipants, setMaxParticipants] = useState(24);

  useEffect(() => {
    if (initialEvent) {
      setEventType(initialEvent.eventType);
      setTitle(initialEvent.title);
      setDescription(initialEvent.description);
      setDateTime(toDateTimeLocalValue(initialEvent.dateTime));
      setMaxParticipants(initialEvent.maxParticipants);
    } else {
      setEventType('TOURNAMENT');
      setTitle('Новый турнир');
      setDescription('Описание мероприятия');
      setDateTime(getDefaultDateTime());
      setMaxParticipants(24);
    }
  }, [initialEvent]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    await onSubmit({
      eventType,
      title,
      description,
      dateTime,
      maxParticipants,
    });
  }

  return (
    <form className="form event-form" onSubmit={handleSubmit}>
      <label>
        Тип мероприятия
        <select
          value={eventType}
          onChange={(event) => setEventType(event.target.value as EventType)}
        >
          <option value="TOURNAMENT">Турнир</option>
          <option value="TRAINING">Тренировка</option>
        </select>
      </label>

      <label>
        Название
        <input
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          type="text"
        />
        {validationErrors.title && (
          <span className="field-error">{validationErrors.title}</span>
        )}
      </label>

      <label>
        Описание
        <textarea
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          rows={3}
        />
        {validationErrors.description && (
          <span className="field-error">{validationErrors.description}</span>
        )}
      </label>

      <label>
        Дата и время
        <input
          value={dateTime}
          onChange={(event) => setDateTime(event.target.value)}
          type="datetime-local"
        />
        {validationErrors.dateTime && (
          <span className="field-error">{validationErrors.dateTime}</span>
        )}
      </label>

      <label>
        Максимум участников
        <input
          value={maxParticipants}
          onChange={(event) => setMaxParticipants(Number(event.target.value))}
          type="number"
          min={1}
        />
        {validationErrors.maxParticipants && (
          <span className="field-error">{validationErrors.maxParticipants}</span>
        )}
      </label>

      <div className="form-actions">
        <button className="button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Сохранение...' : submitLabel ?? 'Создать мероприятие'}
        </button>

        {initialEvent && onCancelEdit && (
          <button
            className="button secondary"
            type="button"
            disabled={isSubmitting}
            onClick={onCancelEdit}
          >
            Отменить редактирование
          </button>
        )}
      </div>
    </form>
  );
}