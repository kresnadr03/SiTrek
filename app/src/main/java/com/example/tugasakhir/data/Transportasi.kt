package com.example.tugasakhir.data

data class Transportasi(
    val nama: String = "",             // ← default biar bisa dibaca dari Firebase
    val imageResId: Int = 0,           // ← default value penting
    var isSelected: Boolean = false    // hanya dipakai di UI
)
