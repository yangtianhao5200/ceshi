package com.novachat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novachat.app.ui.components.CircleIconButton
import com.novachat.app.ui.components.Composer
import com.novachat.app.ui.components.OrbAvatar
import com.novachat.app.ui.theme.NovaPrimary
import com.novachat.app.ui.theme.NovaStroke
import com.novachat.app.ui.theme.NovaSurface
import com.novachat.app.ui.theme.TextPrimary
import com.novachat.app.ui.theme.TextSecondary
import com.novachat.app.ui.theme.TextTertiary

private data class Suggestion(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val prompt: String
)

private val suggestions = listOf(
    Suggestion(Icons.Filled.MailOutline, "帮我写一封邮件", "正式、简洁，面向客户", "帮我写一封面向客户的商务邮件，语气正式简洁。"),
    Suggestion(Icons.Filled.Code, "解释这段代码", "逐步拆解核心逻辑", "请逐步解释下面这段代码的核心逻辑：\n"),
    Suggestion(Icons.Filled.FitnessCenter, "制定健身计划", "每周 4 次，居家训练", "帮我制定一份每周 4 次的居家健身计划。"),
    Suggestion(Icons.Filled.Translate, "翻译并润色", "中英互译，语气自然", "把下面的内容翻译成地道的英文并润色：\n")
)

@Composable
fun WelcomeScreen(
    configured: Boolean,
    draft: String,
    modelName: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 12.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "星语 AI",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = "设置",
                onClick = onOpenSettings
            )
        }

        Spacer(Modifier.height(44.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OrbAvatar(size = 110.dp, sparkle = true)
            Spacer(Modifier.height(26.dp))
            Text(
                text = "今天想聊点什么？",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = "随时提问、写作、翻译或一起头脑风暴",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(30.dp))

        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            suggestions.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            modifier = Modifier.weight(1f),
                            onClick = { onSuggestion(suggestion.prompt) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (!configured) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF231F42))
                    .border(1.dp, NovaStroke, RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    tint = NovaPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("尚未配置 API Key", color = TextPrimary, fontSize = 12.5.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
                    Text("点击填写地址、密钥与模型", color = TextTertiary, fontSize = 10.5.sp, lineHeight = 14.sp)
                }
                Text("去配置", color = Color(0xFFA78BFA), fontSize = 11.5.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Composer(
            value = draft,
            onValueChange = onDraftChange,
            streaming = false,
            onSend = onSend,
            onStop = {},
            modifier = Modifier.padding(top = 6.dp),
            chips = if (configured) listOf("总结要点", "翻译成英文", "写得更正式") else emptyList(),
            onChipClick = onSuggestion
        )
    }
}

@Composable
private fun SuggestionCard(
    suggestion: Suggestion,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NovaSurface)
            .border(1.dp, NovaStroke, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = suggestion.icon,
            contentDescription = null,
            tint = Color(0xFFB0A4F5),
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.height(9.dp))
        Text(
            text = suggestion.title,
            color = TextPrimary,
            fontSize = 12.5.sp, lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = suggestion.subtitle,
            color = TextTertiary,
            fontSize = 10.sp, lineHeight = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
