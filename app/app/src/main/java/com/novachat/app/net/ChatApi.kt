package com.novachat.app.net

import com.novachat.app.data.ChatMessage
import com.novachat.app.data.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface StreamEvent {
    data class Delta(val text: String) : StreamEvent
    data class Failure(val message: String, val code: Int? = null) : StreamEvent
    data object Done : StreamEvent
}

data class TestResult(val ok: Boolean, val message: String, val latencyMs: Long = 0L)

/** Pure helpers kept free of Android APIs so they can be unit tested on the JVM. */
object SseParser {
    private const val PREFIX = "data:"

    fun isDone(line: String): Boolean = line.trim().replace(" ", "") == "data:[DONE]"

    /** Returns the incremental text carried by one SSE line, or null when the line carries none. */
    fun contentOf(line: String): String? {
        val trimmed = line.trim()
        if (!trimmed.startsWith(PREFIX)) return null
        val payload = trimmed.removePrefix(PREFIX).trim()
        if (payload.isEmpty() || payload == "[DONE]") return null
        val choices = JSONObject(payload).optJSONArray("choices") ?: return null
        if (choices.length() == 0) return null
        val delta = choices.optJSONObject(0)?.optJSONObject("delta") ?: return null
        val text = delta.optString("content")
        return text.ifEmpty { null }
    }
}

object ChatPayloads {
    /** Builds an OpenAI-compatible Chat Completions request body. */
    fun build(
        settings: Settings,
        history: List<ChatMessage>,
        stream: Boolean,
        maxTokens: Int? = null
    ): String {
        val messages = JSONArray()
        if (settings.systemPrompt.isNotBlank()) {
            messages.put(JSONObject().put("role", ChatMessage.ROLE_SYSTEM).put("content", settings.systemPrompt))
        }
        history.forEach { message ->
            if (message.content.isNotBlank() && message.error == null) {
                messages.put(JSONObject().put("role", message.role).put("content", message.content))
            }
        }
        val body = JSONObject()
            .put("model", settings.model)
            .put("messages", messages)
            .put("stream", stream)
            .put("temperature", settings.temperature.toDouble())
        if (maxTokens != null) body.put("max_tokens", maxTokens)
        return body.toString()
    }

    fun errorMessage(body: String, code: Int): String {
        val fromJson = runCatching {
            JSONObject(body).optJSONObject("error")?.optString("message")
        }.getOrNull()
        return when {
            !fromJson.isNullOrBlank() -> fromJson
            body.isNotBlank() -> body.take(180)
            else -> when (code) {
                401 -> "API Key 无效或已过期"
                403 -> "没有访问权限"
                404 -> "接口地址或模型不存在"
                429 -> "请求过于频繁，请稍后再试"
                in 500..599 -> "服务端错误"
                else -> "请求失败"
            }
        }
    }
}

class ChatApi(
    private val client: OkHttpClient = defaultClient()
) {

    /** Streams a chat completion, emitting incremental deltas as they arrive. */
    fun stream(settings: Settings, history: List<ChatMessage>): Flow<StreamEvent> = flow {
        if (!settings.isConfigured) {
            emit(StreamEvent.Failure("请先在设置中填写 API Key、地址与模型"))
            return@flow
        }
        val call = try {
            client.newCall(request(settings, ChatPayloads.build(settings, history, stream = true), true))
        } catch (error: Exception) {
            emit(StreamEvent.Failure("接口地址无效：${error.message ?: settings.baseUrl}"))
            return@flow
        }
        try {
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    emit(StreamEvent.Failure(ChatPayloads.errorMessage(response.body?.string().orEmpty(), response.code), response.code))
                    return@use
                }
                val source = response.body?.source()
                if (source == null) {
                    emit(StreamEvent.Failure("响应为空"))
                    return@use
                }
                var finished = false
                while (!finished && currentCoroutineContext().isActive) {
                    val line = source.readUtf8Line() ?: break
                    if (SseParser.isDone(line)) {
                        finished = true
                        break
                    }
                    SseParser.contentOf(line)?.let { emit(StreamEvent.Delta(it)) }
                }
                emit(StreamEvent.Done)
            }
        } catch (error: IOException) {
            emit(StreamEvent.Failure(error.message ?: "网络连接失败"))
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    /** Sends a 1-token completion to validate the key, endpoint and model. */
    suspend fun testConnection(settings: Settings): TestResult = withContext(Dispatchers.IO) {
        if (!settings.isConfigured) {
            return@withContext TestResult(false, "请先填写 API Key、接口地址和模型名称")
        }
        val started = System.currentTimeMillis()
        try {
            val payload = ChatPayloads.build(
                settings,
                listOf(ChatMessage.user("ping")),
                stream = false,
                maxTokens = 1
            )
            client.newCall(request(settings, payload, acceptSse = false)).execute().use { response ->
                val elapsed = System.currentTimeMillis() - started
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val modelName = runCatching {
                        JSONObject(body).optString("model")
                    }.getOrNull().orEmpty().ifBlank { settings.model }
                    TestResult(true, "连接正常 · $modelName · ${elapsed} ms", elapsed)
                } else {
                    TestResult(false, ChatPayloads.errorMessage(body, response.code), elapsed)
                }
            }
        } catch (error: Exception) {
            TestResult(false, error.message ?: "无法连接服务器", System.currentTimeMillis() - started)
        }
    }

    private fun request(settings: Settings, payload: String, acceptSse: Boolean): Request {
        val builder = Request.Builder()
            .url(settings.chatEndpoint)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .header("Authorization", "Bearer ${settings.apiKey.trim()}")
            .header("Content-Type", "application/json")
        if (acceptSse) builder.header("Accept", "text/event-stream")
        return builder.build()
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
