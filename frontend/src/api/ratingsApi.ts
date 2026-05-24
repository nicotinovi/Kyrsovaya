//API для рейтинга
import { apiRequest } from './httpClient';
import type { PageResponse } from '../types/api';
import type { RatingEntryRequest, RatingEntryResponse, RatingResponse } from '../types/rating';

export function getRatings(page = 0, size = 20) {
  return apiRequest<PageResponse<RatingResponse>>(`/ratings?page=${page}&size=${size}`);
}

export function getMyRating(token: string) {
  return apiRequest<RatingResponse>('/me/rating', {
    token,
  });
}

export function getTournamentRatingEntries(tournamentId: number, token: string) {
  return apiRequest<RatingEntryResponse[]>(
    `/admin/tournaments/${tournamentId}/rating-entries`,
    {
      token,
    }
  );
}

export function assignTournamentPoints(
  tournamentId: number,
  requests: RatingEntryRequest[],
  token: string
) {
  return apiRequest<RatingEntryResponse[]>(
    `/admin/tournaments/${tournamentId}/rating-entries`,
    {
      method: 'POST',
      body: requests,
      token,
    }
  );
}

export function recalculateRatings(token: string) {
  return apiRequest<RatingResponse[]>('/admin/ratings/recalculate', {
    method: 'POST',
    token,
  });
}