package com.novachat.app

import com.novachat.app.data.ChatMessage
import com.novachat.app.data.Settings
import com.novachat.app.net.ChatApi
import com.novachat.app.net.StreamEvent
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Exercises the real OkHttp client + SSE parsing against a local mock server. */
class ApiIntegrationTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun settings(): Settings = Settings(
        providerId = "custom",
        baseUrl = server.url("/v1").toString(),
        apiKey = "sk-test-key",
        model = "deepseek-chat"
    )

    @Test
    fun `streams deltas until done and sends openai payload`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/event-stream")
                .setBody(
                    "data: {\"choices\":[{\"delta\":{\"content\":\"你好\"}}]}\n\n" +
                        "data: {\"choices\":[{\"delta\":{\"content\":\"，世界\"}}]}\n\n" +
                        "data: [DONE]\n\n"
                )
        )

        val events = ChatApi().stream(settings(), listOf(ChatMessage.user("hi"))).toList()

        assertEquals(
            listOf("你好", "，世界"),
            events.filterIsInstance<StreamEvent.Delta>().map { it.text }
        )
        assertTrue(events.last() is StreamEvent.Done)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("Bearer sk-test-key", recorded.getHeader("Authorization"))
        assertTrue(recorded.path!!.endsWith("/chat/completions"))
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"stream\":true"))
        assertTrue(body.contains("\"model\":\"deepseek-chat\""))
        assertTrue(body.contains("\"role\":\"user\""))
    }

    @Test
    fun `surfaces provider error message and status code`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\":{\"message\":\"Invalid API key provided\"}}")
        )

        val events = ChatApi().stream(settings(), listOf(ChatMessage.user("hi"))).toList()

        val failure = events.filterIsInstance<StreamEvent.Failure>().single()
        assertEquals("Invalid API key provided", failure.message)
        assertEquals(401, failure.code)
    }

    @Test
    fun `missing key fails fast without network call`() = runBlocking {
        val events = ChatApi()
            .stream(settings().copy(apiKey = ""), listOf(ChatMessage.user("hi")))
            .toList()

        assertTrue(events.single() is StreamEvent.Failure)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `test connection reports model and latency`() = runBlocking {
        server.enqueue(MockResponse().setBody("{\"model\":\"deepseek-chat\",\"choices\":[]}"))

        val result = ChatApi().testConnection(settings())

        assertTrue(result.message, result.ok)
        assertTrue(result.message.contains("deepseek-chat"))
        val recorded = server.takeRequest()
        assertTrue(recorded.body.readUtf8().contains("\"max_tokens\":1"))
    }

    @Test
    fun `test connection maps http failure`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))

        val result = ChatApi().testConnection(settings())

        assertTrue(!result.ok)
        assertTrue(result.message.contains("not found"))
    }
}
