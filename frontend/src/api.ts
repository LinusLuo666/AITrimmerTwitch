import type {
  ChatResponse,
  CreateTaskPayload,
  GeneralConfig,
  Quality,
  Task,
} from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function request<T>(input: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${input}`, {
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
    ...init,
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body = await response.json();
      if (typeof body.message === 'string') {
        message = body.message;
      }
    } catch {
      // 忽略解析错误，保留原始信息
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const api = {
  getQualities: () => request<Quality[]>('/api/config/qualities'),
  getGeneralConfig: () => request<GeneralConfig>('/api/config/general'),
  getTasks: () => request<Task[]>('/api/tasks'),
  createTask: (payload: CreateTaskPayload) =>
    request<Task>('/api/tasks', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  updateTaskStatus: (id: string, action: TaskStatusAction) =>
    request<Task>(`/api/tasks/${id}/${action}`, { method: 'POST' }),
  sendChatMessage: (message: string) =>
    request<ChatResponse>('/api/chat', {
      method: 'POST',
      body: JSON.stringify({ message }),
    }),
};

export type TaskStatusAction =
  | 'approve'
  | 'pause'
  | 'cancel'
  | 'running'
  | 'completed'
  | 'failed';
