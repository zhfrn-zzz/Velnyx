package dev.zhafran.velnyx.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import dev.zhafran.velnyx.feature.auth.presentation.ConfirmEmailScreen
import dev.zhafran.velnyx.feature.auth.presentation.GetStartedScreen
import dev.zhafran.velnyx.feature.auth.presentation.LoginScreen
import dev.zhafran.velnyx.feature.auth.presentation.SignUpScreen
import dev.zhafran.velnyx.feature.auth.presentation.SplashScreen
import dev.zhafran.velnyx.feature.profile.presentation.ProfileSetupScreen
import dev.zhafran.velnyx.feature.clubs.presentation.ClubsScreen
import dev.zhafran.velnyx.feature.history.presentation.HistoryDetailScreen
import dev.zhafran.velnyx.feature.history.presentation.HistoryListScreen
import dev.zhafran.velnyx.feature.home.presentation.HomeScreen
import dev.zhafran.velnyx.feature.home.presentation.InfoDetailScreen
import dev.zhafran.velnyx.feature.onboarding.presentation.PermissionAutostartScreen
import dev.zhafran.velnyx.feature.onboarding.presentation.PermissionBackgroundScreen
import dev.zhafran.velnyx.feature.onboarding.presentation.PermissionBatteryScreen
import dev.zhafran.velnyx.feature.onboarding.presentation.PermissionLocationScreen
import dev.zhafran.velnyx.feature.onboarding.presentation.PermissionNotificationsScreen
import dev.zhafran.velnyx.core.util.PermissionHelper
import dev.zhafran.velnyx.feature.profile.presentation.SettingsScreen
import dev.zhafran.velnyx.feature.programs.presentation.ProgramsScreen
import dev.zhafran.velnyx.feature.runsummary.presentation.RunSummaryScreen
import dev.zhafran.velnyx.feature.tracking.presentation.ActiveRunViewModel
import dev.zhafran.velnyx.feature.tracking.presentation.CountdownScreen
import dev.zhafran.velnyx.feature.tracking.presentation.LiveTrackingScreen
import dev.zhafran.velnyx.feature.tracking.presentation.RunState

@Composable
fun VelnyxNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
    ) {
        // ── Auth ─────────────────────────────────────────────────────────────
        composable<SplashRoute> {
            SplashScreen(
                onGoToGetStarted = {
                    navController.navigate(GetStartedRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onGoToProfileSetup = {
                    navController.navigate(ProfileSetupRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onGoToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<GetStartedRoute> {
            GetStartedScreen(
                onNavigateToSignUp = { navController.navigate(SignUpRoute) },
                onNavigateToLogin = { navController.navigate(LoginRoute) },
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(SignUpRoute) },
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(GetStartedRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<SignUpRoute> {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                    navController.navigate(LoginRoute)
                },
                onNavigateBack = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(ProfileSetupRoute) {
                        popUpTo(GetStartedRoute) { inclusive = true }
                    }
                },
                onConfirmEmailRequired = {
                    navController.navigate(ConfirmEmailRoute(email = it)) {
                        popUpTo(SignUpRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<ProfileSetupRoute> {
            ProfileSetupScreen(
                onProfileSaved = {
                    navController.navigate(PermissionNotificationsRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<ConfirmEmailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ConfirmEmailRoute>()
            ConfirmEmailScreen(
                email = route.email,
                onNavigateBack = {
                    navController.navigate(LoginRoute) {
                        popUpTo(GetStartedRoute)
                    }
                },
            )
        }

        // ── Onboarding (permission wizard) ──────────────────────────────────
        composable<PermissionNotificationsRoute> {
            PermissionNotificationsScreen(
                onContinue = {
                    navController.navigate(PermissionLocationRoute) {
                        popUpTo(PermissionNotificationsRoute) { inclusive = true }
                    }
                },
                onSkipAll = {
                    navController.navigate(HomeRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<PermissionLocationRoute> {
            PermissionLocationScreen(
                onContinue = {
                    navController.navigate(PermissionBackgroundRoute) {
                        popUpTo(PermissionLocationRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<PermissionBackgroundRoute> {
            PermissionBackgroundScreen(
                onContinue = {
                    navController.navigate(PermissionBatteryRoute) {
                        popUpTo(PermissionBackgroundRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<PermissionBatteryRoute> {
            PermissionBatteryScreen(
                onContinue = {
                    if (PermissionHelper.isTranssionOEM()) {
                        navController.navigate(PermissionAutostartRoute) {
                            popUpTo(PermissionBatteryRoute) { inclusive = true }
                        }
                    } else {
                        navController.navigate(HomeRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable<PermissionAutostartRoute> {
            PermissionAutostartScreen(
                onContinue = {
                    navController.navigate(HomeRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
            )
        }

        // ── Home ─────────────────────────────────────────────────────────────
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToCountdown = {
                    navController.navigate(TrackingGraphRoute)
                },
                onNavigateToHistory = { navController.navigate(HistoryListRoute) },
                onNavigateToPrograms = { navController.navigate(ProgramsRoute) },
                onNavigateToClubs = { navController.navigate(ClubsRoute) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onNavigateToInfoDetail = { articleId ->
                    navController.navigate(InfoDetailRoute(articleId = articleId))
                },
            )
        }

        composable<InfoDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<InfoDetailRoute>()
            InfoDetailScreen(
                articleId = route.articleId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Tracking (nested graph — shared ViewModel) ──────────────────────
        navigation<TrackingGraphRoute>(startDestination = CountdownRoute) {
            composable<CountdownRoute> { backStackEntry ->
                val parentEntry = try {
                    navController.getBackStackEntry(TrackingGraphRoute)
                } catch (e: IllegalArgumentException) {
                    return@composable // Graph already popped (exit animation), skip
                }
                val viewModel: ActiveRunViewModel = hiltViewModel(parentEntry)
                val runState by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) { viewModel.onStartTapped() }

                LaunchedEffect(runState) {
                    if (runState is RunState.Running) {
                        navController.navigate(LiveTrackingRoute) {
                            popUpTo(CountdownRoute) { inclusive = true }
                        }
                    }
                }

                when (val s = runState) {
                    is RunState.Countdown -> CountdownScreen(secLeft = s.secLeft)
                    else -> CountdownScreen(secLeft = 3)
                }
            }

            composable<LiveTrackingRoute> { backStackEntry ->
                val parentEntry = try {
                    navController.getBackStackEntry(TrackingGraphRoute)
                } catch (e: IllegalArgumentException) {
                    return@composable // Graph already popped (exit animation), skip
                }
                val viewModel: ActiveRunViewModel = hiltViewModel(parentEntry)
                val runState by viewModel.state.collectAsStateWithLifecycle()

                BackHandler(enabled = true) { /* no-op — must use finish button */ }

                // One-shot guard: prevents the Finished navigation from re-firing
                // during recomposition after the tracking graph is popped, which
                // would re-enter getBackStackEntry(TrackingGraphRoute) and crash.
                var navigated by remember { mutableStateOf(false) }
                LaunchedEffect(runState) {
                    val current = runState
                    if (current is RunState.Finished && !navigated) {
                        navigated = true
                        val s = current.summary
                        navController.navigate(
                            RunSummaryRoute(
                                distanceM = s.distanceM,
                                durationS = (s.durationMs / 1000).toInt(),
                                avgPace = s.avgPaceSecondsPerKm,
                                calories = s.calories,
                            )
                        ) {
                            popUpTo(HomeRoute)
                        }
                    }
                }

                LiveTrackingScreen(
                    state = runState,
                    onPause = viewModel::onPauseTapped,
                    onResume = viewModel::onResumeTapped,
                    onFinish = viewModel::onFinishTapped,
                )
            }
        }

        composable<RunSummaryRoute> { backStack ->
            val args = backStack.toRoute<RunSummaryRoute>()
            RunSummaryScreen(
                distanceM = args.distanceM,
                durationS = args.durationS,
                avgPace = args.avgPace,
                calories = args.calories,
                onDone = {
                    navController.navigate(HomeRoute) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        // ── History ──────────────────────────────────────────────────────────
        composable<HistoryListRoute> {
            HistoryListScreen(
                onNavigateToDetail = { runId ->
                    navController.navigate(HistoryDetailRoute(runId = runId))
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<HistoryDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HistoryDetailRoute>()
            HistoryDetailScreen(
                runId = route.runId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Supporting ───────────────────────────────────────────────────────
        composable<ProgramsRoute> {
            ProgramsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<ClubsRoute> {
            ClubsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Profile ──────────────────────────────────────────────────────────
        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(GetStartedRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
