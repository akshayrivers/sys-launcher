package com.example.syslauncher.services

import android.content.Context
import com.example.syslauncher.utils.LoggingHelper

/**
 * Simple AI Assistant for common queries from elderly users.
 * This is a lightweight, offline AI that understands simple commands.
 */
class SimpleAiAssistant(private val context: Context) {
    
    data class AiResponse(
        val message: String,
        val action: String? = null,  // e.g., "CALL_SON", "OPEN_GALLERY"
        val requiresConfirmation: Boolean = false
    )
    
    /**
     * Process natural language query from senior users
     */
    fun processQuery(query: String): AiResponse {
        val normalized = query.lowercase().trim()
        LoggingHelper.info("AI Processing: $normalized")
        
        return when {
            // Call intents
            normalized.contains("call son") || normalized.contains("phone son") ->
                AiResponse("I will call your son for you.", "CALL_SON", true)
            
            normalized.contains("call daughter") || normalized.contains("phone daughter") ->
                AiResponse("I will call your daughter for you.", "CALL_DAUGHTER", true)
            
            normalized.contains("call home") || normalized.contains("phone home") ->
                AiResponse("I will call your home for you.", "CALL_HOME", true)
            
            normalized.contains("help") || normalized.contains("emergency") || normalized.contains("urgent") ->
                AiResponse("I will get help for you immediately.", "CALL_HELP", true)
            
            // Gallery intents
            normalized.contains("gallery") || normalized.contains("photo") || normalized.contains("picture") ->
                AiResponse("Opening your photo gallery.", "OPEN_GALLERY", false)
            
            // YouTube intents
            normalized.contains("youtube") || normalized.contains("video") ->
                AiResponse("Opening YouTube for you.", "OPEN_YOUTUBE", false)
            
            // WhatsApp intents
            normalized.contains("whatsapp") || normalized.contains("message") ->
                AiResponse("Opening WhatsApp to send messages.", "OPEN_WHATSAPP", false)
            
            // Flashlight intents
            normalized.contains("torch") || normalized.contains("flashlight") || normalized.contains("light") ->
                AiResponse("Turning on the flashlight.", "TOGGLE_FLASHLIGHT", false)
            
            // Time intents
            normalized.contains("time") || normalized.contains("what time") ->
                AiResponse("Telling you the time now.", "SPEAK_TIME", false)
            
            // Volume intents
            normalized.contains("volume") || normalized.contains("louder") ->
                AiResponse("Adjusting volume for you.", "ADJUST_VOLUME", false)
            
            // Contacts intents
            normalized.contains("contact") || normalized.contains("phone book") ->
                AiResponse("Opening your contacts.", "OPEN_CONTACTS", false)
            
            // Help/Tutorial intents
            normalized.contains("help") || normalized.contains("how") || normalized.contains("what can you do") ->
                AiResponse("I can help you call family, open photos, YouTube, WhatsApp, and more. Speak your need simply.", null, false)
            
            // Default response
            else -> AiResponse("I understand you want: $query. Please speak more clearly or use the buttons.", null, false)
        }
    }
    
    /**
     * Get available commands for UI help
     */
    fun getAvailableCommands(): List<String> {
        return listOf(
            "Call son",
            "Call daughter",
            "Call home",
            "Help (emergency)",
            "Gallery/Photos",
            "YouTube",
            "WhatsApp",
            "Flashlight/Torch",
            "What time is it?",
            "Contacts"
        )
    }
}
