import json
from pathlib import Path

import pytest

from ai_trimmer.config import ConfigurationError, WorkspaceProperties
from ai_trimmer.path_utils import OutsideWorkspaceError, resolve_workspace_path
from ai_trimmer.settings_store import CURRENT_VERSION, SettingsStore, migrate


def test_workspace_from_mapping(tmp_path: Path) -> None:
    props = WorkspaceProperties.from_mapping({"root": "workspace"}, base_dir=tmp_path)
    assert props.root == (tmp_path / "workspace").resolve()


def test_workspace_from_file_json(tmp_path: Path) -> None:
    config = {"workspace": {"root": "./relative"}}
    config_path = tmp_path / "config.json"
    config_path.write_text(json.dumps(config), encoding="utf-8")

    props = WorkspaceProperties.from_file(config_path)
    assert props.root == (tmp_path / "relative").resolve()


def test_workspace_from_file_missing() -> None:
    with pytest.raises(ConfigurationError):
        WorkspaceProperties.from_file("/tmp/does/not/exist.json")


def test_resolve_workspace_path(tmp_path: Path) -> None:
    props = WorkspaceProperties(root=tmp_path)
    target = tmp_path / "videos" / "clip.mp4"
    target.parent.mkdir()
    target.touch()

    resolved = resolve_workspace_path(props, "videos/clip.mp4", must_exist=True)
    assert resolved == target

    with pytest.raises(OutsideWorkspaceError):
        resolve_workspace_path(props, "../outside.mp4")


def test_settings_store_roundtrip(tmp_path: Path) -> None:
    store_path = tmp_path / "settings.json"
    store = SettingsStore(store_path)

    settings = store.load()
    assert settings.workspace.root == tmp_path
    assert settings.ffmpeg_path is None
    assert settings.compression_presets == {}

    updated = store.update(
        ffmpeg_path=tmp_path / "bin" / "ffmpeg",
        compression_presets={"default": {"crf": 18}},
    )

    assert updated.ffmpeg_path == tmp_path / "bin" / "ffmpeg"
    assert "default" in updated.compression_presets

    reloaded = store.load()
    assert reloaded == updated


def test_migrate_from_version_zero(tmp_path: Path) -> None:
    payload = {
        "version": 0,
        "workspace_root": str(tmp_path / "media"),
        "ffmpeg_path": str(tmp_path / "ffmpeg"),
    }

    migrated = migrate(payload)
    assert migrated["version"] == CURRENT_VERSION
    assert migrated["workspace"]["root"] == str(tmp_path / "media")
    assert migrated["ffmpeg_path"] == str(tmp_path / "ffmpeg")
