export type InstructionRequest = {
  title: string;
  description: string;
  priority: 'low' | 'medium' | 'high';
  channel: string;
  tags: string[];
};

export type InstructionResponse = {
  id: string;
  title: string;
  description: string;
  priority: 'low' | 'medium' | 'high';
  channel: string;
  tags: string[];
  status: 'pending' | 'approved' | 'rejected' | 'in_progress';
  createdAt: string;
  updatedAt: string;
  requestedBy: string;
  reviewedBy?: string;
  rejectionReason?: string;
};

export type TaskHistoryEntry = {
  id: string;
  instructionId: string;
  clipUrl: string;
  outcome: 'success' | 'failed' | 'cancelled';
  durationSeconds: number;
  processedAt: string;
};
