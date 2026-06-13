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
    /**
     * Process natural language query from senior users
     */
    fun processQuery(query: String): AiResponse {
        val normalized = query.lowercase().trim()
        val prefs = com.example.syslauncher.AppPrefs(context)
        val lang = prefs.language().lowercase()
        LoggingHelper.info("AI Processing: $normalized in language: $lang")
        
        // Check for Call Son
        val isCallSon = normalized.contains("call son") || normalized.contains("phone son") || 
            (lang == "hindi" && (normalized.contains("beta") || normalized.contains("bete ko") || normalized.contains("पुत्र") || normalized.contains("बेटा"))) ||
            (lang == "dogri" && (normalized.contains("puttar") || normalized.contains("putre") || normalized.contains("पुत्तर")))
            
        // Check for Call Daughter
        val isCallDaughter = normalized.contains("call daughter") || normalized.contains("phone daughter") ||
            (lang == "hindi" && (normalized.contains("beti") || normalized.contains("बेटी") || normalized.contains("बिटिया"))) ||
            (lang == "dogri" && (normalized.contains("dhee") || normalized.contains("dhiya") || normalized.contains("धी")))
            
        // Check for Call Home
        val isCallHome = normalized.contains("call home") || normalized.contains("phone home") ||
            (lang == "hindi" && (normalized.contains("ghar") || normalized.contains("घर"))) ||
            (lang == "dogri" && (normalized.contains("ghar") || normalized.contains("घर")))
            
        // Check for Help
        val isHelp = normalized.contains("help") || normalized.contains("emergency") || normalized.contains("urgent") ||
            (lang == "hindi" && (normalized.contains("madad") || normalized.contains("bachao") || normalized.contains("मदद") || normalized.contains("आपातकाल"))) ||
            (lang == "dogri" && (normalized.contains("madad") || normalized.contains("bachao") || normalized.contains("मदद") || normalized.contains("सहायता")))

        // Check for Gallery
        val isGallery = normalized.contains("gallery") || normalized.contains("photo") || normalized.contains("picture") || normalized.contains("album") ||
            (lang == "hindi" && (normalized.contains("tasveer") || normalized.contains("तस्वीर") || normalized.contains("फ़ोटो"))) ||
            (lang == "dogri" && (normalized.contains("tasveer") || normalized.contains("तस्वीर") || normalized.contains("फ़ोटो")))

        // Check for YouTube
        val isYoutube = normalized.contains("youtube") || normalized.contains("video") || normalized.contains("song") || normalized.contains("music") ||
            (lang == "hindi" && (normalized.contains("gaana") || normalized.contains("गीत") || normalized.contains("वीडियो"))) ||
            (lang == "dogri" && (normalized.contains("gaana") || normalized.contains("गीत") || normalized.contains("वीडियो")))

        // Check for WhatsApp
        val isWhatsapp = normalized.contains("whatsapp") || normalized.contains("message") || normalized.contains("chat") ||
            (lang == "hindi" && (normalized.contains("sandesh") || normalized.contains("संदेश"))) ||
            (lang == "dogri" && (normalized.contains("sandesh") || normalized.contains("सुनेहा")))

        // Check for Flashlight
        val isFlashlight = normalized.contains("torch") || normalized.contains("flashlight") || normalized.contains("light") ||
            (lang == "hindi" && (normalized.contains("ujala") || normalized.contains("रोशनी") || normalized.contains("टॉर्च"))) ||
            (lang == "dogri" && (normalized.contains("chanan") || normalized.contains("टॉर्च")))

        // Check for Time
        val isTime = normalized.contains("time") || normalized.contains("what time") ||
            (lang == "hindi" && (normalized.contains("samay") || normalized.contains("समय") || normalized.contains("बजे"))) ||
            (lang == "dogri" && (normalized.contains("vela") || normalized.contains("बजे") || normalized.contains("वक्त")))

        // Check for Volume
        val isVolume = normalized.contains("volume") || normalized.contains("louder") || normalized.contains("quieter") ||
            (lang == "hindi" && (normalized.contains("aawaz") || normalized.contains("आवाज़"))) ||
            (lang == "dogri" && (normalized.contains("aawaz") || normalized.contains("आवाज़")))

        // Check for Contacts
        val isContacts = normalized.contains("contact") || normalized.contains("phone book") ||
            (lang == "hindi" && (normalized.contains("phone number") || normalized.contains("नंबर"))) ||
            (lang == "dogri" && (normalized.contains("नंबर")))

        return when {
            isCallSon -> {
                val msg = when(lang) {
                    "hindi" -> "मैं आपके बेटे को फोन कर रहा हूँ।"
                    "dogri" -> "मैं तुंदे पुत्तर गी फोन करदा पियां।"
                    else -> "Calling your son now."
                }
                AiResponse(msg, "CALL_SON", true)
            }
            isCallDaughter -> {
                val msg = when(lang) {
                    "hindi" -> "मैं आपकी बेटी को फोन कर रहा हूँ।"
                    "dogri" -> "मैं तुंदी धी गी फोन करदा पियां।"
                    else -> "Calling your daughter now."
                }
                AiResponse(msg, "CALL_DAUGHTER", true)
            }
            isCallHome -> {
                val msg = when(lang) {
                    "hindi" -> "मैं घर पर फोन कर रहा हूँ।"
                    "dogri" -> "मैं घरे फोन करदा पियां।"
                    else -> "Calling home now."
                }
                AiResponse(msg, "CALL_HOME", true)
            }
            isHelp -> {
                val msg = when(lang) {
                    "hindi" -> "मदद बुलाई जा रही है। आपके केयरटेकर से संपर्क किया जा रहा है।"
                    "dogri" -> "मदद सद्दी जा करदी ऐ। तुंदे केयरटेकर कने संपर्क कीता जा करदा ऐ।"
                    else -> "Getting help for you immediately. Alerting caretaker."
                }
                AiResponse(msg, "CALL_HELP", true)
            }
            isGallery -> {
                val msg = when(lang) {
                    "hindi" -> "आपकी फ़ोटो गैलरी खोली जा रही है।"
                    "dogri" -> "तुंदी फ़ोटो गैलरी खोली जा करदी ऐ।"
                    else -> "Opening your photo gallery."
                }
                AiResponse(msg, "OPEN_GALLERY", false)
            }
            isYoutube -> {
                val msg = when(lang) {
                    "hindi" -> "यूट्यूब खोला जा रहा है।"
                    "dogri" -> "यूट्यूब खोला जा करदा ऐ।"
                    else -> "Opening YouTube."
                }
                AiResponse(msg, "OPEN_YOUTUBE", false)
            }
            isWhatsapp -> {
                val msg = when(lang) {
                    "hindi" -> "व्हाट्सएप खोला जा रहा है।"
                    "dogri" -> "व्हाट्सएप खोला जा करदा ऐ।"
                    else -> "Opening WhatsApp."
                }
                AiResponse(msg, "OPEN_WHATSAPP", false)
            }
            isFlashlight -> {
                val msg = when(lang) {
                    "hindi" -> "टॉर्च चालू की जा रही है।"
                    "dogri" -> "टॉर्च चालू कीती जा करदी ऐ।"
                    else -> "Toggling the flashlight."
                }
                AiResponse(msg, "TOGGLE_FLASHLIGHT", false)
            }
            isTime -> {
                val msg = when(lang) {
                    "hindi" -> "समय बताया जा रहा है।"
                    "dogri" -> "वक्त दस्या जा करदा ऐ।"
                    else -> "Telling you the time."
                }
                AiResponse(msg, "SPEAK_TIME", false)
            }
            isVolume -> {
                val msg = when(lang) {
                    "hindi" -> "आवाज़ व्यवस्थित की जा रही है।"
                    "dogri" -> "आवाज़ ठीक कीती जा करदी ऐ।"
                    else -> "Adjusting volume."
                }
                AiResponse(msg, "ADJUST_VOLUME", false)
            }
            isContacts -> {
                val msg = when(lang) {
                    "hindi" -> "संपर्क सूची खोली जा रही है।"
                    "dogri" -> "संपर्क सूची खोली जा करदी ऐ।"
                    else -> "Opening your contacts list."
                }
                AiResponse(msg, "OPEN_CONTACTS", false)
            }
            normalized.contains("how") || normalized.contains("what can you do") || 
            (lang == "hindi" && (normalized.contains("का काम") || normalized.contains("क्या कर"))) -> {
                val msg = when(lang) {
                    "hindi" -> "मैं परिवार को फोन करने, फोटो या वीडियो खोलने, टॉर्च जलाने और समय बताने में मदद कर सकता हूँ।"
                    "dogri" -> "मैं परिवार गी फोन करने, फोटो या वीडियो खोलने, टॉर्च जगाने ते वक्त दस्सन च मदद करी सकदा हां।"
                    else -> "I can help you call family, open photos, watch videos, turn on flashlight, tell time, and more."
                }
                AiResponse(msg, null, false)
            }
            else -> {
                val msg = when(lang) {
                    "hindi" -> "मुझे समझ नहीं आया: $query । कृपया स्पष्ट बोलें।"
                    "dogri" -> "गी समझ नेईं आया: $query । कृपया साफ बोलो।"
                    else -> "I understand you want: $query. Please speak more clearly or use the buttons."
                }
                AiResponse(msg, null, false)
            }
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
