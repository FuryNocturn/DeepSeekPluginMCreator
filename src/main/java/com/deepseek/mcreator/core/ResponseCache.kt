package com.deepseek.mcreator.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class CacheEntry(
    val response: String,
    val timestamp: String = LocalDateTime.now().toString()
) {
    fun isExpired(hours: Long = 24): Boolean {
        return ChronoUnit.HOURS.between(
            LocalDateTime.parse(timestamp),
            LocalDateTime.now()
        ) > hours
    }
}

object ResponseCache {
    private val cacheFile: Path = Paths.get("deepseek_cache.json")
    private val json = Json { prettyPrint = true }
    private val memoryCache = ConcurrentHashMap<String, CacheEntry>()

    init {
        loadCacheFromDisk()
    }

    fun getResponse(promptHash: String): String? {
        return memoryCache[promptHash]?.takeIf { !it.isExpired() }?.response
    }

    fun cacheResponse(prompt: String, response: String) {
        val entry = CacheEntry(response)
        memoryCache[prompt.hashCode().toString()] = entry
        saveCacheToDisk()
    }

    private fun loadCacheFromDisk() {
        if (Files.exists(cacheFile)) {
            try {
                val cached = json.decodeFromString<Map<String, CacheEntry>>(
                    Files.readString(cacheFile)
                            memoryCache.putAll(cached)
            } catch (e: Exception) {
                ErrorReporter.reportError(e)
            }
        }
    }

    private fun saveCacheToDisk() {
        try {
            Files.writeString(
                cacheFile,
                json.encodeToString(memoryCache)
            )
        } catch (e: Exception) {
            ErrorReporter.reportError(e)
        }
    }
}