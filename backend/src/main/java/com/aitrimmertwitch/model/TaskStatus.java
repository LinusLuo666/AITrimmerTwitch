package com.aitrimmertwitch.model;

/**
 * 描述任务所处的生命周期状态。
 */
public enum TaskStatus {
	PENDING_APPROVAL,
	APPROVED,
	RUNNING,
	PAUSED,
	CANCELLED,
	COMPLETED,
	FAILED
}
