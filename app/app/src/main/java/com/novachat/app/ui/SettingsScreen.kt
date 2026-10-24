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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novachat.app.TestState
import com.novachat.app.data.Provider
import com.novachat.app.data.Settings
import com.novachat.app.ui.components.NovaChip
import com.novachat.app.ui.components.SectionLabel
import com.novachat.app.ui.theme.ErrorRed
import com.novachat.app.ui.theme.NovaBlue
import com.novachat.app.ui.theme.NovaPrimary
import com.novachat.app.ui.theme.NovaStroke
import com.novachat.app.ui.theme.NovaSurface
import com.novachat.app.ui.theme.SuccessGreen
import com.novachat.app.ui.theme.TextPrimary
import com.novachat.app.ui.theme.TextSecondary
import com.novachat.app.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun SettingsScreen(
    draft: Settings,
    onChange: (Settings) -> Unit,
    test: TestState,
    encrypted: Boolean,
    onTest: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onClearKey: () -> Unit,
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
                .background(Color(0xCC0B0A16))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary
                )
            }
            Text(
                text = "设置",
                color = TextPrimary,
                fontSize = 15.sp, lineHeight = 21.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "保存",
                color = Color(0xFFA78BFA),
                fontSize = 13.sp, lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onSave)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(14.dp))
            SectionLabel("服务提供商")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Provider.entries.forEach { provider ->
                    NovaChip(
                        text = provider.label,
                        selected = draft.providerId == provider.id,
                        onClick = {
                            onChange(
                                if (provider == Provider.CUSTOM) {
                                    draft.copy(providerId = provider.id)
                                } else {
                                    draft.copy(
                                        providerId = provider.id,
                                        baseUrl = provider.baseUrl,
                                        model = provider.model
                                    )
                                }
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SettingsField(
                label = "API BASE URL",
                value = draft.baseUrl,
                onValueChange = { onChange(draft.copy(baseUrl = it)) },
                placeholder = "https://api.deepseek.com/v1",
                hint = "兼容 OpenAI Chat Completions 协议的地址均可",
                keyboardType = KeyboardType.Uri
            )
            Spacer(Modifier.height(16.dp))
            ApiKeyField(
                value = draft.apiKey,
                onValueChange = { onChange(draft.copy(apiKey = it.trim())) },
                onClear = onClearKey
            )
            Spacer(Modifier.height(16.dp))
            SettingsField(
                label = "模型名称",
                value = draft.model,
                onValueChange = { onChange(draft.copy(model = it)) },
                placeholder = "deepseek-chat",
                hint = "填写服务商支持的模型 ID"
            )
            Spacer(Modifier.height(16.dp))
            SettingsField(
                label = "系统提示词",
                value = draft.systemPrompt,
                onValueChange = { onChange(draft.copy(systemPrompt = it)) },
                placeholder = "你是一个乐于助人的中文 AI 助手。",
                singleLine = false,
                minHeight = 74.dp
            )

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("温度", Modifier.weight(1f))
                Text(
                    text = String.format(Locale.US, "%.1f", draft.temperature),
                    color = Color(0xFFA78BFA),
                    fontSize = 12.sp, lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Slider(
                value = draft.temperature,
                onValueChange = { onChange(draft.copy(temperature = it)) },
                valueRange = 0f..2f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = NovaPrimary,
                    inactiveTrackColor = Color(0xFF3A365C)
                )
            )

            Spacer(Modifier.height(8.dp))
            TestCard(test = test, onTest = onTest)

            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(NovaPrimary, Color(0xFF8B5CF6))))
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "保存并开始对话",
                    color = Color.White,
                    fontSize = 14.sp, lineHeight = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = if (encrypted) {
                    "密钥使用 EncryptedSharedPreferences 加密，仅保存在本机"
                } else {
                    "当前设备不支持硬件加密，密钥以明文保存在应用私有目录"
                },
                color = TextTertiary,
                fontSize = 10.sp, lineHeight = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    hint: String? = null,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 54.dp,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        SectionLabel(label)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(NovaSurface)
                .border(1.dp, NovaStroke, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            if (value.isEmpty()) {
                Text(text = placeholder, color = TextTertiary, fontSize = 13.sp, lineHeight = 18.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp),
                cursorBrush = SolidColor(NovaPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (hint != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = hint, color = TextTertiary, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun ApiKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    var revealed = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("API KEY", Modifier.weight(1f))
            if (value.isNotEmpty()) {
                Text(
                    text = "清除",
                    color = TextTertiary,
                    fontSize = 10.5.sp, lineHeight = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClear)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NovaSurface)
                .border(1.dp, NovaStroke, RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f).padding(vertical = 15.dp)) {
                if (value.isEmpty()) {
                    Text("sk-...", color = TextTertiary, fontSize = 13.sp, lineHeight = 18.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(NovaPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (revealed.value) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(onClick = { revealed.value = !revealed.value }) {
                Icon(
                    imageVector = if (revealed.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "显示或隐藏密钥",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "仅保存在本机，不会上传到任何第三方服务器",
            color = TextTertiary,
            fontSize = 10.sp, lineHeight = 14.sp
        )
    }
}

@Composable
private fun TestCard(test: TestState, onTest: () -> Unit) {
    val (tint, title, subtitle) = when (test) {
        is TestState.Idle -> Triple(NovaBlue, "测试连接", "保存前可先用 1 token 验证地址与密钥")
        is TestState.Running -> Triple(NovaBlue, "正在测试连接…", "正在请求服务商接口")
        is TestState.Ok -> Triple(SuccessGreen, "连接正常", test.message)
        is TestState.Failed -> Triple(ErrorRed, "连接失败", test.message)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF231F42))
            .border(1.dp, tint.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .clickable(enabled = test !is TestState.Running, onClick = onTest)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (test) {
            is TestState.Running -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = NovaPrimary
            )
            else -> Icon(
                imageVector = when (test) {
                    is TestState.Ok -> Icons.Filled.CheckCircle
                    is TestState.Failed -> Icons.Filled.ErrorOutline
                    else -> Icons.Filled.CheckCircle
                },
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 12.5.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2)
        }
        Text(
            text = "重新测试",
            color = Color(0xFFA78BFA),
            fontSize = 10.5.sp, lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
