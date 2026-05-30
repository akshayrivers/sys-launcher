package com.example.syslauncher

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syslauncher.services.VoiceCueManager
import java.util.Locale

class LocalAiActivity : AppCompatActivity() {

    private lateinit var voiceCueManager: VoiceCueManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_ai)

        voiceCueManager = VoiceCueManager(this)

        val prefs = AppPrefs(this)
        val input = findViewById<EditText>(R.id.etAiInput)
        val output = findViewById<TextView>(R.id.tvAiOutput)
        val btnRun = findViewById<Button>(R.id.btnAiRun)

        voiceCueManager.speak("Ask me anything about using this phone.")

        btnRun.setOnClickListener {
            if (!prefs.localModelInstalled()) {
                voiceCueManager.speak(getString(R.string.local_ai_missing))
                Toast.makeText(this, getString(R.string.local_ai_missing), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val query = input.text.toString().trim().lowercase(Locale.getDefault())
            val response = runLocalAssistant(query)
            output.text = response
            voiceCueManager.speak(response)
        }
    }

    private fun runLocalAssistant(query: String): String {
        if (query.isBlank()) return "Say something simple, like call son or open photos."
        return when {
            query.contains("call son") -> "To call your son, tap his photo on the home screen."
            query.contains("call daughter") -> "To call your daughter, tap her photo on the home screen."
            query.contains("help") -> "To get help, tap the red help button on the home screen."
            query.contains("gallery") || query.contains("photo") -> "To see photos, tap the gallery button."
            query.contains("youtube") || query.contains("video") -> "To watch videos, tap the YouTube button."
            query.contains("volume") -> "To change volume, use the louder and quieter buttons."
            query.contains("time") -> "To hear the time, tap the time button."
            else -> "I can help with simple phone actions. Try asking about calling family or opening photos."
        }
    }

    override fun onDestroy() {
        voiceCueManager.destroy()
        super.onDestroy()
    }
}
