import type { ApiError } from '../types/api';

/**адрес бэкенда API */
const API_BASE_URL = 'http://localhost:8080/api';
/**класс ошибок, то есть если бэк вернет ошику JSON то фронт превратит ее в объект ApiRequestError и можно будетпоказать:
error.message
error.validationErrors.email  */
export class ApiRequestError extends Error {
  status: number;
  validationErrors: Record<string, string>;

  constructor(apiError: ApiError) {
    super(apiError.message);
    this.name = 'ApiRequestError';
    this.status = apiError.status;
    this.validationErrors = apiError.validationErrors;
  }
}

type RequestOptions = {
  method?: string;
  body?: unknown;
  token?: string | null;
};

/**универсальная функция для запросов
 * <T> - тип ожидаеого объекта
 */
export async function apiRequest<T>(
  path: string,
  options: RequestOptions = {}
): Promise<T> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    /**говорим бэку: ожидаем JSON */
  };

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json; charset=utf-8';
    /**ставится только если есть body: говорим бэку что отправляем JSON */
  }

  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`;
    /**добавляется если пережан JWT-токен */
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });
/** обработка ошибок */
  if (!response.ok) {
    const apiError = (await response.json()) as ApiError;
    throw new ApiRequestError(apiError);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
/**функции ниже работают с токеном в браузере */
export function getStoredToken() {
  return localStorage.getItem('token');
}

export function setStoredToken(token: string) {
  localStorage.setItem('token', token); /**после login сохраняем токен */
  /**потом в запросах берется этот токен и отправляется в заголовке бэку */
}

export function clearStoredToken() {
  localStorage.removeItem('token');
}