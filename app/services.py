from __future__ import annotations

from datetime import datetime
from typing import Iterable
from uuid import UUID

from .models import CommandPreview, FFmpegInstruction, Task, TaskStatus
from .repositories import InMemoryTaskRepository, TaskNotFoundError, TaskRepository


class InvalidTaskStateError(RuntimeError):
    """Raised when attempting an unsupported state transition."""


class FFmpegTaskService:
    """Coordinates command generation and human approval for FFmpeg tasks."""

    def __init__(self, repository: TaskRepository | None = None) -> None:
        self._repository = repository or InMemoryTaskRepository()

    @property
    def repository(self) -> TaskRepository:
        return self._repository

    def create_task(self, instruction: FFmpegInstruction) -> Task:
        command_preview = self._build_command_preview(instruction)
        task = Task.create(instruction, command_preview)
        self._repository.add(task)
        return task

    def list_pending_tasks(self) -> Iterable[Task]:
        return self._repository.list_by_status(TaskStatus.PENDING_APPROVAL)

    def list_history(self) -> Iterable[Task]:
        return self._repository.list_all()

    def approve_task(self, task_id: UUID) -> Task:
        task = self._repository.get(task_id)
        if task.status is not TaskStatus.PENDING_APPROVAL:
            raise InvalidTaskStateError("Only pending tasks can be approved")

        now = datetime.utcnow()
        approved = task.update_status(status=TaskStatus.APPROVED, approved_at=now)
        self._repository.update(approved)

        executed = self._execute_task(approved)
        self._repository.update(executed)
        return executed

    def cancel_task(self, task_id: UUID) -> Task:
        task = self._repository.get(task_id)
        if task.status is TaskStatus.CANCELED:
            return task
        if task.status is TaskStatus.EXECUTED:
            raise InvalidTaskStateError("Executed tasks cannot be canceled")

        canceled = task.update_status(status=TaskStatus.CANCELED, canceled_at=datetime.utcnow())
        self._repository.update(canceled)
        return canceled

    def _build_command_preview(self, instruction: FFmpegInstruction) -> CommandPreview:
        command_parts = ["ffmpeg", "-y", "-i", instruction.source]
        if instruction.start_time:
            command_parts.extend(["-ss", instruction.start_time])
        if instruction.end_time:
            command_parts.extend(["-to", instruction.end_time])
        if instruction.video_codec:
            command_parts.extend(["-c:v", instruction.video_codec])
        if instruction.audio_codec:
            command_parts.extend(["-c:a", instruction.audio_codec])
        if instruction.extra_args:
            command_parts.extend(instruction.extra_args)
        command_parts.append(instruction.output)
        command = " ".join(command_parts)
        return CommandPreview(command=command, description="Pending approval before execution")

    def _execute_task(self, task: Task) -> Task:
        # In a production system this would queue work on a background runner. For now we
        # simply mark the task as executed after approval.
        return task.update_status(status=TaskStatus.EXECUTED, executed_at=datetime.utcnow())


__all__ = [
    "FFmpegTaskService",
    "InvalidTaskStateError",
    "TaskNotFoundError",
]
