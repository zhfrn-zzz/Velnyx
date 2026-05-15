package dev.zhafran.velnyx

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

@HiltAndroidApp
class VelnyxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this, BuildConfig.MAPTILER_API_KEY, WellKnownTileServer.MapTiler)
    }
}
