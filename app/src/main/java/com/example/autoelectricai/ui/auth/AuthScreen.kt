package com.example.autoelectricai.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.R
import com.example.autoelectricai.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isLoginMode by remember { mutableStateOf(true) }

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo or Icon
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Ensure you have this
                contentDescription = "Logo",
                tint = AmberPrimary,
                modifier = Modifier.size(100.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "AutoElectric AI",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Умный помощник автоэлектрика",
                color = TextSecondary,
                fontSize = 14.sp
            )
            
            Spacer(Modifier.height(48.dp))

            AnimatedContent(
                targetState = isLoginMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "auth_mode"
            ) { loginMode ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email", color = TextHint) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = DarkSurface2,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Пароль", color = TextHint) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextHint) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, "Toggle password visibility", tint = TextHint)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = DarkSurface2,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (loginMode) ImeAction.Done else ImeAction.Next),
                        singleLine = true
                    )

                    if (!loginMode) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = confirmPassword.value,
                            onValueChange = { confirmPassword.value = it },
                            label = { Text("Повторите пароль", color = TextHint) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextHint) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = DarkSurface2,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            singleLine = true
                        )
                    }

                    if (uiState is AuthUiState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (uiState as AuthUiState.Error).message,
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (loginMode) {
                                viewModel.signInWithEmail(email.value, password.value)
                            } else {
                                viewModel.registerWithEmail(email.value, password.value, confirmPassword.value)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (loginMode) "Войти" else "Зарегистрироваться", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isLoginMode) "Нет аккаунта? " else "Уже есть аккаунт? ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isLoginMode) "Регистрация" else "Войти",
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        isLoginMode = !isLoginMode
                        viewModel.resetState()
                    }
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = "Или продолжить без регистрации",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    viewModel.signInAnonymously()
                }
            )
            
            Spacer(Modifier.height(8.dp))
            Text("Телеграм-авторизация в разработке", color = TextHint, fontSize = 12.sp)
        }
    }
}
