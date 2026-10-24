package com.novachat.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NovaColorScheme = darkColorScheme(
    primary = NovaPrimary,
    onPrimary = Color.White,
    secondary = NovaBlue,
    onSecondary = Color.White,
    tertiary = NovaAccent,
    background = BgTop,
    onBackground = TextPrimary,
    surface = NovaSurface,
    onSurface = TextPrimary,
    surfaceVariant = NovaSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = NovaStroke,
    error = ErrorRed
)

val NovaTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 31.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 19.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)

private val NovaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NovaChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaColorScheme,
        typography = NovaTypography,
        shapes = NovaShapes,
        content = content
    )
}

/** Deep-space gradient background with two soft radial glows, matching the design sheet. */
@Composable
fun NovaBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            .drawBehind {
                val topGlowCenter = Offset(size.width * 0.86f, -size.width * 0.05f)
                val topRadius = size.width * 0.95f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NovaPrimary.copy(alpha = 0.30f), Color.Transparent),
                        center = topGlowCenter,
                        radius = topRadius
                    ),
                    radius = topRadius,
                    center = topGlowCenter
                )
                val sideGlowCenter = Offset(-size.width * 0.1f, size.height * 0.58f)
                val sideRadius = size.width * 0.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NovaBlue.copy(alpha = 0.22f), Color.Transparent),
                        center = sideGlowCenter,
                        radius = sideRadius
                    ),
                    radius = sideRadius,
                    center = sideGlowCenter
                )
            },
        content = content
    )
}
