from __future__ import annotations

from threading import RLock
from typing import Dict, Iterable, List
from uuid import UUID

from .models import Task, TaskStatus


class TaskNotFoundError(LookupError):
    """Raised when the requested task is missing from the repository."""


class TaskRepository:
    """Abstract storage for FFmpeg tasks."""

    def add(self, task: Task) -> None:
        raise NotImplementedError

    def get(self, task_id: UUID) -> Task:
        raise NotImplementedError

    def list_all(self) -> List[Task]:
        raise NotImplementedError

    def list_by_status(self, status: TaskStatus) -> List[Task]:
        raise NotImplementedError

    def update(self, task: Task) -> None:
        raise NotImplementedError


class InMemoryTaskRepository(TaskRepository):
    """Thread-safe in-memory repository for queued FFmpeg tasks."""

    def __init__(self) -> None:
        self._tasks: Dict[UUID, Task] = {}
        self._lock = RLock()

    def add(self, task: Task) -> None:
        with self._lock:
            self._tasks[task.id] = task

    def get(self, task_id: UUID) -> Task:
        with self._lock:
            try:
                return self._tasks[task_id]
            except KeyError as exc:
                raise TaskNotFoundError(str(task_id)) from exc

    def list_all(self) -> List[Task]:
        with self._lock:
            return sorted(self._tasks.values(), key=lambda t: t.created_at)

    def list_by_status(self, status: TaskStatus) -> List[Task]:
        with self._lock:
            return [task for task in self._tasks.values() if task.status is status]

    def update(self, task: Task) -> None:
        with self._lock:
            if task.id not in self._tasks:
                raise TaskNotFoundError(str(task.id))
            self._tasks[task.id] = task

    def extend(self, tasks: Iterable[Task]) -> None:
        with self._lock:
            for task in tasks:
                self._tasks[task.id] = task
