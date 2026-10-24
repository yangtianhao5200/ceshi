package com.novachat.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novachat.app.data.ChatMessage
import com.novachat.app.data.ChatStore
import com.novachat.app.data.Settings
import com.novachat.app.data.SettingsRepository
import com.novachat.app.net.ChatApi
import com.novachat.app.net.StreamEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class Screen { Welcome, Chat, Settings }

sealed interface TestState {
    data object Idle : TestState
    data object Running : TestState
    data class Ok(val message: String) : TestState
    data class Failed(val message: String) : TestState
}

data class UiState(
    val screen: Screen = Screen.Welcome,
    val settings: Settings = Settings(),
    val draftSettings: Settings? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val streaming: Boolean = false,
    val test: TestState = TestState.Idle,
    val encrypted: Boolean = true,
    val toast: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val store = ChatStore(application)
    private val api = ChatApi()
    private var streamJob: Job? = null

    var state by mutableStateOf(
        UiState(
            settings = repository.load(),
            messages = store.load(),
            encrypted = repository.isEncrypted
        )
    )
        private set

    init {
        state = state.copy(screen = if (state.messages.isEmpty()) Screen.Welcome else Screen.Chat)
    }

    fun updateDraft(text: String) {
        state = state.copy(draft = text)
    }

    fun fillDraft(text: String) {
        state = state.copy(draft = text, screen = Screen.Welcome)
    }

    fun consumeToast() {
        state = state.copy(toast = null)
    }

    fun onCopied() {
        state = state.copy(toast = "已复制到剪贴板")
    }

    fun openSettings() {
        state = state.copy(
            screen = Screen.Settings,
            draftSettings = state.settings,
            test = TestState.Idle,
            toast = null
        )
    }

    fun closeSettings() {
        state = state.copy(
            screen = if (state.messages.isEmpty()) Screen.Welcome else Screen.Chat,
            draftSettings = null
        )
    }

    fun updateDraftSettings(settings: Settings) {
        state = state.copy(draftSettings = settings, test = TestState.Idle)
    }

    fun saveSettings() {
        val draft = state.draftSettings ?: return
        repository.save(draft)
        state = state.copy(
            settings = draft,
            draftSettings = null,
            screen = if (state.messages.isEmpty()) Screen.Welcome else Screen.Chat,
            toast = "设置已保存"
        )
    }

    fun clearApiKey() {
        repository.clearApiKey()
        state = state.copy(
            settings = state.settings.copy(apiKey = ""),
            draftSettings = (state.draftSettings ?: state.settings).copy(apiKey = ""),
            test = TestState.Idle,
            toast = "已清除 API Key"
        )
    }

    fun testConnection() {
        if (state.test is TestState.Running) return
        val target = state.draftSettings ?: state.settings
        if (target.baseUrl.isBlank() || target.apiKey.isBlank() || target.model.isBlank()) {
            state = state.copy(test = TestState.Failed("请先填写接口地址、API Key 和模型名称"))
            return
        }
        state = state.copy(test = TestState.Running)
        viewModelScope.launch {
            val result = api.testConnection(target)
            state = state.copy(
                test = if (result.ok) TestState.Ok(result.message) else TestState.Failed(result.message)
            )
        }
    }

    fun sendPrompt(text: String? = null) {
        val prompt = (text ?: state.draft).trim()
        if (prompt.isEmpty() || state.streaming) return
        if (!state.settings.isConfigured) {
            state = state.copy(
                screen = Screen.Settings,
                draftSettings = state.settings,
                toast = "请先配置 API Key"
            )
            return
        }
        val userMessage = ChatMessage.user(prompt)
        val assistant = ChatMessage.assistant()
        val history = state.messages + userMessage
        state = state.copy(
            messages = history + assistant,
            draft = "",
            streaming = true,
            screen = Screen.Chat
        )
        startStream(history, assistant.id)
    }

    fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        val messages = state.messages.toMutableList()
        val last = messages.lastOrNull()
        if (last != null && last.isAssistant && last.content.isBlank()) {
            messages.removeAt(messages.lastIndex)
        }
        state = state.copy(messages = messages, streaming = false)
        store.save(state.messages)
    }

    fun regenerate() {
        if (state.streaming) return
        val lastAssistantIndex = state.messages.indexOfLast { it.isAssistant }
        if (lastAssistantIndex < 0) return
        val history = state.messages.take(lastAssistantIndex).filter { it.error == null }
        if (history.none { it.isUser }) return
        val assistant = ChatMessage.assistant()
        state = state.copy(messages = history + assistant, streaming = true)
        startStream(history, assistant.id)
    }

    fun newChat() {
        streamJob?.cancel()
        streamJob = null
        store.clear()
        state = state.copy(messages = emptyList(), draft = "", streaming = false, screen = Screen.Welcome)
    }

    private fun startStream(history: List<ChatMessage>, assistantId: String) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            api.stream(state.settings, history).collect { event ->
                when (event) {
                    is StreamEvent.Delta -> appendDelta(assistantId, event.text)
                    is StreamEvent.Failure -> failStream(assistantId, event.message)
                    StreamEvent.Done -> completeStream(assistantId)
                }
            }
        }
    }

    private fun appendDelta(assistantId: String, delta: String) {
        state = state.copy(
            messages = state.messages.map { message ->
                if (message.id == assistantId) message.copy(content = message.content + delta) else message
            }
        )
    }

    private fun completeStream(assistantId: String) {
        val streamed = state.messages.firstOrNull { it.id == assistantId }
        val messages = if (streamed != null && streamed.content.isBlank()) {
            state.messages.map {
                if (it.id == assistantId) it.copy(error = "模型没有返回内容，请检查模型名称或稍后重试") else it
            }
        } else {
            state.messages
        }
        state = state.copy(messages = messages, streaming = false)
        streamJob = null
        store.save(state.messages)
    }

    private fun failStream(assistantId: String, message: String) {
        state = state.copy(
            messages = state.messages.map { item ->
                when {
                    item.id != assistantId -> item
                    item.content.isBlank() -> item.copy(error = message)
                    else -> item.copy(content = item.content + "\n\n[生成中断：$message]")
                }
            },
            streaming = false
        )
        streamJob = null
        store.save(state.messages)
    }
}
