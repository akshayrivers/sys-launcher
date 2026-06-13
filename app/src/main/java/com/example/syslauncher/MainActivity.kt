package com.example.syslauncher

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.EditText
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.provider.MediaStore
import com.example.syslauncher.services.VoiceCueManager
import com.example.syslauncher.utils.AccessibilityHelper
import com.example.syslauncher.utils.LoggingHelper
import com.example.syslauncher.utils.PermissionHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: AppPrefs
    private lateinit var contactStore: ContactStore
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var accessibilityHelper: AccessibilityHelper
    private lateinit var voiceCueManager: VoiceCueManager

    private var isAuthForConfig = true

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val email = account?.email.orEmpty().trim()
                    if (email.isNotBlank()) {
                        verifyCaretakerEmailAndProceed(email)
                    } else {
                        Toast.makeText(this, "Google account email not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ApiException) {
                    LoggingHelper.error("Google sign in failed code=${e.statusCode}", e)
                    Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private var pendingNumber: String? = null

    private lateinit var recognizedText: TextView
    private lateinit var pluginInfoText: TextView
    private lateinit var btnCall1: ImageButton
    private lateinit var btnCall2: ImageButton
    private lateinit var btnCall3: ImageButton
    private lateinit var btnCall4: ImageButton
    private lateinit var lblCall1: TextView
    private lateinit var lblCall2: TextView
    private lateinit var lblCall3: TextView
    private lateinit var lblCall4: TextView
    private var speechRecognizer: SpeechRecognizer? = null
    private var flashOn = false

    private val remoteAssistLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val action = result.data?.getStringExtra("assist_action").orEmpty()
            when (action) {
                "CALL_SON" -> {
                    voiceCueManager.sayCallingSon()
                    callWithPermission(numberForRole("SON", prefs.sonNumber()))
                }
                "CALL_HELP" -> {
                    voiceCueManager.sayHelpRequested()
                    callWithPermission(numberForRole("HELP", prefs.helpNumber()))
                }
                "RESET_UI" -> recreate()
            }
        }

    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val confidenceScores = data?.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES)
            val spoken = matches?.firstOrNull().orEmpty()
            val confidence = confidenceScores?.firstOrNull() ?: 1.0f
            if (spoken.isBlank()) {
                recognizedText.text = getString(R.string.recognized_default)
            } else {
                showRecognizedText(spoken, confidence)
            }

            if (spoken.isNotBlank() && confidence >= 0.55f) {
                triggerIntentFromKeywords(spoken.lowercase(Locale.getDefault()), confidence)
            } else if (spoken.isNotBlank()) {
                voiceCueManager.speak(getString(R.string.low_confidence_voice))
                Toast.makeText(this, "Low confidence. Use buttons.", Toast.LENGTH_SHORT).show()
            } else {
                startFallbackSpeechRecognizer()
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val hasCall = grants[Manifest.permission.CALL_PHONE] == true ||
                permissionHelper.hasCallPermission()

            val hasAudio = grants[Manifest.permission.RECORD_AUDIO] == true ||
                permissionHelper.hasAudioPermission()

            val number = pendingNumber
            if (hasCall && number != null) {
                pendingNumber = null
                startCall(number)
            }

            if (!hasAudio) {
                voiceCueManager.speak(getString(R.string.microphone_permission_denied))
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPrefs(this)
        contactStore = ContactStore(this)
        permissionHelper = PermissionHelper(this)
        accessibilityHelper = AccessibilityHelper(this)
        voiceCueManager = VoiceCueManager(this)
        
        LoggingHelper.info("MainActivity created")
        
        if (!prefs.isProvisioned()) {
            startActivity(Intent(this, ProvisioningActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        setupFullscreen()
        setupCrashRecovery()
        setupBackBehavior()
        setupButtons()
        refreshQuickButtons()
        
        voiceCueManager.sayWelcome()
    }

    override fun onResume() {
        super.onResume()
        setupFullscreen()
        refreshQuickButtons()
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        voiceCueManager.destroy()
        setTorch(false)
        super.onDestroy()
    }

    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setupCrashRecovery() {
        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            LoggingHelper.error("Uncaught exception", exception)
            val restartIntent = Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(restartIntent)
            Runtime.getRuntime().exit(0)
        }
    }

    private fun setupBackBehavior() {
        onBackPressedDispatcher.addCallback(this) {
            // Intentionally no-op to reduce accidental exits from launcher screen.
        }
    }

    private fun setupButtons() {
        btnCall1 = findViewById(R.id.btnCall1)
        btnCall2 = findViewById(R.id.btnCall2)
        btnCall3 = findViewById(R.id.btnCall3)
        btnCall4 = findViewById(R.id.btnCall4)
        val btnVoice = findViewById<Button>(R.id.btnVoice)
        val btnContacts = findViewById<Button>(R.id.btnContactsSection)
        val btnGallery = findViewById<Button>(R.id.btnGallerySection)
        val btnYoutube = findViewById<Button>(R.id.btnYoutubeSection)
        val btnWhatsapp = findViewById<Button>(R.id.btnWhatsapp)
        val btnFlashlight = findViewById<Button>(R.id.btnFlashlight)
        val btnSpeakTime = findViewById<Button>(R.id.btnSpeakTime)
        val btnVolumeDown = findViewById<Button>(R.id.btnVolumeDown)
        val btnVolumeUp = findViewById<Button>(R.id.btnVolumeUp)
        val btnAi = findViewById<Button>(R.id.btnAiAssistant)
        val btnConfig = findViewById<Button>(R.id.btnCaretakerConfig)
        recognizedText = findViewById(R.id.txtRecognized)
        pluginInfoText = findViewById(R.id.txtPluginInfo)
        lblCall1 = findViewById(R.id.lblCall1)
        lblCall2 = findViewById(R.id.lblCall2)
        lblCall3 = findViewById(R.id.lblCall3)
        lblCall4 = findViewById(R.id.lblCall4)

        pluginInfoText.text = "Lang: ${prefs.language()} | Plugin: ${prefs.plugin()}"

        // Setup accessibility
        accessibilityHelper.setupAccessibleImageButton(btnCall1, getString(R.string.contact_1))
        accessibilityHelper.setupAccessibleImageButton(btnCall2, getString(R.string.contact_2))
        accessibilityHelper.setupAccessibleImageButton(btnCall3, getString(R.string.contact_3))
        accessibilityHelper.setupAccessibleImageButton(btnCall4, getString(R.string.contact_4))
        accessibilityHelper.setupAccessibleButton(btnVoice, getString(R.string.voice_button))
        accessibilityHelper.setupAccessibleButton(btnContacts, getString(R.string.section_contacts))
        accessibilityHelper.setupAccessibleButton(btnGallery, getString(R.string.section_gallery))
        accessibilityHelper.setupAccessibleButton(btnYoutube, getString(R.string.section_youtube))
        accessibilityHelper.setupAccessibleButton(btnWhatsapp, getString(R.string.section_whatsapp))
        accessibilityHelper.setupAccessibleButton(btnFlashlight, getString(R.string.section_flashlight))
        accessibilityHelper.setupAccessibleButton(btnSpeakTime, getString(R.string.section_speak_time))
        accessibilityHelper.setupAccessibleButton(btnVolumeDown, getString(R.string.volume_down))
        accessibilityHelper.setupAccessibleButton(btnVolumeUp, getString(R.string.volume_up))
        accessibilityHelper.setupAccessibleButton(btnAi, getString(R.string.section_ai))
        accessibilityHelper.setupAccessibleButton(btnConfig, getString(R.string.section_config))

        btnCall1.setOnClickListener { callSlot(1, "SON", prefs.sonNumber()) }
        btnCall2.setOnClickListener { callSlot(2, "DAUGHTER", prefs.daughterNumber()) }
        btnCall3.setOnClickListener { callSlot(3, "HOME", prefs.homeNumber()) }
        btnCall4.setOnClickListener { onHelpPressed() }
        btnCall1.setOnLongClickListener { showSlotAssignmentDialog(1); true }
        btnCall2.setOnLongClickListener { showSlotAssignmentDialog(2); true }
        btnCall3.setOnLongClickListener { showSlotAssignmentDialog(3); true }
        btnCall4.setOnLongClickListener { showSlotAssignmentDialog(4); true }
        btnVoice.setOnClickListener { launchVoiceInput() }
        btnContacts.setOnClickListener { 
            voiceCueManager.speak(getString(R.string.section_contacts))
            startActivity(Intent(this, ContactsActivity::class.java)) 
        }
        btnGallery.setOnClickListener { 
            voiceCueManager.sayOpeningGallery()
            openGallery() 
        }
        btnYoutube.setOnClickListener { 
            voiceCueManager.sayOpeningYouTube()
            openYoutube() 
        }
        btnWhatsapp.setOnClickListener { 
            voiceCueManager.sayOpeningWhatsApp()
            openWhatsApp() 
        }
        btnFlashlight.setOnClickListener { toggleFlashlight(btnFlashlight) }
        btnSpeakTime.setOnClickListener { speakTimeNow() }
        btnVolumeDown.setOnClickListener { changeVolume(false) }
        btnVolumeUp.setOnClickListener { changeVolume(true) }
        btnAi.setOnClickListener { 
            voiceCueManager.speak(getString(R.string.section_ai))
            startActivity(Intent(this, LocalAiActivity::class.java)) 
        }
        btnConfig.setOnClickListener { openCaretakerConfig() }
    }

    private fun launchVoiceInput() {
        if (!permissionHelper.hasAudioPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                )
            )
            return
        }

        voiceCueManager.sayListeningForVoiceCommand()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command")
        }

        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            startFallbackSpeechRecognizer()
        }
    }

    private fun triggerIntentFromKeywords(spoken: String, confidence: Float) {
        val structuredIntent = when {
            spoken.contains("son") -> VoiceIntent("CALL", "SON", confidence)
            spoken.contains("daughter") -> VoiceIntent("CALL", "DAUGHTER", confidence)
            spoken.contains("home") -> VoiceIntent("CALL", "HOME", confidence)
            spoken.contains("help") -> VoiceIntent("CALL", "HELP", confidence)
            spoken.contains("time") -> VoiceIntent("ACTION", "TIME", confidence)
            spoken.contains("light") || spoken.contains("torch") -> VoiceIntent("ACTION", "TORCH", confidence)
            else -> null
        } ?: return

        executeVoiceIntent(structuredIntent)
    }

    private fun executeVoiceIntent(intent: VoiceIntent) {
        if (intent.confidence < 0.55f) {
            return
        }
        when {
            intent.action == "CALL" && intent.target == "SON" -> callSlot(1, "SON", prefs.sonNumber())
            intent.action == "CALL" && intent.target == "DAUGHTER" -> callSlot(2, "DAUGHTER", prefs.daughterNumber())
            intent.action == "CALL" && intent.target == "HOME" -> callSlot(3, "HOME", prefs.homeNumber())
            intent.action == "CALL" && intent.target == "HELP" -> onHelpPressed()
            intent.action == "ACTION" && intent.target == "TIME" -> speakTimeNow()
            intent.action == "ACTION" && intent.target == "TORCH" -> toggleFlashlight(findViewById(R.id.btnFlashlight))
        }
    }

    private fun callWithPermission(number: String) {
        if (number.isBlank()) {
            voiceCueManager.speak(getString(R.string.no_number_configured))
            Toast.makeText(this, "No number configured", Toast.LENGTH_SHORT).show()
            return
        }
        if (permissionHelper.hasCallPermission()) {
            startCall(number)
            return
        }

        pendingNumber = number
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)
        ) {
            voiceCueManager.speak(getString(R.string.phone_permission_denied))
        }

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CALL_PHONE
            )
        )
    }

    private fun startCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
        }
        try {
            startActivity(intent)
            LoggingHelper.info("Call initiated to $number")
        } catch (_: SecurityException) {
            voiceCueManager.speak(getString(R.string.phone_permission_denied))
        } catch (_: Exception) {
            voiceCueManager.speak(getString(R.string.unable_to_call))
        }
    }

    private fun onHelpPressed() {
        voiceCueManager.sayHelpRequested()
        notifyHelper()

        AlertDialog.Builder(this)
            .setTitle("Remote help request")
            .setMessage("Allow caretaker assist session for 10 minutes?")
            .setPositiveButton("Allow") { _: DialogInterface, _: Int ->
                showCaretakerAuthDialog(false)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun notifyHelper() {
        val helper = prefs.helperAccounts().firstOrNull()
        if (helper == null) {
            callWithPermission(numberForRole("HELP", prefs.helpNumber()))
            return
        }

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$helper")
            putExtra("sms_body", "Help requested from Senior Launcher device.")
        }
        try {
            startActivity(smsIntent)
        } catch (_: Exception) {
            callWithPermission(numberForRole("HELP", prefs.helpNumber()))
        }
    }

    private fun showCaretakerAuthDialog(forConfig: Boolean) {
        isAuthForConfig = forConfig
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_caretaker_auth, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnGoogle = dialogView.findViewById<Button>(R.id.btnAuthGoogle)
        val etPin = dialogView.findViewById<EditText>(R.id.etAuthPin)
        val btnVerifyPin = dialogView.findViewById<Button>(R.id.btnAuthVerifyPin)

        btnGoogle.setOnClickListener {
            dialog.dismiss()
            startGoogleSignInFlow()
        }

        btnVerifyPin.setOnClickListener {
            val pin = etPin.text.toString().trim()
            if (pin == prefs.caretakerPin()) {
                dialog.dismiss()
                if (forConfig) {
                    voiceCueManager.sayEnteringCaretakerMode()
                    startActivity(Intent(this, CaretakerConfigActivity::class.java))
                } else {
                    val assistIntent = Intent(this, RemoteAssistActivity::class.java).apply {
                        putExtra("session_started_at", SystemClock.elapsedRealtime())
                    }
                    remoteAssistLauncher.launch(assistIntent)
                }
            } else {
                voiceCueManager.sayInvalidPin()
                Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun startGoogleSignInFlow() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun verifyCaretakerEmailAndProceed(email: String) {
        val caretakerEmail = prefs.caretakerEmail().trim().lowercase(Locale.getDefault())
        val helperEmails = prefs.helperAccounts().map { it.trim().lowercase(Locale.getDefault()) }
        val inputEmail = email.lowercase(Locale.getDefault())

        if (inputEmail == caretakerEmail || helperEmails.contains(inputEmail)) {
            if (isAuthForConfig) {
                voiceCueManager.sayEnteringCaretakerMode()
                startActivity(Intent(this, CaretakerConfigActivity::class.java))
            } else {
                val assistIntent = Intent(this, RemoteAssistActivity::class.java).apply {
                    putExtra("session_started_at", SystemClock.elapsedRealtime())
                }
                remoteAssistLauncher.launch(assistIntent)
            }
        } else {
            val errMsg = "Access Denied: $email is not registered"
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
            voiceCueManager.speak("Access denied. This Google account is not registered.")
        }
    }

    data class VoiceIntent(
        val action: String,
        val target: String,
        val confidence: Float
    )

    private fun numberForRole(role: String, fallback: String): String {
        return contactStore.findByRole(role)?.phone ?: fallback
    }

    private fun bindQuickContact(button: ImageButton, role: String) {
        val contact = contactStore.findByRole(role) ?: return
        if (!contact.photoUri.isNullOrBlank()) {
            try {
                button.setImageURI(Uri.parse(contact.photoUri))
            } catch (_: Exception) {
                // Keep placeholder image if custom image cannot be loaded.
            }
        }
    }

    private fun refreshQuickButtons() {
        bindSlotContact(btnCall1, 1, "SON")
        bindSlotContact(btnCall2, 2, "DAUGHTER")
        bindSlotContact(btnCall3, 3, "HOME")
        bindSlotContact(btnCall4, 4, "HELP")
        refreshContactLabels()
    }

    private fun bindSlotContact(button: ImageButton, slot: Int, fallbackRole: String) {
        val contact = contactForSlot(slot, fallbackRole)
        if (!contact?.photoUri.isNullOrBlank()) {
            try {
                button.setImageURI(Uri.parse(contact?.photoUri))
                return
            } catch (_: Exception) {
            }
        }
        bindQuickContact(button, fallbackRole)
    }

    private fun contactForSlot(slot: Int, fallbackRole: String): ContactItem? {
        val assignedId = prefs.homeSlotContact(slot)
        return if (assignedId.isNotBlank()) {
            contactStore.findById(assignedId)
        } else {
            contactStore.findByRole(fallbackRole)
        }
    }

    private fun callSlot(slot: Int, fallbackRole: String, fallbackNumber: String) {
        val contact = contactForSlot(slot, fallbackRole)
        val name = contact?.name ?: fallbackRole
        voiceCueManager.sayCalling(name)
        val number = contact?.phone ?: numberForRole(fallbackRole, fallbackNumber)
        callWithPermission(number)
    }

    private fun showSlotAssignmentDialog(slot: Int) {
        val contacts = contactStore.loadContacts()
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No contacts available", Toast.LENGTH_SHORT).show()
            return
        }
        val names = contacts.map { "${it.name} (${it.role})" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Assign quick slot $slot")
            .setItems(names) { _, which ->
                prefs.setHomeSlotContact(slot, contacts[which].id)
                refreshQuickButtons()
                Toast.makeText(this, "Quick contact updated", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Use default role") { _, _ ->
                prefs.setHomeSlotContact(slot, "")
                refreshQuickButtons()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startFallbackSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        Toast.makeText(this@MainActivity, "Voice error: $error", Toast.LENGTH_SHORT).show()
                    }
                    override fun onResults(results: Bundle?) {
                        val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = list?.firstOrNull().orEmpty()
                        if (spoken.isBlank()) return
                        showRecognizedText(spoken, 0.7f)
                        triggerIntentFromKeywords(spoken.lowercase(Locale.getDefault()), 0.7f)
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

    private fun showRecognizedText(text: String, confidence: Float) {
        recognizedText.text = "$text (conf: ${"%.2f".format(confidence)})"
    }

    private fun refreshContactLabels() {
        lblCall1.text = contactForSlot(1, "SON")?.name ?: getString(R.string.contact_1)
        lblCall2.text = contactForSlot(2, "DAUGHTER")?.name ?: getString(R.string.contact_2)
        lblCall3.text = contactForSlot(3, "HOME")?.name ?: getString(R.string.contact_3)
        lblCall4.text = contactForSlot(4, "HELP")?.name ?: getString(R.string.contact_4)
    }

    private fun openGallery() {
        val galleryAppIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_GALLERY)
        }
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val filesImageIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        try {
            startActivity(galleryAppIntent)
        } catch (_: Exception) {
            try {
                startActivity(pickImageIntent)
            } catch (_: Exception) {
                try {
                    startActivity(filesImageIntent)
                } catch (_: Exception) {
                    Toast.makeText(this, "Gallery unavailable", Toast.LENGTH_SHORT).show()
                }
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

    private fun toggleFlashlight(button: Button) {
        flashOn = !flashOn
        if (!setTorch(flashOn)) {
            flashOn = false
            Toast.makeText(this, "Torch unavailable", Toast.LENGTH_SHORT).show()
        } else {
            if (flashOn) voiceCueManager.sayFlashlightOn() else voiceCueManager.sayFlashlightOff()
        }
        button.text = if (flashOn) "Torch ON" else getString(R.string.section_flashlight)
    }

    private fun setTorch(enabled: Boolean): Boolean {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val chars = cameraManager.getCameraCharacteristics(id)
            val flash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val facingBack = chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            flash && facingBack
        } ?: return false
        return try {
            cameraManager.setTorchMode(cameraId, enabled)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun changeVolume(isIncrease: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val direction = if (isIncrease) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        if (isIncrease) voiceCueManager.sayVolumeIncreased() else voiceCueManager.sayVolumeDecreased()
    }

    private fun speakTimeNow() {
        val now = Date()
        val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
        recognizedText.text = "Time: $timeString"
        voiceCueManager.sayTimeIs(timeString)
    }

    private fun openCaretakerConfig() {
        showCaretakerAuthDialog(true)
    }
}
