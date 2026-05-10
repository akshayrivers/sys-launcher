package com.example.syslauncher.utils

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

/**
 * Helper class to manage accessibility features for senior citizens and users with reading difficulties.
 * Focuses on visual cues, haptic feedback, and large touch targets.
 */
class AccessibilityHelper(private val context: Context) {
    
    /**
     * Apply accessibility settings to a button for better touch feedback.
     */
    fun setupAccessibleButton(button: Button, contentDescription: String) {
        button.contentDescription = contentDescription
        button.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        button.setOnClickListener {
            provideTactileFeedback(button)
        }
    }
    
    /**
     * Apply accessibility settings to an image button.
     */
    fun setupAccessibleImageButton(button: ImageButton, contentDescription: String) {
        button.contentDescription = contentDescription
        button.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        button.setOnClickListener {
            provideTactileFeedback(button)
        }
    }
    
    /**
     * Setup an image view for accessibility
     */
    fun setupAccessibleImageView(imageView: ImageView, contentDescription: String) {
        imageView.contentDescription = contentDescription
        imageView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
    
    /**
     * Setup text view for accessibility
     */
    fun setupAccessibleTextView(textView: TextView, importance: Int = View.IMPORTANT_FOR_ACCESSIBILITY_YES) {
        textView.importantForAccessibility = importance
    }
    
    /**
     * Apply accessibility-friendly vibration feedback.
     */
    fun provideTactileFeedback(view: View) {
        // Provide haptic feedback for touch-sensitive users
        view.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
    
    /**
     * Set minimum touch target size (recommended 48dp for elderly users)
     */
    fun getMinimumTouchSize(): Int = 48 // dp, converts to pixels at runtime
    
    /**
     * Get text size for body text (18sp+ for elderly users)
     */
    fun getBodyTextSize(): Float = 18f
    
    /**
     * Get text size for labels (16sp+ for elderly users)
     */
    fun getLabelTextSize(): Float = 16f
    
    /**
     * Get text size for headings (24sp+ for elderly users)
     */
    fun getHeadingTextSize(): Float = 24f
}
