package com.deepseek.mcreator.core

interface DeepSeekExtension {
    fun beforeSend(prompt: String): String
    fun afterReceive(response: String): String
}

object ExtensionManager {
    private val extensions = mutableListOf<DeepSeekExtension>()

    fun registerExtension(extension: DeepSeekExtension) {
        extensions.add(extension)
    }

    fun processPrompt(prompt: String): String {
        return extensions.fold(prompt) { acc, ext -> ext.beforeSend(acc) }
    }

    fun processResponse(response: String): String {
        return extensions.fold(response) { acc, ext -> ext.afterReceive(acc) }
    }

    // Extensiones incorporadas
    init {
        registerExtension(object : DeepSeekExtension {
            override fun beforeSend(prompt: String) = prompt
            override fun afterReceive(response: String) =
                response.replace("```java", """<span style="color: #569CD6">```java""")
                    .replace("```", """```</span>""")
        })
    }
}