import { FormEvent, useEffect, useState } from 'react';
import { getSettings, updateSettings } from '../services/api';

type SettingsState = {
  autoApproveThreshold: number;
  allowManualOverrides: boolean;
  notifications: boolean;
};

const defaultSettings: SettingsState = {
  autoApproveThreshold: 80,
  allowManualOverrides: true,
  notifications: true
};

const SettingsPage = () => {
  const [settings, setSettings] = useState<SettingsState>(defaultSettings);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const loadSettings = async () => {
      try {
        const data = await getSettings();
        setSettings(data);
      } catch (error) {
        console.error('Failed to load settings', error);
        setStatusMessage('Failed to load settings. Using defaults.');
      }
    };

    loadSettings();
  }, []);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      setIsLoading(true);
      setStatusMessage(null);
      await updateSettings(settings);
      setStatusMessage('Settings saved successfully.');
    } catch (error) {
      console.error('Failed to update settings', error);
      setStatusMessage('Unable to save settings, please retry.');
    } finally {
      setIsLoading(false);
    }
  };

  const toggle = (key: keyof SettingsState) => {
    setSettings((current) => ({ ...current, [key]: !current[key] }));
  };

  return (
    <section>
      <h2 className="section-title">Administration settings</h2>
      <p style={{ marginBottom: '1.5rem', color: '#475569' }}>
        Configure automation policies, reviewer overrides, and security safeguards for the AI
        trimming pipeline.
      </p>

      <form className="card" onSubmit={onSubmit}>
        <div className="settings-group">
          <h3>Automation</h3>
          <label htmlFor="threshold">Auto-approve confidence threshold</label>
          <input
            id="threshold"
            type="number"
            min={0}
            max={100}
            value={settings.autoApproveThreshold}
            onChange={(event) =>
              setSettings((current) => ({ ...current, autoApproveThreshold: Number(event.target.value) }))
            }
          />
        </div>

        <div className="settings-group">
          <h3>Manual controls</h3>
          <div className="settings-option">
            <span>Allow manual overrides</span>
            <button
              type="button"
              onClick={() => toggle('allowManualOverrides')}
              className={`toggle-switch ${settings.allowManualOverrides ? 'active' : ''}`}
            >
              <span className="sr-only">Toggle manual overrides</span>
            </button>
          </div>
          <div className="settings-option">
            <span>Send notifications to reviewers</span>
            <button
              type="button"
              onClick={() => toggle('notifications')}
              className={`toggle-switch ${settings.notifications ? 'active' : ''}`}
            >
              <span className="sr-only">Toggle reviewer notifications</span>
            </button>
          </div>
        </div>

        <button className="primary" type="submit" disabled={isLoading}>
          {isLoading ? 'Saving…' : 'Save changes'}
        </button>
        {statusMessage && (
          <p style={{ marginTop: '1rem', color: statusMessage.includes('Unable') ? '#dc2626' : '#16a34a' }}>
            {statusMessage}
          </p>
        )}
      </form>
    </section>
  );
};

export default SettingsPage;
