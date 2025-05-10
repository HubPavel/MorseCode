package com.s26493.morsecode

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvEmail: TextView
    private lateinit var tvUid: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvEmail = findViewById(R.id.tvEmail)
        tvUid = findViewById(R.id.tvUid)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)

        val user = auth.currentUser
        if (user != null) {
            tvEmail.text = "Email: ${user.email}"
            tvUid.text = "UID: ${user.uid}"
        } else {
            Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show()
            finish()
        }


        btnChangePassword.setOnClickListener {
            changePassword()
        }


        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }


        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }
    }


    private fun changePassword() {
        val user = auth.currentUser
        user?.let {
            auth.sendPasswordResetEmail(user.email!!)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent to ${user.email}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send password reset email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun deleteAccount() {
        val user = auth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to delete account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
