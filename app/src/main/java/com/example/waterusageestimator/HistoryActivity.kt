package com.example.waterusageestimator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.waterusageestimator.databinding.ActivityHistoryBinding
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarHistory.setNavigationOnClickListener { finish() }

        fetchHistory()
    }

    private fun fetchHistory() {
        db.collection("waterUsage")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val records = documents.mapNotNull { doc ->
                    val timestamp = doc.getDate("timestamp")
                    val usage = doc.getDouble("dailyUsage")
                    if (timestamp != null && usage != null) {
                        WaterUsageRecord(timestamp, usage)
                    } else {
                        null
                    }
                }
                setupRecyclerView(records)
                calculateAndDisplayAverage(records)
            }
            .addOnFailureListener { 
                // Handle failure
            }
    }

    private fun setupRecyclerView(records: List<WaterUsageRecord>) {
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = HistoryAdapter(records)
    }

    private fun calculateAndDisplayAverage(records: List<WaterUsageRecord>) {
        if (records.isNotEmpty()) {
            val average = records.map { it.usage }.average()
            binding.tvAverageUsage.text = "Average Daily Usage: ${average.toInt()} L"
        } else {
            binding.tvAverageUsage.text = "Average Daily Usage: 0 L"
        }
    }
}