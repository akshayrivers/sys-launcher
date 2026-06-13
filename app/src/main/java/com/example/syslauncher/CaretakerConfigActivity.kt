package com.example.syslauncher

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syslauncher.services.VoiceCueManager

class CaretakerConfigActivity : AppCompatActivity() {

    private lateinit var voiceCueManager: VoiceCueManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caretaker_config)

        voiceCueManager = VoiceCueManager(this)

        val prefs = AppPrefs(this)
        val etCaretaker = findViewById<EditText>(R.id.etCfgCaretakerEmail)
        val etHelpers = findViewById<EditText>(R.id.etCfgHelpers)
        val etSon = findViewById<EditText>(R.id.etCfgSonNumber)
        val etDaughter = findViewById<EditText>(R.id.etCfgDaughterNumber)
        val etHome = findViewById<EditText>(R.id.etCfgHomeNumber)
        val etHelp = findViewById<EditText>(R.id.etCfgHelpNumber)
        val etLanguage = findViewById<EditText>(R.id.etCfgLanguage)
        val etPlugin = findViewById<EditText>(R.id.etCfgPlugin)
        val etPin = findViewById<EditText>(R.id.etCfgPin)
        val btnInstallModel = findViewById<Button>(R.id.btnInstallLocalAi)
        val btnSave = findViewById<Button>(R.id.btnSaveConfig)

        etCaretaker.setText(prefs.caretakerEmail())
        etHelpers.setText(prefs.helpersCsv())
        etSon.setText(prefs.sonNumber())
        etDaughter.setText(prefs.daughterNumber())
        etHome.setText(prefs.homeNumber())
        etHelp.setText(prefs.helpNumber())
        etLanguage.setText(prefs.language())
        etPlugin.setText(prefs.plugin())
        etPin.setText(prefs.caretakerPin())

        btnInstallModel.setOnClickListener {
            prefs.setLocalModelInstalled(true, "tiny-assist-v1")
            voiceCueManager.speak(getString(R.string.local_ai_installed))
            Toast.makeText(this, getString(R.string.local_ai_installed), Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            val pin = etPin.text.toString().trim()
            if (pin.length < 4) {
                voiceCueManager.speak("PIN must be at least 4 digits")
                Toast.makeText(this, "PIN too short", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.updateCaretakerConfig(
                caretakerEmail = etCaretaker.text.toString().trim(),
                helpersCsv = etHelpers.text.toString().trim(),
                sonNumber = etSon.text.toString().trim(),
                daughterNumber = etDaughter.text.toString().trim(),
                homeNumber = etHome.text.toString().trim(),
                helpNumber = etHelp.text.toString().trim(),
                language = etLanguage.text.toString().trim(),
                pluginId = etPlugin.text.toString().trim(),
                caretakerPin = pin
            )
            voiceCueManager.speak("Settings saved successfully.")
            Toast.makeText(this, "Config saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        voiceCueManager.destroy()
        super.onDestroy()
    }
}
