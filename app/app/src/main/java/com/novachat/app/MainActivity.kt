package com.novachat.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novachat.app.ui.ChatScreen
import com.novachat.app.ui.SettingsScreen
import com.novachat.app.ui.WelcomeScreen
import com.novachat.app.ui.theme.NovaBackground
import com.novachat.app.ui.theme.NovaChatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            NovaChatTheme {
                NovaBackground {
                    NovaApp()
                }
            }
        }
    }
}

@Composable
private fun NovaApp(viewModel: ChatViewModel = viewModel()) {
    val state = viewModel.state
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    val copyToClipboard: (String) -> Unit = { text ->
        clipboard.setText(AnnotatedString(text))
        viewModel.onCopied()
    }

    when (state.screen) {
        Screen.Welcome -> WelcomeScreen(
            configured = state.settings.isConfigured,
            draft = state.draft,
            modelName = state.settings.model,
            onDraftChange = viewModel::updateDraft,
            onSend = { viewModel.sendPrompt() },
            onSuggestion = viewModel::fillDraft,
            onOpenSettings = viewModel::openSettings
        )

        Screen.Chat -> ChatScreen(
            messages = state.messages,
            modelName = state.settings.model,
            streaming = state.streaming,
            draft = state.draft,
            onDraftChange = viewModel::updateDraft,
            onSend = { viewModel.sendPrompt() },
            onStop = viewModel::stopStreaming,
            onCopy = copyToClipboard,
            onRegenerate = viewModel::regenerate,
            onOpenSettings = viewModel::openSettings,
            onNewChat = viewModel::newChat
        )

        Screen.Settings -> SettingsScreen(
            draft = state.draftSettings ?: state.settings,
            onChange = viewModel::updateDraftSettings,
            test = state.test,
            encrypted = state.encrypted,
            onTest = viewModel::testConnection,
            onSave = viewModel::saveSettings,
            onBack = viewModel::closeSettings,
            onClearKey = viewModel::clearApiKey
        )
    }

    BackHandler(enabled = state.screen == Screen.Settings) {
        viewModel.closeSettings()
    }

    LaunchedEffect(state.toast) {
        state.toast?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }
}
