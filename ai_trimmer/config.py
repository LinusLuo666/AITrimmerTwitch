"""Configuration objects and helpers for AITrimmerTwitch."""

from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Mapping, MutableMapping, Optional

try:  # pragma: no cover - optional dependency
    import yaml  # type: ignore
except ModuleNotFoundError:  # pragma: no cover - handled gracefully at runtime
    yaml = None  # type: ignore


class ConfigurationError(RuntimeError):
    """Raised when configuration files cannot be parsed."""


@dataclass(frozen=True, slots=True)
class WorkspaceProperties:
    """Strongly typed description of the active workspace.

    The class can be constructed directly or via :meth:`from_mapping` or
    :meth:`from_file` to bind configuration originating from JSON or YAML
    documents.  Relative workspace paths are resolved against the location of
    the configuration file unless a ``base_dir`` is supplied explicitly.
    """

    root: Path

    def __post_init__(self) -> None:
        normalized = self.root.expanduser()
        object.__setattr__(self, "root", normalized.resolve())

    @classmethod
    def from_mapping(
        cls,
        mapping: Mapping[str, Any],
        *,
        base_dir: Optional[Path] = None,
    ) -> "WorkspaceProperties":
        """Create an instance from a mapping.

        Parameters
        ----------
        mapping:
            Mapping containing a ``root`` entry with the desired workspace path.
        base_dir:
            Optional directory used to resolve relative paths.  When omitted the
            current working directory is used.
        """

        if "root" not in mapping:
            raise ConfigurationError("workspace configuration requires a 'root' entry")

        raw_root = mapping["root"]
        if not isinstance(raw_root, (str, bytes, Path)):
            raise ConfigurationError("workspace root must be a string or pathlib.Path")

        root_path = Path(raw_root)
        if not root_path.is_absolute():
            base = base_dir if base_dir is not None else Path.cwd()
            root_path = (base / root_path).resolve()

        return cls(root=root_path)

    @classmethod
    def from_file(cls, file_path: Path | str) -> "WorkspaceProperties":
        """Load workspace configuration from a YAML or JSON file."""

        path = Path(file_path).expanduser().resolve()
        if not path.exists():
            raise ConfigurationError(f"configuration file does not exist: {path}")

        try:
            data = _load_config_file(path)
        except OSError as exc:  # pragma: no cover - filesystem errors
            raise ConfigurationError(f"failed reading configuration: {exc}") from exc

        if "workspace" in data and isinstance(data["workspace"], Mapping):
            workspace_section = data["workspace"]
        else:
            workspace_section = data

        if not isinstance(workspace_section, Mapping):
            raise ConfigurationError("workspace configuration must be a mapping")

        return cls.from_mapping(workspace_section, base_dir=path.parent)


def _load_config_file(path: Path) -> MutableMapping[str, Any]:
    """Read a configuration file, supporting JSON and optionally YAML."""

    suffix = path.suffix.lower()
    if suffix in {".json"}:
        with path.open("r", encoding="utf-8") as handle:
            return json.load(handle)

    if suffix in {".yaml", ".yml"}:
        if yaml is None:  # pragma: no cover - depends on optional package
            raise ConfigurationError(
                "YAML configuration requested but PyYAML is not installed"
            )
        with path.open("r", encoding="utf-8") as handle:
            return yaml.safe_load(handle) or {}

    raise ConfigurationError(f"unsupported configuration format: '{suffix}'")


__all__ = ["WorkspaceProperties", "ConfigurationError"]
