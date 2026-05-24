/**описывает записи посетителей на мероприятия */
import type { EventResponse } from './event';
import type { UserResponse } from './auth';

export type EnrollmentStatus = 'REGISTERED' | 'PRESENT' | 'CANCELLED';

export interface EnrollmentResponse {
  id: number;
  status: EnrollmentStatus;
  createdAt: string;
  visitor: UserResponse;
  event: EventResponse;
}

export interface AdminEnrollmentRequest {
  visitorId: number;
}