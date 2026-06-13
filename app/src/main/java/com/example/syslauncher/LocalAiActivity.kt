package com.example.syslauncher

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.syslauncher.services.SimpleAiAssistant
import com.example.syslauncher.services.VoiceCueManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalAiActivity : AppCompatActivity() {

    private lateinit var voiceCueManager: VoiceCueManager
    private lateinit var aiAssistant: SimpleAiAssistant
    private lateinit var prefs: AppPrefs
    private lateinit var contactStore: ContactStore
    
    private lateinit var inputView: EditText
    private lateinit var outputView: TextView
    private lateinit var btnListen: Button
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var flashOn = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val hasAudio = grants[Manifest.permission.RECORD_AUDIO] == true
            if (hasAudio) {
                startListeningForVoice()
            } else {
                voiceCueManager.speak("Microphone permission is needed to listen to your voice.")
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_ai)

        voiceCueManager = VoiceCueManager(this)
        aiAssistant = SimpleAiAssistant(this)
        prefs = AppPrefs(this)
        contactStore = ContactStore(this)

        inputView = findViewById(R.id.etAiInput)
        outputView = findViewById(R.id.tvAiOutput)
        btnListen = findViewById(R.id.btnAiRun)

        val lang = prefs.language()
        val welcomeMsg = when (lang.lowercase()) {
            "hindi" -> "आप मुझसे फोन का इस्तेमाल करने के बारे में कुछ भी पूछ सकते हैं। सुनने के लिए बटन दबाएं।"
            "dogri" -> "तुस मेरे कोलूं फोन चलाने बारे किश भी पुच्छी सकदे ओ। सुन्न आस्तै बटन नपो।"
            else -> "Ask me anything about using this phone. Tap the button to start."
        }
        voiceCueManager.speak(welcomeMsg)
        outputView.text = welcomeMsg

        btnListen.setOnClickListener {
            if (!prefs.localModelInstalled()) {
                val missingMsg = getString(R.string.local_ai_missing)
                voiceCueManager.speak(missingMsg)
                Toast.makeText(this, missingMsg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkAudioPermissionAndListen()
        }
    }

    private fun checkAudioPermissionAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListeningForVoice()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun startListeningForVoice() {
        voiceCueManager.sayListeningForVoiceCommand()
        
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        Toast.makeText(this@LocalAiActivity, "Voice error: $error", Toast.LENGTH_SHORT).show()
                    }
                    override fun onResults(results: Bundle?) {
                        val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = list?.firstOrNull().orEmpty()
                        if (spoken.isNotBlank()) {
                            inputView.setText(spoken)
                            processUserQuery(spoken)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer?.startListening(intent)
    }

    private fun processUserQuery(query: String) {
        val aiResponse = aiAssistant.processQuery(query)
        outputView.text = aiResponse.message
        voiceCueManager.speak(aiResponse.message)
        
        if (aiResponse.action != null) {
            btnListen.postDelayed({
                executeAiAction(aiResponse.action)
            }, 2500) // Delay slightly so the user hears the TTS speech before action executes
        }
    }

    private fun executeAiAction(action: String) {
        when (action) {
            "CALL_SON" -> makeCall(prefs.sonNumber(), "SON")
            "CALL_DAUGHTER" -> makeCall(prefs.daughterNumber(), "DAUGHTER")
            "CALL_HOME" -> makeCall(prefs.homeNumber(), "HOME")
            "CALL_HELP" -> makeCall(prefs.helpNumber(), "HELP")
            "OPEN_GALLERY" -> openGallery()
            "OPEN_YOUTUBE" -> openYoutube()
            "OPEN_WHATSAPP" -> openWhatsApp()
            "TOGGLE_FLASHLIGHT" -> toggleFlashlight()
            "SPEAK_TIME" -> speakTimeNow()
            "ADJUST_VOLUME" -> changeVolume(true)
            "OPEN_CONTACTS" -> startActivity(Intent(this, ContactsActivity::class.java))
        }
    }

    private fun makeCall(number: String, role: String) {
        val finalNumber = contactStore.findByRole(role)?.phone ?: number
        if (finalNumber.isBlank()) {
            val noNum = getString(R.string.no_number_configured)
            voiceCueManager.speak(noNum)
            Toast.makeText(this, noNum, Toast.LENGTH_SHORT).show()
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$finalNumber")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Call permission needed to call $role", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val galleryAppIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_GALLERY)
        }
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivity(galleryAppIntent)
        } catch (_: Exception) {
            try {
                startActivity(pickImageIntent)
            } catch (_: Exception) {
                Toast.makeText(this, "Gallery unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openYoutube() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        try {
            startActivity(appIntent)
        } catch (_: Exception) {
            try {
                startActivity(webIntent)
            } catch (_: Exception) {
                Toast.makeText(this, "YouTube unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openWhatsApp() {
        val appIntent = packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (appIntent != null) {
            startActivity(appIntent)
            return
        }
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"))
        try {
            startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(this, "WhatsApp unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFlashlight() {
        flashOn = !flashOn
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val chars = cameraManager.getCameraCharacteristics(id)
            val flash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val facingBack = chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            flash && facingBack
        } ?: return

        try {
            cameraManager.setTorchMode(cameraId, flashOn)
            if (flashOn) voiceCueManager.sayFlashlightOn() else voiceCueManager.sayFlashlightOff()
        } catch (_: Exception) {
            Toast.makeText(this, "Torch error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakTimeNow() {
        val now = Date()
        val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
        outputView.text = "Time: $timeString"
        voiceCueManager.sayTimeIs(timeString)
    }

    private fun changeVolume(isIncrease: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val direction = if (isIncrease) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        if (isIncrease) voiceCueManager.sayVolumeIncreased() else voiceCueManager.sayVolumeDecreased()
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        voiceCueManager.destroy()
        super.onDestroy()
    }
}
