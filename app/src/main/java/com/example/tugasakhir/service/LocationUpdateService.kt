package com.example.tugasakhir.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tugasakhir.R
import com.example.tugasakhir.data.DataLokasi
import com.example.tugasakhir.data.Lokasi
import com.example.tugasakhir.data.Transportasi
import com.example.tugasakhir.handler.TransportasiHandler
import com.example.tugasakhir.utils.FirebaseManager
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class LocationUpdateService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("sitrek", MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Log.d("LocationService", "Service dibuat")

        startForegroundService()
        mulaiPemantauanLokasi()
    }

    private fun startForegroundService() {
        val channelId = "location_update_channel"
        val channelName = "Location Update"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Berbagi Lokasi Aktif")
            .setContentText("SITREK sedang mengirim lokasi...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
        Log.d("LocationService", "Foreground service dimulai")
    }

    private fun mulaiPemantauanLokasi() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    Log.d("LocationService", "Lokasi diterima: ${location.latitude}, ${location.longitude}")
                    kirimLokasi(location)
                }
            }
        }

        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
            Log.d("LocationService", "Mulai memantau lokasi...")
        } else {
            Log.e("LocationService", "Permission lokasi belum diberikan")
        }
    }

    private fun kirimLokasi(location: Location) {
        val jenisTransportasi = TransportasiHandler.ambilJenisTransportasi()
        if (jenisTransportasi == null) {
            Log.e("LocationService", "Jenis transportasi belum diatur")
            return
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val lokasiData = Lokasi(location.latitude, location.longitude, timestamp)
        val transportasi = Transportasi(jenisTransportasi, 0)
        val dataLokasi = DataLokasi(lokasiData, transportasi, System.currentTimeMillis())

        val userId = prefs.getString("user_id", null)
        if (userId == null) {
            Log.e("LocationService", "User ID tidak ditemukan di SharedPreferences")
            return
        }

        FirebaseManager.kirimDataJikaBelumAda(
            userId = userId,
            data = dataLokasi,
            onSuccess = {
                Log.d("LocationService", "Lokasi berhasil dikirim (prioritas OK)")
            },
            onRejected = {
                Log.w("LocationService", "Sinkronisasi ditolak (bukan pemilih pertama / kalah mayoritas)")
            },
            onError = {
                Log.e("LocationService", "Gagal mengirim lokasi: $it")
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("LocationService", "Service dihentikan dan location update dihentikan")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val userId = prefs.getString("user_id", null)
        if (userId != null) {
            FirebaseManager.hapusLokasi(userId,
                onSuccess = {
                    Log.d("LocationService", "Data lokasi dihapus saat app ditutup")
                },
                onError = {
                    Log.e("LocationService", "Gagal hapus lokasi saat app ditutup: $it")
                }
            )
        }
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
