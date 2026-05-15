package dev.zhafran.velnyx.core.util

import kotlin.math.roundToInt

object MetCalculator {

    fun calculate(avgPaceSecondsPerKm: Int, weightKg: Float, durationHours: Float): Int {
        if (avgPaceSecondsPerKm <= 0 || durationHours <= 0f) return 0
        val met = when {
            avgPaceSecondsPerKm > 480 -> 6.0f  // > 8:00/km
            avgPaceSecondsPerKm >= 360 -> 9.0f // 6:00–8:00/km
            else -> 11.0f                       // < 6:00/km
        }
        return (met * weightKg * durationHours).roundToInt()
    }
}
