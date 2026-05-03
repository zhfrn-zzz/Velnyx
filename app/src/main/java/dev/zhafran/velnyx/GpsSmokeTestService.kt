package dev.zhafran.velnyx

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * M0 throwaway — proves foreground GPS works with screen off on demo device.
 * Delete after M2 RunTrackingService is proven.
 *
 * Logcat filter: tag=GPS_TEST
 */
class GpsSmokeTestService : Service() {

    private lateinit var fusedLocation: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { loc ->
                Log.d(TAG, "lat=%.6f lon=%.6f acc=%.1fm spd=%.2fm/s".format(
                    loc.latitude, loc.longitude, loc.accuracy, loc.speed
                ))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promoteToForeground()
        requestUpdates()
        Log.d(TAG, "Service started — watching Logcat for GPS_TEST")
        return START_STICKY
    }

    private fun promoteToForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "GPS Smoke Test", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VELNYX — GPS Smoke Test")
            .setContentText("Logging fixes to Logcat (tag: GPS_TEST)")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
        ServiceCompat.startForeground(
            this, NOTIF_ID, notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .build()
        fusedLocation.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocation.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Service stopped — location updates removed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val TAG = "GPS_TEST"
        private const val CHANNEL_ID = "gps_smoke_test"
        private const val NOTIF_ID = 9001
    }
}
