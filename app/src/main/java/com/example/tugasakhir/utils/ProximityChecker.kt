package com.example.tugasakhir.utils

import android.location.Location

object ProximityChecker {
    fun isWithinRadius(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        radiusInMeter: Double = 50.0
    ): Boolean {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0] <= radiusInMeter
    }
}
