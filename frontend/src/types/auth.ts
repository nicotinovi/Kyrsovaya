/**описывает пользователя, логин, регистрацию и ответ авторизации */
export type UserRole = 'VISITOR' | 'ADMINISTRATOR';

export interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}
