package com.example.ict2_project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)

        val fullName = sharedPreferences.getString("fullName", "User")
        tvWelcome.text = "Welcome, $fullName!"

        btnLogout.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}