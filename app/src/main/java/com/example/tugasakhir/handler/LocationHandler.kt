package com.example.tugasakhir.handler

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

object LocationHandler {

    private const val LOCATION_REQUEST_CODE = 100

    fun isLocationGranted(activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun mintaIzinLokasi(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST_CODE
        )
    }

    fun ambilLokasiSekali(
        activity: Activity,
        onSuccess: (Location) -> Unit,
        onGagal: () -> Unit
    ) {
        val client = LocationServices.getFusedLocationProviderClient(activity)

        if (!isLocationGranted(activity)) {
            onGagal()
            return
        }

        client.lastLocation
            .addOnSuccessListener { lokasi ->
                if (lokasi != null) onSuccess(lokasi)
                else onGagal()
            }
            .addOnFailureListener { onGagal() }
    }
}
