//страница рейтинга
import { useCallback, useEffect, useState } from 'react';
import * as ratingsApi from '../api/ratingsApi';
import { useAuth } from '../context/AuthContext';
import type { RatingResponse } from '../types/rating';

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('ru-RU', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function RatingsPage() {
  const { token, isAuthenticated, isAdmin } = useAuth();

  const [ratings, setRatings] = useState<RatingResponse[]>([]);
  const [myRating, setMyRating] = useState<RatingResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRecalculating, setIsRecalculating] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const loadRatings = useCallback(async function loadRatings() {
    setIsLoading(true);
    setError('');

    try {
      const page = await ratingsApi.getRatings(0, 50);
      setRatings(page.content);

      if (token && isAuthenticated && !isAdmin) {
        const currentRating = await ratingsApi.getMyRating(token);
        setMyRating(currentRating);
      } else {
        setMyRating(null);
      }
    } catch {
      setError('Не удалось загрузить рейтинг');
    } finally {
      setIsLoading(false);
    }
  }, [token, isAuthenticated, isAdmin]);

  useEffect(() => {
    loadRatings();
  }, [loadRatings]);

  async function handleRecalculate() {
    if (!token) {
      return;
    }

    setError('');
    setSuccessMessage('');
    setIsRecalculating(true);

    try {
      await ratingsApi.recalculateRatings(token);
      setSuccessMessage('Рейтинг пересчитан');
      await loadRatings();
    } catch {
      setError('Не удалось пересчитать рейтинг');
    } finally {
      setIsRecalculating(false);
    }
  }

  return (
    <section className="content-section">
      <div className="section-header">
        <div>
          <h2>Рейтинг игроков</h2>
          <p>Турнирная таблица Full House Club</p>
        </div>

        {isAdmin && (
          <button
            className="button secondary"
            type="button"
            disabled={isRecalculating}
            onClick={handleRecalculate}
          >
            {isRecalculating ? 'Пересчет...' : 'Пересчитать'}
          </button>
        )}
      </div>

      {myRating && (
        <div className="rating-summary">
          <strong>Ваш рейтинг</strong>
          <span>Место: {myRating.rankPosition}</span>
          <span>Очки: {myRating.totalPoints}</span>
        </div>
      )}

      {successMessage && <div className="success">{successMessage}</div>}

      {error && <div className="error">{error}</div>}

      {isLoading && <div className="state-message">Загрузка рейтинга...</div>}

      {!isLoading && ratings.length === 0 && (
        <div className="state-message">Рейтинг пока пуст</div>
      )}

      {!isLoading && ratings.length > 0 && (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Место</th>
                <th>Игрок</th>
                <th>Email</th>
                <th>Очки</th>
                <th>Обновлен</th>
              </tr>
            </thead>
            <tbody>
              {ratings.map((rating) => (
                <tr key={rating.id}>
                  <td>{rating.rankPosition}</td>
                  <td>
                    {rating.visitor.firstName} {rating.visitor.lastName}
                  </td>
                  <td>{rating.visitor.email}</td>
                  <td>{rating.totalPoints}</td>
                  <td>{formatDateTime(rating.lastUpdated)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
