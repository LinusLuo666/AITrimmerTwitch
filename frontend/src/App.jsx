import { useEffect, useState } from 'react';

function App() {
  const [tasks, setTasks] = useState([]);
  const [config, setConfig] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function loadData() {
      try {
        const [taskResponse, configResponse] = await Promise.all([
          fetch('/api/tasks'),
          fetch('/api/config')
        ]);

        if (!taskResponse.ok || !configResponse.ok) {
          throw new Error('Failed to fetch backend data');
        }

        const [taskData, configData] = await Promise.all([
          taskResponse.json(),
          configResponse.json()
        ]);

        setTasks(taskData);
        setConfig(configData);
      } catch (err) {
        setError(err.message);
      }
    }

    loadData();
  }, []);

  return (
    <div className="app">
      <header>
        <h1>AI Trimmer Control Panel</h1>
        <p>Baseline integration between the Spring Boot backend and Vite frontend.</p>
      </header>
      {error && <p className="error">{error}</p>}
      <section>
        <h2>Runtime Configuration</h2>
        {config ? (
          <ul>
            <li><strong>Default language:</strong> {config.defaultLanguage}</li>
            <li><strong>Max concurrent tasks:</strong> {config.maxConcurrentTasks}</li>
            <li><strong>Feature flags enabled:</strong> {config.featureFlagsEnabled ? 'Yes' : 'No'}</li>
          </ul>
        ) : (
          <p>Loading configuration...</p>
        )}
      </section>
      <section>
        <h2>Recent Tasks</h2>
        {tasks.length ? (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task) => (
                <tr key={task.id}>
                  <td>{task.id}</td>
                  <td>{task.name}</td>
                  <td>{task.status}</td>
                  <td>{new Date(task.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>No tasks available.</p>
        )}
      </section>
    </div>
  );
}

export default App;
