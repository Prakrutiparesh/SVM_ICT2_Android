package com.example.ict2_project.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ict2_project.api.ApiService
import com.example.ict2_project.adapters.UpdatesAdapter
import com.example.ict2_project.databinding.ActivityUpdatesListBinding
import com.example.ict2_project.api.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UpdatesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatesListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.recyclerViewUpdates.layoutManager = LinearLayoutManager(this)
        fetchUpdates()
    }

    private fun fetchUpdates() {
        val apiService = RetrofitClient.instance
        lifecycleScope.launch {
            try {
                val response = apiService.getActiveUpdates()
                if (response.isSuccessful) {
                    val allUpdates = response.body() ?: emptyList()

                    // ✅ Filter only staff notices
                    val staffNotices = allUpdates.filter { update ->
                        update.category.equals("staffNotice", ignoreCase = true)
                    }

                    if (staffNotices.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.recyclerViewUpdates.visibility = View.GONE
                    } else {
                        binding.recyclerViewUpdates.adapter = UpdatesAdapter(staffNotices)
                        binding.recyclerViewUpdates.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(
                        this@UpdatesListActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this@UpdatesListActivity, "Network error", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(this@UpdatesListActivity, "Server error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}