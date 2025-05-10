package com.s26493.morsecode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class TranslatorActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var outputMorse: TextView
    private lateinit var btnGo: Button
    private lateinit var btnPlay: Button
    private lateinit var btnFlashlight: Button

    private lateinit var db: FirebaseFirestore
    private val morseCodeMap = mutableMapOf<Char, String>()
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translator)


        inputText = findViewById(R.id.inputText)
        outputMorse = findViewById(R.id.outputMorse)
        btnGo = findViewById(R.id.btnGo)
        btnPlay = findViewById(R.id.btnPlay)
        btnFlashlight = findViewById(R.id.btnFlashlight)


        db = FirebaseFirestore.getInstance()
        loadMorseData()


        checkCameraPermission()


        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.firstOrNull()


        btnGo.setOnClickListener {
            val text = inputText.text.toString().uppercase()
            val morseTranslation = text.map { char ->
                morseCodeMap[char] ?: ""
            }.joinToString(" ")

            outputMorse.text = if (morseTranslation.isNotEmpty()) {
                morseTranslation
            } else {
                "Translation error"
            }
        }


        btnPlay.setOnClickListener {
            playMorse(outputMorse.text.toString())
        }


        btnFlashlight.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                playMorseWithFlashlight(outputMorse.text.toString())
            } else {
                Toast.makeText(this, "Camera permission is required for flashlight", Toast.LENGTH_SHORT).show()
                checkCameraPermission()
            }
        }
    }


    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                101
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun playMorseWithFlashlight(morse: String) {
        Thread {
            println("Flashlight: Starting Morse playback with flashlight")
            for (char in morse) {
                when (char) {
                    '.' -> flashLight(true, 200)
                    '-' -> flashLight(true, 600)
                    ' ' -> Thread.sleep(400)
                }
                flashLight(false, 200) // Отключение вспышки между символами
            }
            println("Flashlight: Morse playback with flashlight finished")
        }.start()
    }

    private fun flashLight(state: Boolean, duration: Long) {
        try {
            println("Flashlight: Method called. State: $state, Duration: $duration ms")

            if (state) {
                cameraId?.let {
                    cameraManager.setTorchMode(it, true)
                    println("Flashlight: Torch mode enabled")
                }
            } else {
                cameraId?.let {
                    cameraManager.setTorchMode(it, false)
                    println("Flashlight: Torch mode disabled")
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                cameraId?.let {
                    cameraManager.setTorchMode(it, false)
                    println("Flashlight: Torch mode turned OFF after delay ($duration ms)")
                }
            }, duration)

        } catch (e: CameraAccessException) {
            e.printStackTrace()
            println("Flashlight ERROR: ${e.message}")
        }
    }


    private fun playMorse(morse: String) {
        val shortBeep = R.raw.beep_short
        val longBeep = R.raw.beep_long

        Thread {
            println("Sound: Starting Morse playback with sound")
            for (char in morse) {
                when (char) {
                    '.' -> playSound(shortBeep)
                    '-' -> playSound(longBeep)
                    ' ' -> Thread.sleep(400)
                }
                Thread.sleep(200)
            }
            println("Sound: Morse playback with sound finished")
        }.start()
    }

    private fun playSound(resourceId: Int) {
        val mediaPlayer = MediaPlayer.create(this, resourceId)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }


    private fun loadMorseData() {
        db.collection("morseCode").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val key = document.id.firstOrNull()
                    val code = document.getString("code")
                    if (key != null && code != null) {
                        morseCodeMap[key] = code
                    }
                }
                Toast.makeText(this, "Morse data loaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load Morse data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
