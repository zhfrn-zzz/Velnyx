# VELNYX — Claude Code Project Context

> Strava-like running tracker for Android. Solo project, school deadline **May 31, 2026**. First Android project for the developer (experienced web/Laravel/Python background). Read this file at the start of every Claude Code session.

---

## What this project is

VELNYX is an Android running tracker. The graded demo executes this journey on a real outdoor run:

> Sign in (Google or email) → tap **START** → app records GPS while running outside, screen off, phone in pocket → pause / resume / finish → see summary with route polyline → run saves to cloud → reopen later, find the run in history, view route on map.

Everything else in the design (programs, clubs, info cards) is supporting decoration. **Do not build features outside the current milestone in `plan.md`.**

## Non-negotiable constraints

- **First Android project.** Prefer official Google patterns over clever ones. When in doubt, copy from `developer.android.com` samples.
- **5-week deadline.** Scope creep is risk #1. Cut features early, not late.
- **Demo on physical Android device.** Emulator only for inner-loop dev.
- **Indonesian Android OEMs (Xiaomi/Oppo/Vivo) aggressively kill background services.** Account for this in design and demo prep.
- **Single-module Gradle project.** Multi-module is overkill for this scope.

## Tech stack — locked

| Layer | Choice | Notes |
|---|---|---|
| Language | Kotlin (latest stable, JVM target 17) | — |
| UI | Jetpack Compose + Material 3 | No XML layouts. |
| Min SDK | API 26 (Android 8.0) | — |
| Target SDK | API 35 (Android 15) | — |
| Build | Gradle Kotlin DSL (`.gradle.kts`) | Use Version Catalog (`libs.versions.toml`). |
| Architecture | MVVM + UDF (Unidirectional Data Flow) | ViewModel exposes `StateFlow<UiState>`. |
| State | `StateFlow` / `SharedFlow` + Compose `collectAsStateWithLifecycle()` | No `LiveData`. |
| DI | Hilt | Standard Google recommendation. |
| Async | Kotlin Coroutines + Flow | — |
| Navigation | Navigation Compose | Type-safe routes via Kotlin Serialization. |
| Local DB | Room | For active-run buffer + offline cache. |
| Backend | Supabase (Auth + Postgres + Storage) via `supabase-kt` | Official community SDK, well-maintained. |
| Maps | MapLibre Native Android SDK (`org.maplibre.gl:android-sdk`) | Use `AndroidView` to embed in Compose. |
| Map tiles | MapTiler (free tier, 100k req/month) | Custom style JSON to match design aesthetic. |
| Location | `FusedLocationProviderClient` (Google Play Services Location) | Gold standard. |
| Background | Foreground `Service` with `foregroundServiceType="location"` | Required for Android 14+. |
| Permissions | Accompanist Permissions (`accompanist-permissions`) | Compose-native permission state. |
| Sensors (stretch) | `SensorManager` `TYPE_STEP_DETECTOR` | For cadence. |
| Image loading | Coil 3 | Compose-first. |
| Serialization | `kotlinx.serialization` | For network DTOs and saved state. |
| Lint | ktlint + detekt | Run before every commit. |

**Adding a new dependency requires justification in the commit message.** No vibe-installs.

## Project structure (single module, feature packages)

```
app/src/main/java/dev/zhafran/velnyx/
  VelnyxApp.kt                  # @HiltAndroidApp
  MainActivity.kt               # Single-activity, hosts NavHost
  core/
    data/
      db/                       # Room database, entities, DAOs
      network/                  # Supabase client, OkHttp config
      preferences/              # DataStore for user prefs
    location/                   # RunTrackingService (foreground), LocationRepository
    designsystem/
      theme/                    # Color, Typography, Shape, VelnyxTheme {}
      component/                # Buttons, Cards, Pills (reusable Compose)
    navigation/                 # NavGraph, Route sealed classes
    util/                       # PaceFormatter, DistanceFormatter, MetCalculator, polyline simplifier
  feature/
    auth/
      data/, domain/, presentation/
    home/
    tracking/                   # ⭐ ActiveRunViewModel + state machine + LiveTrackingScreen
    runsummary/
    history/                    # list + detail with map
    profile/
    programs/                   # static content
    clubs/                      # static read-only
  di/                           # Hilt modules: NetworkModule, DatabaseModule, etc.
```

Each feature follows: `data/` (repos, DTOs, mappers) — `domain/` (use cases if non-trivial; skip if just delegating) — `presentation/` (ViewModel + Composables).

## Architectural rules

1. **Single source of truth for active run state.** `ActiveRunViewModel` owns the state machine: `Idle → Countdown → Running → Paused → Running → Finishing → Finished`. Sealed class for `RunState`. No other component mutates run state.
2. **GPS points write to Room immediately on every location update.** Never trust RAM. App can be killed at any moment — recovery on next launch must read from local DB.
3. **Supabase upload happens only on `finish`.** One batched insert: `runs` row + many `run_points`. Don't sync per-point.
4. **No business logic in Composables.** Composables read state, dispatch events. Calculations in ViewModels, services, or `util/`.
5. **Repository pattern.** Composables and ViewModels never touch `SupabaseClient` or `RoomDatabase` directly.
6. **One `Activity`, many Composables.** Single-Activity architecture. Navigation via Navigation Compose.
7. **State holders are stable.** Use `@Immutable` / `data class` for UI state.
8. **Side effects in `LaunchedEffect` / `DisposableEffect`.** Never call repos directly inside Composable bodies.

## Design tokens

`#BAFF29` is the primary green (electric lime, confirmed). Surface white, accent black. Pill-shaped buttons, card radius 16–20dp, large tap targets (running app: gloves + sweat + bouncing).

```kotlin
// core/designsystem/theme/Color.kt
val VelnyxLime = Color(0xFFBAFF29)
val VelnyxBlack = Color(0xFF0A0A0A)
val VelnyxWhite = Color(0xFFFFFFFF)
val VelnyxGray100 = Color(0xFFF5F5F5)
// ... extract rest from Figma
```

Type: extract exact font names from Figma export and configure via `Typography` in `VelnyxTheme`. Add fonts to `res/font/` or use Google Fonts downloadable.

## Critical Android gotchas

1. **Permission ladder, in this order:**
   - `POST_NOTIFICATIONS` (Android 13+) — needed for foreground service notification
   - `ACCESS_FINE_LOCATION` — request first
   - `ACCESS_BACKGROUND_LOCATION` — only after fine is granted, on a *separate* user-initiated tap. Android 11+ requires deep-linking to system Settings; the system dialog won't appear. Show your own rationale screen explaining why before sending the user out.

2. **Foreground service for tracking.** Manifest:
   ```xml
   <service
     android:name=".core.location.RunTrackingService"
     android:foregroundServiceType="location"
     android:exported="false" />
   ```
   `AndroidManifest.xml` must also declare `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_LOCATION` permissions. Use `ServiceCompat.startForeground(this, NOTIF_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)` on Android 14+.

3. **Battery optimization whitelist.** First-run onboarding must guide user to disable battery optimization for VELNYX. Use `Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)` (requires `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission). Without this, OEM kills the service after ~10–30 minutes.

4. **OEM autostart permissions** (Xiaomi MIUI, Oppo ColorOS, Vivo FuntouchOS). Cannot be requested programmatically — user must enable autostart manually in OEM-specific settings. Document in onboarding with screenshots if possible.

5. **GPS noise filtering:**
   - Discard fixes with `accuracy > 30m`
   - Ignore distance contribution if `speed < 0.5 m/s` (filter standing still)
   - Without these, distance creeps when paused at traffic lights

6. **Polyline simplification.** A 5km run = 1000+ raw points. Apply Douglas-Peucker (epsilon ~5m) before storing to Supabase or rendering on detail map. Otherwise the detail map gets sluggish.

7. **MapLibre + Compose lifecycle.** Wrap `MapView` in `AndroidView { factory = ::MapView }`. Use `DisposableEffect` to forward lifecycle events (`onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`, `onLowMemory`) to the MapView, otherwise it leaks.

8. **MapLibre attribution required.** Bottom-right of every map: "© OpenStreetMap contributors / © MapTiler".

9. **Supabase RLS is mandatory.** Every table has `user_id uuid` and policy `auth.uid() = user_id`. Test isolation with two accounts. Never disable RLS, even "temporarily."

10. **`supabase-kt` is JVM-first, not Android-first.** Most APIs are coroutine-based. Wrap in repositories that expose `Flow<T>` or `suspend fun` to ViewModels.

11. **Process death recovery.** If Android kills the process during a run (low memory, OEM, manual swipe-away), the foreground service should self-restart and resume reading GPS into the same `active_run` Room entry. On next app launch, check for unfinished active runs and offer "resume" or "discard".

## DO NOT build (scope guards)

- ❌ Apple sign-in, Facebook sign-in (only Google + email)
- ❌ Music player or now-playing integration
- ❌ Real-time social feed, posts, kudos, comments
- ❌ Real club join/leave/post (static read-only stub only)
- ❌ Real program enrollment or progress tracking (static content only)
- ❌ Live cadence during run *unless* in stretch milestone
- ❌ Elevation gain *unless* in stretch milestone (and only post-run via API, not live)
- ❌ Weather chip on home screen
- ❌ Push notifications
- ❌ iOS / Wear OS / Apple Watch

If Claude Code suggests "while we're here, let's add X" and X is in this list — **decline.**

## Build & run

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device
./gradlew assembleRelease               # final school submission build
./gradlew lint                          # Android lint
./gradlew ktlintCheck detekt            # style + static analysis
./gradlew testDebugUnitTest             # JVM unit tests
```

**Important:** Test GPS / battery / foreground service in **release build** on a real device, not just debug. Debug builds get different battery treatment from the OS.

## Code conventions

- ktlint + detekt enforced. Run before every commit.
- One `@Composable` per file once the function exceeds ~50 lines or has private helpers.
- ViewModel state is a single `data class UiState`. No multiple `StateFlow`s per screen.
- Sealed classes for finite state (`RunState`, `AuthState`).
- `private val` first, then `val`, then `var`. No `var` outside `ViewModel` internals.
- User-visible strings live in `res/values/strings.xml`. (Not localized for MVP, but centralized.)
- No magic numbers — extract to `core/designsystem` or feature-level `const val`.
- All `suspend` paths handle `CancellationException` correctly (never catch it generically).
- Composables stateless when possible. State hoisted to ViewModel.

## Demo device

Decide which physical Android device VELNYX will be demo'd on **by end of M0**. From that point, every milestone's "definition of done" includes "tested on demo device, not just emulator." Background GPS behavior varies enormously by OEM — there's no point developing against an emulator and discovering on May 30 that your demo phone kills the service.

## When unsure

1. Check `plan.md` for current milestone scope.
2. If a task isn't in the current milestone's checklist, **don't do it.**
3. If something blocks the current task and isn't covered here, ask the developer before improvising.
4. Prefer `developer.android.com` and `kotlinlang.org` examples over Stack Overflow.
