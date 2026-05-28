package com.example.ict2_project

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ict2_project.api.RetrofitClient
import com.example.ict2_project.models.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etIdentifier: TextInputEditText
    private lateinit var btnSendOtp: MaterialButton
    private lateinit var otpLayout: TextInputLayout
    private lateinit var etOtp: TextInputEditText
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var btnResetPassword: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etIdentifier = findViewById(R.id.etIdentifier)
        btnSendOtp = findViewById(R.id.btnSendOtp)
        otpLayout = findViewById(R.id.otpLayout)
        etOtp = findViewById(R.id.etOtp)
        newPasswordLayout = findViewById(R.id.newPasswordLayout)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        progressBar = findViewById(R.id.progressBar)
        tvMessage = findViewById(R.id.tvMessage)

        btnSendOtp.setOnClickListener { requestOtp() }
        btnResetPassword.setOnClickListener { resetPassword() }
    }

    private fun requestOtp() {
        val identifier = etIdentifier.text.toString().trim()
        if (identifier.isEmpty()) {
            showMessage("Please enter username or email", isError = true)
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSendOtp.isEnabled = false

        val request = ForgotPasswordRequest(identifier)
        RetrofitClient.instance.forgotPassword(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val body = response.body()
                    showMessage(
                        body?.message ?: "OTP sent! Check your email (or see console)",
                        isError = false
                    )
                    // Show OTP and new password fields
                    otpLayout.visibility = View.VISIBLE
                    newPasswordLayout.visibility = View.VISIBLE
                    btnResetPassword.visibility = View.VISIBLE
                    btnSendOtp.isEnabled = false
                } else {
                    val error = response.errorBody()?.string() ?: "Failed to send OTP"
                    showMessage(error, isError = true)
                    btnSendOtp.isEnabled = true
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showMessage("Network error: ${t.message}", isError = true)
                btnSendOtp.isEnabled = true
            }
        })
    }

    private fun resetPassword() {
        val identifier = etIdentifier.text.toString().trim()
        val otp = etOtp.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()

        if (identifier.isEmpty() || otp.isEmpty() || newPassword.isEmpty()) {
            showMessage("All fields are required", isError = true)
            return
        }
        if (newPassword.length < 6) {
            showMessage("Password must be at least 6 characters", isError = true)
            return
        }

        progressBar.visibility = View.VISIBLE
        btnResetPassword.isEnabled = false

        val request = ResetPasswordRequest(identifier, otp, newPassword)
        RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    showMessage("Password reset successful! Please login.", isError = false)
                    // Close activity after a delay
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Reset failed"
                    showMessage(error, isError = true)
                    btnResetPassword.isEnabled = true
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showMessage("Network error: ${t.message}", isError = true)
                btnResetPassword.isEnabled = true
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showMessage(msg: String, isError: Boolean) {
        tvMessage.text = msg
        tvMessage.setTextColor(
            if (isError) resources.getColor(android.R.color.holo_red_dark) else resources.getColor(
                android.R.color.holo_green_dark
            )
        )
        tvMessage.visibility = View.VISIBLE
    }
}