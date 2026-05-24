package dev.zhafran.velnyx.core.navigation

import kotlinx.serialization.Serializable

// ── Auth ─────────────────────────────────────────────────────────────────────
@Serializable
data object SplashRoute

@Serializable
data object GetStartedRoute

@Serializable
data object LoginRoute

@Serializable
data object SignUpRoute

@Serializable
data object ProfileSetupRoute

@Serializable
data class ConfirmEmailRoute(val email: String)

// ── Onboarding (permission wizard steps) ────────────────────────────────────
@Serializable
data object PermissionNotificationsRoute

@Serializable
data object PermissionLocationRoute

@Serializable
data object PermissionBackgroundRoute

@Serializable
data object PermissionBatteryRoute

@Serializable
data object PermissionAutostartRoute

// ── Home ─────────────────────────────────────────────────────────────────────
@Serializable
data object HomeRoute

// ── Tracking ─────────────────────────────────────────────────────────────────
@Serializable
data object TrackingGraphRoute

@Serializable
data object CountdownRoute

@Serializable
data class LiveTrackingRoute(val resumeRunId: Long = -1L)

@Serializable
data class RunSummaryRoute(
    val distanceM: Int = 0,
    val durationS: Int = 0,
    val avgPace: Int = 0,
    val calories: Int = 0,
)

// ── History ──────────────────────────────────────────────────────────────────
@Serializable
data object HistoryListRoute

@Serializable
data class HistoryDetailRoute(val runId: String)

// ── Supporting ───────────────────────────────────────────────────────────────
@Serializable
data object ProgramsRoute

@Serializable
data object ClubsRoute

// ── Profile ──────────────────────────────────────────────────────────────────
@Serializable
data object SettingsRoute
