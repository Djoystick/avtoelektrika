package com.example.autoelectricai.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val TAG = "AppLogger"
    private const val MAX_FILE_SIZE = 2 * 1024 * 1024 // 2 MB

    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun init(context: Context) {
        try {
            val logsDir = File(context.cacheDir, "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            logFile = File(logsDir, "app_logs.txt")
            if (logFile?.exists() == false) {
                logFile?.createNewFile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init log file", e)
        }
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        writeToFile("DEBUG", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val stackTrace = throwable?.let { Log.getStackTraceString(it) } ?: ""
        val fullMessage = if (stackTrace.isNotEmpty()) "$message\n$stackTrace" else message
        writeToFile("ERROR", tag, fullMessage)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        writeToFile("WARN", tag, message)
    }
    
    fun i(tag: String, message: String) {
        Log.i(tag, message)
        writeToFile("INFO", tag, message)
    }

    private fun writeToFile(level: String, tag: String, message: String) {
        val file = logFile ?: return
        try {
            if (file.exists() && file.length() > MAX_FILE_SIZE) {
                // Clear file if too big
                file.writeText("")
            }
            val timestamp = dateFormat.format(Date())
            val logLine = "[$timestamp] $level/$tag: $message\n"
            val writer = PrintWriter(FileWriter(file, true))
            writer.append(logLine)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log", e)
        }
    }

    fun getLogFile(): File? {
        return logFile
    }
}
