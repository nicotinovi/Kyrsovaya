import { apiRequest } from './httpClient';
import type { UserResponse } from '../types/auth';

export function getVisitors(token: string) {
  return apiRequest<UserResponse[]>('/admin/visitors', {
    token,
  });
}