package com.aitrimmertwitch.model;

/**
 * 描述任務目前所處的生命週期狀態。
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
