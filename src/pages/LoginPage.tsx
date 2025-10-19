import type { Location } from 'react-router-dom';
import { FormEvent, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

type LocationState = {
  from?: Location;
};

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | undefined;

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      setIsSubmitting(true);
      setError(null);
      await login(username, password);
      navigate(state?.from?.pathname ?? '/submit', { replace: true });
    } catch (err) {
      console.error('Failed to authenticate', err);
      setError('Invalid credentials or server error.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section>
      <h2 className="section-title">Sign in</h2>
      <p style={{ marginBottom: '1.5rem', color: '#475569' }}>
        Sign in to manage AI trimming instructions. Roles control which pages are accessible.
      </p>
      <form className="card" onSubmit={onSubmit}>
        <div>
          <label htmlFor="username">Username</label>
          <input
            id="username"
            autoComplete="username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </div>
        <button className="primary" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
        {error && (
          <p style={{ marginTop: '1rem', color: '#dc2626' }}>
            {error}
          </p>
        )}
      </form>
    </section>
  );
};

export default LoginPage;
