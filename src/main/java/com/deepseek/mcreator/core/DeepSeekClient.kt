package com.deepseek.mcreator.core

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class DeepSeekClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    var selectedModel = "deepseek-chat"
    var temperature = 0.7
    var maxTokens = 2000

    data class ApiResponse(
        val choices: List<Choice>,
        val usage: Usage
    )

    data class Choice(val message: Message)
    data class Message(val content: String)
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )

    fun sendPrompt(prompt: String, context: String = ""): Triple<String, Int, Int> {
        val apiKey = ApiKeyManager.getApiKey() ?: throw IllegalStateException("API Key no configurada")

        val processedPrompt = ExtensionManager.processPrompt(
            buildString {
                append("Eres un experto en desarrollo de mods para Minecraft con MCreator. ")
                append("Contexto actual:\n$context\n\n")
                append("Pregunta: $prompt")
            }
        )

        val requestBody = gson.toJson(mapOf(
            "model" to selectedModel,
            "messages" to listOf(mapOf(
                "role" to "user",
                "content" to processedPrompt
            )),
            "temperature" to temperature,
            "max_tokens" to maxTokens
        ))

        val cachedResponse = ResponseCache.getResponse(processedPrompt)
        if (cachedResponse != null) {
            return Triple(cachedResponse, 0, 0)
        }

        val request = Request.Builder()
            .url("https://api.deepseek.com/v1/chat/completions")
            .post(requestBody.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Error en la API: ${response.code} - ${response.message}")
            }

            val body = response.body?.string() ?: throw IOException("Respuesta vacía")
            val apiResponse = gson.fromJson(body, ApiResponse::class.java)
            val processedResponse = ExtensionManager.processResponse(apiResponse.choices[0].message.content)

            ResponseCache.cacheResponse(processedPrompt, processedResponse)

            Triple(
                processedResponse,
                apiResponse.usage.prompt_tokens,
                apiResponse.usage.completion_tokens
            )
        } catch (e: Exception) {
            ErrorReporter.reportError(e)
            throw IOException("Error al comunicarse con la API: ${e.message}")
        }
    }

    fun analyzeCode(code: String): Pair<String, Int> {
        val prompt = """
            Analiza este código de MCreator y sugiere mejoras:
            ```
            $code
            ```
            
            Proporciona:
            1. Problemas encontrados
            2. Sugerencias de optimización
            3. Mejoras de estilo
            4. Posibles bugs
            5. Recomendaciones de documentación
        """.trimIndent()

        val (response, inputTokens, outputTokens) = sendPrompt(prompt)
        CostCalculator.recordOperation("Code Analysis", inputTokens, outputTokens)
        return Pair(response, inputTokens + outputTokens)
    }

    fun analyzeTexture(imageFile: File): String {
        return try {
            val imageData = Base64.getEncoder().encodeToString(imageFile.readBytes())
            val prompt = """
                Analiza esta textura para Minecraft:
                - Formato: ${imageFile.extension}
                - Tamaño: ${imageFile.length()} bytes
                - Datos base64: [imagen]
                
                Proporciona recomendaciones para:
                1. Optimización
                2. Paleta de colores
                3. Consistencia de estilo
            """.trimIndent()

            val (response, _, _) = sendPrompt(prompt)
            response
        } catch (e: Exception) {
            ErrorReporter.reportError(e)
            "Error al analizar textura: ${e.message}"
        }
    }
}