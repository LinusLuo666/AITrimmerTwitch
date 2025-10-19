import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

const App = (): JSX.Element => (
  <main style={{ fontFamily: 'system-ui, sans-serif', padding: '2rem' }}>
    <h1>AI Trimmer Twitch</h1>
    <p>
      Frontend build tooling is configured. Run <code>npm install</code> followed by
      <code>npm run build</code> to produce static assets under <code>dist/</code>.
    </p>
  </main>
);

const container = document.getElementById('root');
if (!container) {
  throw new Error('Failed to find the root element');
}

createRoot(container).render(
  <StrictMode>
    <App />
  </StrictMode>
);
