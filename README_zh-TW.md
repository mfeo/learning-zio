# 學習 ZIO (Scala 的非同步與並行框架)

🌍 [Read in English (英文版)](./README.md)

這是一個專為初學者設計的實作專案，帶您一步步學習 [ZIO 2](https://zio.dev/)。ZIO 是一個強大、型別安全 (Type-safe) 且具備高度可組合性的 Scala 函式庫，專門用來處理非同步 (Async) 與並行 (Concurrent) 程式設計。

每個範例都是一個獨立的可執行應用程式，並附有對應的測試。建議您實際閱讀程式碼、執行它，甚至修改參數來觀察結果，這是最好的學習方式。

## 什麼是 ZIO？(給初學者的核心概念)

在進入範例之前，我們先了解 ZIO 的核心概念：

想像你在寫一份**食譜**。食譜本身不會變出蛋糕，它只是「描述」了烤蛋糕的步驟。這就是 **ZIO Effect** 的概念：它是一段「描述程式該做什麼」的資料結構。直到你把這份食譜交給廚師（ZIO Runtime 執行環境）去執行，事情才會真正發生（例如：讀取檔案、寫入資料庫、發送網路請求）。這種特性被稱為「純粹的 (Pure)」與「惰性執行 (Lazy)」。

ZIO 的核心型別非常強大，它由三個參數組成：`ZIO[R, E, A]`

- **R (Requirement / Environment)**：執行這段程式**需要什麼環境或依賴**？（例如：資料庫連線、設定檔）。如果不需要任何外部依賴，型別就是 `Any`。
- **E (Error)**：這段程式**可能會發生什麼錯誤**？（例如：`IOException`, `UserNotFound`）。如果保證絕對不會失敗，型別就是 `Nothing`。
- **A (Answer / Success)**：這段程式成功執行後**會回傳什麼結果**？（例如：`Int`, `String`）。如果只是執行動作而沒有回傳值，型別就是 `Unit`。

---

## 技術堆疊

- **Scala** 3.6.4
- **ZIO** 2.1.16 (包含 zio, zio-streams, zio-test)
- **Mill** 1.0.6 (Scala 的建置工具)

## 開始使用

### 先決條件

- JDK 17 或以上版本
- [Mill](https://mill-build.org/) 1.0.6 或以上版本

### 常用指令

```bash
# 編譯專案
mill app.compile

# 執行所有測試 (強烈建議在修改程式碼後執行)
mill app.test

# 執行特定的範例程式
mill app.runMain MainApp
mill app.runMain examples.LayerExample
mill app.runMain examples.StreamExample
mill app.runMain examples.ScheduleExample
mill app.runMain examples.RefExample
mill app.runMain examples.STMExample
mill app.runMain examples.ScopeExample
mill app.runMain examples.QueueExample
mill app.runMain examples.HubExample
mill app.runMain examples.PromiseExample
mill app.runMain examples.SemaphoreExample
mill app.runMain examples.ErrorExample

# 開啟包含專案 classpath 的 Scala 互動式環境 (REPL)，方便隨時測試小段程式碼
mill app.console
```

## 專案結構

```
app/
├── src/
│   ├── MainApp.scala          # 進入點：基礎的 ZIO effect 與 Fiber 介紹
│   ├── LayerExample.scala     # ZLayer：現代化的依賴注入 (Dependency Injection)
│   ├── StreamExample.scala    # ZIO Streams：處理無窮無盡的資料流
│   ├── ScheduleExample.scala  # Schedule：優雅地處理重試與排程
│   ├── RefExample.scala       # Ref：安全、無鎖的可變狀態
│   ├── STMExample.scala       # STM：軟體交易記憶體 (多個變數的原子操作)
│   ├── ScopeExample.scala     # Scope：保證資源不外洩的生命週期管理
│   ├── QueueExample.scala     # Queue：讓不同的 Fiber 互相傳遞訊息
│   ├── HubExample.scala       # Hub：一對多的訊息廣播中心
│   ├── PromiseExample.scala   # Promise：一次性的任務同步與交接
│   ├── SemaphoreExample.scala # Semaphore：控制並行數量的號誌 (例如：API 速率限制)
│   └── ErrorExample.scala     # Error：型別安全的錯誤處理與復原
└── test/src/
    └── (對應每個範例的單元測試檔案，例如 MainAppSpec.scala)
```

## 建議學習順序

如果您是 ZIO 的新手，強烈建議您**依照以下順序**學習。每個主題都建立在前一個主題的概念之上，循序漸進能幫助您建立穩固的基礎。

1. **MainApp** — 了解什麼是 ZIO Effect，以及如何使用 for-comprehension 將它們串接起來。
2. **ErrorExample** — 學習 ZIO 如何在編譯時期強迫你處理錯誤，以及如何從錯誤中復原。
3. **RefExample** — 拋棄傳統的 `var`！學習 ZIO 如何安全地在並行環境下處理可變狀態。
4. **ScheduleExample** — 處理網路請求失敗的救星：學習使用策略自動重複與重試。
5. **LayerExample** — 學習使用 ZIO 內建的依賴注入來建構低耦合的應用程式。
6. **ScopeExample** — 告別 Try-Catch-Finally！安全地管理資源（如檔案、連線），保證資源必定被釋放。
7. **StreamExample** — 學習如何以低記憶體消耗處理大量甚至無限的資料序列。
8. **SemaphoreExample** — 學習如何限制並行任務的數量（例如：不要同時發出超過 10 個 API 請求）。
9. **QueueExample** — 學習並行程式中最重要的概念：讓多個獨立執行的任務 (Fiber) 交換資料。
10. **HubExample** — 學習如何將一筆資料同時廣播給多個傾聽中的任務。
11. **PromiseExample** — 學習等待某個條件達成（一次性訊號）再繼續執行的同步技巧。
12. **STMExample** — 終極武器：如何像關聯式資料庫的 Transaction 一樣，以原子方式同時更新多個狀態。

---

## 範例深度指南

### 1. MainApp — ZIO 基礎與 Fiber (輕量級執行緒)

> **核心概念：** 建立 Effect、串接 Effect、以及啟動 Fiber。

**您將學到什麼：**

- **Console I/O** — `Console.printLine` 不是印出文字，而是「回傳一個會印出文字的 Effect」。
- **For-comprehension** — 這是 Scala 用來循序執行 (Sequential) 程式碼的語法糖。每個 `<-` 都會等待前一個 effect 執行完畢，並將結果傳給下一行。
- **Fiber (纖程)** — 這是 ZIO 版本的「輕量級執行緒」。一般 JVM 執行緒很昂貴，但 ZIO 的 Fiber 非常輕量，你可以同時啟動數十萬個。
  - 使用 `.fork` 將一個任務放到背景執行 (啟動一個新 Fiber)。
  - 使用 `.join` 等待該背景任務完成並取得結果。

---

### 2. Ref — 執行緒安全的可變狀態

> **核心概念：** 在並行世界裡，傳統的 `var` 是危險的。`Ref` 提供了一個安全、無鎖 (Lock-free) 的變數容器。

**為什麼不直接用 `var`？** 
假設有 100 個人同時要把帳戶餘額加 1 元 (讀取 -> 加 1 -> 寫回)。使用 `var` 會發生覆寫衝突 (Race Condition)，最後餘額可能只加了 60 元。`Ref` 保證這些操作是「原子性 (Atomic)」的，也就是一次只有一個人能順利完成更新，確保結果絕對精準。

**您將學到什麼：**
- **`Ref.make(0)`** — 建立一個初始值為 0 的狀態容器。
- **`update` 與 `modify`** — 傳入一個函式，以原子方式安全地修改內容，並取得修改後的新狀態。

---

### 3. Schedule — 重複與重試策略

> **核心概念：** 當遇到不穩定的外部服務（例如資料庫連線超時、API 暫時無回應），我們需要重試。`Schedule` 把「重試邏輯」變成可重複使用的策略。

**您將學到什麼：**
- **指數退避 (Exponential Backoff)** — 失敗時不要馬上重試，而是等待 1秒、2秒、4秒、8秒... 這樣可以避免對已經癱瘓的伺服器造成更大壓力。
  ```scala
  // 範例：使用指數退避重試，但最多只重試 5 次
  effect.retry(Schedule.exponential(1秒) && Schedule.recurs(5))
  ```

---

### 4. ZLayer — 依賴注入 (Dependency Injection)

> **核心概念：** 解耦 (Decoupling)。你的業務邏輯不應該自己建立資料庫連線，而是「要求」別人提供給它。

**您將學到什麼：**
- **定義需求** — 在型別 `ZIO[R, E, A]` 中，宣告 `R` 需要 `Database` 服務。
- **提供實作** — 使用 `.provide(Layer)` 將實際的資料庫實作 (或測試用的假資料庫) 注入到應用程式中。ZIO 會在編譯時期檢查你是否忘記提供任何需要的依賴！

---

### 5. Scope — 資源管理 (再也不會忘記關閉檔案)

> **核心概念：** 有借有還，再借不難。`Scope` 保證你打開的資源一定會被關閉，即使程式發生例外崩潰。

**您將學到什麼：**
- **`ZIO.acquireRelease(獲取資源)(釋放資源)`** — 將開啟檔案與關閉檔案的動作綁定在一起。只要獲取成功，ZIO 保證在區塊結束時（無論是正常結束還是出錯崩潰），都會精準執行釋放動作。

---

### 6. ZIO Streams — 高效處理海量資料序列

> **核心概念：** 當你要處理 10GB 的日誌檔案時，你不能把它全部載入記憶體（會發生 OutOfMemory）。Stream 讓你可以像水管一樣，一小批一小批地處理資料。

**您將學到什麼：**
- **`ZStream` (水源)** — 資料的來源（例如：讀取檔案的一行、一個集合）。
- **`ZPipeline` (濾水器)** — 在資料流經時進行轉換或過濾（例如：把字串轉數字、過濾掉空白行）。
- **`ZSink` (水桶)** — 收集最終的結果（例如：計算總和、寫入另一個檔案）。

---

### 7. Queue — Fiber 之間的溝通橋樑

> **核心概念：** Fiber 之間不應該透過共享變數來溝通，而應該透過「傳遞訊息」來溝通。`Queue` 就是它們之間的郵筒。

**您將學到什麼：**
- **背壓 (Back-pressure)** — 如果生產者 (Producer) 製造資料的速度太快，消費者 (Consumer) 來不及處理怎麼辦？`Queue.bounded(N)` 會設定容量上限。當 Queue 滿了，生產者會自動「暫停 (Suspend)」等待，直到有空位為止。這完全不需要你寫任何 sleep 或 lock 程式碼！

---

### 8. Hub — 廣播給多個消費者

> **核心概念：** `Queue` 是一對一（一封信只能被一個人拿走），而 `Hub` 是一對多（像廣播電台，所有聽眾都聽得到）。

**您將學到什麼：**
- 當一筆資料需要同時被「寫入資料庫」與「發送 Email 通知」時，可以讓這兩個獨立的 Fiber 訂閱 (`subscribe`) 同一個 Hub。

---

### 9. Promise — 一次性 Fiber 同步

> **核心概念：** 一個只能被裝入一次值的盒子。用來讓一個 Fiber 等待另一個 Fiber 的通知。

**生活化比喻：** 就像接力賽跑，第二棒 (Fiber B) 在原地等待 (`await`)，直到第一棒 (Fiber A) 將接力棒交給他 (`succeed`)，第二棒才會開始起跑。

---

### 10. STM — 軟體交易記憶體 (Software Transactional Memory)

> **核心概念：** 當你需要同時更新「多個」獨立變數，且不能接受中途被打斷或出錯（這稱為交易 Transaction）。

**生活化比喻：** 從 A 帳戶扣 100 元，並把 100 元加到 B 帳戶。這兩個動作必須「同時成功」或「同時失敗」。STM 讓你可以把這兩個操作包裝成一個交易，底層原理與資料庫的 Transaction 非常相似，確保資料的絕對一致性。

---

### 11. Semaphore — 控制並行數量的號誌

> **核心概念：** 限制同時執行某段程式碼的 Fiber 數量。

**生活化比喻：** 一間只有 5 個座位的餐廳。當 5 個座位都被佔滿時，後面的客人 (Fiber) 必須在門口排隊等待 (`acquire`)，直到有人吃完離開 (`release`)。非常適合用來做 API 的速率限制 (Rate Limiting)，避免瞬間流量灌爆後端伺服器。

---

### 12. Error Handling — 失敗、缺陷與復原

> **核心概念：** ZIO 嚴格區分了「預期中的業務錯誤 (Failures)」與「不可預期的崩潰缺陷 (Defects)」。

- **Failure (預期的錯誤)**：例如「密碼錯誤」、「找不到用戶」。它會標示在 `ZIO[R, E, A]` 的 `E` 之中。編譯器會強迫你必須處理它（透過 `catchAll` 等語法）。
- **Defect (不可預期的缺陷)**：例如「NullPointerException」、「陣列越界」。這種錯誤代表程式有 Bug，會導致 Fiber 直接崩潰（死掉），它不會出現在型別簽名中。

**您將學到什麼：**
- 如何將容易拋出 Exception 的危險程式碼，轉換為安全的 ZIO Effect。
- 使用 `catchAll` 來優雅地捕捉並處理錯誤，給予使用者友善的回應。

---

## ZIO 並行原語 (Concurrency Primitives) 快速對照表

| 原語 | 可變性 | 寫入者 | 讀取者 | 常見生活比喻與使用場景 |
|---|---|---|---|---|
| **Ref** | 可多次更新 | 任何 Fiber | 任何 Fiber | **計數器**：記錄網站總瀏覽人數、切換設定標誌。 |
| **Promise** | 只能寫入一次 | 單一 Fiber | 多個 Fiber | **起跑槍/接力棒**：等待某個初始化動作完成後再繼續。 |
| **Queue** | 持續不斷 | 生產者們 | 每則訊息一個消費者 | **輸送帶**：將大量工作任務分發給背景的 Worker 處理。 |
| **Hub** | 持續不斷 | 發布者們 | 所有訂閱者 | **廣播電台**：價格更新時，同時通知所有圖表與日誌系統。 |
| **Semaphore**| 固定數量的許可 | 任何 Fiber | 任何 Fiber | **停車場車位**：限制最多只能同時發起 10 個資料庫連線。 |
| **STM/TRef** | 可多次更新 | 交易內執行 | 交易內執行 | **銀行轉帳**：確保多個帳戶的加減款項能原子性地完成。 |

## 學習資源

- [ZIO 官方入門文件 (英文)](https://zio.dev/overview/getting-started)
- [ZIO GitHub 專案庫](https://github.com/zio/zio)
- [Mill 建置工具官方文件](https://mill-build.org/)