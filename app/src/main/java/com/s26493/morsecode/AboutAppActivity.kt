package com.s26493.morsecode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class AboutAppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)


        val tvAboutApp: TextView = findViewById(R.id.tvAboutApp)
        tvAboutApp.text = """
            Morse Code Learning App
            Version: 1.0
            
            Authors:
            - Your Name
            
            Technologies:
            - Kotlin
            - Firebase Authentication
            - Android SDK 34
        """.trimIndent()
    }
}