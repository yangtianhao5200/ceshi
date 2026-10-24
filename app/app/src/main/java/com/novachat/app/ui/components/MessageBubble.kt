package com.novachat.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novachat.app.data.ChatMessage
import com.novachat.app.ui.theme.ErrorRed
import com.novachat.app.ui.theme.NovaPrimary
import com.novachat.app.ui.theme.NovaStroke
import com.novachat.app.ui.theme.NovaSurface
import com.novachat.app.ui.theme.TextPrimary
import com.novachat.app.ui.theme.TextSecondary
import com.novachat.app.ui.theme.UserBubbleEnd
import com.novachat.app.ui.theme.UserBubbleStart

@Composable
fun MessageRow(
    message: ChatMessage,
    streaming: Boolean,
    showActions: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.isUser) {
        UserBubble(text = message.content, modifier = modifier)
    } else {
        AssistantBubble(
            message = message,
            streaming = streaming,
            showActions = showActions,
            onCopy = onCopy,
            onRegenerate = onRegenerate,
            modifier = modifier
        )
    }
}

@Composable
private fun UserBubble(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(UserBubbleStart, UserBubbleEnd)))
                .padding(horizontal = 15.dp, vertical = 12.dp)
        ) {
            SelectionContainer {
                Text(text = text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun AssistantBubble(
    message: ChatMessage,
    streaming: Boolean,
    showActions: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        OrbAvatar(size = 26.dp)
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f, fill = true)) {
            Text(
                text = "星语 AI",
                color = TextSecondary,
                fontSize = 11.sp, lineHeight = 15.sp,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.size(6.dp))
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(NovaSurface)
                    .border(1.dp, NovaStroke.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 15.dp, vertical = 13.dp)
            ) {
                when {
                    message.error != null -> ErrorContent(message.error)
                    message.content.isEmpty() && streaming -> TypingDots()
                    else -> Row(verticalAlignment = Alignment.Bottom) {
                        SelectionContainer(Modifier.weight(1f, fill = false)) {
                            Text(
                                text = message.content,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        if (streaming) {
                            BlinkingCursor()
                        }
                    }
                }
            }
            if (showActions && !streaming) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionPill(text = "复制", onClick = onCopy)
                    ActionPill(text = "重新生成", onClick = onRegenerate)
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = message,
            color = ErrorRed,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ActionPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF262340))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TypingDots() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 620, delayMillis = index * 160),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                Modifier
                    .padding(end = 5.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(TextSecondary.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun BlinkingCursor() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )
    Text(
        text = "\u258E",
        color = NovaPrimary.copy(alpha = alpha),
        fontSize = 15.sp, lineHeight = 21.sp,
        modifier = Modifier.padding(start = 2.dp)
    )
}
