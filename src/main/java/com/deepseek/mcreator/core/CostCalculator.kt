package com.deepseek.mcreator.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class CostRecord(
    val timestamp: String,
    val operation: String,
    val tokensUsed: Int,
    val estimatedCost: Double
)

object CostCalculator {
    // Tarifas actuales de DeepSeek (ajustar según cambios)
    private const val INPUT_COST_PER_TOKEN = 0.0000025 // $0.0000025 por token de entrada
    private const val OUTPUT_COST_PER_TOKEN = 0.0000035 // $0.0000035 por token de salida

    private val costHistoryFile: Path = Paths.get("deepseek_cost_history.json")
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val json = Json { prettyPrint = true }

    /**
     * Calcula el costo estimado de una operación
     * @param inputTokens Tokens usados en el prompt
     * @param outputTokens Tokens generados en la respuesta
     * @return Costo estimado en dólares
     */
    fun calculateCost(inputTokens: Int, outputTokens: Int): Double {
        return (inputTokens * INPUT_COST_PER_TOKEN) + (outputTokens * OUTPUT_COST_PER_TOKEN)
    }

    /**
     * Registra una nueva operación en el historial de costos
     */
    fun recordOperation(operation: String, inputTokens: Int, outputTokens: Int) {
        val cost = calculateCost(inputTokens, outputTokens)
        val newRecord = CostRecord(
            timestamp = LocalDateTime.now().format(formatter),
            operation = operation,
            tokensUsed = inputTokens + outputTokens,
            estimatedCost = cost
        )

        val currentHistory = loadHistory()
        val updatedHistory = currentHistory + newRecord

        Files.writeString(costHistoryFile, json.encodeToString(updatedHistory))
    }

    /**
     * Obtiene el historial completo de costos
     */
    fun loadHistory(): List<CostRecord> {
        return if (Files.exists(costHistoryFile)) {
            try {
                json.decodeFromString<List<CostRecord>>(Files.readString(costHistoryFile))
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Calcula el costo total acumulado
     */
    fun getTotalCost(): Double {
        return loadHistory().sumOf { it.estimatedCost }
    }

    /**
     * Obtiene el resumen de uso por periodo
     */
    fun getUsageSummary(days: Int = 30): Map<String, Double> {
        val cutoff = LocalDateTime.now().minusDays(days.toLong())
        return loadHistory()
            .filter { record ->
                LocalDateTime.parse(record.timestamp, formatter).isAfter(cutoff)
            }
            .groupBy { it.timestamp.substring(0, 10) } // Agrupar por fecha
            .mapValues { (_, records) -> records.sumOf { it.estimatedCost } }
    }

    /**
     * Formatea el costo para mostrar en UI
     */
    fun formatCost(cost: Double): String {
        return "$${"%.4f".format(cost)} USD"
    }
}