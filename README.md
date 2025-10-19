# AITrimmerTwitch

Utility modules for managing local workspaces, persisted settings, and safe path
resolution for the AITrimmerTwitch tooling.

## Workspace configuration

Use a JSON or YAML configuration file containing a ``workspace`` section to
create a :class:`~ai_trimmer.config.WorkspaceProperties` instance::

```json
{
  "workspace": {
    "root": "~/twitch-clips"
  }
}
```

```python
from pathlib import Path
from ai_trimmer.config import WorkspaceProperties

props = WorkspaceProperties.from_file(Path("config.json"))
print(props.root)
```

## Resolving user supplied paths

Combine user supplied filenames with the configured workspace using
:func:`~ai_trimmer.path_utils.resolve_workspace_path`::

```python
from ai_trimmer.path_utils import resolve_workspace_path

clip_path = resolve_workspace_path(props, "clips/highlight.mp4", must_exist=True)
```

The helper ensures the resolved path remains inside the workspace and raises
:class:`~ai_trimmer.path_utils.OutsideWorkspaceError` otherwise.

## Persisting application settings

Application settings such as the FFmpeg binary path, compression presets, and
workspace configuration are persisted with
:class:`~ai_trimmer.settings_store.SettingsStore`.  The store writes a JSON file
and automatically applies schema migrations when loading existing data::

```python
from ai_trimmer.settings_store import SettingsStore

store = SettingsStore("~/.config/aitrimmer/settings.json")
settings = store.load()

store.update(ffmpeg_path="/usr/bin/ffmpeg")
```

All functionality is covered by unit tests which can be executed with ``pytest``.
