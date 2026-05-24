/**Api-файл для авторизации */
import { apiRequest } from './httpClient';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserResponse,
} from '../types/auth';

/**принимает объект: email: string, password: string */
/**отправляет: POST /api/auth/login */
export function login(request: LoginRequest) { 
  return apiRequest<AuthResponse>('/auth/login', {
    method: 'POST',
    body: request,
  });
}

/**принимает:{
  firstName: string;
  lastName: string;
  email: string;
  password: string;
} */
/**отправляет: POST /api/auth/register */
export function register(request: RegisterRequest) {
  return apiRequest<AuthResponse>('/auth/register', {
    method: 'POST',
    body: request,
  });
}

/**отправляет: GET /api/auth/me и передает токен */
export function getCurrentUser(token: string) {
  return apiRequest<UserResponse>('/auth/me', {
    token,
  });
}