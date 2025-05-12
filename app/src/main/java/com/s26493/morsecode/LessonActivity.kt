package com.s26493.morsecode

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LessonActivity : AppCompatActivity() {

    private lateinit var tvLessonTitle: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var keyboard: GridLayout
    private lateinit var btnStartLesson: Button
    private lateinit var btnNextLesson: Button
    private lateinit var btnRepeat: Button
    private lateinit var btnSound: Button

    private val db = FirebaseFirestore.getInstance()
    private var lessonId: String? = null
    private var playOrder = mutableListOf<String>()
    private var currentLetterIndex = 0
    private var morseMap = mutableMapOf<String, Pair<String, String>>() // [litera] = (krótki dźwięk, długi dźwięk)
    private var mediaPlayer: MediaPlayer? = null
    private var mistakeCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

        // Inicjalizacja UI
        tvLessonTitle = findViewById(R.id.tvLessonTitle)
        tvQuestion = findViewById(R.id.tvQuestion)
        keyboard = findViewById(R.id.keyboard)
        btnStartLesson = findViewById(R.id.btnStartLesson)
        btnNextLesson = findViewById(R.id.btnNextLesson)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnSound = findViewById(R.id.btnSound)

        // Pobranie ID lekcji z Intent
        lessonId = intent.getStringExtra("LESSON_ID")

        // Załadowanie lekcji z Firestore
        lessonId?.let { loadLesson(it) }

        // Obsługa przycisku rozpoczęcia lekcji
        btnStartLesson.setOnClickListener {
            btnStartLesson.visibility = Button.GONE
            startLesson()
        }

        // Obsługa przycisku powtórzenia dźwięku
        btnRepeat.setOnClickListener {
            playCurrentLetter()
        }

        // Obsługa przycisku przejścia do następnej lekcji
        btnNextLesson.setOnClickListener {
            goToNextLesson()
        }
    }

    // Pobranie danych lekcji z Firestore
    private fun loadLesson(lessonId: String) {
        db.collection("lessons").document(lessonId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: "Nieznana lekcja"
                    val letters = document.get("playOrder") as? List<String> ?: emptyList()

                    tvLessonTitle.text = title
                    playOrder.clear()
                    playOrder.addAll(letters)

                    loadMorseData()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Nie udało się załadować lekcji: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Pobranie danych kodu Morse’a z Firestore
    private fun loadMorseData() {
        db.collection("morseCode").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val letter = doc.id
                    val code = doc.getString("code") ?: ""
                    val shortSound = doc.getString("soundShort") ?: "beep_short"
                    val longSound = doc.getString("soundLong") ?: "beep_long"
                    morseMap[letter] = Pair(code, "$shortSound|$longSound")
                    Log.d("Sound", "Załadowano: $letter -> Kod: $code, Dźwięki: $shortSound | $longSound")
                }
                setupKeyboard()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Nie udało się załadować danych Morse’a: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Tworzenie klawiatury QWERTY
    private fun setupKeyboard() {
        keyboard.removeAllViews()
        keyboard.columnCount = 10

        for (char in ('A'..'Z') + ('0'..'9')) {
            val button = Button(this)
            button.text = char.toString()
            button.textSize = 16f
            button.gravity = Gravity.CENTER

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(4, 4, 4, 4)
            button.layoutParams = params

            if (playOrder.contains(char.toString())) {
                button.isEnabled = true
                button.alpha = 1.0f
                button.setOnClickListener { onLetterClicked(char.toString()) }
            } else {
                button.isEnabled = false
                button.alpha = 0.3f
            }

            keyboard.addView(button)
        }
    }

    // Rozpoczęcie lekcji
    private fun startLesson() {
        currentLetterIndex = 0
        mistakeCount = 0
        showNextLetter()
    }

    // Wyświetlenie następnej litery
    private fun showNextLetter() {
        if (currentLetterIndex < playOrder.size) {
            val letter = playOrder[currentLetterIndex]
            tvQuestion.text = "?"
            playMorseCode(letter)
        } else {
            btnNextLesson.visibility = Button.VISIBLE
        }
    }
    // Odtwarzanie dźwięku dla aktualnej litery
    private fun playCurrentLetter() {
        if (currentLetterIndex < playOrder.size) {
            val letter = playOrder[currentLetterIndex]
            playMorseCode(letter)
        }
    }

    // Odtwarzanie dźwięku dla aktualnej litery
    // Odtwarzanie kodu Morse’a
    // Odtwarzanie kodu Morse’a
    private fun playMorseCode(letter: String) {
        val morseCode = morseMap[letter]

        if (morseCode != null) {
            val morseSequence = morseCode.first // Teraz morseSequence to kod typu .- lub --
            Log.d("Sound", "Odtwarzanie kodu Morse’a dla litery: $letter | Kod: $morseSequence")

            for (char in morseSequence) {
                if (char == '.') {
                    playSound(R.raw.beep_short)
                } else if (char == '-') {
                    playSound(R.raw.beep_long)
                }
            }
        } else {
            Log.e("Sound", "Brak danych dla litery: $letter")
        }
    }



    // Odtwarzanie dźwięku
    private fun playSound(resourceId: Int) {
        mediaPlayer?.release() // Oczyszczamy poprzedni MediaPlayer
        mediaPlayer = MediaPlayer.create(this, resourceId)

        if (mediaPlayer == null) {
            Log.e("Sound", "MediaPlayer nie został poprawnie utworzony!")
            Toast.makeText(this, "Błąd odtwarzania dźwięku", Toast.LENGTH_SHORT).show()
            return
        }

        mediaPlayer?.setOnCompletionListener {
            it.release() // Oczyszczamy MediaPlayer po zakończeniu
            Log.d("Sound", "Dźwięk zakończony.")
        }

        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            Log.e("Sound", "Błąd odtwarzania: $what | $extra")
            true
        }

        mediaPlayer?.start()
        Log.d("Sound", "Odtwarzanie dźwięku: $resourceId")
    }


    // Obsługa kliknięcia litery na klawiaturze
    private fun onLetterClicked(letter: String) {
        if (letter == playOrder[currentLetterIndex]) {
            currentLetterIndex++
            mistakeCount = 0
            showNextLetter()
        } else {
            mistakeCount++
            Toast.makeText(this, "Spróbuj ponownie!", Toast.LENGTH_SHORT).show()
        }
    }

    // Przejście do następnej lekcji
    private fun goToNextLesson() {
        val nextLessonId = "lesson" + (lessonId?.removePrefix("lesson")?.toIntOrNull()?.plus(1) ?: return)
        val intent = Intent(this, LessonActivity::class.java)
        intent.putExtra("LESSON_ID", nextLessonId)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
