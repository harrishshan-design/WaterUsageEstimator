package com.example.waterusageestimator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import kotlin.math.roundToInt

class CalculatorActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val SHOWER_FLOW_RATE = 9.5 // liters per minute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)

        val etPeople = findViewById<TextInputEditText>(R.id.etPeople)
        val etShowerLength = findViewById<TextInputEditText>(R.id.etShowerLength)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnCalculate.setOnClickListener {
            calculateWaterUsage(
                etPeople, etShowerLength, tvResult
            )
        }

        btnBack.setOnClickListener {
            finish() // Go back to the previous activity
        }
    }

    private fun calculateWaterUsage(
        etPeople: TextInputEditText,
        etShowerLength: TextInputEditText,
        tvResult: TextView
    ) {
        try {
            val people = etPeople.text.toString().toIntOrNull() ?: 1
            val showerLength = etShowerLength.text.toString().toDoubleOrNull() ?: 10.0

            val showerWaterDaily = people * showerLength * SHOWER_FLOW_RATE
            val totalDailyUsage = showerWaterDaily
            val totalWeeklyUsage = totalDailyUsage * 7
            val totalMonthlyUsage = totalDailyUsage * 30

            tvResult.text = "Estimated Daily Usage: ${totalDailyUsage.roundToInt()} L\n"
            tvResult.append("Estimated Weekly Usage: ${totalWeeklyUsage.roundToInt()} L\n")
            tvResult.append("Estimated Monthly Usage: ${totalMonthlyUsage.roundToInt()} L")

            saveCalculationToFirestore(totalDailyUsage, totalWeeklyUsage, totalMonthlyUsage)

        } catch (e: Exception) {
            tvResult.text = "Error: Please check your inputs"
        }
    }

    private fun saveCalculationToFirestore(daily: Double, weekly: Double, monthly: Double) {
        val calculation = hashMapOf(
            "dailyUsage" to daily,
            "weeklyUsage" to weekly,
            "monthlyUsage" to monthly,
            "timestamp" to Date()
        )

        db.collection("waterUsage")
            .add(calculation)
            .addOnSuccessListener { _ ->
                Toast.makeText(this, "Calculation saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { _ ->
                Toast.makeText(this, "Error saving calculation", Toast.LENGTH_SHORT).show()
            }
    }
}