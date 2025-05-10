package com.s26493.morsecode

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SetupActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addLessonsToFirestore()
    }

    private fun addLessonsToFirestore() {
        val lessons = listOf(
            mapOf(
                "lesson" to 1,
                "title" to "Lesson 1: E & T",
                "newLetters" to listOf("E", "T"),
                "availableLetters" to listOf("E", "T"),
                "playOrder" to listOf("E", "T", "E", "T", "E", "T", "E", "T"),
                "morseCode" to mapOf("E" to ".", "T" to "-"),
                "fillSteps" to mapOf("new" to 6, "old" to 4)
            ),
            mapOf(
                "lesson" to 2,
                "title" to "Lesson 2: A & N",
                "newLetters" to listOf("A", "N"),
                "availableLetters" to listOf("E", "T", "A", "N"),
                "playOrder" to listOf("A", "N", "E", "T", "A", "N", "A", "T", "N", "E"),
                "morseCode" to mapOf("A" to ".-", "N" to "-.", "T" to "-", "E" to "."),
                "fillSteps" to mapOf("new" to 6, "old" to 4)
            ),
            mapOf(
                "lesson" to 3,
                "title" to "Lesson 3: I & M",
                "newLetters" to listOf("I", "M"),
                "availableLetters" to listOf("E", "T", "A", "N", "I", "M"),
                "playOrder" to listOf("I", "M", "I", "A", "M", "N", "I", "T", "M", "E", "M", "I", "M", "A", "I", "N", "T", "M", "E", "I"),
                "morseCode" to mapOf("I" to "..", "M" to "--", "T" to "-", "E" to ".", "A" to ".-", "N" to "-.", "M" to "--"),
                "fillSteps" to mapOf("new" to 6, "old" to 4)
            )
        )

        for (lesson in lessons) {
            val lessonId = "lesson${lesson["lesson"]}"
            db.collection("lessons").document(lessonId)
                .set(lesson)
                .addOnSuccessListener {
                    Log.d("Firestore", "Lesson $lessonId added successfully!")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding lesson $lessonId", e)
                }
        }
    }
}
