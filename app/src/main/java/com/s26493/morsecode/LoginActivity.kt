package com.s26493.morsecode

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLoginRegister: Button
    private lateinit var btnContinueWithoutRegistration: Button
    private lateinit var btnAboutApp: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        btnLoginRegister = findViewById(R.id.btnLoginRegister)
        btnContinueWithoutRegistration = findViewById(R.id.btnContinueWithoutRegistration)
        btnAboutApp = findViewById(R.id.btnAboutApp)

        auth = FirebaseAuth.getInstance()

        TooltipCompat.setTooltipText(btnLoginRegister, "Tap to login, long press to register")
        TooltipCompat.setTooltipText(btnContinueWithoutRegistration, "Progress will not be saved")

        // Logowanie przez naciśnięcie przycisku
        btnLoginRegister.setOnClickListener {
            val email = etLogin.text.toString()
            val password = etPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                performLogin(email, password)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Długie naciśnięcie - Rejestracja
        btnLoginRegister.setOnLongClickListener {
            val email = etLogin.text.toString()
            val password = etPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                performRegistration(email, password)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
            true
        }

        btnContinueWithoutRegistration.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        btnAboutApp.setOnClickListener {
            startActivity(Intent(this, AboutAppActivity::class.java))
        }
    }


    private fun performLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful for $email", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MenuActivity::class.java))
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun performRegistration(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful for $email", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MenuActivity::class.java))
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
