package com.novachat.app.data

/** Provider presets shown as chips on the settings screen. */
enum class Provider(val id: String, val label: String, val baseUrl: String, val model: String) {
    OPENAI("openai", "OpenAI", "https://api.openai.com/v1", "gpt-4o-mini"),
    DEEPSEEK("deepseek", "DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
    CUSTOM("custom", "自定义", "", "");

    companion object {
        fun fromId(id: String): Provider = entries.firstOrNull { it.id == id } ?: CUSTOM
    }
}

/**
 * User configuration for an OpenAI-compatible chat completion endpoint.
 */
data class Settings(
    val providerId: String = Provider.DEEPSEEK.id,
    val baseUrl: String = Provider.DEEPSEEK.baseUrl,
    val apiKey: String = "",
    val model: String = Provider.DEEPSEEK.model,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val temperature: Float = 0.7f
) {
    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank()

    /** Accepts both `https://host/v1` and a full `.../chat/completions` URL. */
    val chatEndpoint: String
        get() {
            val trimmed = baseUrl.trim().trimEnd('/')
            val suffix = "/chat/completions"
            val base = if (trimmed.endsWith(suffix)) trimmed.dropLast(suffix.length) else trimmed
            return base + suffix
        }

    val host: String
        get() = baseUrl.trim()
            .removePrefix("https://").removePrefix("http://")
            .trimEnd('/')
            .ifBlank { "未配置" }

    val maskedKey: String
        get() = when {
            apiKey.isBlank() -> ""
            apiKey.length <= 6 -> "••••••"
            else -> "••••••••••••" + apiKey.takeLast(4)
        }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = "你是一个乐于助人的中文 AI 助手。"
    }
}
