package com.example.tugasakhir.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tugasakhir.R
import com.example.tugasakhir.service.LocationUpdateService
import com.example.tugasakhir.utils.FirebaseManager
import java.util.concurrent.TimeUnit

class KonfirmasiActivity : AppCompatActivity() {

    private lateinit var tvDurasi: TextView
    private var startTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: SharedPreferences

    private val updateRunnable = object : Runnable {
        override fun run() {
            val elapsedMillis = System.currentTimeMillis() - startTime
            val jam = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
            val menit = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
            val detik = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60

            tvDurasi.text = String.format("%d jam, %d menit, %d detik", jam, menit, detik)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi)

        prefs = getSharedPreferences("sitrek", MODE_PRIVATE)
        tvDurasi = findViewById(R.id.tvDurasi)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val btnKembali = findViewById<Button>(R.id.btnKembali)

        // Start timer
        startTime = System.currentTimeMillis()
        handler.post(updateRunnable)

        // 🚀 Start the location service properly
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        Log.d("SERVICE", "ForegroundService dimulai dari KonfirmasiActivity")

        btnStop.setOnClickListener {
            handler.removeCallbacks(updateRunnable)
            stopService(Intent(this, LocationUpdateService::class.java))

            val userId = prefs.getString("user_id", null)
            if (userId != null) {
                FirebaseManager.hapusLokasi(userId,
                    onSuccess = {
                        prefs.edit().remove("user_id").apply()
                        Toast.makeText(this, "Berhenti berbagi lokasi", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onError = { error ->
                        Toast.makeText(this, "Gagal hapus lokasi: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                finish()
            }
        }

        btnKembali.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}
