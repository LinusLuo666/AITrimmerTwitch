import { useEffect, useMemo, useState } from 'react';
import { getTaskHistory } from '../services/api';
import { TaskHistoryEntry } from '../types';

const TaskHistoryPage = () => {
  const [history, setHistory] = useState<TaskHistoryEntry[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState({
    outcome: 'all',
    search: ''
  });

  useEffect(() => {
    const loadHistory = async () => {
      try {
        setIsLoading(true);
        const data = await getTaskHistory();
        setHistory(data);
        setError(null);
      } catch (err) {
        console.error('Failed to fetch task history', err);
        setError('Unable to load task history.');
      } finally {
        setIsLoading(false);
      }
    };

    loadHistory();
  }, []);

  const filtered = useMemo(() => {
    return history.filter((entry) => {
      const matchesOutcome = filters.outcome === 'all' || entry.outcome === filters.outcome;
      const matchesSearch =
        filters.search.trim().length === 0 ||
        entry.instructionId.toLowerCase().includes(filters.search.toLowerCase());
      return matchesOutcome && matchesSearch;
    });
  }, [filters, history]);

  const averageDuration = useMemo(() => {
    if (!filtered.length) {
      return 0;
    }
    const total = filtered.reduce((sum, entry) => sum + entry.durationSeconds, 0);
    return Math.round(total / filtered.length);
  }, [filtered]);

  return (
    <section>
      <h2 className="section-title">Task history</h2>
      <p style={{ marginBottom: '1.5rem', color: '#475569' }}>
        Review processed instructions and their resulting clips. Filter by outcome or search by
        instruction identifier to audit AI trimming performance.
      </p>

      <div className="card">
        <div className="form-grid">
          <div>
            <label htmlFor="history-outcome">Outcome</label>
            <select
              id="history-outcome"
              value={filters.outcome}
              onChange={(event) => setFilters((current) => ({ ...current, outcome: event.target.value }))}
            >
              <option value="all">All</option>
              <option value="success">Success</option>
              <option value="failed">Failed</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
          <div>
            <label htmlFor="history-search">Search by instruction ID</label>
            <input
              id="history-search"
              placeholder="e.g. instr_12345"
              value={filters.search}
              onChange={(event) => setFilters((current) => ({ ...current, search: event.target.value }))}
            />
          </div>
          <div>
            <label>Average processing duration</label>
            <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>{averageDuration} seconds</div>
          </div>
        </div>
      </div>

      {error && (
        <div className="card" style={{ border: '1px solid #fca5a5', color: '#b91c1c' }}>
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="card">Loading task history…</div>
      ) : (
        <div className="card">
          {filtered.length === 0 ? (
            <p style={{ color: '#64748b' }}>No records match the current filters.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Instruction ID</th>
                  <th>Clip URL</th>
                  <th>Outcome</th>
                  <th>Duration (s)</th>
                  <th>Processed at</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((entry) => (
                  <tr key={entry.id}>
                    <td>{entry.instructionId}</td>
                    <td>
                      <a href={entry.clipUrl} target="_blank" rel="noreferrer">
                        Open clip
                      </a>
                    </td>
                    <td>
                      <span className="badge" style={{ textTransform: 'capitalize' }}>
                        {entry.outcome}
                      </span>
                    </td>
                    <td>{entry.durationSeconds}</td>
                    <td>{new Date(entry.processedAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </section>
  );
};

export default TaskHistoryPage;
