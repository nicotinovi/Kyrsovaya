//верхняя навигация
//Outlet - место куда React Router вставляет текущую страницу
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function AppLayout() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();

  return (
    <main className="app">
      <section className="app-shell">
        <header className="top-bar">
          <NavLink className="brand" to="/">
            Full House Club
          </NavLink>

          <nav className="nav">
            <NavLink to="/schedule">Расписание</NavLink>
            <NavLink to="/ratings">Рейтинг</NavLink>

            {isAuthenticated && !isAdmin && <NavLink to="/cabinet">Кабинет</NavLink>}

            {isAdmin && <NavLink to="/admin">Админ-панель</NavLink>}
          </nav>

          <div className="user-area">
            {isAuthenticated && user ? (
              <>
                <span>{user.firstName}</span>
                <button className="link-button" type="button" onClick={logout}>
                  Выйти
                </button>
              </>
            ) : (
              <NavLink className="button small" to="/login">
                Войти
              </NavLink>
            )}
          </div>
        </header>

        <Outlet />
      </section>
    </main>
  );
}