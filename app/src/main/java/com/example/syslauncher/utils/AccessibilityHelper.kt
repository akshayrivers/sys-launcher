package com.example.syslauncher.utils

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageButton

/**
 * Helper class to manage accessibility features for senior citizens and users with reading difficulties.
 */
class AccessibilityHelper(private val context: Context) {
    
    /**
     * Apply accessibility settings to a button for better touch feedback.
     */
    fun setupAccessibleButton(button: Button, contentDescription: String) {
        button.contentDescription = contentDescription
        button.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
    
    /**
     * Apply accessibility settings to an image button.
     */
    fun setupAccessibleImageButton(button: ImageButton, contentDescription: String) {
        button.contentDescription = contentDescription
        button.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
    
    /**
     * Apply accessibility-friendly vibration feedback.
     */
    fun provideTactileFeedback(view: View) {
        // Provide haptic feedback for touch-sensitive users
        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
    }
    
    /**
     * Set minimum touch target size (recommended 48dp for elderly users)
     */
    fun getMinimumTouchSize(): Int = 48 // dp, converts to pixels at runtime
}
