package com.example.syslauncher.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Manages voice cues and audio feedback for accessibility.
 * This system provides clear audio guidance for seniors with reading difficulties.
 */
class VoiceCueManager(context: Context) : TextToSpeech.OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        textToSpeech = TextToSpeech(context, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.getDefault()
            textToSpeech?.setPitch(0.9f) // Slightly higher pitch for clarity
            textToSpeech?.setSpeechRate(0.8f) // Slower speech for elderly users
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
    
    // Pre-recorded voice cues for common actions
    fun sayWelcome() {
        speak("Welcome to Senior Launcher. Tap photos to call family members.")
    }
    
    fun sayCallingSon() {
        speak("Calling your son now")
    }
    
    fun sayCallingDaughter() {
        speak("Calling your daughter now")
    }
    
    fun sayCallingHome() {
        speak("Calling home now")
    }
    
    fun sayHelpRequested() {
        speak("Help requested. Your caretaker will assist you.")
    }
    
    fun sayOpeningGallery() {
        speak("Opening photos")
    }
    
    fun sayOpeningYouTube() {
        speak("Opening YouTube")
    }
    
    fun sayOpeningWhatsApp() {
        speak("Opening WhatsApp")
    }
    
    fun sayFlashlightOn() {
        speak("Flashlight turned on")
    }
    
    fun sayFlashlightOff() {
        speak("Flashlight turned off")
    }
    
    fun sayListeningForVoiceCommand() {
        speak("I am listening. Say your command now.")
    }
    
    fun sayTimeIs(time: String) {
        speak("The time is $time")
    }
    
    fun sayVolumeIncreased() {
        speak("Volume increased")
    }
    
    fun sayVolumeDecreased() {
        speak("Volume decreased")
    }
    
    fun sayTapToCall() {
        speak("Tap the photo to call this person")
    }
    
    fun sayLongPressToChange() {
        speak("Press and hold to change this contact")
    }
    
    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
