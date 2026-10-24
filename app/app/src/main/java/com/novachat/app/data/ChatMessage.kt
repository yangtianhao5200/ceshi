package com.novachat.app.data

import java.util.UUID

/** A single transcript entry. `role` is either [ROLE_USER] or [ROLE_ASSISTANT]. */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isUser: Boolean get() = role == ROLE_USER
    val isAssistant: Boolean get() = role == ROLE_ASSISTANT

    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        const val ROLE_SYSTEM = "system"

        fun user(text: String) = ChatMessage(role = ROLE_USER, content = text)
        fun assistant(text: String = "") = ChatMessage(role = ROLE_ASSISTANT, content = text)
    }
}
