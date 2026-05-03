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

// ── Onboarding ───────────────────────────────────────────────────────────────
@Serializable
data object PermissionsRoute

// ── Home ─────────────────────────────────────────────────────────────────────
@Serializable
data object HomeRoute

// ── Tracking ─────────────────────────────────────────────────────────────────
@Serializable
data object CountdownRoute

@Serializable
data object LiveTrackingRoute

@Serializable
data class RunSummaryRoute(val runId: String)

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
