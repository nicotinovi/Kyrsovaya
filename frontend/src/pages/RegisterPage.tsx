//страница регистрации
import { useState, type FormEvent } from 'react';
import { ApiRequestError } from '../api/httpClient';
import { useAuth } from '../context/AuthContext';

type RegisterPageProps = {
  onSwitchToLogin: () => void;
};

export function RegisterPage({ onSwitchToLogin }: RegisterPageProps) {
  const { register, isAuthenticated, user, logout } = useAuth();

  const [firstName, setFirstName] = useState('Иван');
  const [lastName, setLastName] = useState('Иванов');
  const [email, setEmail] = useState('ivan.new@example.com');
  const [password, setPassword] = useState('password123');
  const [generalError, setGeneralError] = useState('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setGeneralError('');
    setValidationErrors({});

    const nextValidationErrors: Record<string, string> = {};
    const normalizedEmail = email.trim();
    const emailPattern = /^(?!.*xn--)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/i;

    if (firstName.trim().length < 2) {
      nextValidationErrors.firstName = 'Имя должно содержать минимум 2 символа';
    }

    if (lastName.trim().length < 2) {
      nextValidationErrors.lastName = 'Фамилия должна содержать минимум 2 символа';
    }

    if (!/^[A-Za-zА-Яа-яЁё -]+$/.test(firstName.trim())) {
      nextValidationErrors.firstName = 'Имя может содержать только буквы, пробел и дефис';
    }

    if (!/^[A-Za-zА-Яа-яЁё -]+$/.test(lastName.trim())) {
        nextValidationErrors.lastName = 'Фамилия может содержать только буквы, пробел и дефис';
    }

    if (/[а-яА-ЯёЁ]/.test(normalizedEmail) || !emailPattern.test(normalizedEmail)) {
        nextValidationErrors.email = 'Email должен быть в обычном латинском формате';
    }

    if (Object.keys(nextValidationErrors).length > 0) {
      setValidationErrors(nextValidationErrors);
      return;
    }

    setIsSubmitting(true);

    try {
      await register({
        firstName,
        lastName,
        email: normalizedEmail,
        password,
      });
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setGeneralError(err.message);
        setValidationErrors(err.validationErrors);
      } else {
        setGeneralError('Не удалось выполнить регистрацию');
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  if (isAuthenticated && user) {
    return (
      <section className="panel">
        <h2>Регистрация выполнена</h2>
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

  return (
    <section className="panel">
      <div className="panel-title-row">
        <h2>Регистрация</h2>
        <button className="link-button" type="button" onClick={onSwitchToLogin}>
          Уже есть аккаунт
        </button>
      </div>

      <form className="form" onSubmit={handleSubmit}>
        <label>
          Имя
          <input
            value={firstName}
            onChange={(event) => setFirstName(event.target.value)}
            type="text"
            autoComplete="given-name"
          />
          {validationErrors.firstName && (
            <span className="field-error">{validationErrors.firstName}</span>
          )}
        </label>

        <label>
          Фамилия
          <input
            value={lastName}
            onChange={(event) => setLastName(event.target.value)}
            type="text"
            autoComplete="family-name"
          />
          {validationErrors.lastName && (
            <span className="field-error">{validationErrors.lastName}</span>
          )}
        </label>

        <label>
          Email
          <input
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            type="email"
            inputMode='email'
            autoComplete="email"
          />
          {validationErrors.email && (
            <span className="field-error">{validationErrors.email}</span>
          )}
        </label>

        <label>
          Пароль
          <input
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            type="password"
            autoComplete="new-password"
          />
          {validationErrors.password && (
            <span className="field-error">{validationErrors.password}</span>
          )}
        </label>

        {generalError && <div className="error">{generalError}</div>}

        <button className="button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Регистрация...' : 'Зарегистрироваться'}
        </button>
      </form>
    </section>
  );
}