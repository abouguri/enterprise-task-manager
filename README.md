# Enterprise Task Manager (Android)

An offline-first Android task manager demonstrating production-grade patterns: Clean
Architecture + MVVM, enterprise SSO via Microsoft Entra ID, a genuine offline sync queue with
server-side conflict resolution, and a Kotlin/Jetpack Compose codebase built the way a real
internal-tools team would build it — not a to-do-list toy.

The backend API this app talks to lives in a separate repo:
[TaskManager-Api](https://github.com/abouguri/enterprise-task-manager-api).

## What this demonstrates

- **Offline-first sync, not just a cache.** Every create/edit/delete is instant and local (Room),
  then queued and pushed to the backend by a WorkManager `SyncWorker`. Works fully offline;
  reconnecting drains the queue automatically, no user action required.
- **Real conflict resolution.** When the same task is edited from two places while one was
  offline, the server enforces last-write-wins by timestamp and returns `409 Conflict` with its
  own version of the row. The client detects this, discards the stale local edit, and adopts the
  server's version — verified end-to-end, not just unit-tested in isolation.
- **Enterprise auth done properly.** Sign-in goes through MSAL against a real Microsoft Entra ID
  App Registration — no hand-rolled OAuth, no tokens in `SharedPreferences`. Silent token refresh
  is used for background sync so the user isn't re-prompted to sign in just because a background
  job needed a fresh token.
- **Clean Architecture, enforced, not just aspired to.** Strict inward-only dependencies
  (`presentation → domain ← data`), zero Android imports in `domain/`, and a documented exception
  policy for the one deliberate deviation (`SyncWorker` talks to the DAO/API directly, since it's
  a background data-layer process, not a use-case-driven user action).

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Networking | Retrofit + OkHttp, kotlinx.serialization |
| Local DB | Room |
| Background sync | WorkManager (Hilt-integrated) |
| Async | Kotlin Coroutines + Flow |
| Auth | MSAL (Microsoft Authentication Library) for Entra ID |
| Backend | ASP.NET Core Web API ([separate repo](https://github.com/abouguri/enterprise-task-manager-api)) |

## Architecture

```
presentation/  →  domain/  ←  data/
   (Compose,        (pure         (Room, Retrofit,
   ViewModels)     Kotlin)         WorkManager, MSAL)
```

- `domain/` has zero Android or framework dependencies — pure Kotlin models, repository
  interfaces, and one `UseCase` class per business action.
- `data/` implements those interfaces. DTOs and entities never leak past their own layer; every
  crossing is through an explicit mapper function.
- `presentation/` only ever talks to `domain/` use cases — never a repository or DAO directly.
- Room is the single source of truth for the UI: network responses write into Room, they never
  feed the UI directly.

```
app/src/main/java/.../taskmanager/
├── data/
│   ├── local/        # Room entities, DAOs, TypeConverters
│   ├── remote/        # Retrofit API interfaces, DTOs, auth interceptor
│   ├── repository/    # Repository implementations (combines local + remote)
│   └── sync/          # SyncWorker, SyncScheduler — the offline queue
├── domain/
│   ├── model/          # Pure Kotlin domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # One class per business action
├── presentation/
│   ├── ui/               # Compose screens
│   ├── viewmodel/        # One ViewModel per screen
│   └── navigation/       # NavHost
└── di/                    # Hilt modules
```

The offline sync design in detail
(including exact conflict-resolution semantics), and a running log of non-obvious environment
gotchas discovered while building this.

## Status

- [x] Entra ID sign-in / sign-out, silent token refresh
- [x] Offline-first task CRUD (Room-backed, instant local writes)
- [x] Background sync to the backend (create/update/delete), network-constrained via WorkManager
- [x] Server-enforced last-write-wins conflict resolution, verified end-to-end
- [ ] File attachments
- [ ] Push notifications
- [ ] Search, dark mode
- [ ] Automated test coverage for use cases and sync logic

## Running locally

Requires JDK 17, the Android SDK, and the backend API running (see that repo's README) — this
app has nothing to talk to without it.

```bash
export JAVA_HOME=<path-to-jdk-17>

./gradlew assembleDebug        # build debug APK
./gradlew testDebugUnitTest    # unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (needs an emulator/device)
```

You'll need your own Entra ID App Registration and a `msal_config.json` — this repo doesn't ship
real credentials. (notably: the emulator reaches the host backend at `10.0.2.2`, never `localhost`).

## Related

- [TaskManager-Api](https://github.com/abouguri/enterprise-task-manager-api) — the ASP.NET Core
  backend this app syncs with, deployed on Azure App Service + Azure Database for PostgreSQL.
