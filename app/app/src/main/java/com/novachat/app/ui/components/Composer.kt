package com.novachat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novachat.app.ui.theme.NovaBlue
import com.novachat.app.ui.theme.NovaPrimary
import com.novachat.app.ui.theme.NovaStroke
import com.novachat.app.ui.theme.NovaSurface
import com.novachat.app.ui.theme.TextPrimary
import com.novachat.app.ui.theme.TextSecondary
import com.novachat.app.ui.theme.TextTertiary

@Composable
fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    streaming: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    chips: List<String> = emptyList(),
    onChipClick: (String) -> Unit = {},
    onAttach: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (chips.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chip ->
                    NovaChip(text = chip, onClick = { onChipClick(chip) })
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 10.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(NovaSurface)
                .border(1.dp, NovaStroke, RoundedCornerShape(23.dp))
                .padding(start = 4.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = onAttach, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "更多",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(modifier = Modifier.weight(1f).padding(vertical = 10.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = "输入消息…",
                        color = TextTertiary,
                        fontSize = 14.5.sp, lineHeight = 20.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.5.sp, lineHeight = 21.sp),
                    cursorBrush = SolidColor(NovaPrimary),
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(4.dp))
            val active = streaming || value.isNotBlank()
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) Brush.linearGradient(listOf(NovaPrimary, NovaBlue))
                        else Brush.linearGradient(listOf(Color(0xFF2B2848), Color(0xFF2B2848)))
                    )
                    .clickable {
                        if (streaming) onStop() else if (value.isNotBlank()) onSend()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (streaming) Icons.Filled.Stop else Icons.AutoMirrored.Filled.Send,
                    contentDescription = if (streaming) "停止生成" else "发送",
                    tint = if (active) Color.White else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}
