import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import './App.css';
import { AppLayout } from './components/AppLayout';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { SchedulePage } from './pages/SchedulePage';
import { CabinetPage } from './pages/CabinetPage';
import { RatingsPage } from './pages/RatingsPage';
import { AdminPanelPage } from './pages/AdminPanelPage';
import { RequireAdmin, RequireVisitor } from './components/RouteGuards';

function PlaceholderPage({ title }: { title: string }) {
  return (
    <section className="content-section">
      <div className="state-message">{title} будет добавлен следующим шагом.</div>
    </section>
  );
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'schedule',
        element: <SchedulePage />,
      },
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'register',
        element: <RegisterPage />,
      },
      {
        path: 'ratings',
        element: <RatingsPage />,
      },
      {
        path: 'cabinet',
        element: (
          <RequireVisitor>
            <CabinetPage />
          </RequireVisitor>
        ),
      },
      {
        path: 'admin',
        element: (
          <RequireAdmin>
            <AdminPanelPage />
          </RequireAdmin>
        ),
      }
    ],
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;