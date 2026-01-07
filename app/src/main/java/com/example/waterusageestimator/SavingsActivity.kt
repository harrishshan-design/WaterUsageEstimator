package com.example.waterusageestimator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SavingsActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    // Constants for money saved calculation
    private val BASELINE_USAGE_PER_DAY = 900 // Litres
    private val WATER_TARIFF_PER_1000L = 0.57 // RM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_savings)
        toolbar.setNavigationOnClickListener { finish() }

        fetchTotalSavings()
    }

    private fun fetchTotalSavings() {
        val tvTotalMoneySaved = findViewById<TextView>(R.id.tvTotalMoneySaved)

        db.collection("waterUsage")
            .get()
            .addOnSuccessListener { documents ->
                var totalSavings = 0.0
                for (document in documents) {
                    val dailyUsage = document.getDouble("dailyUsage") ?: BASELINE_USAGE_PER_DAY.toDouble()
                    val dailySaving = (BASELINE_USAGE_PER_DAY - dailyUsage) * (WATER_TARIFF_PER_1000L / 1000)
                    if (dailySaving > 0) { // Only count savings, not losses
                        totalSavings += dailySaving
                    }
                }
                tvTotalMoneySaved.text = String.format("RM %.2f", totalSavings)
            }
            .addOnFailureListener { 
                tvTotalMoneySaved.text = "Error"
            }
    }
}