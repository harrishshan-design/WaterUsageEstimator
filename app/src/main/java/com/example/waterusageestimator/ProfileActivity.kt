package com.example.waterusageestimator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.waterusageestimator.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfileImageProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        loadUserProfile()

        binding.ivProfileImageProfile.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener { 
            uploadImageAndUpdateUser()
        }

        binding.btnLogoutProfile.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user == null) return

        binding.etEmailProfile.setText(user.email)
        binding.etEmailProfile.isEnabled = false

        Firebase.firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    if (profilePictureUrl != null) {
                        Glide.with(this).load(profilePictureUrl).into(binding.ivProfileImageProfile)
                    }
                }
            }
            .addOnFailureListener { 
                // Handle failure
            }
    }

    private fun uploadImageAndUpdateUser() {
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
                    updateUserProfileInFirestore(downloadUri.toString())
                } else {
                    Toast.makeText(this, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
             Toast.makeText(this, "No new image selected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserProfileInFirestore(profilePictureUrl: String) {
        val user = auth.currentUser
        if (user == null) return

        Firebase.firestore.collection("users").document(user.uid)
            .update("profilePictureUrl", profilePictureUrl)
            .addOnSuccessListener { 
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                loadUserProfile()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }
}