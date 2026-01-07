package com.example.waterusageestimator

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class DashboardActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private var usageListener: ListenerRegistration? = null

    // Constants for money saved calculation
    private val BASELINE_USAGE_PER_DAY = 900 // Litres (based on 225L/person for a 4-person household)
    private val WATER_TARIFF_PER_1000L = 0.57 // RM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val btnCalculate = findViewById<LinearLayout>(R.id.btnCalculate)
        val btnTips = findViewById<LinearLayout>(R.id.btnTips)
        val btnSavings = findViewById<LinearLayout>(R.id.btnSavings)
        val btnHistory = findViewById<LinearLayout>(R.id.btnHistory)
        val tvTodayUsage = findViewById<TextView>(R.id.tvTodayUsage)
        val btnProfile = findViewById<ImageButton>(R.id.btnProfile)

        btnCalculate.setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))
        }

        btnTips.setOnClickListener {
            startActivity(Intent(this, TipsActivity::class.java))
        }

        btnSavings.setOnClickListener {
            startActivity(Intent(this, SavingsActivity::class.java))
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        tvTodayUsage.setOnClickListener {
            showEditUsageDialog()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        setupUsageListener()
    }

    override fun onStop() {
        super.onStop()
        usageListener?.remove() // Stop listening when the activity is not visible
    }

    private fun setupUsageListener() {
        val tvTodayUsage = findViewById<TextView>(R.id.tvTodayUsage)
        val tvLastUpdated = findViewById<TextView>(R.id.tvLastUpdated)
        val tvMoneySaved = findViewById<TextView>(R.id.tvMoneySaved)

        usageListener = db.collection("waterUsage")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    tvTodayUsage.text = "Error"
                    tvLastUpdated.text = "(update failed)"
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val latestDocument = snapshot.documents[0]
                    val latestUsage = latestDocument.getDouble("dailyUsage") ?: 0.0
                    val latestTimestamp = latestDocument.getDate("timestamp")

                    tvTodayUsage.text = "${latestUsage.roundToInt()} L"
                    if (latestTimestamp != null) {
                        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        tvLastUpdated.text = "(as of ${sdf.format(latestTimestamp)})"
                    } else {
                        tvLastUpdated.text = "(date not available)"
                    }

                    // Calculate and display money saved
                    val moneySaved = (BASELINE_USAGE_PER_DAY - latestUsage) * (WATER_TARIFF_PER_1000L / 1000)
                    tvMoneySaved.text = String.format("RM %.2f", moneySaved)

                } else {
                    tvTodayUsage.text = "0 L"
                    tvLastUpdated.text = "(no data yet)"
                    tvMoneySaved.text = "RM 0.00"
                }
            }
    }

    private fun showEditUsageDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Daily Usage")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newUsage = input.text.toString().toDoubleOrNull()
            if (newUsage != null) {
                saveNewUsage(newUsage)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveNewUsage(newUsage: Double) {
        val calculation = hashMapOf(
            "dailyUsage" to newUsage,
            "weeklyUsage" to newUsage * 7,
            "monthlyUsage" to newUsage * 30,
            "timestamp" to Date()
        )

        db.collection("waterUsage")
            .add(calculation)
            .addOnSuccessListener { _ ->
                Toast.makeText(this, "Usage saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { _ ->
                Toast.makeText(this, "Error saving usage", Toast.LENGTH_SHORT).show()
            }
    }
}