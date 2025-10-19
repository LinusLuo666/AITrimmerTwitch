import type { FormEvent } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { api, type TaskStatusAction } from './api';
import type {
  CreateTaskPayload,
  GeneralConfig,
  Quality,
  Task,
  TaskSegment,
  TaskStatus,
} from './types';
import './App.css';

const statusLabels: Record<TaskStatus, string> = {
  PENDING_APPROVAL: '等待审核',
  APPROVED: '已批准',
  RUNNING: '执行中',
  PAUSED: '已暂停',
  CANCELLED: '已取消',
  COMPLETED: '已完成',
  FAILED: '执行失败',
};

const statusActions: Array<{
  action: TaskStatusAction;
  label: string;
  color: 'primary' | 'warning' | 'danger' | 'neutral';
  available: (status: TaskStatus) => boolean;
}> = [
  {
    action: 'approve',
    label: '批准',
    color: 'primary',
    available: (status) => status === 'PENDING_APPROVAL',
  },
  {
    action: 'running',
    label: '标记执行',
    color: 'primary',
    available: (status) => status === 'APPROVED' || status === 'PAUSED',
  },
  {
    action: 'pause',
    label: '暂停',
    color: 'warning',
    available: (status) => status === 'RUNNING',
  },
  {
    action: 'completed',
    label: '完成',
    color: 'neutral',
    available: (status) => status === 'RUNNING' || status === 'APPROVED',
  },
  {
    action: 'failed',
    label: '标记失败',
    color: 'danger',
    available: (status) => status === 'RUNNING',
  },
  {
    action: 'cancel',
    label: '取消',
    color: 'danger',
    available: (status) =>
      status === 'PENDING_APPROVAL' ||
      status === 'APPROVED' ||
      status === 'PAUSED',
  },
];

const initialSegment: TaskSegment = { start: '00:00:00', end: '00:00:30' };

type ChatMessage = {
  role: 'user' | 'assistant';
  content: string;
  task?: Task | null;
  timestamp: string;
};

function App() {
  const [qualities, setQualities] = useState<Quality[]>([]);
  const [generalConfig, setGeneralConfig] = useState<GeneralConfig | null>(
    null,
  );
  const [tasks, setTasks] = useState<Task[]>([]);
  const [segments, setSegments] = useState<TaskSegment[]>([initialSegment]);
  const [videoName, setVideoName] = useState('');
  const [quality, setQuality] = useState('');
  const [autoApprove, setAutoApprove] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>(() => [
    {
      role: 'assistant',
      content:
        '请按照“视频=demo.mp4; 片段=00:00:05-00:00:30; 画质=medium; 自动批准=true”的格式发送指令，我会帮你生成裁剪任务。',
      timestamp: new Date().toISOString(),
    },
  ]);

  useEffect(() => {
    void bootstrap();
  }, []);

  const defaultQuality = useMemo(
    () => quality || qualities.at(0)?.name || '',
    [quality, qualities],
  );

  async function bootstrap() {
    try {
      const [qualitiesData, configData, tasksData] = await Promise.all([
        api.getQualities(),
        api.getGeneralConfig(),
        api.getTasks(),
      ]);
      setQualities(qualitiesData);
      setGeneralConfig(configData);
      setTasks(tasksData);
      if (!quality && qualitiesData.length > 0) {
        setQuality(qualitiesData[0].name);
      }
    } catch (err) {
      handleError(err);
    }
  }

  function updateSegment(
    index: number,
    field: keyof TaskSegment,
    value: string,
  ) {
    setSegments((prev) =>
      prev.map((segment, idx) =>
        idx === index ? { ...segment, [field]: value } : segment,
      ),
    );
  }

  function addSegment() {
    setSegments((prev) => [...prev, { start: '00:00:00', end: '00:00:30' }]);
  }

  function removeSegment(index: number) {
    setSegments((prev) => prev.filter((_, idx) => idx !== index));
  }

  async function submitTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setMessage(null);
    setError(null);

    const payload: CreateTaskPayload = {
      videoName,
      quality: defaultQuality,
      autoApprove,
      segments,
    };

    try {
      const created = await api.createTask(payload);
      setTasks((prev) => [created, ...prev.filter((task) => task.id !== created.id)]);
      setMessage(`任务创建成功：${created.id}`);
    } catch (err) {
      handleError(err);
    } finally {
      setLoading(false);
    }
  }

  async function changeStatus(id: string, action: TaskStatusAction) {
    setLoading(true);
    setMessage(null);
    setError(null);
    try {
      const updated = await api.updateTaskStatus(id, action);
      setTasks((prev) =>
        prev.map((task) => (task.id === id ? updated : task)),
      );
      setMessage(`状态已更新为：${statusLabels[updated.status]}`);
    } catch (err) {
      handleError(err);
    } finally {
      setLoading(false);
    }
  }

  async function handleChatSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const content = chatInput.trim();
    if (!content) {
      return;
    }

    const userMessage: ChatMessage = {
      role: 'user',
      content,
      timestamp: new Date().toISOString(),
    };
    setChatMessages((prev) => [...prev, userMessage]);
    setChatInput('');
    setChatLoading(true);

    try {
      const response = await api.sendChatMessage(content);
      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: response.reply,
        task: response.task ?? null,
        timestamp: new Date().toISOString(),
      };
      setChatMessages((prev) => [...prev, assistantMessage]);
      if (response.task) {
        const task = response.task;
        setTasks((prev) => [
          task,
          ...prev.filter((item) => item.id !== task.id),
        ]);
      }
    } catch (err) {
      const message =
        err instanceof Error ? err.message : '发生未知错误，请稍后再试。';
      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: message,
        timestamp: new Date().toISOString(),
      };
      setChatMessages((prev) => [...prev, assistantMessage]);
    } finally {
      setChatLoading(false);
    }
  }

  function handleError(err: unknown) {
    if (err instanceof Error) {
      setError(err.message);
    } else {
      setError('发生未知错误，请稍后再试。');
    }
  }

  function resetForm() {
    setVideoName('');
    setSegments([initialSegment]);
    setAutoApprove(false);
    if (qualities.length > 0) {
      setQuality(qualities[0].name);
    }
  }

  return (
    <div className="app">
      <header className="app__header">
        <h1>AITrimmerTwitch 任务控制台</h1>
        <p>创建、审核并执行 AI 裁剪任务。</p>
      </header>

      <section className="card chat">
        <div className="chat__header">
          <h2>聊天指令</h2>
          <p>使用约定格式描述需求，我会尝试自动生成裁剪任务。</p>
        </div>
        <div className="chat__hint">
          示例：<code>视频=highlight.mp4; 片段=00:00:05-00:00:30,00:01:00-00:02:00; 画质=high; 自动批准=true</code>
        </div>
        <div className="chat__messages">
          {chatMessages.map((item, index) => (
            <div
              key={`${item.timestamp}-${index}`}
              className={`chat-bubble chat-bubble--${item.role}`}
            >
              <div className="chat-bubble__meta">
                <span>{item.role === 'user' ? '你' : 'AI'}</span>
                <span>{formatTime(item.timestamp)}</span>
              </div>
              <p>{item.content}</p>
              {item.task && (
                <div className="chat-bubble__task">
                  <span>任务 ID：{item.task.id}</span>
                  <span>视频：{item.task.videoName}</span>
                  <span>画质：{item.task.quality}</span>
                </div>
              )}
            </div>
          ))}
        </div>
        <form className="chat__form" onSubmit={handleChatSubmit}>
          <input
            type="text"
            value={chatInput}
            onChange={(event) => setChatInput(event.target.value)}
            placeholder="例如：视频=demo.mp4; 片段=00:00:05-00:00:30; 画质=medium"
            disabled={chatLoading}
          />
          <button
            type="submit"
            className="button button--primary"
            disabled={chatLoading || !chatInput.trim()}
          >
            {chatLoading ? '发送中...' : '发送'}
          </button>
        </form>
      </section>

      {generalConfig && (
        <section className="card">
          <h2>当前系统配置</h2>
          <div className="config-grid">
            <div>
              <span className="config-label">工作目录</span>
              <code>{generalConfig.workspacePath}</code>
            </div>
            <div>
              <span className="config-label">FFmpeg 路径</span>
              <code>{generalConfig.ffmpegBinary}</code>
            </div>
            <div>
              <span className="config-label">输出前缀</span>
              <code>{generalConfig.outputPrefix}</code>
            </div>
            <div>
              <span className="config-label">禁止重复编辑</span>
              <code>{generalConfig.lockEditedOutputs ? '是' : '否'}</code>
            </div>
          </div>
        </section>
      )}

      <section className="card">
        <div className="card__header">
          <h2>创建新任务</h2>
          <button
            type="button"
            className="button button--ghost"
            onClick={resetForm}
          >
            重置表单
          </button>
        </div>
        <form className="task-form" onSubmit={submitTask}>
          <label className="form-field">
            <span>视频名称（位于工作目录）</span>
            <input
              type="text"
              required
              value={videoName}
              onChange={(event) => setVideoName(event.target.value)}
              placeholder="example.mp4"
            />
          </label>

          <label className="form-field">
            <span>画质档位</span>
            <select
              value={defaultQuality}
              onChange={(event) => setQuality(event.target.value)}
            >
              {qualities.map((item) => (
                <option key={item.name} value={item.name}>
                  {item.name}（{item.videoBitrate} / {item.audioBitrate}）
                </option>
              ))}
            </select>
          </label>

          <label className="form-checkbox">
            <input
              type="checkbox"
              checked={autoApprove}
              onChange={(event) => setAutoApprove(event.target.checked)}
            />
            创建后自动标记为已批准
          </label>

          <div>
            <div className="segments-header">
              <span>时间片段（HH:mm:ss 或 mm:ss）</span>
              <button
                type="button"
                className="button button--ghost"
                onClick={addSegment}
              >
                新增片段
              </button>
            </div>
            <div className="segments-list">
              {segments.map((segment, index) => (
                <div key={index} className="segment-row">
                  <input
                    type="text"
                    required
                    value={segment.start}
                    onChange={(event) =>
                      updateSegment(index, 'start', event.target.value)
                    }
                    placeholder="开始 00:00:00"
                  />
                  <input
                    type="text"
                    required
                    value={segment.end}
                    onChange={(event) =>
                      updateSegment(index, 'end', event.target.value)
                    }
                    placeholder="结束 00:00:30"
                  />
                  {segments.length > 1 && (
                    <button
                      type="button"
                      className="button button--ghost"
                      onClick={() => removeSegment(index)}
                    >
                      移除
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          <button
            type="submit"
            className="button button--primary"
            disabled={loading}
          >
            {loading ? '处理中...' : '提交任务'}
          </button>
        </form>

        {message && <p className="feedback feedback--success">{message}</p>}
        {error && <p className="feedback feedback--error">{error}</p>}
      </section>

      <section className="card">
        <div className="card__header">
          <h2>任务列表</h2>
          <button
            type="button"
            className="button button--ghost"
            onClick={() => void bootstrap()}
          >
            重新加载
          </button>
        </div>
        {tasks.length === 0 ? (
          <p>尚未创建任务。</p>
        ) : (
          <div className="tasks">
            {tasks.map((task) => (
              <article key={task.id} className="task-card">
                <header className="task-card__header">
                  <div>
                    <h3>{task.videoName}</h3>
                    <span className={`status status--${task.status.toLowerCase()}`}>
                      {statusLabels[task.status]}
                    </span>
                  </div>
                  <span className="task-card__meta">
                    创建于 {formatDate(task.createdAt)}
                  </span>
                </header>
                <div className="task-card__body">
                  <div className="task-info">
                    <div>
                      <span className="info-label">画质</span>
                      <code>{task.quality}</code>
                    </div>
                    <div>
                      <span className="info-label">输出文件名</span>
                      <code>{task.outputFileName}</code>
                    </div>
                    <div>
                      <span className="info-label">片段</span>
                      <ul>
                        {task.segments.map((segment, index) => (
                          <li key={index}>
                            {segment.start} → {segment.end}
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                  <div className="task-actions">
                    {statusActions
                      .filter(({ available }) => available(task.status))
                      .map(({ action, label, color }) => (
                        <button
                          key={action}
                          type="button"
                          className={`button button--${color}`}
                          disabled={loading}
                          onClick={() => void changeStatus(task.id, action)}
                        >
                          {label}
                        </button>
                      ))}
                  </div>
                </div>
                <div className="task-card__commands">
                  <span>FFmpeg 命令预览</span>
                  <pre>
                    <code>{task.executionPreview.join('\n\n')}</code>
                  </pre>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(new Date(value));
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value));
}

export default App;
