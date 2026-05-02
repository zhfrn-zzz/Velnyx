# VELNYX — Project Plan

| | |
|---|---|
| **Today** | April 26, 2026 |
| **Deadline** | May 31, 2026 (35 days) |
| **Developer** | Solo, first Android project (experienced web/Laravel/Python dev) |
| **Stack** | Kotlin + Jetpack Compose + Supabase + MapLibre |
| **Realistic budget** | **~180–210 productive hours** |

---

## 1. Goal

Ship a demo-ready Android APK that executes this journey reliably on a real outdoor run:

> Sign in (Google or email) → tap **START** → app records GPS while running outside, screen off, phone in pocket → pause / resume / finish → see summary (distance, duration, avg pace, calories, route polyline) → run saves to cloud → reopen later, find run in history, view route on map.

That single journey is the **graded thesis**. Everything else is decoration that makes the demo look complete.

## 2. In-scope features

**Core (must work on demo day):**
1. Auth: email/password + Google sign-in via Supabase
2. First-run onboarding: name, weight (for calorie calc), permission ladder, battery whitelist
3. Home screen with prominent START + recent activity preview
4. Run tracking: countdown → live tracking → pause/resume → finish
5. Live tracking screen: distance, duration, avg pace, calories, map (live position)
6. Run summary: full stats + route polyline
7. Run persistence: saved to Supabase, recoverable after app kill
8. History list (total km hero + per-run cards)
9. Run detail: stats grid + map with polyline
10. Profile + sign out

**Supporting (visual completeness):**
11. Static Running Programs screen (Eliud Kipchoge, Jacob Kiplimo, Joshua Cheptegei cards from local JSON, no enrollment)
12. Static Clubs discovery screen (read-only Tangerang Runners / JakBar Pacer mock list)
13. Running Information cards on home (static articles from local JSON)

**Stretch (only if M1–M3 finish on time):**
14. Cadence during run via `SensorManager` step detector
15. Elevation gain post-run via Open-Elevation API on saved polyline

## 3. Non-goals (cuts, with rationale)

| Cut | Why |
|---|---|
| Apple sign-in, Facebook sign-in | Adds 2× provider config, Apple-on-Android is awkward, Google covers ~95% of users in Indonesia. Email fallback exists. |
| Music player / now-playing | Licensing + MediaSession is a rabbit hole. Not related to "running tracker" thesis. |
| Real-time social feed, kudos, comments | Realtime + moderation + abuse handling = backend complexity equal to the rest of the project combined. |
| Real club join/leave/post | Same as above. |
| Real program enrollment + progress tracking | Static visual content only. |
| Live cadence (unless stretch achieved) | Pedometer fusion is non-trivial and the GPS run alone proves the thesis. |
| Live elevation | GPS altitude noise is ±15m. Only post-run via API in stretch. |
| Weather chip on home | Cosmetic. OpenWeatherMap key management = friction with no demo payoff. |
| Push notifications | — |
| iOS, Wear OS, Apple Watch | Single platform. |
| Dark mode | Defer post-deadline. |
| Multi-language | English only. |

## 4. Brand

**VELNYX** confirmed. **#BAFF29** primary.

---

## 5. Milestones

### M0 — Setup & Foundations (Apr 26 – Apr 29, 4 days)

**Goal:** Project boots, connects to Supabase, shows themed placeholder home. Background GPS proven on demo device.

- [ ] Install latest Android Studio + JDK 17
- [ ] Confirm physical demo device (which phone will VELNYX be demo'd on?), enable USB debugging, run a Hello World on it
- [ ] `New Project → Empty Activity (Compose)`, package `dev.zhafran.velnyx`, min SDK 26, target 35
- [ ] Convert build files to Kotlin DSL if not already; set up `libs.versions.toml`
- [ ] Add deps: Compose BOM, Material 3, Hilt, Navigation Compose, Room, kotlinx.serialization, Coroutines, Lifecycle Runtime, supabase-kt (auth + postgrest), MapLibre Android SDK, Coil, Accompanist Permissions, Play Services Location
- [ ] Set up Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`)
- [ ] Set up Supabase cloud project; copy URL + anon key into `local.properties`, expose via `BuildConfig`
- [ ] Init git, push to GitHub private repo
- [ ] Configure ktlint + detekt as Gradle plugins
- [ ] Folder structure per CLAUDE.md
- [ ] `VelnyxTheme` with #BAFF29 + placeholder typography
- [ ] **GPS smoke test:** 50-line throwaway screen that requests fine location, starts a foreground service, logs GPS fixes to Logcat. Walk outside for 10 minutes with screen off. **Verify it works on the demo device.** If background service gets killed → debug now (battery whitelist? OEM autostart?), don't postpone.

**Acceptance:** App runs on demo device, hot reload works, `./gradlew lint ktlintCheck` clean, **GPS smoke test successful for 10+ min with screen off.**

---

### M1 — Auth + Theme + Navigation (Apr 30 – May 4, 5 days)

**Goal:** Real auth flows working, theme matches design, navigation skeleton in place.

- [ ] Extract exact tokens from Figma into `core/designsystem/theme/` (Color, Typography, Shape)
- [ ] Add fonts to `res/font/` (or configure Google Fonts downloadable)
- [ ] Build screens per design: Splash → Get Started → Join Us → Log In
- [ ] Reusable Composables: `VelnyxButton` (primary lime, secondary outlined, large), `VelnyxTextField`, `VelnyxCard`
- [ ] Email/password sign-up + log-in via `supabase-kt` `Auth`
- [ ] Google OAuth via Supabase + Credential Manager API (allocate **a full day** — Google Cloud Console + redirect URIs + Credential Manager are fiddly)
- [ ] `AuthRepository` exposes `Flow<AuthState>` (sealed: `Loading`, `Authenticated(user)`, `Unauthenticated`)
- [ ] Root `NavHost` with auth-aware redirect logic
- [ ] First-time profile bootstrap modal: display name + weight (kg, needed for calorie formula). Saved to `profiles` table on Supabase + DataStore locally.
- [ ] Drawer / settings entry with sign-out
- [ ] Form validation, error snackbars, loading states everywhere

**Acceptance:** Cold start lands correctly based on auth. Google sign-in completes round-trip on demo device. Sign out returns to Get Started. New user prompted for weight.

---

### M2 — Run Tracking Core (May 5 – May 16, 12 days) ⭐ **the hard one**

**Goal:** End-to-end GPS run tracking proven on multiple real outdoor runs.

- [ ] Permission flow (POST_NOTIFICATIONS → ACCESS_FINE_LOCATION → ACCESS_BACKGROUND_LOCATION) with rationale screens per CLAUDE.md
- [ ] Battery optimization whitelist prompt
- [ ] OEM autostart explainer (with screenshots if demo device is Xiaomi/Oppo/Vivo)
- [ ] `RunTrackingService` foreground service:
  - declared in manifest with `foregroundServiceType="location"`
  - sticky notification with current distance + duration
  - subscribes to `FusedLocationProviderClient` (1s update interval, high accuracy)
  - exposes location stream to ViewModel via repository
- [ ] Room schema: `ActiveRun`, `ActiveRunPoint`, `ActiveRunSegment` (segments mark pause/resume splits)
- [ ] `ActiveRunViewModel` state machine using sealed `RunState`: `Idle | Countdown(secLeft) | Running(stats) | Paused(stats) | Finishing | Finished(summary)`
- [ ] Pre-run countdown screen (3-2-1-GO, big, animated)
- [ ] Live tracking screen per design:
  - hero distance "00,00"
  - avg pace, duration, calories row
  - mini-map with live position + accumulated polyline
  - control row: map button, pause/resume, finish
  - bottom: GPS strength indicator (top of design shows this)
- [ ] On every GPS fix in service:
  - filter `accuracy > 30m`
  - filter `speed < 0.5 m/s` from distance accumulation
  - append point to Room (immediate write)
  - emit updated stats to ViewModel
- [ ] Pause: stop accumulating distance/duration, keep position stream alive (or stop and resume to save battery — decide based on smoke test)
- [ ] Resume: continue accumulation, mark segment break in Room
- [ ] Finish: stop service, compute summary stats, persist final values, navigate to Run Summary
- [ ] Calorie calc utility: `kcal = MET × weightKg × hours`. MET lookup by avg pace bucket: walking ~3.5, jogging ~7, running fast ~10. Document the lookup table.
- [ ] Run Summary screen per design (post-finish)
- [ ] **Field test ≥ 4 times during this milestone.** Walk a measurable route. Verify distance is within ±10% of map distance, polyline matches actual path, app survives screen-off for 30+ minutes.

**Acceptance:** A 2km outdoor walk with phone in pocket and screen off produces: distance accurate within ±10%, polyline shape matches real path, app didn't get killed, run is in Room DB ready for upload.

---

### M3 — Persistence + History + Map Detail (May 17 – May 22, 6 days)

**Goal:** Finished runs upload to Supabase. History list and detail (with map) work end-to-end.

- [ ] Supabase schema:
  ```sql
  create table profiles (
    id uuid primary key references auth.users on delete cascade,
    display_name text,
    weight_kg numeric,
    created_at timestamptz default now()
  );

  create table runs (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references auth.users on delete cascade not null,
    started_at timestamptz not null,
    ended_at timestamptz not null,
    distance_m int not null,
    duration_s int not null,
    avg_pace_s_per_km int,
    calories int,
    created_at timestamptz default now()
  );

  create table run_points (
    run_id uuid references runs on delete cascade,
    idx int not null,
    lat double precision not null,
    lon double precision not null,
    recorded_at timestamptz not null,
    accuracy real,
    primary key (run_id, idx)
  );
  ```
- [ ] RLS policies on all three tables: `auth.uid() = user_id` (and EXISTS check via parent for `run_points`)
- [ ] Polyline simplification (Douglas-Peucker, epsilon 5m) before upload
- [ ] On finish: upload run + simplified points in single transaction (use `supabase-kt` postgrest with batched insert)
- [ ] `HistoryRepository` exposes `Flow<List<RunSummary>>` from Supabase, cached locally in Room
- [ ] History list screen per design: total km hero card on top, per-run cards (date, distance, pace)
- [ ] History detail screen: stats grid + MapLibre map with polyline + start/end markers
- [ ] MapLibre Compose wrapper (`VelnyxMap` Composable using `AndroidView` + `DisposableEffect` for lifecycle)
- [ ] Custom MapLibre style JSON (start from MapTiler Streets v2, restyle to match design aesthetic — black roads on white, lime accent for selected polyline)
- [ ] Empty state (no runs yet)
- [ ] Pull-to-refresh
- [ ] Loading skeletons + error states

**Acceptance:** Complete a run → kill app → reopen → run is in history → tap → route renders on map with correct shape.

---

### M4 — Visual completeness + stretch (May 23 – May 28, 6 days)

**In priority order. Complete top-down. Stop when polish is solid.**

1. **Static Running Programs screen** — JSON file in `assets/` with 4–6 programs (Kipchoge marathon, Kiplimo half, Cheptegei 10K, etc.). Beautiful card list, detail screen with day-by-day breakdown per design (Mon–Sun pills). No real enrollment, no tracking.
2. **Static Clubs discovery screen** — JSON with 6–8 mock Indonesian running clubs (Tangerang Runners, JakBar Pacer, etc.). Card list + detail showing static "posts" with images. No real join.
3. **Running Information section on home** — JSON-driven article cards per design.
4. **Universal polish pass:** error states, loading skeletons, empty states, snackbars on errors, IME (keyboard) handling on auth screens, accessibility content descriptions on icon buttons.
5. **Onboarding tutorial overlay** — first-time guide to GPS permission flow + battery whitelist (most important — without this, demo dies).
6. **STRETCH:** Cadence during run via `SensorManager` `TYPE_STEP_DETECTOR`. Show live BPM (steps × 60 / minutes). Save to `runs.cadence_avg`.
7. **STRETCH:** Elevation gain post-run via Open-Elevation API. POST simplified polyline → receive elevation array → compute total ascent. Save to `runs.elevation_gain_m`. Async, doesn't block save.

---

### M5 — Demo prep + buffer (May 29 – May 31, 3 days) — **DO NOT SKIP**

- [ ] `./gradlew assembleRelease`, sign with debug or release keystore, install on demo device
- [ ] **Pre-record a backup video** of a successful end-to-end run. If demo gods abandon you on May 31, this video saves the grade.
- [ ] Final field test of the full demo flow on demo device with release APK
- [ ] README polish: screenshots, install instructions, architecture overview, screencast link
- [ ] Project report / documentation for school submission (likely required)
- [ ] Tag `v1.0.0` in git, push final commit

---

## 6. Risk register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Background GPS killed by OEM (Xiaomi/Oppo/Vivo) on demo device | **High** | **High** | M0 GPS smoke test on demo device. If it fails, debug *now*. Document battery whitelist + autostart in onboarding. Backup video for demo day. |
| Compose learning curve eats more budget than expected | Med | Med | Stick to standard patterns. Don't try fancy custom layouts. Lean on official Compose samples. |
| Google Sign-in via Credential Manager + Supabase setup is fiddly | High | Med | Allocate full day in M1. Email auth is fallback for demo. |
| MapLibre + Compose interop has rough edges | Med | Med | Build a tiny standalone `MapView` in Compose during M0 as proof. If it's painful, fallback: render route polyline as Compose `Canvas` with manual lat/lon → screen projection over a static image. Uglier but works. |
| supabase-kt has API breaking changes / incomplete docs | Med | Med | Pin to a recent stable version in `libs.versions.toml`. Don't auto-update. Have raw Ktor/REST fallback for postgrest if SDK fights you. |
| GPS accuracy unreliable in dense Bekasi/Jakarta urban area | Med | Med | Test in open field first to validate pipeline. Then test in real conditions. Tune accuracy filter from 30m to 50m if needed. |
| Scope creep from "but the design shows…" | High | High | This document is the authoritative scope. Designs are aspirational. Anything not in a milestone checklist is out. |
| AI overproduction: false-completion claims | **High** | **High** | Verify every "done" task on real device, not by re-reading code. Don't trust Claude Code's self-assessment of features that touch hardware (GPS, sensors, services). |
| Process death during run = data loss | Med | High | Every GPS fix writes to Room synchronously inside service. On app launch, check for orphaned active run, offer resume/discard. |
| Demo device decision deferred and bites later | Med | High | Lock demo device in M0. All milestone "done" gates require testing on it. |

## 7. Decision log

| Date | Decision | Rationale |
|---|---|---|
| 2026-04-26 | Kotlin + Compose over Flutter | Native is stronger on background GPS reliability (project's #1 risk), iOS already cut so cross-platform value of Flutter = 0, more career value in Kotlin for Indonesian Android dev market. |
| 2026-04-26 | Single-Activity + Compose | Modern Android default. Less ceremony than multi-Activity. |
| 2026-04-26 | Hilt over Koin | Google standard. Compile-time DI = catches errors early. |
| 2026-04-26 | Room over SQLDelight | Google standard, official docs everywhere, KSP-based codegen. |
| 2026-04-26 | supabase-kt over manual REST | Auth + Postgrest abstractions save real time. Risk: SDK edge cases — mitigated by Ktor fallback. |
| 2026-04-26 | MapLibre Native + MapTiler tiles | Custom styling possible to replicate mapcn aesthetic; OSS engine; free tier covers school project. |
| 2026-04-26 | `FusedLocationProviderClient` over raw `LocationManager` | Industry standard, battery-aware, smoothing built-in. |
| 2026-04-26 | Foreground Service over WorkManager for tracking | WM is for periodic background work. Run tracking needs continuous GPS — that's a foreground service. |
| 2026-04-26 | Brand: VELNYX, primary `#BAFF29` | Confirmed by developer. |
| 2026-04-26 | Cuts: Apple/FB auth, music, real social, real clubs, weather, iOS, dark mode | Each documented in §3 with reason. |

## 8. Working agreement with Claude Code

1. Every Claude Code session begins by reading `CLAUDE.md` and this `plan.md`.
2. A feature is in scope **only if it appears in the current milestone's checklist.**
3. New dependencies require justification in commit message.
4. Every commit message references a milestone task (e.g. "M2: GPS noise filter").
5. If Claude Code suggests something outside scope — even "small improvements" — answer is no. Note in `BACKLOG.md` for after deadline.
6. **Verify hardware-touching features on real device, not by re-reading code.** Especially: GPS, foreground service, permissions, sensors. Claude Code cannot reliably self-verify these.
7. **The plan is a living document.** When reality forces a change (a milestone slips, a risk fires), update this file in the same commit. Don't pretend the plan didn't change.

## 9. Open items

- [ ] Confirm demo device by end of M0
- [ ] Extract exact font names from Figma (currently placeholder)
- [ ] Decide stretch items go/no-go at M3 wrap-up based on real time remaining — not now