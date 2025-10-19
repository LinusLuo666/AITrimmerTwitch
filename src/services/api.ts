import axios from 'axios';
import { InstructionRequest, InstructionResponse, TaskHistoryEntry } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api'
});

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem('aitrimmer-auth');
  if (stored) {
    const { token } = JSON.parse(stored) as { token: string };
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

type AuthenticatePayload = {
  username: string;
  password: string;
};

type AuthenticateResponse = {
  id: string;
  name: string;
  role: 'creator' | 'approver' | 'admin';
  token: string;
};

export const authenticate = async (payload: AuthenticatePayload) => {
  const { data } = await api.post<AuthenticateResponse>('/auth/login', payload);
  return data;
};

export const submitInstruction = async (payload: InstructionRequest) => {
  const { data } = await api.post<InstructionResponse>('/instructions', payload);
  return data;
};

export const getPendingInstructions = async () => {
  const { data } = await api.get<InstructionResponse[]>('/instructions/pending');
  return data;
};

export const approveInstruction = async (instructionId: string, approve: boolean) => {
  const { data } = await api.post<InstructionResponse>(
    `/instructions/${instructionId}/${approve ? 'approve' : 'reject'}`
  );
  return data;
};

export const getTaskHistory = async () => {
  const { data } = await api.get<TaskHistoryEntry[]>('/tasks/history');
  return data;
};

type SettingsPayload = {
  autoApproveThreshold: number;
  allowManualOverrides: boolean;
  notifications: boolean;
};

export const updateSettings = async (payload: SettingsPayload) => {
  const { data } = await api.put('/settings', payload);
  return data;
};

export const getSettings = async () => {
  const { data } = await api.get<SettingsPayload>('/settings');
  return data;
};

export default api;
