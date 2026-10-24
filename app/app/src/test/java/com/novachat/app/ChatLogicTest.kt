package com.novachat.app

import com.novachat.app.data.ChatMessage
import com.novachat.app.data.Provider
import com.novachat.app.data.Settings
import com.novachat.app.net.ChatPayloads
import com.novachat.app.net.SseParser
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatLogicTest {

    private val settings = Settings(
        providerId = Provider.DEEPSEEK.id,
        baseUrl = "https://api.deepseek.com/v1",
        apiKey = "sk-test-1234567890",
        model = "deepseek-chat",
        systemPrompt = "你是助手",
        temperature = 0.7f
    )

    @Test
    fun `endpoint is normalised for base and full urls`() {
        assertEquals("https://api.deepseek.com/v1/chat/completions", settings.chatEndpoint)
        assertEquals(
            "https://api.deepseek.com/v1/chat/completions",
            settings.copy(baseUrl = "https://api.deepseek.com/v1/").chatEndpoint
        )
        assertEquals(
            "https://api.openai.com/v1/chat/completions",
            settings.copy(baseUrl = "https://api.openai.com/v1/chat/completions").chatEndpoint
        )
    }

    @Test
    fun `configured requires key url and model`() {
        assertTrue(settings.isConfigured)
        assertFalse(settings.copy(apiKey = "  ").isConfigured)
        assertFalse(settings.copy(baseUrl = "").isConfigured)
        assertFalse(settings.copy(model = "").isConfigured)
    }

    @Test
    fun `masked key keeps only last four characters`() {
        assertEquals("••••••••••••7890", settings.maskedKey)
        assertEquals("", settings.copy(apiKey = "").maskedKey)
    }

    @Test
    fun `payload contains system prompt and skips empty or failed messages`() {
        val history = listOf(
            ChatMessage.user("你好"),
            ChatMessage.assistant(""),
            ChatMessage(role = ChatMessage.ROLE_ASSISTANT, content = "错了", error = "boom")
        )
        val json = JSONObject(ChatPayloads.build(settings, history, stream = true))
        assertEquals("deepseek-chat", json.getString("model"))
        assertTrue(json.getBoolean("stream"))
        assertEquals(0.7, json.getDouble("temperature"), 0.0001)
        val messages = json.getJSONArray("messages")
        assertEquals(2, messages.length())
        assertEquals("system", messages.getJSONObject(0).getString("role"))
        assertEquals("你是助手", messages.getJSONObject(0).getString("content"))
        assertEquals("user", messages.getJSONObject(1).getString("role"))
        assertEquals("你好", messages.getJSONObject(1).getString("content"))
    }

    @Test
    fun `test payload caps tokens and disables streaming`() {
        val history = listOf(ChatMessage.user("ping"))
        val json = JSONObject(ChatPayloads.build(settings, history, stream = false, maxTokens = 1))
        assertFalse(json.getBoolean("stream"))
        assertEquals(1, json.getInt("max_tokens"))
    }

    @Test
    fun `error message prefers provider detail then falls back to status code`() {
        assertEquals(
            "Incorrect API key provided",
            ChatPayloads.errorMessage("{\"error\":{\"message\":\"Incorrect API key provided\"}}", 401)
        )
        assertEquals("API Key 无效或已过期", ChatPayloads.errorMessage("", 401))
        assertEquals("接口地址或模型不存在", ChatPayloads.errorMessage("", 404))
        assertEquals("请求过于频繁，请稍后再试", ChatPayloads.errorMessage("", 429))
        assertEquals("服务端错误", ChatPayloads.errorMessage("", 503))
    }

    @Test
    fun `sse parser extracts incremental content`() {
        val line = "data: {\"choices\":[{\"delta\":{\"content\":\"你好\"}}]}"
        assertEquals("你好", SseParser.contentOf(line))
        assertNull(SseParser.contentOf("data: {\"choices\":[{\"delta\":{}}]}"))
        assertNull(SseParser.contentOf("data: [DONE]"))
        assertNull(SseParser.contentOf(": keep-alive"))
        assertNull(SseParser.contentOf(""))
        assertTrue(SseParser.isDone("data: [DONE]"))
        assertTrue(SseParser.isDone("data:[DONE]"))
        assertFalse(SseParser.isDone(line))
    }
}
