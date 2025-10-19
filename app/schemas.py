from __future__ import annotations

from datetime import datetime
from typing import List, Optional
from uuid import UUID

from pydantic import BaseModel, Field

from .models import CommandPreview, FFmpegInstruction, Task, TaskStatus


class InstructionSchema(BaseModel):
    source: str = Field(..., description="Input video file or stream")
    output: str = Field(..., description="Destination file for the trimmed clip")
    start_time: Optional[str] = Field(None, description="Clip start time (e.g. 00:01:23)")
    end_time: Optional[str] = Field(None, description="Clip end time (e.g. 00:02:10)")
    video_codec: Optional[str] = Field(None, description="Codec passed to -c:v")
    audio_codec: Optional[str] = Field(None, description="Codec passed to -c:a")
    extra_args: List[str] = Field(default_factory=list, description="Additional ffmpeg flags")

    def to_domain(self) -> FFmpegInstruction:
        return FFmpegInstruction.from_sequence(
            source=self.source,
            output=self.output,
            start_time=self.start_time,
            end_time=self.end_time,
            video_codec=self.video_codec,
            audio_codec=self.audio_codec,
            extra_args=self.extra_args,
        )

    @classmethod
    def from_domain(cls, instruction: FFmpegInstruction) -> "InstructionSchema":
        return cls(
            source=instruction.source,
            output=instruction.output,
            start_time=instruction.start_time,
            end_time=instruction.end_time,
            video_codec=instruction.video_codec,
            audio_codec=instruction.audio_codec,
            extra_args=list(instruction.extra_args),
        )


class CommandPreviewSchema(BaseModel):
    command: str
    description: Optional[str] = None

    @classmethod
    def from_domain(cls, preview: CommandPreview) -> "CommandPreviewSchema":
        return cls(command=preview.command, description=preview.description)


class TaskResponse(BaseModel):
    id: UUID
    status: TaskStatus
    instruction: InstructionSchema
    command_preview: CommandPreviewSchema
    created_at: datetime
    updated_at: datetime
    approved_at: Optional[datetime] = None
    executed_at: Optional[datetime] = None
    canceled_at: Optional[datetime] = None

    @classmethod
    def from_domain(cls, task: Task) -> "TaskResponse":
        return cls(
            id=task.id,
            status=task.status,
            instruction=InstructionSchema.from_domain(task.instruction),
            command_preview=CommandPreviewSchema.from_domain(task.command_preview),
            created_at=task.created_at,
            updated_at=task.updated_at,
            approved_at=task.approved_at,
            executed_at=task.executed_at,
            canceled_at=task.canceled_at,
        )


class TaskCreateRequest(BaseModel):
    instruction: InstructionSchema


class TaskListResponse(BaseModel):
    tasks: List[TaskResponse]

    @classmethod
    def from_tasks(cls, tasks) -> "TaskListResponse":
        return cls(tasks=[TaskResponse.from_domain(task) for task in tasks])
