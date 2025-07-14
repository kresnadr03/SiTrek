package com.example.tugasakhir.data

data class DataLokasi(
    val lokasi: Lokasi = Lokasi(),
    val transportasi: Transportasi = Transportasi(),
    val last_updated: Long = System.currentTimeMillis()
)
