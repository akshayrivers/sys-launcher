package com.example.syslauncher.utils

import android.content.Context
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import com.example.syslauncher.R

/**
 * Provides access to icons and drawables for senior-friendly UI.
 * Uses system icons or custom drawables with clear, large imagery.
 */
class IconProvider(private val context: Context) {
    
    fun getCallIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_call)
    
    fun getContactsIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_myplaces)
    
    fun getGalleryIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_gallery)
    
    fun getVideoIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_media_play)
    
    fun getChatIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_email)
    
    fun getFlashlightIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_search)
    
    fun getTimeIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_info)
    
    fun getVolumeIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_media_play)
    
    fun getAiIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_btn_speak_now)
    
    fun getSettingsIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_manage)
    
    fun getHelpIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_info)
    
    fun getPhoneIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_call)
    
    fun getBackIcon(): Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_revert)
}
