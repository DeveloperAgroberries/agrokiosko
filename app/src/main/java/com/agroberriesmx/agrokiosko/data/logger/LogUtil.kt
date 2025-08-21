package com.agroberriesmx.agrokiosko.data.logger

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import okio.IOException
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class LogUtil (private val context: Context) {
    private val logFilename = "app_logs.txt"

    // Crea el archivo de log si no existe
    private fun ensureLogFileExists(): File {
        val logFile = File(context.filesDir, logFilename)
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        return logFile
    }

    // Función común para escribir logs
    private fun writeLog(message: String) {
        try {
            val logFile = ensureLogFileExists()
            BufferedWriter(OutputStreamWriter(FileOutputStream(logFile, true))).use { writer ->
                writer.write(message)
                writer.newLine()
            }
        } catch (e: IOException) {
            // Mejor manejo de la excepción, quizás logueando en una ubicación de fallback.
            e.printStackTrace()
        }
    }

    fun logMessage(message: String) {
        val logMessage = "${System.currentTimeMillis()} - $message"
        writeLog(logMessage)
    }

    fun logErrorMessage(error: String) {
        val logMessage = "ERROR: ${System.currentTimeMillis()} - $error"
        writeLog(logMessage)
    }

    fun getLogFile(): File {
        return File(context.filesDir, logFilename)
    }

    fun readLogFile(): String {
        val logFile = getLogFile()
        return if (logFile.exists()) {
            logFile.readText()
        } else {
            "No hay logs disponibles"
        }
    }

    fun shareLogFile(): String {
        val logFile = getLogFile()

        if (logFile.exists()) {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                logFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Compartir logs").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            return "Logs compartidos."
        } else {
            return "El archivo de logs no existe."
        }
    }
}