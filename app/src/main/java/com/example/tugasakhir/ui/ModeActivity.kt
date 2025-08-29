package com.example.tugasakhir.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.tugasakhir.R

class ModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)

        val btnPenunggu = findViewById<Button>(R.id.btnPenunggu)
        val btnPenyedia = findViewById<Button>(R.id.btnPenyedia)

        btnPenunggu.setOnClickListener {
            // Sementara langsung ke PetaActivity (nanti bisa diganti dengan RoleManager)
            val intent = Intent(this, PetaActivity::class.java)
            intent.putExtra("role", "penunggu")
            startActivity(intent)
        }

        btnPenyedia.setOnClickListener {
            val intent = Intent(this, TransportasiActivity::class.java)
            intent.putExtra("role", "penyedia")
            startActivity(intent)
        }
    }
}
