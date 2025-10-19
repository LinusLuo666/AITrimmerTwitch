# Running the backend continuously on Windows

The backend can be packaged as an executable JAR using Gradle. Once the file exists you can host it as either a Windows service or a scheduled task, depending on the operational requirements.

## 1. Build the backend JAR

```powershell
cd backend
gradle clean build
```

The resulting file is located at `backend/build/libs/ai-trimmer-backend.jar`.

## 2. Register a Windows service (using built-in `sc.exe`)

1. Copy the JAR and any configuration to a permanent location, for example `C:\AITrimmer\backend`.
2. Create a wrapper batch file so the service can launch Java:

   ```bat
   @echo off
   set JAVA_HOME=C:\Program Files\Java\jdk-17
   set APP_HOME=C:\AITrimmer\backend
   "%JAVA_HOME%\bin\java.exe" -jar "%APP_HOME%\ai-trimmer-backend.jar" --spring.profiles.active=prod
   ```
3. Register the service:

   ```powershell
   sc.exe create AITrimmerBackend binPath= "C:\AITrimmer\backend\start-backend.bat" start= auto
   sc.exe description AITrimmerBackend "AI Trimmer Twitch backend"
   ```
4. Grant the service account access to the workspace and FFmpeg directory if necessary.
5. Start the service and confirm it stays running:

   ```powershell
   sc.exe start AITrimmerBackend
   sc.exe query AITrimmerBackend
   ```

> **Tip:** For more advanced control (restart policies, stdout capture) consider using the free [NSSM](https://nssm.cc/) utility instead of `sc.exe`.

## 3. Schedule a recurring task instead of a service

If you only need the backend to run during certain windows—e.g. when streams are active—you can configure a scheduled task:

```powershell
$action = New-ScheduledTaskAction -Execute "C:\Program Files\Java\jdk-17\bin\java.exe" -Argument "-jar C:\AITrimmer\backend\ai-trimmer-backend.jar"
$trigger = New-ScheduledTaskTrigger -AtStartup
Register-ScheduledTask -TaskName "AITrimmerBackend" -Action $action -Trigger $trigger -RunLevel Highest
```

Alternative triggers include `-Daily` or `-Once` with a specific start time. Combine with `-RepetitionInterval (New-TimeSpan -Minutes 15)` to poll for new streams.

To remove the task later run:

```powershell
Unregister-ScheduledTask -TaskName "AITrimmerBackend" -Confirm:$false
```

## 4. Logging and monitoring

* Redirect output of the batch file to a log (`>> "%APP_HOME%\logs\backend.log" 2>&1`).
* Use the Event Viewer (`Applications and Services Logs`) to monitor service start/stop events.
* Pair the scheduled task with the `-TaskPath` parameter to group AI Trimmer jobs in the Task Scheduler UI.

## 5. Updating the service or task

When you rebuild the backend:

1. Stop the service or disable the scheduled task.
2. Replace the JAR (and update configuration files if necessary).
3. Start the service again or re-enable the scheduled task.
4. Confirm the new version by checking the logs or application health endpoint.
