export type TaskStatus =
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'RUNNING'
  | 'PAUSED'
  | 'CANCELLED'
  | 'COMPLETED'
  | 'FAILED';

export interface TaskSegment {
  start: string;
  end: string;
}

export interface Task {
  id: string;
  videoName: string;
  status: TaskStatus;
  segments: TaskSegment[];
  quality: string;
  outputFileName: string;
  executionPreview: string[];
  createdAt: string;
  updatedAt: string;
}

export interface Quality {
  name: string;
  videoBitrate: string;
  audioBitrate: string;
  preset: string;
  crf: number | null;
}

export interface GeneralConfig {
  workspacePath: string;
  ffmpegBinary: string;
  outputPrefix: string;
  lockEditedOutputs: boolean;
}

export interface CreateTaskPayload {
  videoName: string;
  quality: string;
  autoApprove: boolean;
  segments: TaskSegment[];
}
