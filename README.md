# Download Manager

A focused Android download manager built with Kotlin and Jetpack Compose.

Paste a direct file URL, download it in the background with WorkManager, track progress through notifications, and manage completed files from an in-app download history with open, share, retry, cancel, delete, and clear actions.

* * *

## Overview

Download Manager is a lightweight reference app for direct file downloads on Android. It combines a Jetpack Compose UI, a Room-backed download history, and a WorkManager-powered background pipeline so downloads can continue reliably outside the foreground app session.

The app follows a clean, layered architecture (UI → domain → data) with Koin for dependency injection.

* * *

## Features

- Download files from direct `http` or `https` URLs.
- Run downloads in the background with WorkManager foreground execution.
- Show progress, completion, and failure notifications.
- Persist download history with Room.
- Retry, cancel, delete, or clear downloads.
- Open or share completed files using `FileProvider`.
- Block unsupported streaming hosts such as YouTube and Vimeo.

> This app is for direct file URLs. It is not a media extractor for streaming platforms.

* * *

## Screens and user flows

**Home / Download screen**
- Paste a direct file URL into the input field and tap "Start Download".
- While a download is in progress, the button shows "Working..." and is disabled.
- Download errors are shown inline below the URL field.

**Download history**
- Lists all persisted downloads with file name, URL, status, and progress.
- A progress bar reflects completion for running and finished downloads.
- Completed downloads offer "Open" and "Share" actions.
- Failed or cancelled downloads offer "Retry".
- All downloads offer "Delete"; "Clear All" removes the entire history.
- An empty state is shown when there are no downloads.

**Background**
- Downloads are enqueued as unique WorkManager one-time work requests.
- A foreground notification shows progress while the download runs.
- Completion and failure notifications are posted when the work finishes.

* * *

## Technology stack

| Concern | Library / Tool | Version |
| --- | --- | --- |
| Language | Kotlin | `2.2.10` |
| UI | Jetpack Compose BOM | `2026.02.01` |
| Architecture | MVVM + Clean Architecture | - |
| Dependency injection | Koin | `4.2.2` |
| Persistence | Room | `2.8.3` |
| Background work | WorkManager | `2.11.2` |
| Networking | OkHttp | `5.4.0` |
| Build | Android Gradle Plugin | `9.2.1` |
| Gradle | Gradle wrapper | `9.4.1` |
| Minimum SDK | Android | `24` |
| Target SDK | Android | `36` |

* * *

## Architecture and project structure

The app is organized into layered packages under `com.abhijeet.downloadmanager`:

```
app/src/main/java/com/abhijeet/downloadmanager/
├── core/             # File helpers, DI modules, utilities
├── data/             # Room database, downloader, repository implementation
├── domain/           # Models, repository contract, use cases
├── notification/     # Foreground and status notifications
├── permission/       # Notification permission handling
├── presentation/     # Compose screens, UI state, ViewModel, components
├── receiver/         # Boot receiver
└── worker/           # WorkManager download worker
```

Layers:
- **Presentation** (`presentation`): Jetpack Compose screens, UI state models, ViewModel, UI events. State is collected with `collectAsStateWithLifecycle` and exposed via `StateFlow`/`SharedFlow`.
- **Domain** (`domain`): `DownloadItem`/`DownloadStatus` models, `DownloadRepository` interface, and single-responsibility use cases (start, cancel, retry, delete, open, share, clear).
- **Data** (`data`): Room database, DAO, entity, mapper, and `DownloadRepositoryImpl` backed by WorkManager and OkHttp.
- **Core** (`core`): Koin DI modules, file manager, file name resolver, MIME type resolver, URL validator, constants.

* * *

## State management

The UI uses a unidirectional data flow:
- `DownloadViewModel` exposes `uiState: StateFlow<DownloadUiState>` and `intents: SharedFlow<Intent>`.
- `DownloadScreen` collects `uiState` with `collectAsStateWithLifecycle()` and emits navigation intents from the `intents` shared flow.
- User actions are dispatched as `DownloadEvent` objects to `viewModel.onEvent()`.
- State is hoisted: the screen is stateless and receives state and callbacks as parameters.

* * *

## Navigation

The app currently implements a single screen (`DownloadScreen`). Navigation from that screen (opening or sharing a downloaded file) is handled through Android `Intent`s emitted by the ViewModel and launched via `ActivityResultLauncher`/`context.startActivity()`. There is no multi-destination navigation graph in this version.

* * *

## Data persistence and networking

- Download metadata and history are persisted in a Room database (`download.db`, schema version 2).
- Files are downloaded with OkHttp on `Dispatchers.IO`, with progress reported back to the repository and WorkManager foreground notification.
- WorkManager enqueues each download as a unique one-time work request with `NetworkType.CONNECTED` constraints and a `DownloadWorkerFactory`.
- Completed and shared files are exposed through `FileProvider` (`${applicationId}.fileprovider`) with `file_paths.xml`.

* * *

## Prerequisites

- Android Studio with a recent Android SDK setup
- JDK 11+
- An Android device or emulator running API 24+
- Android SDK Build-Tools and platform for compileSdk 37 / targetSdk 36

* * *

## Setup and installation

### Clone

```bash
cd DownloadManager
```

### Open in Android Studio

1. Open Android Studio and select "Open an existing project".
2. Choose the `DownloadManager` folder (this project root).
3. Wait for the Gradle sync to complete.

### Build

```bash
./gradlew :app:assembleDebug
```

### Install on a device or emulator

```bash
./gradlew :app:installDebug
```

Or run the `app` configuration directly from Android Studio.

* * *

## Usage

1. Paste a direct file URL into the input field.
2. Tap "Start Download".
3. Grant notification permission on Android 13+ when prompted.
4. Monitor progress from the app or the foreground notification.
5. Open or share the file once the download is complete.

* * *

## Testing

Run the unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

The project includes unit tests for the ViewModel, URL validator, file name resolver, download mapper, and start-download use case.

* * *

## Configuration and environment variables

The app requires no API keys, access tokens, or environment variables. All configuration is handled through Gradle (`app/build.gradle.kts`) and `gradle/libs.versions.toml`.

If you integrate Firebase, Google Sign-In, OAuth, app links, or push notifications, configure those services in their respective consoles for the application ID `com.abhijeet.downloadmanager`. No service credentials are stored in this repository.

* * *

## License and attribution

This project is provided as a reference implementation. Third-party libraries are distributed under their own licenses; please review the licenses of Kotlin, Jetpack Compose, Koin, Room, WorkManager, OkHttp, and the AndroidX libraries before use.

The launcher icons and sample images are included for demonstration purposes.

* * *

Demo images are stored under `DemoImages/`.
