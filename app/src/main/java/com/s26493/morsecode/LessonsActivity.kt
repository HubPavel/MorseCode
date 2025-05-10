package com.s26493.morsecode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LessonsActivity : AppCompatActivity() {

    private lateinit var lessonsContainer: LinearLayout
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var unlockedLessons = 1 // По умолчанию открыт только Lesson 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lessons)

        lessonsContainer = findViewById(R.id.lessonsContainer)

        loadUserProgress()
    }

    private fun loadUserProgress() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("userProgress").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        unlockedLessons = document.getLong("unlockedLessons")?.toInt() ?: 1
                    }
                    loadLessons()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error loading user progress", e)
                    loadLessons() // Загружаем уроки даже если прогресс не найден
                }
        } else {
            loadLessons() // Гость - только первый урок
        }
    }

    private fun loadLessons() {
        db.collection("lessons").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val lessonId = document.id
                    val lessonNumber = document.getLong("lesson")?.toInt() ?: 0
                    val lessonTitle = document.getString("title") ?: "Unknown Lesson"

                    val lessonButton = Button(this)
                    lessonButton.text = lessonTitle
                    lessonButton.setBackgroundColor(resources.getColor(R.color.button_color))
                    lessonButton.setTextColor(resources.getColor(R.color.button_text_color))
                    lessonButton.textSize = 18f

                    if (lessonNumber > unlockedLessons) {
                        lessonButton.isEnabled = false
                    }

                    lessonButton.setOnClickListener {
                        val intent = Intent(this, LessonActivity::class.java)
                        intent.putExtra("LESSON_ID", lessonId)
                        startActivity(intent)
                    }

                    lessonsContainer.addView(lessonButton)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error loading lessons", e)
            }
    }
}
