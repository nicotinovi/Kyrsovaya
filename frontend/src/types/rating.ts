/**описывает рейтинг и начисления очков */
import type { UserResponse } from './auth';

export interface RatingResponse {
  id: number;
  visitor: UserResponse;
  totalPoints: number;
  rankPosition: number;
  lastUpdated: string;
}

export interface RatingEntryRequest {
  visitorId: number;
  points: number;
}

export interface RatingEntryResponse {
  id: number;
  visitor: UserResponse;
  tournamentId: number;
  points: number;
  assignedByAdmin: UserResponse | null;
  assignedAt: string;
}