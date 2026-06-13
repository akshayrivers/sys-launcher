package com.example.syslauncher.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import com.example.syslauncher.AppPrefs

/**
 * Manages voice cues and audio feedback for accessibility.
 * This system provides clear audio guidance for seniors with reading difficulties.
 */
class VoiceCueManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private lateinit var prefs: AppPrefs
    
    init {
        textToSpeech = TextToSpeech(context, this)
        prefs = AppPrefs(context)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val lang = prefs.language().lowercase()
            val locale = when (lang) {
                "hindi" -> Locale("hi", "IN")
                "dogri" -> Locale("hi", "IN") // Dogri uses similar phonetics/Devanagari, standard fallback
                else -> Locale.getDefault()
            }
            textToSpeech?.language = locale
            textToSpeech?.setPitch(0.85f) // Slightly lower pitch for deep clarity
            textToSpeech?.setSpeechRate(0.75f) // Slower speed for senior citizens
            isInitialized = true
        }
    }
    
    /**
     * Speak with clear, simple language for seniors
     */
    fun speak(message: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (isInitialized && textToSpeech != null) {
            textToSpeech?.speak(message, queueMode, null, "default")
        }
    }
    
    // Pre-recorded localized voice cues for common actions
    fun sayWelcome() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "सीनियर लॉन्चर में आपका स्वागत है। परिवार को फोन करने के लिए फोटो पर टैप करें।"
            "dogri" -> "सीनियर लॉन्चर च तुंदा स्वागत ऐ। परिवार गी फोन करने आस्तै फोटो पर टैप करो।"
            else -> "Welcome to Senior Launcher. Tap photos to call family members."
        }
        speak(msg)
    }
    
    fun sayCalling(name: String) {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "$name को फोन लगाया जा रहा है।"
            "dogri" -> "$name गी फोन लाया जा करदा ऐ।"
            else -> "Calling $name now."
        }
        speak(msg)
    }

    fun sayCallingSon() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "आपके बेटे को फोन लगाया जा रहा है।"
            "dogri" -> "तुंदे पुत्तर गी फोन लाया जा करदा ऐ।"
            else -> "Calling your son now."
        }
        speak(msg)
    }
    
    fun sayCallingDaughter() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "आपकी बेटी को फोन लगाया जा रहा है।"
            "dogri" -> "तुंदी धी गी फोन लाया जा करदा ऐ।"
            else -> "Calling your daughter now."
        }
        speak(msg)
    }
    
    fun sayCallingHome() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "घर पर फोन लगाया जा रहा है।"
            "dogri" -> "घरे फोन लाया जा करदा ऐ।"
            else -> "Calling home now."
        }
        speak(msg)
    }
    
    fun sayHelpRequested() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "मदद की गुहार भेजी गई है। आपके केयरटेकर आपकी सहायता करेंगे।"
            "dogri" -> "मदद दी अपील भेजी गई ऐ। तुंदे केयरटेकर तुंदी सहायता करगे।"
            else -> "Help requested. Your caretaker will assist you."
        }
        speak(msg)
    }
    
    fun sayOpeningGallery() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "फ़ोटो गैलरी खोली जा रही है।"
            "dogri" -> "फ़ोटो गैलरी खोली जा करदी ऐ।"
            else -> "Opening photos."
        }
        speak(msg)
    }
    
    fun sayOpeningYouTube() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "यूट्यूब खोला जा रहा है।"
            "dogri" -> "यूट्यूब खोला जा करदा ऐ।"
            else -> "Opening YouTube."
        }
        speak(msg)
    }
    
    fun sayOpeningWhatsApp() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "व्हाट्सएप खोला जा रहा है।"
            "dogri" -> "व्हाट्सएप खोला जा करदा ऐ।"
            else -> "Opening WhatsApp."
        }
        speak(msg)
    }
    
    fun sayFlashlightOn() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "टॉर्च चालू कर दी गई है।"
            "dogri" -> "टॉर्च चालू करी दित्ती गई ऐ।"
            else -> "Flashlight turned on."
        }
        speak(msg)
    }
    
    fun sayFlashlightOff() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "टॉर्च बंद कर दी गई है।"
            "dogri" -> "टॉर्च बंद करी दित्ती गई ऐ।"
            else -> "Flashlight turned off."
        }
        speak(msg)
    }
    
    fun sayListeningForVoiceCommand() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "मैं सुन रहा हूँ। अपना आदेश बोलें।"
            "dogri" -> "मैं सुनदा पियां। अपना हुक्म बोलो।"
            else -> "I am listening. Say your command now."
        }
        speak(msg)
    }
    
    fun sayTimeIs(time: String) {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "समय $time हुआ है।"
            "dogri" -> "वक्त $time होया ऐ।"
            else -> "The time is $time."
        }
        speak(msg)
    }
    
    fun sayVolumeIncreased() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "आवाज़ बढ़ा दी गई है।"
            "dogri" -> "आवाज़ बताई दित्ती गई ऐ।"
            else -> "Volume increased."
        }
        speak(msg)
    }
    
    fun sayVolumeDecreased() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "आवाज़ कम कर दी गई है।"
            "dogri" -> "आवाज़ कट करी दित्ती गई ऐ।"
            else -> "Volume decreased."
        }
        speak(msg)
    }
    
    fun sayTapToCall() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "फोन करने के लिए इस फोटो पर टैप करें।"
            "dogri" -> "फोन करने आस्तै इस फोटो पर टैप करो।"
            else -> "Tap the photo to call this person."
        }
        speak(msg)
    }
    
    fun sayLongPressToChange() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "संपर्क बदलने के लिए दबाकर रखें।"
            "dogri" -> "संपर्क बदलने आस्तै दबोई रखियै।"
            else -> "Press and hold to change this contact."
        }
        speak(msg)
    }

    fun sayAppClosed() {
        speak("Closing app")
    }

    fun sayInvalidPin() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "गलत कोड। कृपया दोबारा प्रयास करें।"
            "dogri" -> "गलत कोड। कृपया दुबारा कोशिश करो।"
            else -> "Incorrect code. Please try again."
        }
        speak(msg)
    }

    fun sayProvisioningComplete() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "सेटअप पूरा हुआ। घर में आपका स्वागत है।"
            "dogri" -> "सेटअप पूरा होया। घरे च तुंदा स्वागत ऐ।"
            else -> "Setup complete. Welcome home."
        }
        speak(msg)
    }

    fun sayEnteringCaretakerMode() {
        val msg = when (prefs.language().lowercase()) {
            "hindi" -> "केयरटेकर सेटअप मोड में प्रवेश कर रहे हैं।"
            "dogri" -> "केयरटेकर सेटअप मोड च जा करदे ओ।"
            else -> "Entering caretaker setup mode."
        }
        speak(msg)
    }
    
    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
