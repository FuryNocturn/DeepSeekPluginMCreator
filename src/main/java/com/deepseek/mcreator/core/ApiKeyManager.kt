package com.deepseek.mcreator.core

import org.jasypt.util.text.BasicTextEncryptor
import java.nio.file.Files
import java.nio.file.Paths
import java.util.prefs.Preferences

object ApiKeyManager {
    private const val ENCRYPTION_PASSWORD = "your-strong-password" // Cambiar en producción
    private val prefs = Preferences.userNodeForPackage(ApiKeyManager::class.java)
    private val encryptor = BasicTextEncryptor().apply {
        setPassword(ENCRYPTION_PASSWORD)
    }

    fun saveApiKey(key: String) {
        val encrypted = encryptor.encrypt(key)
        prefs.put("api_key", encrypted)
    }

    fun getApiKey(): String? {
        val encrypted = prefs.get("api_key", null) ?: return null
        return try {
            encryptor.decrypt(encrypted)
        } catch (e: Exception) {
            null
        }
    }

    fun hasApiKey(): Boolean = getApiKey() != null
}