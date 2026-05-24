import { useState } from 'react';
import './App.css';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';

type AuthView = 'login' | 'register';

function App() {
  const [authView, setAuthView] = useState<AuthView>('login');

  return (
    <main className="app">
      <section className="app-shell">
        <header className="app-header">
          <div>
            <h1>Poker Club</h1>
            <p>Система управления турнирами спортивного poker-клуба</p>
          </div>
        </header>

        {authView === 'login' ? (
          <LoginPage onSwitchToRegister={() => setAuthView('register')} />
        ) : (
          <RegisterPage onSwitchToLogin={() => setAuthView('login')} />
        )}
      </section>
    </main>
  );
}

export default App;