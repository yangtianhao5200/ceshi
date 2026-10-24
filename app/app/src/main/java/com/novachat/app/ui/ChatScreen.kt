package com.novachat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novachat.app.data.ChatMessage
import com.novachat.app.ui.components.CircleIconButton
import com.novachat.app.ui.components.Composer
import com.novachat.app.ui.components.MessageRow
import com.novachat.app.ui.components.OrbAvatar
import com.novachat.app.ui.components.StatusDot
import com.novachat.app.ui.theme.NovaStroke
import com.novachat.app.ui.theme.SuccessGreen
import com.novachat.app.ui.theme.TextPrimary
import com.novachat.app.ui.theme.TextSecondary

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    modelName: String,
    streaming: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onCopy: (String) -> Unit,
    onRegenerate: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val lastMessage = messages.lastOrNull()

    LaunchedEffect(messages.size, lastMessage?.content?.length) {
        val target = listState.layoutInfo.totalItemsCount - 1
        if (target >= 0) listState.animateScrollToItem(target)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        ChatTopBar(
            modelName = modelName,
            onOpenSettings = onOpenSettings,
            onNewChat = onNewChat
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "date") { DateChip() }
            items(messages, key = { it.id }) { message ->
                val isLastAssistant = message.isAssistant && message.id == lastMessage?.id
                MessageRow(
                    message = message,
                    streaming = streaming && isLastAssistant,
                    showActions = isLastAssistant && !streaming && message.error == null && message.content.isNotBlank(),
                    onCopy = { onCopy(message.content) },
                    onRegenerate = onRegenerate
                )
            }
            item(key = "tail") { Spacer(Modifier.height(4.dp)) }
        }

        Composer(
            value = draft,
            onValueChange = onDraftChange,
            streaming = streaming,
            onSend = onSend,
            onStop = onStop,
            chips = listOf("提炼要点", "换个说法", "继续深入"),
            onChipClick = onDraftChange
        )
    }
}

@Composable
private fun ChatTopBar(
    modelName: String,
    onOpenSettings: () -> Unit,
    onNewChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xCC0B0A16))
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OrbAvatar(size = 38.dp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "星语 AI",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(color = SuccessGreen)
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "在线 · $modelName",
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    maxLines = 1
                )
            }
        }
        Text(
            text = "新对话",
            color = TextSecondary,
            fontSize = 11.5.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onNewChat)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
        CircleIconButton(
            icon = Icons.Filled.Settings,
            contentDescription = "设置",
            onClick = onOpenSettings
        )
    }
}

@Composable
private fun DateChip() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF262340))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "本次对话",
                color = TextSecondary,
                fontSize = 10.5.sp,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
