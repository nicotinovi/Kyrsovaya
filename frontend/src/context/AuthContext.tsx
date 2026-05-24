/** тут будет храниться состояние авторизации:
 * текущий пользователь
 * токен
 * функция входа/выхода
 * функция регистрации
 */

import {
  createContext, //контекст позволяет хранить данные для приложения
  useContext, //позволяет компонентам читать данные из контекста
  useEffect, //выполняет код при запуске компнента или при изменении данных
  useMemo, //запоминает объект, чтобы React не создавал его заного без необходимости
  useState, //хранит состояние компонента
  type ReactNode, //тип содержимого компонента
} from 'react';
//импорт всех функций из authApi
import * as authApi from '../api/authApi';
//импорт функций работы с localStorage
import {
  clearStoredToken, //удаляет токен
  getStoredToken, //достает токен
  setStoredToken, //сохраняет токен
} from '../api/httpClient';
//импорт typeScript типов
import type {
  AuthResponse, //ответ бэка после входа или регистрации
  LoginRequest, //данные формы входа
  RegisterRequest, //данные формы регистрации
  UserResponse, //данные текщего пользователя
} from '../types/auth';

//описание хранения AuthContext
type AuthContextValue = {
  user: UserResponse | null; //описание текущего пользователя(если не вошел то null )
  token: string | null;
  isLoading: boolean; //показывает идет ли сейчас проверка авторизации
  isAuthenticated: boolean; //true -если пользоваетль вошел, false - если не вошел
  isAdmin: boolean; //если роль админа - то true, false - в противном случае
  login: (request: LoginRequest) => Promise<void>; //функция входа(Promise<void> возвращает потому что асинхронно работает)
  register: (request: RegisterRequest) => Promise<void>; //форма регистрации, тоже асинхронная
  logout: () => void; //функция выхода(ничег не возвращает)
};

//создание React Context
const AuthContext = createContext<AuthContextValue | null>(null);
//преобразование AuthResponse в UserResponse
function authResponseToUser(response: AuthResponse): UserResponse {
  return {
    id: response.userId,
    firstName: response.firstName,
    lastName: response.lastName,
    email: response.email,
    role: response.role,
  };
}

//компонент, который оборачивает приложение и дает ему доступ к авторизации
//useState - фнукция для изменения значения
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null); //хранит текущего пользователя
  const [token, setToken] = useState<string | null>(() => getStoredToken()); //берет токен из localStorage
  //() => getStoredToken() - выполнится один раз при запуске
  //нужно чтобы пользователь не вылетял при обновлении страницы
  const [isLoading, setIsLoading] = useState(true);
  //true - потому что изначально мы не знаем авторизованли пользователь, валиден ли токен, и есть ли он вообще

  //когда приложение открывается, оно проверяте
  useEffect(() => {
    async function loadUser() {
      if (!token) { //есть ли токен
        setIsLoading(false);
        return;
      }

      try { //еслли токен есть то отправляем запрос бэку для его получения
        const currentUser = await authApi.getCurrentUser(token);
        setUser(currentUser); // сохраняем пользователя
      } catch { //если запрос отказ, значит токен просрочен, поврежден или бэк отклонил апрос
        clearStoredToken(); //удаляем токен
        setToken(null); //очищаем токен из React 
        setUser(null); //очиаем пользователя
      } finally { //выполняется всегда при любом исходе
        setIsLoading(false);//проверка авторизации остановлена
      }
    }

    loadUser();
  }, [token]);

  //отправляем логин и пароль на бэк
  async function login(request: LoginRequest) {
    const response = await authApi.login(request);
    setStoredToken(response.token); ///сохраняем токен в localSorage
    setToken(response.token); //созраянем токен в реакт состояние
    setUser(authResponseToUser(response)); //интерфейс сразу знает что пользователь ошел
  }
 //также как и login
  async function register(request: RegisterRequest) {
    const response = await authApi.register(request);
    setStoredToken(response.token);
    setToken(response.token);
    setUser(authResponseToUser(response));
  }
//функция выхода
  function logout() {
    clearStoredToken(); //удаляем токен из браузера
    setToken(null); //очищаем его из реакта
    setUser(null); //очищаем пользователя
  }

  //формируется объект который будет доступен всему приложению
  //REact не будет его пересоздавать без необходимости(только при изменении данных)
  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isLoading,
      isAuthenticated: user !== null && token !== null, //польхователь авторизован если есть эти параетры
      isAdmin: user?.role === 'ADMINISTRATOR', 
      login,
      register,
      logout,
    }),
    [user, token, isLoading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>; //тут хранится вся авторизация
}

//функция для доступа к авторизаии
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth должна быть использована внутри AuthProvider');
  }

  return context;
}