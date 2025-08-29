package com.example.tugasakhir.ui;

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.tugasakhir.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // Arahkan ke ModeActivity setelah 2 detik
            val intent = Intent(this, ModeActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) // 2 detik
    }
}
