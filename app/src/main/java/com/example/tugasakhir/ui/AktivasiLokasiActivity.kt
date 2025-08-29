package com.example.tugasakhir.ui

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tugasakhir.R
import com.example.tugasakhir.data.DataLokasi
import com.example.tugasakhir.data.Lokasi
import com.example.tugasakhir.data.Transportasi
import com.example.tugasakhir.handler.LocationHandler
import com.example.tugasakhir.handler.TransportasiHandler
import com.example.tugasakhir.service.LocationUpdateService
import com.example.tugasakhir.utils.FirebaseManager
import java.text.SimpleDateFormat
import java.util.*

class AktivasiLokasiActivity : AppCompatActivity() {

    private val LOCATION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aktivasi_lokasi)

        val btnAktifkan = findViewById<Button>(R.id.btnAktifkanLokasi)
        val btnTolak = findViewById<Button>(R.id.btnTolakLokasi)

        btnAktifkan.setOnClickListener {
            if (LocationHandler.isLocationGranted(this)) {
                ambilLokasiDanKirim()
            } else {
                LocationHandler.mintaIzinLokasi(this)
            }
        }

        btnTolak.setOnClickListener {
            Toast.makeText(this, "Lokasi tidak diaktifkan.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ambilLokasiDanKirim()
        } else {
            Toast.makeText(this, "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ambilLokasiDanKirim() {
        LocationHandler.ambilLokasiSekali(this,
            onSuccess = { lokasi: Location ->
                val jenisTransportasi = TransportasiHandler.ambilJenisTransportasi()
                if (jenisTransportasi != null) {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val lokasiData = Lokasi(
                        latitude = lokasi.latitude,
                        longitude = lokasi.longitude,
                        timestamp = timestamp
                    )
                    val transportasi = Transportasi(jenisTransportasi, 0)
                    val dataLokasi = DataLokasi(lokasiData, transportasi)

                    val prefs = getSharedPreferences("sitrek", MODE_PRIVATE)
                    var userId = prefs.getString("user_id", null)
                    if (userId == null) {
                        userId = UUID.randomUUID().toString()
                        prefs.edit().putString("user_id", userId).apply()
                    }

                    FirebaseManager.kirimDataLokasi(
                        userId = userId,
                        data = dataLokasi,
                        onSuccess = {
                            startService(Intent(this, LocationUpdateService::class.java))
                            startActivity(Intent(this, KonfirmasiActivity::class.java))
                            finish()
                        },
                        onError = { error ->
                            Toast.makeText(this, "Gagal kirim lokasi: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(this, "Data transportasi kosong.", Toast.LENGTH_SHORT).show()
                }
            },
            onGagal = {
                Toast.makeText(this, "Gagal mengambil lokasi.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}