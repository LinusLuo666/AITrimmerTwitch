"""Persistent storage for application settings."""

from __future__ import annotations

import json
from dataclasses import dataclass, field, replace
from pathlib import Path
from typing import Any, Callable, Dict, Mapping, MutableMapping, Optional

from .config import WorkspaceProperties


CURRENT_VERSION = 1


@dataclass(frozen=True, slots=True)
class Settings:
    """Serializable representation of persisted application settings."""

    workspace: WorkspaceProperties
    ffmpeg_path: Optional[Path] = None
    compression_presets: Mapping[str, Any] = field(default_factory=dict)

    def to_json(self) -> Dict[str, Any]:
        return {
            "version": CURRENT_VERSION,
            "workspace": {"root": str(self.workspace.root)},
            "ffmpeg_path": str(self.ffmpeg_path) if self.ffmpeg_path else None,
            "compression_presets": dict(self.compression_presets),
        }

    @classmethod
    def from_json(cls, payload: Mapping[str, Any]) -> "Settings":
        workspace_data = payload.get("workspace") or {}
        if not isinstance(workspace_data, Mapping):
            raise ValueError("workspace entry in settings must be a mapping")

        workspace = WorkspaceProperties.from_mapping(workspace_data)

        ffmpeg_raw = payload.get("ffmpeg_path")
        ffmpeg_path = Path(ffmpeg_raw) if isinstance(ffmpeg_raw, str) else None

        presets = payload.get("compression_presets") or {}
        if not isinstance(presets, Mapping):
            raise ValueError("compression_presets must be a mapping")

        return cls(
            workspace=workspace,
            ffmpeg_path=ffmpeg_path,
            compression_presets=dict(presets),
        )

    def with_updates(self, **kwargs: Any) -> "Settings":
        """Return a new :class:`Settings` instance with specific fields updated."""

        return replace(self, **kwargs)


Migration = Callable[[MutableMapping[str, Any]], MutableMapping[str, Any]]


def migrate(payload: MutableMapping[str, Any]) -> MutableMapping[str, Any]:
    """Upgrade raw persisted settings to the latest schema version."""

    version = int(payload.get("version", 0))

    migrations: Dict[int, Migration] = {
        0: _migrate_from_v0_to_v1,
    }

    while version < CURRENT_VERSION:
        migration = migrations.get(version)
        if migration is None:
            raise RuntimeError(f"no migration path from version {version}")
        payload = migration(payload)
        version = int(payload.get("version", version + 1))

    return payload


def _migrate_from_v0_to_v1(payload: MutableMapping[str, Any]) -> MutableMapping[str, Any]:
    workspace_root = payload.get("workspace") or payload.get("workspace_root")
    if isinstance(workspace_root, str):
        workspace_section: MutableMapping[str, Any] = {"root": workspace_root}
    elif isinstance(workspace_root, Mapping):
        workspace_section = dict(workspace_root)
    else:
        workspace_section = {"root": str(Path.cwd())}

    ffmpeg_path = payload.get("ffmpeg_path")
    compression_presets = payload.get("compression_presets") or {}

    migrated: MutableMapping[str, Any] = {
        "version": 1,
        "workspace": workspace_section,
        "ffmpeg_path": ffmpeg_path,
        "compression_presets": compression_presets,
    }
    return migrated


class SettingsStore:
    """Persist settings to a JSON file with schema migration support."""

    def __init__(self, location: Path | str) -> None:
        self._location = Path(location).expanduser().resolve()
        self._location.parent.mkdir(parents=True, exist_ok=True)

    @property
    def location(self) -> Path:
        return self._location

    def load(self, *, create_default: bool = True) -> Settings:
        """Load settings from disk, optionally creating defaults."""

        if not self._location.exists():
            if not create_default:
                raise FileNotFoundError(self._location)
            default = Settings(
                workspace=WorkspaceProperties(root=self._location.parent),
                compression_presets={},
            )
            self.save(default)
            return default

        with self._location.open("r", encoding="utf-8") as handle:
            payload = json.load(handle)

        migrated = migrate(payload)
        return Settings.from_json(migrated)

    def save(self, settings: Settings) -> None:
        data = settings.to_json()
        with self._location.open("w", encoding="utf-8") as handle:
            json.dump(data, handle, indent=2)

    def update(
        self,
        *,
        ffmpeg_path: Optional[Path] = None,
        workspace: Optional[WorkspaceProperties] = None,
        compression_presets: Optional[Mapping[str, Any]] = None,
    ) -> Settings:
        """Persist updates to the settings, returning the new value."""

        current = self.load()
        new_settings = current.with_updates(
            ffmpeg_path=ffmpeg_path if ffmpeg_path is not None else current.ffmpeg_path,
            workspace=workspace if workspace is not None else current.workspace,
            compression_presets=(
                compression_presets
                if compression_presets is not None
                else current.compression_presets
            ),
        )
        self.save(new_settings)
        return new_settings


__all__ = ["Settings", "SettingsStore", "migrate", "CURRENT_VERSION"]
