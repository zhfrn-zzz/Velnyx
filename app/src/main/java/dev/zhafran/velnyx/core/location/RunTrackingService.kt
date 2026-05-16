package dev.zhafran.velnyx.core.location

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import dev.zhafran.velnyx.MainActivity
import dev.zhafran.velnyx.core.data.db.ActiveRunDao
import dev.zhafran.velnyx.core.data.db.ActiveRunPointEntity
import dev.zhafran.velnyx.core.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject

/**
 * Stats emitted by the service, derived from Room on each valid GPS fix.
 */
data class RunStats(
    val distanceM: Int = 0,
    val durationS: Int = 0,
)

@AndroidEntryPoint
class RunTrackingService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var dao: ActiveRunDao
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var runId: Long = -1L
    private var pointIdx: Int = 0
    private var lastLocation: Location? = null
    private var totalDistanceM: Float = 0f
    private var isPaused: Boolean = false

    private val _stats = MutableStateFlow(RunStats())
    val stats: StateFlow<RunStats> = _stats

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { loc -> processLocation(loc) }
        }
    }

    inner class LocalBinder : Binder() {
        val service: RunTrackingService get() = this@RunTrackingService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        dao = AppDatabase.getInstance(this).activeRunDao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                runId = intent.getLongExtra(EXTRA_RUN_ID, -1L)
                isPaused = false
                promoteToForeground()
                requestUpdates()
                scope.launch {
                    pointIdx = dao.getPointCount(runId)
                    lastLocation = null
                    val run = dao.getActiveRun()
                    if (run != null) {
                        totalDistanceM = run.distanceM.toFloat()
                    }
                }
            }
            ACTION_PAUSE -> {
                isPaused = true
            }
            ACTION_RESUME -> {
                isPaused = false
                lastLocation = null
            }
            ACTION_STOP -> {
                fusedLocation.removeLocationUpdates(locationCallback)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun processLocation(loc: Location) {
        // PATH 1 — Map display. Publish every raw fix from the fused
        // provider, including indoor WiFi/cell-tower fixes with
        // accuracy > 30m. The map marker should track the user even
        // when GPS-grade fixes aren't available.
        locationRepository.publishRaw(LatLng(loc.latitude, loc.longitude))

        // PATH 2 — Run metrics. Filter aggressively: only fixes that
        // are accurate AND moving contribute to distance / pace /
        // calories. This prevents distance creep at traffic lights
        // and rejects garbage WiFi-only fixes drifting hundreds of
        // meters indoors.
        if (loc.accuracy > ACCURACY_THRESHOLD_M) return
        if (loc.speed < SPEED_THRESHOLD_MS) return
        if (isPaused) return

        lastLocation?.let { prev ->
            totalDistanceM += prev.distanceTo(loc)
        }
        lastLocation = loc

        scope.launch {
            dao.insertPoint(
                ActiveRunPointEntity(
                    runId = runId,
                    idx = pointIdx++,
                    lat = loc.latitude,
                    lon = loc.longitude,
                    recordedAt = System.currentTimeMillis(),
                    accuracy = loc.accuracy,
                    speed = loc.speed,
                )
            )
            // Update distance in Room
            dao.getActiveRun()?.let { run ->
                if (run.id == runId) {
                    dao.updateRun(run.copy(distanceM = totalDistanceM.toInt()))
                }
            }
            // Compute duration from Room fields (crash-safe)
            val run = dao.getActiveRun()
            if (run != null) {
                val now = System.currentTimeMillis()
                val durationMs = now - run.startedAt - run.totalPausedMs
                _stats.value = RunStats(
                    distanceM = totalDistanceM.toInt(),
                    durationS = (durationMs / 1000).toInt(),
                )
            }
        }

        updateNotification()
    }

    private fun promoteToForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Run Tracking", NotificationManager.IMPORTANCE_LOW)
            )
        }
        ServiceCompat.startForeground(
            this, NOTIF_ID, buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification())
    }

    private fun buildNotification(): android.app.Notification {
        val s = _stats.value
        val mins = s.durationS / 60
        val secs = s.durationS % 60
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VELNYX — Running")
            .setContentText("%.2f km • %02d:%02d".format(s.distanceM / 1000f, mins, secs))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun requestUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .build()
        fusedLocation.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocation.removeLocationUpdates(locationCallback)
        scope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "run_tracking"
        const val NOTIF_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RUN_ID = "EXTRA_RUN_ID"

        private const val ACCURACY_THRESHOLD_M = 30f
        private const val SPEED_THRESHOLD_MS = 1.0f

        fun startIntent(context: Context, runId: Long): Intent =
            Intent(context, RunTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RUN_ID, runId)
            }

        fun pauseIntent(context: Context): Intent =
            Intent(context, RunTrackingService::class.java).apply { action = ACTION_PAUSE }

        fun resumeIntent(context: Context): Intent =
            Intent(context, RunTrackingService::class.java).apply { action = ACTION_RESUME }

        fun stopIntent(context: Context): Intent =
            Intent(context, RunTrackingService::class.java).apply { action = ACTION_STOP }
    }
}
