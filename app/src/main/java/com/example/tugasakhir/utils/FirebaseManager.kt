package com.example.tugasakhir.utils

import android.util.Log
import com.example.tugasakhir.data.DataLokasi
import com.google.firebase.database.FirebaseDatabase

object FirebaseManager {
    private val db = FirebaseDatabase.getInstance()
    private val lokasiRef = db.getReference("lokasi_kendaraan")

    fun kirimDataLokasi(
        userId: String,
        data: DataLokasi,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        lokasiRef.child(userId).setValue(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }

    fun hapusLokasi(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        lokasiRef.child(userId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }

    fun kirimDataJikaBelumAda(
        userId: String,
        data: DataLokasi,
        onSuccess: () -> Unit,
        onRejected: () -> Unit,
        onError: (String) -> Unit
    ) {
        lokasiRef.get().addOnSuccessListener { snapshot ->
            val semua = snapshot.children.mapNotNull { child ->
                val value = child.getValue(DataLokasi::class.java)
                if (value != null) child.key?.let { it to value } else null
            }

            val konflik = semua.filter { (uid, existing) ->
                uid != userId &&
                        existing.transportasi.nama.lowercase() != data.transportasi.nama.lowercase() &&
                        ProximityChecker.isWithinRadius(
                            existing.lokasi.latitude, existing.lokasi.longitude,
                            data.lokasi.latitude, data.lokasi.longitude
                        )
            }

            if (konflik.isNotEmpty()) {
                val semuaDalamRadius = semua.filter { (_, existing) ->
                    ProximityChecker.isWithinRadius(
                        existing.lokasi.latitude, existing.lokasi.longitude,
                        data.lokasi.latitude, data.lokasi.longitude
                    )
                }

                if (semuaDalamRadius.size == 2) {
                    // Konflik 2 pengguna → yang sinkronisasi pertama yang diterima
                    val first = semuaDalamRadius.minByOrNull { it.second.lokasi.timestamp }
                    if (first?.first == userId) {
                        lokasiRef.child(userId).setValue(data)
                            .addOnSuccessListener {
                                Log.d("FirebaseManager", "Sinkronisasi diterima (konflik 2 orang, prioritas awal)")
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onError(it.message ?: "Unknown error")
                            }
                    } else {
                        Log.w("FirebaseManager", "Sinkronisasi ditolak (konflik 2 orang, bukan prioritas awal)")
                        onRejected()
                    }
                } else {
                    // Konflik ≥3 pengguna → mayoritas dan prioritas sinkronisasi pertama dalam mayoritas
                    val grup = semuaDalamRadius.groupBy { it.second.transportasi.nama.lowercase() }
                    val mayoritas = grup.maxByOrNull { it.value.size }?.key

                    val userPilihan = data.transportasi.nama.lowercase()

                    if (userPilihan == mayoritas) {
                        val dataMayoritas = semuaDalamRadius.filter {
                            it.second.transportasi.nama.lowercase() == mayoritas
                        }
                        val yangLebihAwal = dataMayoritas.minByOrNull { it.second.lokasi.timestamp }

                        
                        if (yangLebihAwal?.first == userId) {
                            lokasiRef.child(userId).setValue(data)
                                .addOnSuccessListener {
                                    Log.d("FirebaseManager", "Sinkronisasi diterima (konflik 3+, mayoritas dan prioritas awal)")
                                    onSuccess()
                                }
                                .addOnFailureListener {
                                    onError(it.message ?: "Unknown error")
                                }
                        } else {
                            Log.w("FirebaseManager", "Sinkronisasi ditolak (konflik 3+, mayoritas tapi bukan prioritas awal)")
                            onRejected()
                        }
                    } else {
                        Log.w("FirebaseManager", "Sinkronisasi ditolak (konflik 3+, kalah vote mayoritas)")
                        onRejected()
                    }
                }
                return@addOnSuccessListener
            }

            // Jika tidak ada konflik, langsung kirim
            lokasiRef.child(userId).setValue(data)
                .addOnSuccessListener {
                    Log.d("FirebaseManager", "Tidak ada konflik, sinkronisasi langsung diterima")
                    onSuccess()
                }
                .addOnFailureListener {
                    onError(it.message ?: "Unknown error")
                }
        }.addOnFailureListener {
            onError(it.message ?: "Gagal membaca Firebase")
        }
    }
}
