package com.s26493.morsecode

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MenuActivity : AppCompatActivity() {

    private lateinit var btnTranslator: Button
    private lateinit var btnLessons: Button
    private lateinit var btnAccount: Button

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        btnTranslator = findViewById(R.id.btnTranslator)
        btnLessons = findViewById(R.id.btnLessons)
        btnAccount = findViewById(R.id.btnAccount)


        btnTranslator.setOnClickListener {
            startActivity(Intent(this, TranslatorActivity::class.java))
        }


        btnLessons.setOnClickListener {
            startActivity(Intent(this, LessonsActivity::class.java))
        }


        btnAccount.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }


        if (auth.currentUser == null) {
            btnAccount.isEnabled = false
            btnAccount.alpha = 0.5f
        }
    }
}

