package com.example.waterusageestimator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    // Water usage constants (liters)
    private val SHOWER_FLOW_RATE = 9.5 // liters per minute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)

        val etPeople = findViewById<TextInputEditText>(R.id.etPeople)
        val etShowerLength = findViewById<TextInputEditText>(R.id.etShowerLength)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnCalculate.setOnClickListener {
            calculateWaterUsage(
                etPeople, etShowerLength, tvResult
            )
        }
    }

    private fun calculateWaterUsage(
        etPeople: TextInputEditText,
        etShowerLength: TextInputEditText,
        tvResult: TextView
    ) {
        try {
            // Get input values with defaults
            val people = etPeople.text.toString().toIntOrNull() ?: 1
            val showerLength = etShowerLength.text.toString().toDoubleOrNull() ?: 10.0

            // Calculate water usage
            val showerWaterDaily = people * showerLength * SHOWER_FLOW_RATE
            val totalDailyUsage = showerWaterDaily
            val totalWeeklyUsage = totalDailyUsage * 7
            val totalMonthlyUsage = totalDailyUsage * 30

            // Display results
            tvResult.text = "Estimated Daily Usage: ${totalDailyUsage.roundToInt()} L\n"
            tvResult.append("Estimated Weekly Usage: ${totalWeeklyUsage.roundToInt()} L\n")
            tvResult.append("Estimated Monthly Usage: ${totalMonthlyUsage.roundToInt()} L")

        } catch (e: Exception) {
            tvResult.text = "Error: Please check your inputs"
        }
    }
}