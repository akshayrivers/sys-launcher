package com.example.syslauncher

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LocalAiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_ai)

        val prefs = AppPrefs(this)
        val input = findViewById<EditText>(R.id.etAiInput)
        val output = findViewById<TextView>(R.id.tvAiOutput)
        val btnRun = findViewById<Button>(R.id.btnAiRun)

        btnRun.setOnClickListener {
            if (!prefs.localModelInstalled()) {
                Toast.makeText(this, getString(R.string.local_ai_missing), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val query = input.text.toString().trim().lowercase(Locale.getDefault())
            output.text = runLocalAssistant(query)
        }
    }

    private fun runLocalAssistant(query: String): String {
        if (query.isBlank()) return "Say something simple, e.g. call son or open gallery."
        return when {
            query.contains("call son") -> "Action suggestion: Tap Son photo on home."
            query.contains("call daughter") -> "Action suggestion: Tap Daughter photo on home."
            query.contains("help") -> "Action suggestion: Tap Help photo for caretaker session."
            query.contains("gallery") -> "Action suggestion: Tap Gallery section button."
            query.contains("youtube") -> "Action suggestion: Tap YouTube section button."
            query.contains("volume") -> "Action suggestion: Use Volume + / Volume - buttons."
            query.contains("time") -> "Action suggestion: Tap Speak Time button."
            else -> "I am local AI. I support simple phone actions only."
        }
    }
}
