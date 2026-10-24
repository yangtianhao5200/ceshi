package com.novachat.app.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Small JSON-file backed transcript store (single conversation). */
class ChatStore(context: Context) {

    private val file = File(context.applicationContext.filesDir, "chat_history.json")

    fun load(): List<ChatMessage> = runCatching {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            ChatMessage(
                id = item.optString("id"),
                role = item.optString("role", ChatMessage.ROLE_ASSISTANT),
                content = item.optString("content"),
                createdAt = item.optLong("createdAt")
            )
        }
    }.getOrElse { error ->
        Log.w(TAG, "Unable to read transcript", error)
        emptyList()
    }

    fun save(messages: List<ChatMessage>) {
        runCatching {
            val array = JSONArray()
            messages.filter { it.content.isNotBlank() && it.error == null }.forEach { message ->
                array.put(
                    JSONObject()
                        .put("id", message.id)
                        .put("role", message.role)
                        .put("content", message.content)
                        .put("createdAt", message.createdAt)
                )
            }
            file.writeText(array.toString())
        }.onFailure { Log.w(TAG, "Unable to persist transcript", it) }
    }

    fun clear() {
        runCatching { if (file.exists()) file.delete() }
    }

    private companion object {
        const val TAG = "ChatStore"
    }
}
