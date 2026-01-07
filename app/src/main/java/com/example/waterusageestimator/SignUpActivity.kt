package com.example.waterusageestimator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class SignUpActivity : AppCompatActivity() {

    private lateinit var ivProfileImage: ImageView
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivProfileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth

        ivProfileImage = findViewById(R.id.ivProfileImage)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail_signup)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword_signup)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword_signup)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        ivProfileImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            uploadImageAndSaveUser(email)
                        } else {
                            Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                etConfirmPassword.error = "Passwords do not match"
            }
        }
    }

    private fun uploadImageAndSaveUser(email: String) {
        val user = auth.currentUser
        if (user == null) return

        if (selectedImageUri != null) {
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("profileImages/${user.uid}/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(selectedImageUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    saveUserToFirestore(email, downloadUri.toString())
                } else {
                    Toast.makeText(this, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    saveUserToFirestore(email, null)
                }
            }
        } else {
            saveUserToFirestore(email, null)
        }
    }

    private fun saveUserToFirestore(email: String, profilePictureUrl: String?) {
        val user = auth.currentUser
        if (user == null) return

        val userMap = hashMapOf(
            "email" to email,
            "profilePictureUrl" to profilePictureUrl
        )

        Firebase.firestore.collection("users").document(user.uid)
            .set(userMap)
            .addOnSuccessListener { 
                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1500)
             }
            .addOnFailureListener { 
                 Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show()
             }
    }
}