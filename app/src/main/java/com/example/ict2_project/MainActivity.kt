package com.example.ict2_project

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.ict2_project.R
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.models.LoginRequest
import com.example.ict2_project.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
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

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        fixPasswordToggle(tilPassword)

        sharedPreferences = getSharedPreferences("TeacherApp", MODE_PRIVATE)

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

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
                        // Debug log
                        android.util.Log.d("Login", "UserId: ${user.userId}")
                        android.util.Log.d("Login", "GroupId: ${user.groupId}")
                        android.util.Log.d("Login", "FullName: ${user.fullName}")

                        // Check if teacher (groupId = 2)
                        if (user.groupId == 2) {
                            sharedPreferences.edit().apply {
                                putBoolean("isLoggedIn", true)
                                putInt("UserId", user.userId)
                                putString("username", user.username)
                                putString("fullName", user.fullName ?: user.username)
                                putInt("GroupId", user.groupId ?: 0)
                                apply()
                            }

                            fetchAndSaveStaffId(user.userId)
                        } else {
                            showError("Access denied. Only teachers can login. Your GroupId: ${user.groupId}")
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

    private fun fetchAndSaveStaffId(userId: Int) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val staff = apiService.getStaffByUserId(userId)
                if (staff != null && staff.staffId > 0) {
                    sharedPreferences.edit().putInt("StaffId", staff.staffId).apply()
                    android.util.Log.d("Login", "StaffId saved: ${staff.staffId}")
                    Toast.makeText(this@MainActivity, "Welcome ${staff.fullName}!", Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.e("Login", "Staff record not found")
                    Toast.makeText(this@MainActivity, "Staff record not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("Login", "Error: ${e.message}")
            }

            startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
            finish()
        }
    }

    private fun fixPasswordToggle(textInputLayout: TextInputLayout) {
        val editText = textInputLayout.editText as? TextInputEditText ?: return
        val endIconView =
            textInputLayout.findViewById<View>(com.google.android.material.R.id.text_input_end_icon)

        endIconView?.setOnClickListener {
            val selection = editText.selectionEnd
            if (editText.transformationMethod == null) {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                endIconView.isSelected = false
            } else {
                editText.transformationMethod = null
                endIconView.isSelected = true
            }
            editText.setSelection(selection)
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
}