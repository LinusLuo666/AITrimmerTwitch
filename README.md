# AITrimmerTwitch

Tooling scaffolding for the AI Trimmer Twitch project. This repository now ships with:

* A Gradle-based Java backend that builds to an executable JAR.
* A React + Vite frontend that compiles to static assets.
* A PowerShell setup script that provisions the local workspace and records the FFmpeg binary path.
* Windows-specific guidance for keeping the backend running continuously.

## Prerequisites

* Java 17+
* Node.js 18+
* FFmpeg installed locally (the setup script will ask for the executable path).

## Backend build

```bash
cd backend
gradle clean build
```

The runnable artifact is written to `backend/build/libs/ai-trimmer-backend.jar` with the main class `com.aitrimmer.backend.App`. The Gradle build also executes the frontend build (`npm run build`) so the compiled assets are always in sync with the backend release.

## Frontend build

```bash
cd frontend
npm install
npm run build
```

Static assets are output to `frontend/dist/` and can be served by any HTTP server or copied to the backend's public directory.

## Initial setup on Windows

Run the PowerShell helper to create the workspace folders (`clips`, `logs`, `temp`), capture the FFmpeg binary location, and write a `.env` file with the key environment variables. Use `-PersistEnv` to save them to the current user's profile.

```powershell
pwsh ./scripts/setup.ps1 -WorkspaceRoot "C:\\AITrimmer"
```

## Continuous operation

Guidance for installing the backend as a Windows service or scheduled task is available at [`docs/windows-service.md`](docs/windows-service.md).
