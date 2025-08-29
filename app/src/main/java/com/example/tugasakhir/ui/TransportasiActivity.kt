package com.example.tugasakhir.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tugasakhir.R
import com.example.tugasakhir.data.Transportasi
import com.example.tugasakhir.handler.TransportasiHandler

class TransportasiActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: TransportasiAdapter
    private lateinit var transportasiList: MutableList<Transportasi>
    private var selectedIndex: Int = -1

    private val LOCATION_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transportasi)

        transportasiList = mutableListOf(
            Transportasi("Bus", R.drawable.ic_bus),
            Transportasi("Wirawiri", R.drawable.ic_wirawiri),
            Transportasi("Mikrolet", R.drawable.ic_mikrolet)
        )

        rv = findViewById(R.id.rvTransportasi)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = TransportasiAdapter(transportasiList) { index ->
            transportasiList.forEach { it.isSelected = false }
            transportasiList[index].isSelected = true
            selectedIndex = index
            adapter.notifyDataSetChanged()
        }
        rv.adapter = adapter

        findViewById<Button>(R.id.btnPilih).setOnClickListener {
            if (selectedIndex == -1) {
                Toast.makeText(this, "Pilih jenis transportasi dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            bukaAktivasiLokasi()
        }

        findViewById<Button>(R.id.btnKembali).setOnClickListener {
            finish()
        }

        // ⬇️ Langsung tampilkan dialog izin lokasi saat activity dibuka
        if (!isLocationGranted()) {
            tampilkanDialogIzinLokasi()
        }
    }

    private fun isLocationGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun tampilkanDialogIzinLokasi() {
        val view = layoutInflater.inflate(R.layout.layout_dialog_izin_lokasi_penyedia, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnIzinkanLokasi).setOnClickListener {
            dialog.dismiss()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }

        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == LOCATION_REQUEST_CODE && results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Izin lokasi diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bukaAktivasiLokasi() {
        val selected = transportasiList[selectedIndex]
        TransportasiHandler.simpanJenisTransportasi(selected.nama)
        startActivity(Intent(this, AktivasiLokasiActivity::class.java))
        finish()
    }
}
