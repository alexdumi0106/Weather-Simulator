package com.example.weathersimulator.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

private val SkyTop = Color(0xFF0E7BB2)
private val SkyMiddle = Color(0xFF77B6D8)
private val SkyBottom = Color(0xFF6C7F93)

val AuthTitleColor = Color(0xFFFFFFFF)
val AuthSubtitleColor = Color(0xFFE3F2FA)
val AuthFieldTextColor = Color(0xFFF3F8FC)
val AuthHintColor = Color(0xB3E5F4FD)
val AuthLinkColor = Color(0xFFF2F6FA)
val AuthAccentColor = Color(0xFFF6C861)

@Composable
fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyTop, SkyMiddle, SkyBottom)
                )
            )
    ) {
        content()
    }
}

@Composable
fun AuthGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.28f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = TextStyle(color = AuthFieldTextColor),
        placeholder = {
            Text(
                text = placeholder,
                color = AuthHintColor
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
            disabledContainerColor = Color.White.copy(alpha = 0.06f),
            focusedBorderColor = Color.White.copy(alpha = 0.45f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
            focusedTextColor = AuthFieldTextColor,
            unfocusedTextColor = AuthFieldTextColor,
            focusedPlaceholderColor = AuthHintColor,
            unfocusedPlaceholderColor = AuthHintColor,
            cursorColor = AuthFieldTextColor
        ),
        visualTransformation = visualTransformation
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = AuthAccentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = AuthAccentColor.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = AuthAccentColor.copy(alpha = if (enabled) 0.95f else 0.45f)
        )
    ) {
        Text(text = text)
    }
}