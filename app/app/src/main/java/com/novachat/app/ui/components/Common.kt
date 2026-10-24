package com.novachat.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novachat.app.ui.theme.NovaAccent
import com.novachat.app.ui.theme.NovaBlue
import com.novachat.app.ui.theme.NovaPrimary
import com.novachat.app.ui.theme.NovaStroke
import com.novachat.app.ui.theme.NovaSurface
import com.novachat.app.ui.theme.TextSecondary
import com.novachat.app.ui.theme.TextTertiary

/** Gradient orb used as the assistant avatar / brand mark. */
@Composable
fun OrbAvatar(size: Dp, modifier: Modifier = Modifier, sparkle: Boolean = false) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(NovaPrimary.copy(alpha = 0.45f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(size * 0.16f)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NovaPrimary, NovaBlue, NovaAccent)))
        )
        if (sparkle) {
            Canvas(Modifier.fillMaxSize()) {
                val r = this.size.minDimension * 0.14f
                val stroke = this.size.minDimension * 0.035f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                drawLine(Color.White, Offset(center.x - r, center.y), Offset(center.x + r, center.y), stroke, StrokeCap.Round)
                drawLine(Color.White, Offset(center.x, center.y - r), Offset(center.x, center.y + r), stroke, StrokeCap.Round)
            }
        }
    }
}

/** Rounded pill chip used for quick prompts and provider selection. */
@Composable
fun NovaChip(
    text: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(Brush.linearGradient(listOf(NovaPrimary, NovaBlue)))
                } else {
                    Modifier
                        .background(NovaSurface)
                        .border(1.dp, NovaStroke, CircleShape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextSecondary,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = TextTertiary,
        fontSize = 10.5.sp,
        letterSpacing = 0.8.sp,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier, size: Dp = 6.dp) {
    Box(modifier.size(size).clip(CircleShape).background(color))
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = TextSecondary,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = modifier.size(40.dp)) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}
