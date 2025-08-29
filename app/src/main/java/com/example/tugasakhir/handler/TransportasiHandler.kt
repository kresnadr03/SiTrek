package com.example.tugasakhir.handler

object TransportasiHandler {
    private var jenisDipilih: String? = null

    fun simpanJenisTransportasi(jenis: String) {
        jenisDipilih = jenis
    }

    fun ambilJenisTransportasi(): String? {
        return jenisDipilih
    }
}