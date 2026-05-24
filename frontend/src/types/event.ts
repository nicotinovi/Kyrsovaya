/**описывает турниры и тренировки */
export type EventType = 'TOURNAMENT' | 'TRAINING';

export type EventStatus =
  | 'OPEN_FOR_REGISTRATION'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface EventResponse {
  id: number;
  eventType: EventType;
  title: string;
  description: string;
  dateTime: string;
  maxParticipants: number;
  enrolledCount: number;
  availablePlaces: number;
  status: EventStatus;
  createdAt: string;
}

export interface EventRequest {
  eventType: EventType;
  title: string;
  description: string;
  dateTime: string;
  maxParticipants: number;
}