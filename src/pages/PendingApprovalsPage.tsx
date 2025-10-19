import { useCallback, useEffect, useMemo, useState } from 'react';
import useLiveStatus from '../hooks/useLiveStatus';
import {
  approveInstruction,
  getPendingInstructions
} from '../services/api';
import { InstructionResponse } from '../types';

const statusColors: Record<InstructionResponse['status'], string> = {
  pending: 'status-pending',
  approved: 'status-approved',
  rejected: 'status-rejected',
  in_progress: 'status-approved'
};

const PendingApprovalsPage = () => {
  const [instructions, setInstructions] = useState<InstructionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPending = useCallback(async () => {
    try {
      setIsLoading(true);
      const pending = await getPendingInstructions();
      setInstructions(pending);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch pending instructions', err);
      setError('Unable to load pending instructions.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPending();
  }, [fetchPending]);

  const handleAction = async (instructionId: string, approve: boolean) => {
    try {
      await approveInstruction(instructionId, approve);
      setInstructions((current) =>
        current.map((instruction) =>
          instruction.id === instructionId
            ? { ...instruction, status: approve ? 'approved' : 'rejected' }
            : instruction
        )
      );
    } catch (err) {
      console.error('Failed to update instruction', err);
      setError('Failed to update instruction status.');
    }
  };

  const onMessage = useCallback((update: InstructionResponse) => {
    setInstructions((current) => {
      const existing = current.find((item) => item.id === update.id);
      if (existing) {
        return current.map((item) => (item.id === update.id ? update : item));
      }
      return [update, ...current];
    });
  }, []);

  useLiveStatus({
    enabled: true,
    onMessage,
    poller: fetchPending
  });

  const grouped = useMemo(() => {
    return instructions.reduce(
      (acc, instruction) => {
        acc[instruction.status] = [...acc[instruction.status], instruction];
        return acc;
      },
      {
        pending: [] as InstructionResponse[],
        approved: [] as InstructionResponse[],
        rejected: [] as InstructionResponse[],
        in_progress: [] as InstructionResponse[]
      }
    );
  }, [instructions]);

  return (
    <section>
      <h2 className="section-title">Pending approvals</h2>
      <p style={{ marginBottom: '1.5rem', color: '#475569' }}>
        Approve or reject incoming instructions. Updates arrive live via WebSocket with polling
        fallback, ensuring reviewers stay in sync even if the socket connection drops.
      </p>

      {error && (
        <div className="card" style={{ border: '1px solid #fca5a5', color: '#b91c1c' }}>
          {error}
        </div>
      )}

      {isLoading && <div className="card">Loading pending instructions…</div>}

      {(['pending', 'in_progress', 'approved', 'rejected'] as const).map((status) => (
        <div className="card" key={status}>
          <h3 style={{ textTransform: 'capitalize' }}>{status.replace('_', ' ')}</h3>
          {grouped[status].length === 0 ? (
            <p style={{ color: '#64748b' }}>No instructions in this state.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Instruction</th>
                  <th>Channel</th>
                  <th>Priority</th>
                  <th>Requested by</th>
                  <th>Updated</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {grouped[status].map((instruction) => (
                  <tr key={instruction.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{instruction.title}</div>
                      <div style={{ fontSize: '0.9rem', color: '#475569' }}>{instruction.description}</div>
                    </td>
                    <td>{instruction.channel}</td>
                    <td>
                      <span className="badge" style={{ textTransform: 'capitalize' }}>
                        {instruction.priority}
                      </span>
                    </td>
                    <td>{instruction.requestedBy}</td>
                    <td>{new Date(instruction.updatedAt).toLocaleString()}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                        <span className={`status-dot ${statusColors[instruction.status]}`} />
                        {status === 'pending' || status === 'in_progress' ? (
                          <>
                            <button
                              className="primary"
                              type="button"
                              onClick={() => handleAction(instruction.id, true)}
                            >
                              Approve
                            </button>
                            <button
                              className="primary"
                              type="button"
                              style={{ background: '#ef4444' }}
                              onClick={() => handleAction(instruction.id, false)}
                            >
                              Reject
                            </button>
                          </>
                        ) : (
                          <span style={{ color: '#475569' }}>No actions available</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ))}
    </section>
  );
};

export default PendingApprovalsPage;
