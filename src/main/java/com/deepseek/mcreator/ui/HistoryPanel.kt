package com.deepseek.mcreator.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class HistoryEntry(
    val timestamp: String,
    val prompt: String,
    val response: String,
    val tokensUsed: Int
)

object HistoryManager {
    private val historyFile: Path = Path.of("deepseek_history.json")
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val json = Json { prettyPrint = true }

    fun addEntry(prompt: String, response: String, tokens: Int) {
        val entries = loadHistory().toMutableList()
        entries.add(HistoryEntry(
            timestamp = LocalDateTime.now().format(formatter),
            prompt = prompt,
            response = response,
            tokensUsed = tokens
        ))

        Files.writeString(historyFile, json.encodeToString(entries))
    }

    fun loadHistory(): List<HistoryEntry> {
        return if (Files.exists(historyFile)) {
            json.decodeFromString(Files.readString(historyFile))
        } else {
            emptyList()
        }
    }
}

object CostCalculator {
    private const val COST_PER_TOKEN = 0.000002 // Ajustar según tarifas actuales

    fun calculateCost(tokens: Int): Double = tokens * COST_PER_TOKEN

    fun getTotalSpent(): Double {
        return HistoryManager.loadHistory()
            .sumOf { calculateCost(it.tokensUsed) }
    }
}