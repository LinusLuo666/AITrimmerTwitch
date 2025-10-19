# AITrimmerTwitch

AITrimmerTwitch 旨在提供一套以 Spring Boot 為核心、前後端分離的 AI 輔助影片裁剪方案。使用者只需指定來源影片、欲保留的時間區段與畫質等級，系統即可生成對應的 FFmpeg 指令與任務流程，並透過前端控制台進行審核與狀態管理。

## 專案結構

```
AITrimmerTwitch/
├── backend/          # Spring Boot (Gradle) 後端專案
└── frontend/         # Vite + React + TypeScript 前端專案
```

## 目前功能摘要

- **Spring Boot 後端**
  - `video.processor` 組態涵蓋工作資料夾、FFmpeg 執行檔路徑、輸出前綴與畫質參數（低／中／高，可自行擴充）。
  - 任務模型（來源影片、時間片段、品質、狀態、FFmpeg 指令預覽）以及 JSON 檔案持久化（存放於工作資料夾 `tasks/tasks.json`）。
  - REST API：
    - `POST /api/tasks` 建立任務並回傳 FFmpeg 命令預覽。
    - `GET /api/tasks` 查詢所有任務。
    - `POST /api/tasks/{id}/{action}` 更新任務狀態（審核、執行、暫停、取消、完成、失敗）。
    - `GET /api/config/general`、`GET /api/config/qualities` 提供前端初始化資訊。
- **React 前端**
  - 任務建立表單（影片名、畫質、可調整的時間片段、是否自動批准）。
  - 任務列表與狀態操作按鈕。
  - FFmpeg 命令預覽以審核流程對應需求。

## 先決條件

- JDK 21（或至少 JDK 17）用於後端。
- Node.js 18+ 用於前端。
- Windows 上已安裝 FFmpeg，可在 `backend/src/main/resources/application.yml` 中設定路徑；其他平台請自行調整。
- 若要啟用 Spring AI 的 OpenAI 介面，請設定環境變數 `SPRING_AI_OPENAI_API_KEY`；預設使用 `placeholder`，不會對外呼叫。

> **注意**：請確認執行環境載入到 JDK 21（例如使用 `jenv` 時需在 shell 內 `eval "$(jenv init -)"`）。若落回系統預設的 JDK 8 會導致 `./gradlew test` 失敗。
> ```
> cd backend
> ./gradlew test
> ```

## 快速開始

### 後端

```bash
cd backend
# 第一次啟動前可自行調整 application.yml 中的工作資料夾與 FFmpeg 路徑
# 若需調整 JVM 記憶體或代理設定，可編輯 backend/gradle.properties
./gradlew bootRun
```

啟動後提供的關鍵端點：

- 健康檢查：`GET http://localhost:8080/actuator/health`
- 任務 API：`GET http://localhost:8080/api/tasks`

### 前端

```bash
cd frontend
cp .env.example .env   # 如需修改後端位址可編輯 .env
npm install
npm run dev
```

開啟瀏覽器訪問 `http://localhost:5173`，即可使用控制台建立及管理任務。

## 下一步開發計畫（建議順序）

1. **任務執行引擎**
   - 實作後端實際呼叫 FFmpeg、產生暫存檔與輸出檔的流程。
   - 建立任務佇列與執行緒管理，確保單工作執行。
2. **審核與通知流程**
   - 將命令預覽整合通知管道（如 Email/Slack），待人工確認後才允許執行。
   - 記錄審核歷程與操作人員資訊。
3. **AI 指令生成與提示詞管理**
   - 接入 Spring AI，提供與第三方模型整合的接口，允許由自然語言自動生成裁剪片段與品質建議。
   - 設計提示詞版本與日誌管理。
4. **任務結果與錯誤追蹤**
   - 保存 FFmpeg 執行輸出、錯誤紀錄與最終產出檔案資訊。
   - 前端新增日誌檢視與再次審核管控。
5. **使用者與權限管理**
   - 加入登入流程、角色權限與 API token。
   - 強化對工作資料夾的操作審計。
6. **測試與部署**
   - 擴充單元／整合測試，加入前端端到端測試。
   - 規劃 CI/CD、封裝 Docker 映像以便部署。

## 後續備忘

- `video.processor.workspace-path` 須指向實際存在的資料夾，且來源影片需放置在該目錄內。
- 系統會為輸出檔案添加 `AITRIM_` 前綴並附帶任務 UUID，避免重複編輯。
- 現階段尚未建立真實的多任務佇列與排程，僅提供狀態標記與命令預覽，後續可依上述計畫逐步補齊。 
