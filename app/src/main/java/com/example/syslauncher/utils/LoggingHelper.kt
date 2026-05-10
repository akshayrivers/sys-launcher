package com.example.syslauncher.utils

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized logging utility for debugging and monitoring.
 */
object LoggingHelper {
    private const val TAG = "SysLauncher"
    private var logFile: File? = null
    
    fun setLogFile(file: File) {
        logFile = file
    }
    
    fun info(message: String, tag: String = TAG) {
        Log.i(tag, message)
        writeToFile("INFO: $message")
    }
    
    fun error(message: String, exception: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, exception)
        writeToFile("ERROR: $message${if (exception != null) " - ${exception.message}" else ""}")
    }
    
    fun debug(message: String, tag: String = TAG) {
        Log.d(tag, message)
        writeToFile("DEBUG: $message")
    }
    
    private fun writeToFile(message: String) {
        try {
            logFile?.appendText("${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())} - $message\n")
        } catch (_: Exception) {
            // Silently fail if file write is not possible
        }
    }
}
