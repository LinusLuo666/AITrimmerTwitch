from __future__ import annotations

from dataclasses import dataclass, replace
from datetime import datetime
from enum import Enum
from typing import Optional, Sequence, Tuple
from uuid import UUID, uuid4


class TaskStatus(str, Enum):
    """Lifecycle states for queued FFmpeg tasks."""

    PENDING_APPROVAL = "pending_approval"
    APPROVED = "approved"
    EXECUTED = "executed"
    CANCELED = "canceled"


@dataclass(frozen=True)
class CommandPreview:
    """A lightweight representation of the command that will be executed."""

    command: str
    description: Optional[str] = None


@dataclass(frozen=True)
class FFmpegInstruction:
    """User supplied parameters for trimming and exporting a clip."""

    source: str
    output: str
    start_time: Optional[str] = None
    end_time: Optional[str] = None
    video_codec: Optional[str] = None
    audio_codec: Optional[str] = None
    extra_args: Tuple[str, ...] = ()

    @classmethod
    def from_sequence(
        cls,
        *,
        source: str,
        output: str,
        start_time: Optional[str] = None,
        end_time: Optional[str] = None,
        video_codec: Optional[str] = None,
        audio_codec: Optional[str] = None,
        extra_args: Optional[Sequence[str]] = None,
    ) -> "FFmpegInstruction":
        return cls(
            source=source,
            output=output,
            start_time=start_time,
            end_time=end_time,
            video_codec=video_codec,
            audio_codec=audio_codec,
            extra_args=tuple(extra_args or ()),
        )


@dataclass(frozen=True)
class Task:
    """Represents a queued command awaiting human approval before execution."""

    id: UUID
    instruction: FFmpegInstruction
    command_preview: CommandPreview
    status: TaskStatus
    created_at: datetime
    updated_at: datetime
    approved_at: Optional[datetime] = None
    executed_at: Optional[datetime] = None
    canceled_at: Optional[datetime] = None

    @classmethod
    def create(cls, instruction: FFmpegInstruction, command_preview: CommandPreview) -> "Task":
        now = datetime.utcnow()
        return cls(
            id=uuid4(),
            instruction=instruction,
            command_preview=command_preview,
            status=TaskStatus.PENDING_APPROVAL,
            created_at=now,
            updated_at=now,
        )

    def update_status(
        self,
        *,
        status: TaskStatus,
        approved_at: Optional[datetime] = None,
        executed_at: Optional[datetime] = None,
        canceled_at: Optional[datetime] = None,
    ) -> "Task":
        return replace(
            self,
            status=status,
            approved_at=approved_at if approved_at is not None else self.approved_at,
            executed_at=executed_at if executed_at is not None else self.executed_at,
            canceled_at=canceled_at if canceled_at is not None else self.canceled_at,
            updated_at=datetime.utcnow(),
        )
