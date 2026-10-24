package com.novachat.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.novachat.app.data.ChatMessage
import com.novachat.app.data.Settings
import com.novachat.app.ui.ChatScreen
import com.novachat.app.ui.SettingsScreen
import com.novachat.app.ui.WelcomeScreen
import com.novachat.app.ui.theme.NovaBackground
import com.novachat.app.ui.theme.NovaChatTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val outDir = File("/workspace/dist/screens").apply { mkdirs() }

    private fun capture(name: String, content: @Composable () -> Unit) {
        rule.setContent {
            NovaChatTheme {
                NovaBackground { content() }
            }
        }
        rule.waitForIdle()
        val view: View = rule.activity.window.decorView
        val width = if (view.width > 0) view.width else 1170
        val height = if (view.height > 0) view.height else 2532
        if (view.width == 0 || view.height == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, width, height)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        FileOutputStream(File(outDir, "$name.png")).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    @Test
    fun welcome() {
        capture("welcome") {
            WelcomeScreen(
                configured = true,
                draft = "",
                modelName = "deepseek-chat",
                onDraftChange = {},
                onSend = {},
                onSuggestion = {},
                onOpenSettings = {}
            )
        }
    }

    @Test
    fun chat() {
        val messages = listOf(
            ChatMessage.user("帮我把这段产品介绍改写得更有吸引力，控制在 100 字以内。"),
            ChatMessage.assistant("改写后的版本：\n\n让每一次灵感都有回应，轻触即达的智能对话体验。")
        )
        capture("chat") {
            ChatScreen(
                messages = messages,
                modelName = "deepseek-chat",
                streaming = false,
                draft = "",
                onDraftChange = {},
                onSend = {},
                onStop = {},
                onCopy = {},
                onRegenerate = {},
                onOpenSettings = {},
                onNewChat = {}
            )
        }
    }

    @Test
    fun settings() {
        capture("settings") {
            SettingsScreen(
                draft = Settings(
                    providerId = "deepseek",
                    baseUrl = "https://api.deepseek.com/v1",
                    apiKey = "sk-abcdef1234567890",
                    model = "deepseek-chat",
                    systemPrompt = "你是一个乐于助人的中文 AI 助手。",
                    temperature = 0.7f
                ),
                onChange = {},
                test = TestState.Ok("deepseek-chat · 延迟 312 ms"),
                encrypted = true,
                onTest = {},
                onSave = {},
                onBack = {},
                onClearKey = {}
            )
        }
    }
}
