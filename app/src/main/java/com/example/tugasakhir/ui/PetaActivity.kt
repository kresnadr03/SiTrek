package com.example.tugasakhir.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.tugasakhir.R
import com.example.tugasakhir.data.DataLokasi
import com.example.tugasakhir.utils.ProximityChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class PetaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 101
    private lateinit var database: DatabaseReference
    private var lokasiUser: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance().getReference("lokasi_kendaraan")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnKembali).setOnClickListener {
            finish()
        }

        if (!isLocationGranted()) {
            showIzinLokasiDialog()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (isLocationGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
                tampilkanLokasiPengguna()
                ambilDataDariFirebase()
            }
        }
    }

    private fun isLocationGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun showIzinLokasiDialog() {
        val view = layoutInflater.inflate(R.layout.layout_dialog_izin_lokasi, null)
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

        if (requestCode == LOCATION_REQUEST_CODE && results.isNotEmpty()) {
            if (results[0] == PackageManager.PERMISSION_GRANTED) {
                if (::mMap.isInitialized &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    tampilkanLokasiPengguna()
                    ambilDataDariFirebase()
                }
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk menggunakan fitur ini.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tampilkanLokasiPengguna() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { lokasi: Location? ->
            lokasi?.let {
                lokasiUser = it
                val latlng = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
            }
        }
    }

    private fun ambilDataDariFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val semuaData = mutableListOf<DataLokasi>()
                for (child in snapshot.children) {
                    val data = child.getValue(DataLokasi::class.java)
                    if (data != null) semuaData.add(data)
                }

                val konflik = semuaData.filter { a ->
                    semuaData.any { b ->
                        a != b &&
                                !a.transportasi.nama.equals(b.transportasi.nama, ignoreCase = true) &&
                                ProximityChecker.isWithinRadius(
                                    a.lokasi.latitude, a.lokasi.longitude,
                                    b.lokasi.latitude, b.lokasi.longitude
                                )
                    }
                }
                if (konflik.isNotEmpty()) {
                    AlertDialog.Builder(this@PetaActivity)
                        .setTitle("Konflik Transportasi")
                        .setMessage("Beberapa pengguna memilih kendaraan berbeda di lokasi berdekatan. Sistem akan memilih berdasarkan mayoritas atau prioritas awal.")
                        .setPositiveButton("Oke", null)
                        .show()
                }

                val hasil = resolveKonflik(semuaData)

                mMap.clear()
                hasil.forEach { data ->
                    val pos = LatLng(data.lokasi.latitude, data.lokasi.longitude)
                    val icon = getMarkerIconByJenis(data.transportasi.nama)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(data.transportasi.nama)
                            .icon(icon)
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PetaActivity, "Gagal ambil data kendaraan", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resolveKonflik(data: List<DataLokasi>): List<DataLokasi> {
        if (data.size == 2) {
            val a = data[0]
            val b = data[1]

            val isConflicting = !a.transportasi.nama.equals(b.transportasi.nama, ignoreCase = true) &&
                    ProximityChecker.isWithinRadius(
                        a.lokasi.latitude, a.lokasi.longitude,
                        b.lokasi.latitude, b.lokasi.longitude
                    )

            return if (isConflicting) {
                listOf(data.minByOrNull { it.lokasi.timestamp }!!)
            } else {
                data
            }
        }

        val grouped = data.groupBy { it.transportasi.nama.lowercase() }
        val mayoritas = grouped.maxByOrNull { it.value.size }?.key
        return data.filter { it.transportasi.nama.equals(mayoritas, ignoreCase = true) }
    }

    private fun getMarkerIconByJenis(jenis: String): BitmapDescriptor {
        return when (jenis.lowercase()) {
            "bus" -> BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker)
            "wirawiri" -> BitmapDescriptorFactory.fromResource(R.drawable.ic_wirawiri_marker)
            "mikrolet" -> BitmapDescriptorFactory.fromResource(R.drawable.ic_mikrolet_marker)
            else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }
}
