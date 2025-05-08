/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.logging

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val LOG_DIRECTORY = "logs"

class FileLogger(
    private val context: Context,
    private val prefix: String? = null,
    override val loggingTag: String,
    override val logToCrashlytics: () -> Boolean,
) : AbstractLogger() {
    private val logFile: File get() = getLogFile(context)

    private fun logToFile(level: String, messages: Collection<String>) {
        val timestamp = logEntryDateFormat.format(Date())
        val logMessages = messages.joinToString(separator = System.lineSeparator()) { message ->
            "$timestamp [$level] ${prefix?.let { "$it: " } ?: ""}$message"
        }

        try {
            FileWriter(logFile, true).use { writer ->
                writer.appendLine(logMessages)
            }
        } catch (e: IOException) {
            logcatLogger.warning("Failed to write log to file", e)
        }
    }

    override fun verboseImpl(messages: Collection<String>) {
        logToFile("VERBOSE", messages)
    }

    override fun debugImpl(messages: Collection<String>) {
        logToFile("DEBUG", messages)
    }

    override fun infoImpl(messages: Collection<String>) {
        logToFile("INFO", messages)
    }

    override fun warningImpl(messages: Collection<String>) {
        logToFile("WARNING", messages)
    }

    override fun errorImpl(messages: Collection<String>) {
        logToFile("ERROR", messages)
    }

    companion object {
        private val fileNameDateFormat: SimpleDateFormat by lazy {
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        }

        private val logEntryDateFormat: SimpleDateFormat by lazy {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        }

        private val thirtyDaysAgo: Date get() = Calendar.getInstance()
            .apply { add(Calendar.DAY_OF_YEAR, -30) }.time

        private fun getLogDirectory(context: Context): File =
            File(context.filesDir, LOG_DIRECTORY).also { directory ->
                if (!directory.exists()) {
                    directory.mkdirs()
                }
            }

        private fun getLogFile(context: Context): File {
            val date = fileNameDateFormat.format(Date())
            return File(getLogDirectory(context), "logs_$date.txt").also { file ->
                if (!file.exists()) {
                    file.createNewFile()
                }
            }
        }

        fun tryCleanOldLogFilesAsync(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val logDir = File(context.filesDir, LOG_DIRECTORY)
                    if (logDir.exists() && logDir.isDirectory) {
                        cleanLogFilesDirectory(logDir)
                    }
                } catch (e: Exception) {
                    logcatLogger.warning("Failed to clean old log files", e)
                }
            }
        }

        private fun cleanLogFilesDirectory(logDirectory: File) {
            val allLogFiles = logDirectory.listFiles()

            if (allLogFiles == null || allLogFiles.size < 10) {
                return // no need to clean up
            }

            allLogFiles.forEach { file ->
                try {
                    if (file.isFile && file.lastModified() < thirtyDaysAgo.time) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    logcatLogger.warning("Failed to delete log file '${file.name}'", e)
                }
            }
        }

        fun exportLogFile(context: Context, fileUri: Uri) {
            val logFile = getLogFile(context)

            if (logFile.exists()) {
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    logFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } else {
                throw FileNotFoundException("Log file for today does not exist.")
            }
        }
    }
}
