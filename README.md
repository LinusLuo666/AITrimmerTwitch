# AITrimmerTwitch

AITrimmerTwitch 致力于打造一套以 Spring Boot 为核心、前后端分离的 AI 辅助视频裁剪方案。用户只需指定源视频、希望保留的时间片段以及画质档位，系统即可生成对应的 FFmpeg 命令与任务流程，并通过前端控制台完成审核与状态管理。

## 项目结构

```
AITrimmerTwitch/
├── backend/          # Spring Boot (Gradle) 后端项目
└── frontend/         # Vite + React + TypeScript 前端项目
```

## 当前功能概览

- **Spring Boot 后端**
  - `video.processor` 配置项覆盖工作目录、FFmpeg 可执行文件、输出前缀与画质参数（默认提供低 / 中 / 高，可自定义扩展）。
  - 任务模型（源视频、时间片段、画质、状态、FFmpeg 命令预览）以及基于 JSON 文件的持久化（保存于工作目录 `tasks/tasks.json`）。
  - REST API：
    - `POST /api/tasks` 创建任务并返回 FFmpeg 命令预览。
    - `GET /api/tasks` 查询全部任务。
    - `POST /api/tasks/{id}/{action}` 更新任务状态（审核、运行、暂停、取消、完成、失败）。
    - `GET /api/config/general`、`GET /api/config/qualities` 提供前端初始化信息。
- **React 前端**
  - 任务创建表单（视频名称、画质档位、可调节时间片段、是否自动审核通过）。
  - 任务列表与状态操作按钮。
  - FFmpeg 命令预览，方便人工确认。

## 环境要求

- 后端需 JDK 21（最低 JDK 17）。
- 前端需 Node.js 18+。
- Windows 环境已安装 FFmpeg，可在 `backend/src/main/resources/application.yml` 中调整路径；其他平台请自行修改。
- 如需启用 Spring AI 调用 OpenAI 接口，请设置环境变量 `SPRING_AI_OPENAI_API_KEY`；默认值为 `placeholder`，不会对外发起请求。

> **注意**：务必确认运行 shell 已加载 JDK 21（例如使用 `jenv` 时，需要执行 `eval "$(jenv init -)"`）。若回退到系统默认的 JDK 8，`./gradlew test` 将会失败。
> ```
> cd backend
> ./gradlew test
> ```

## 快速开始

### 后端

```bash
cd backend
# 首次启动前可按需修改 application.yml 中的工作目录与 FFmpeg 路径
# 若需调整 JVM 内存 / 代理等，可编辑 backend/gradle.properties
./gradlew bootRun
```

启动后可访问以下关键接口：

- 健康检查：`GET http://localhost:8080/actuator/health`
- 任务列表：`GET http://localhost:8080/api/tasks`

### 前端

```bash
cd frontend
cp .env.example .env   # 如需修改后端地址，可编辑 .env
npm install
npm run dev
```

打开浏览器访问 `http://localhost:5173`，即可使用任务控制台创建与管理任务。

## 后续开发计划（建议顺序）

1. **任务执行引擎**
   - 实现后端实际调用 FFmpeg 的流程，负责拆分片段、合成输出。
   - 构建任务队列与线程管理，确保单任务串行执行。
2. **审核与通知流程**
   - 将命令预览接入通知渠道（如 Email/Slack），人工确认后再真正执行。
   - 记录审核记录与操作人信息。
3. **AI 指令生成与提示词管理**
   - 集成 Spring AI，接入第三方模型，通过自然语言生成裁剪建议。
   - 设计提示词版本与日志管理。
4. **任务结果与错误追踪**
   - 保存 FFmpeg 执行日志、错误信息与最终输出文件记录。
   - 前端增加日志查看、二次审核控制。
5. **用户与权限管理**
   - 增加登录、角色权限与 API Token。
   - 加强对工作目录的操作审计。
6. **测试与部署**
   - 补充单元 / 集成测试与前端端到端测试。
   - 规划 CI/CD，并提供 Docker 镜像便于部署。

## 其他注意事项

- `video.processor.workspace-path` 必须指向实际存在的目录，源视频需放置其中。
- 系统会为输出文件添加 `AITRIM_` 前缀并包含任务 UUID，默认禁止重复编辑已经产出的文件。
- 当前尚未实现真正的任务并发与调度，仅提供状态流转与命令预览，可按上述路线逐步完善。 
