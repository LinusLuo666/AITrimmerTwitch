from __future__ import annotations

from fastapi import FastAPI, HTTPException
from uuid import UUID

from .repositories import TaskNotFoundError
from .schemas import (
    TaskCreateRequest,
    TaskListResponse,
    TaskResponse,
)
from .services import FFmpegTaskService, InvalidTaskStateError

app = FastAPI(title="AI Trimmer Task Queue")
service = FFmpegTaskService()


@app.post("/tasks", response_model=TaskResponse, status_code=201)
def create_task(request: TaskCreateRequest) -> TaskResponse:
    task = service.create_task(request.instruction.to_domain())
    return TaskResponse.from_domain(task)


@app.get("/tasks/pending", response_model=TaskListResponse)
def list_pending_tasks() -> TaskListResponse:
    tasks = service.list_pending_tasks()
    return TaskListResponse.from_tasks(tasks)


@app.get("/tasks/history", response_model=TaskListResponse)
def list_history() -> TaskListResponse:
    tasks = service.list_history()
    return TaskListResponse.from_tasks(tasks)


@app.post("/tasks/{task_id}/approve", response_model=TaskResponse)
def approve_task(task_id: UUID) -> TaskResponse:
    try:
        task = service.approve_task(task_id)
    except TaskNotFoundError:
        raise HTTPException(status_code=404, detail="Task not found")
    except InvalidTaskStateError as exc:
        raise HTTPException(status_code=409, detail=str(exc))
    return TaskResponse.from_domain(task)


@app.post("/tasks/{task_id}/cancel", response_model=TaskResponse)
def cancel_task(task_id: UUID) -> TaskResponse:
    try:
        task = service.cancel_task(task_id)
    except TaskNotFoundError:
        raise HTTPException(status_code=404, detail="Task not found")
    except InvalidTaskStateError as exc:
        raise HTTPException(status_code=409, detail=str(exc))
    return TaskResponse.from_domain(task)
