package com.example.syslauncher

import android.content.Context

class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("senior_launcher_prefs", Context.MODE_PRIVATE)

    fun isProvisioned(): Boolean = prefs.getBoolean(KEY_PROVISIONED, false)

    fun setProvisioned(value: Boolean) {
        prefs.edit().putBoolean(KEY_PROVISIONED, value).apply()
    }

    fun saveProvisioning(
        caretakerEmail: String,
        helperAccountsCsv: String,
        sonNumber: String,
        daughterNumber: String,
        homeNumber: String,
        helpNumber: String,
        language: String,
        pluginId: String,
        caretakerPin: String
    ) {
        prefs.edit()
            .putString(KEY_CARETAKER_EMAIL, caretakerEmail)
            .putString(KEY_HELPERS, helperAccountsCsv)
            .putString(KEY_SON_NUMBER, sonNumber)
            .putString(KEY_DAUGHTER_NUMBER, daughterNumber)
            .putString(KEY_HOME_NUMBER, homeNumber)
            .putString(KEY_HELP_NUMBER, helpNumber)
            .putString(KEY_LANGUAGE, language)
            .putString(KEY_PLUGIN, pluginId)
            .putString(KEY_CARETAKER_PIN, caretakerPin)
            .putBoolean(KEY_PROVISIONED, true)
            .apply()
    }

    fun sonNumber(): String = prefs.getString(KEY_SON_NUMBER, "").orEmpty()
    fun daughterNumber(): String = prefs.getString(KEY_DAUGHTER_NUMBER, "").orEmpty()
    fun homeNumber(): String = prefs.getString(KEY_HOME_NUMBER, "").orEmpty()
    fun helpNumber(): String = prefs.getString(KEY_HELP_NUMBER, "").orEmpty()

    fun helperAccounts(): List<String> {
        val csv = prefs.getString(KEY_HELPERS, "").orEmpty()
        if (csv.isBlank()) return emptyList()
        return csv.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    fun language(): String = prefs.getString(KEY_LANGUAGE, "Hindi").orEmpty()
    fun plugin(): String = prefs.getString(KEY_PLUGIN, "builtin.keyword.v1").orEmpty()
    fun caretakerEmail(): String = prefs.getString(KEY_CARETAKER_EMAIL, "").orEmpty()
    fun helpersCsv(): String = prefs.getString(KEY_HELPERS, "").orEmpty()
    fun caretakerPin(): String = prefs.getString(KEY_CARETAKER_PIN, "1234").orEmpty()
    fun localModelInstalled(): Boolean = prefs.getBoolean(KEY_LOCAL_MODEL_INSTALLED, false)
    fun localModelName(): String = prefs.getString(KEY_LOCAL_MODEL_NAME, "tiny-assist-v1").orEmpty()

    fun setLocalModelInstalled(installed: Boolean, modelName: String = "tiny-assist-v1") {
        prefs.edit()
            .putBoolean(KEY_LOCAL_MODEL_INSTALLED, installed)
            .putString(KEY_LOCAL_MODEL_NAME, modelName)
            .apply()
    }

    fun updateCaretakerConfig(
        caretakerEmail: String,
        helpersCsv: String,
        sonNumber: String,
        daughterNumber: String,
        homeNumber: String,
        helpNumber: String,
        language: String,
        pluginId: String,
        caretakerPin: String
    ) {
        saveProvisioning(
            caretakerEmail = caretakerEmail,
            helperAccountsCsv = helpersCsv,
            sonNumber = sonNumber,
            daughterNumber = daughterNumber,
            homeNumber = homeNumber,
            helpNumber = helpNumber,
            language = language,
            pluginId = pluginId,
            caretakerPin = caretakerPin
        )
    }

    fun setHomeSlotContact(slot: Int, contactId: String) {
        prefs.edit().putString(slotKey(slot), contactId).apply()
    }

    fun homeSlotContact(slot: Int): String {
        return prefs.getString(slotKey(slot), "").orEmpty()
    }

    private fun slotKey(slot: Int): String = "home_slot_$slot"

    companion object {
        private const val KEY_PROVISIONED = "provisioned"
        private const val KEY_CARETAKER_EMAIL = "caretaker_email"
        private const val KEY_HELPERS = "helpers"
        private const val KEY_SON_NUMBER = "son_number"
        private const val KEY_DAUGHTER_NUMBER = "daughter_number"
        private const val KEY_HOME_NUMBER = "home_number"
        private const val KEY_HELP_NUMBER = "help_number"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_PLUGIN = "plugin"
        private const val KEY_CARETAKER_PIN = "caretaker_pin"
        private const val KEY_LOCAL_MODEL_INSTALLED = "local_model_installed"
        private const val KEY_LOCAL_MODEL_NAME = "local_model_name"
    }
}
