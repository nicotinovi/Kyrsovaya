//страница личного кабинета
import { useEffect, useState } from 'react';
import { ApiRequestError } from '../api/httpClient';
import * as enrollmentsApi from '../api/enrollmentsApi';
import { useAuth } from '../context/AuthContext';
import type { EnrollmentResponse } from '../types/enrollment';

const eventStatusLabels = {
  OPEN_FOR_REGISTRATION: 'Открыта запись',
  IN_PROGRESS: 'Идет',
  COMPLETED: 'Завершено',
  CANCELLED: 'Отменено',
};

const enrollmentStatusLabels = {
  REGISTERED: 'Записан',
  PRESENT: 'Присутствовал',
  CANCELLED: 'Отменено',
};


function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('ru-RU', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function CabinetPage() {
  const { token, isAuthenticated, isAdmin, user } = useAuth();

  const [enrollments, setEnrollments] = useState<EnrollmentResponse[]>([]); //список записей
  const [isLoading, setIsLoading] = useState(true); //флаг ззагрузки
  const [cancellingId, setCancellingId] = useState<number | null>(null); //Id записи, которую сейчас отменяем.
  const [error, setError] = useState(''); // текст ошибки.
  const [successMessage, setSuccessMessage] = useState(''); //сообщение об успешном действии.

  //Функция загружает записи текущего пользователя.
  async function loadEnrollments() {
    if (!token || isAdmin) {
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const data = await enrollmentsApi.getMyEnrollments(token);
      setEnrollments(data);
    } catch {
      setError('Не удалось загрузить записи');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadEnrollments();
  }, [token]); //когда страница открылась загружаем записи

  //Функция вызывается, когда пользователь нажимает кнопку “Отменить”.
  async function handleCancel(enrollmentId: number) {
    if (!token) {
      return;
    }

    setError('');
    setSuccessMessage('');
    setCancellingId(enrollmentId);

    try {
      await enrollmentsApi.cancelEnrollment(enrollmentId, token);
      setSuccessMessage('Запись отменена');
      await loadEnrollments();
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError('Не удалось отменить запись');
      }
    } finally {
      setCancellingId(null);
    }
  }

  if (!isAuthenticated || !user) {
    return (
        <section className="content-section">
        <div className="state-message">
            Личный кабинет доступен только авторизованному посетителю.
        </div>
        </section>
    );
    }

    if (isAdmin) {
    return (
        <section className="content-section">
        <div className="state-message">
            Для администратора доступна отдельная админ-панель.
        </div>
        </section>
    );
    }

  return (
    <section className="content-section">
      <div className="section-header">
        <div>
          <h2>Личный кабинет</h2>
          <p>
            {user.firstName} {user.lastName} — {user.email}
          </p>
        </div>
      </div>

      {successMessage && <div className="success">{successMessage}</div>}

      {error && <div className="error">{error}</div>}

      {isLoading && <div className="state-message">Загрузка записей...</div>}

      {!isLoading && enrollments.length === 0 && (
        <div className="state-message">У вас пока нет записей на мероприятия</div>
      )}

      {!isLoading && enrollments.length > 0 && (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Мероприятие</th>
                <th>Тип</th>
                <th>Дата</th>
                <th>Статус мероприятия</th>
                <th>Статус записи</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {enrollments.map((enrollment) => {
                const canCancel =
                  enrollment.status === 'REGISTERED' &&
                  enrollment.event.status === 'OPEN_FOR_REGISTRATION';

                return (
                  <tr key={enrollment.id}>
                    <td>{enrollment.event.title}</td>
                    <td>
                      {enrollment.event.eventType === 'TOURNAMENT'
                        ? 'Турнир'
                        : 'Тренировка'}
                    </td>
                    <td>{formatDateTime(enrollment.event.dateTime)}</td>
                    <td>{eventStatusLabels[enrollment.event.status]}</td>
                    <td>{enrollmentStatusLabels[enrollment.status]}</td>
                    <td className="table-actions">
                      {canCancel && (
                        <button
                          className="button secondary"
                          type="button"
                          disabled={cancellingId === enrollment.id}
                          onClick={() => handleCancel(enrollment.id)}
                        >
                          {cancellingId === enrollment.id ? 'Отмена...' : 'Отменить'}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}