import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

type RouteGuardProps = {
  children: React.ReactNode;
};

export function RequireAuth({ children }: RouteGuardProps) {
  const { isLoading, isAuthenticated } = useAuth();

  if (isLoading) {
    return <div className="state-message">Проверка авторизации...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export function RequireAdmin({ children }: RouteGuardProps) {
  const { isLoading, isAuthenticated, isAdmin } = useAuth();

  if (isLoading) {
    return <div className="state-message">Проверка авторизации...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export function RequireVisitor({ children }: RouteGuardProps) {
  const { isLoading, isAuthenticated, isAdmin } = useAuth();

  if (isLoading) {
    return <div className="state-message">Проверка авторизации...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (isAdmin) {
    return <Navigate to="/admin" replace />;
  }

  return children;
}