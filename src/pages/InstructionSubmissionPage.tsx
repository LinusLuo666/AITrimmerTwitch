import { FormEvent, useMemo, useState } from 'react';
import { submitInstruction } from '../services/api';
import { InstructionRequest } from '../types';

const createDefaultForm = (): InstructionRequest => ({
  title: '',
  description: '',
  priority: 'medium',
  channel: '',
  tags: []
});

const InstructionSubmissionPage = () => {
  const [form, setForm] = useState<InstructionRequest>(createDefaultForm);
  const [tagInput, setTagInput] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const isSubmitDisabled = useMemo(() => {
    return !form.title || !form.description || !form.channel || isSubmitting;
  }, [form, isSubmitting]);

  const addTag = () => {
    const trimmed = tagInput.trim();
    if (trimmed && !form.tags.includes(trimmed)) {
      setForm((current) => ({ ...current, tags: [...current.tags, trimmed] }));
    }
    setTagInput('');
  };

  const removeTag = (tag: string) => {
    setForm((current) => ({ ...current, tags: current.tags.filter((t) => t !== tag) }));
  };

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      setIsSubmitting(true);
      setStatusMessage(null);
      await submitInstruction(form);
      setStatusMessage('Instruction submitted successfully');
      setForm(createDefaultForm());
    } catch (error) {
      console.error('Failed to submit instruction', error);
      setStatusMessage('Failed to submit instruction. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section>
      <h2 className="section-title">Instruction Submission</h2>
      <p style={{ marginBottom: '1.5rem', color: '#475569' }}>
        Provide detailed guidance for the Twitch trimmer agent. Include any context, target segments,
        or content policies the AI should follow when preparing highlight clips.
      </p>

      <form className="card" onSubmit={onSubmit}>
        <div className="form-grid">
          <div>
            <label htmlFor="title">Instruction title</label>
            <input
              id="title"
              name="title"
              placeholder="Example: Capture the best hype moments from the last stream"
              value={form.title}
              onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))}
              required
            />
          </div>
          <div>
            <label htmlFor="channel">Channel</label>
            <input
              id="channel"
              name="channel"
              placeholder="twitch.tv/yourchannel"
              value={form.channel}
              onChange={(event) => setForm((current) => ({ ...current, channel: event.target.value }))}
              required
            />
          </div>
          <div>
            <label htmlFor="priority">Priority</label>
            <select
              id="priority"
              name="priority"
              value={form.priority}
              onChange={(event) =>
                setForm((current) => ({ ...current, priority: event.target.value as InstructionRequest['priority'] }))
              }
            >
              <option value="low">Low</option>
              <option value="medium">Medium</option>
              <option value="high">High</option>
            </select>
          </div>
        </div>

        <label htmlFor="description">Detailed instructions</label>
        <textarea
          id="description"
          name="description"
          rows={6}
          placeholder="Describe what the AI should look for, the tone to maintain, and any mandatory inclusions."
          value={form.description}
          onChange={(event) =>
            setForm((current) => ({ ...current, description: event.target.value }))
          }
        />

        <label htmlFor="tags">Tags</label>
        <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
          <input
            id="tags"
            name="tags"
            placeholder="Add context tags (press Add to confirm)"
            value={tagInput}
            onChange={(event) => setTagInput(event.target.value)}
          />
          <button
            className="primary"
            type="button"
            onClick={addTag}
            disabled={!tagInput.trim()}
            style={{ whiteSpace: 'nowrap' }}
          >
            Add tag
          </button>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
          {form.tags.map((tag) => (
            <span key={tag} className="badge" style={{ backgroundColor: '#e2e8f0' }}>
              {tag}
              <button
                type="button"
                onClick={() => removeTag(tag)}
                style={{
                  marginLeft: '0.5rem',
                  background: 'none',
                  border: 'none',
                  color: '#ef4444',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
                aria-label={`Remove tag ${tag}`}
              >
                ×
              </button>
            </span>
          ))}
        </div>

        <button className="primary" type="submit" disabled={isSubmitDisabled}>
          {isSubmitting ? 'Submitting…' : 'Submit instruction'}
        </button>
        {statusMessage && (
          <p style={{ marginTop: '1rem', color: statusMessage.startsWith('Failed') ? '#dc2626' : '#16a34a' }}>
            {statusMessage}
          </p>
        )}
      </form>
    </section>
  );
};

export default InstructionSubmissionPage;
