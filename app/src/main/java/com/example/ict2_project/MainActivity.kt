package com.example.ict2_project

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.models.LoginRequest
import com.example.ict2_project.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)   // ✅ get reference

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Check if already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        // ✅ Forgot password click listener – works immediately, no login click needed
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        // Login button click listener
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty()) {
                showError("Username/Email is required")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showError("Password is required")
                return@setOnClickListener
            }

            performLogin(username, password)
        }
    }

    private fun performLogin(username: String, password: String) {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        btnLogin.isEnabled = false

        val request = LoginRequest(username, password)
        val call = RetrofitClient.instance.login(request)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        // Check groupId = 2 (Faculty/Staff)
                        if (user.groupId == 2) {
                            // Save login session
                            sharedPreferences.edit().apply {
                                putBoolean("isLoggedIn", true)
                                putInt("userId", user.userId)
                                putString("username", user.username)
                                putString("fullName", user.fullName ?: user.username)
                                putInt("groupId", user.groupId ?: 0)
                                apply()
                            }

                            // Go to Dashboard
                            val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            showError("Access denied: This app is only for Faculty/Staff.")
                        }
                    } else {
                        showError("Invalid response from server")
                    }
                } else {
                    when (response.code()) {
                        401 -> showError("Invalid username/email or password")
                        else -> showError("Login failed: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                showError("Network error: ${t.localizedMessage}")
            }
        })
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
}