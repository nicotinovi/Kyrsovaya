// страница входа
import { Link } from 'react-router-dom';
import { useState, type FormEvent } from 'react';
import { ApiRequestError } from '../api/httpClient';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const { login, isAuthenticated, user, logout } = useAuth(); //страница берет данные из AuthContext

  const [email, setEmail] = useState('admin@pokerclub.local'); //текст в поле email, setEmail - функция для изменения 
  const [password, setPassword] = useState('admin12345'); //пароль
  const [error, setError] = useState(''); //текст ошибки
  const [isSubmitting, setIsSubmitting] = useState(false); //флаг-форма сейчас отправляется

  //вызывается когда пользователь нажимает кнопку войти
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError(''); //очищаем страую ошибку перед запросом
    setIsSubmitting(true); //ставим флаг загрузки

    try {
      await login({ email, password });
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError('Не удалось выполнить вход');
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  //если пользователь вошел, то показываем не форму а блок
  if (isAuthenticated && user) {
    return (
      <section className="panel">
        <h2>Вы вошли в систему</h2>
        <p>
          {user.firstName} {user.lastName} — {user.email}
        </p>
        <p>Роль: {user.role}</p>
        <button className="button secondary" type="button" onClick={logout}>
          Выйти
        </button>
      </section>
    );
  }
 //если не вошел
  return (
    <section className="panel">
      <div className="panel-title-row">
        <h2>Вход</h2>
        <Link className="link-button" to="/register">
          Зарегистрироваться
        </Link>
      </div>

      <form className="form" onSubmit={handleSubmit}>
        <label>
          Email
          <input
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            type="email"
            inputMode='email'
            autoComplete="email"
          />
        </label>

        <label>
          Пароль
          <input
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            type="password"
            autoComplete="current-password"
          />
        </label>

        {error && <div className="error">{error}</div>}

        <button className="button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Вход...' : 'Войти'}
        </button>
      </form>
    </section>
  );
}