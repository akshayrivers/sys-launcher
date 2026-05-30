package com.example.syslauncher

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syslauncher.services.VoiceCueManager

class ProvisioningActivity : AppCompatActivity() {

    private lateinit var voiceCueManager: VoiceCueManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provisioning)

        voiceCueManager = VoiceCueManager(this)
        voiceCueManager.speak("Welcome. Please set up the launcher for your loved one.")

        val etCaretaker = findViewById<EditText>(R.id.etCaretakerEmail)
        val etHelpers = findViewById<EditText>(R.id.etHelpers)
        val etSon = findViewById<EditText>(R.id.etSonNumber)
        val etDaughter = findViewById<EditText>(R.id.etDaughterNumber)
        val etHome = findViewById<EditText>(R.id.etHomeNumber)
        val etHelp = findViewById<EditText>(R.id.etHelpNumber)
        val etPin = findViewById<EditText>(R.id.etCaretakerPin)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val spinnerPlugin = findViewById<Spinner>(R.id.spinnerPlugin)
        val btnComplete = findViewById<Button>(R.id.btnCompleteSetup)

        val languageOptions = listOf("Hindi", "Dogri", "English")
        spinnerLanguage.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languageOptions
        )

        val pluginOptions = listOf(
            "builtin.keyword.v1 (offline, 10MB)",
            "builtin.keyword.hi.v1 (offline, 12MB)"
        )
        spinnerPlugin.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            pluginOptions
        )

        btnComplete.setOnClickListener {
            val caretaker = etCaretaker.text.toString().trim()
            val helpers = etHelpers.text.toString().trim()
            val son = etSon.text.toString().trim()
            val daughter = etDaughter.text.toString().trim()
            val home = etHome.text.toString().trim()
            val help = etHelp.text.toString().trim()
            val pin = etPin.text.toString().trim()
            val language = spinnerLanguage.selectedItem.toString()
            val pluginId = spinnerPlugin.selectedItem.toString().substringBefore(" ")

            if (caretaker.isBlank() || son.isBlank() || daughter.isBlank() || home.isBlank() || help.isBlank()) {
                voiceCueManager.speak("Please fill in all required phone numbers.")
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pin.length < 4) {
                voiceCueManager.speak("The PIN must be at least four digits.")
                Toast.makeText(this, "Caretaker PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AppPrefs(this).saveProvisioning(
                caretakerEmail = caretaker,
                helperAccountsCsv = helpers,
                sonNumber = son,
                daughterNumber = daughter,
                homeNumber = home,
                helpNumber = help,
                language = language,
                pluginId = pluginId,
                caretakerPin = pin
            )
            ContactStore(this).seedDefaultsIfEmpty(son, daughter, home, help)

            voiceCueManager.sayProvisioningComplete()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        voiceCueManager.destroy()
        super.onDestroy()
    }
}
