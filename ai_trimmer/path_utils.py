"""Utilities for safely resolving user supplied paths."""

from __future__ import annotations

from pathlib import Path
from typing import Union

from .config import WorkspaceProperties


class OutsideWorkspaceError(ValueError):
    """Raised when a path resolves outside of the configured workspace."""


PathLike = Union[str, Path]


def resolve_workspace_path(
    workspace: WorkspaceProperties,
    requested: PathLike,
    *,
    must_exist: bool = False,
) -> Path:
    """Resolve a user supplied path safely within the workspace.

    Parameters
    ----------
    workspace:
        The active workspace configuration.
    requested:
        User supplied path, relative or absolute.
    must_exist:
        When ``True`` a :class:`FileNotFoundError` is raised if the resulting
        path does not already exist.
    """

    base = workspace.root.resolve()
    candidate = Path(requested).expanduser()
    if not candidate.is_absolute():
        candidate = base / candidate
    candidate = candidate.resolve()

    if candidate == base or base in candidate.parents:
        if must_exist and not candidate.exists():
            raise FileNotFoundError(candidate)
        return candidate

    raise OutsideWorkspaceError(
        f"requested path '{requested}' resolves outside workspace '{base}'"
    )


__all__ = ["resolve_workspace_path", "OutsideWorkspaceError"]
