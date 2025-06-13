package com.deepseek.mcreator.core

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ErrorReporter {
    private val logFile: Path = Paths.get("deepseek_error_log.txt")
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun reportError(error: Throwable) {
        val errorEntry = buildString {
            appendLine("=== Error Report ===")
            appendLine("Timestamp: ${LocalDateTime.now().format(formatter)}")
            appendLine("Type: ${error.javaClass.simpleName}")
            appendLine("Message: ${error.message}")
            appendLine("Stack Trace:")
            error.stackTrace.forEach { appendLine("  at $it") }
            appendLine()
        }

        Files.writeString(
            logFile,
            errorEntry,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )
    }

    fun getStats(): String {
        return if (Files.exists(logFile)) {
            val lines = Files.readAllLines(logFile)
            """
                Total errors: ${lines.count { it.startsWith("===") }}
                Last error: ${lines.takeLast(10).find { it.startsWith("Timestamp:") }?.substring(11)}
            """.trimIndent()
        } else {
            "No hay errores registrados"
        }
    }

    fun getRecentErrors(count: Int): List<String> {
        return if (Files.exists(logFile)) {
            Files.readAllLines(logFile)
                .filter { it.startsWith("Timestamp:") }
                .takeLast(count)
        } else {
            emptyList()
        }
    }
}