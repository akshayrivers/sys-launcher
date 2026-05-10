package com.example.syslauncher.utils

import android.content.Context

/**
 * Manages theme preferences for better visibility and contrast.
 */
class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_TEXT_SIZE_MULTIPLIER = "text_size_multiplier"
        const val THEME_HIGH_CONTRAST = "high_contrast"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
    }
    
    fun getTheme(): String = prefs.getString(KEY_THEME_MODE, THEME_DARK) ?: THEME_DARK
    
    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME_MODE, theme).apply()
    }
    
    fun getTextSizeMultiplier(): Float = prefs.getFloat(KEY_TEXT_SIZE_MULTIPLIER, 1.0f)
    
    fun setTextSizeMultiplier(multiplier: Float) {
        prefs.edit().putFloat(KEY_TEXT_SIZE_MULTIPLIER, multiplier).apply()
    }
}
