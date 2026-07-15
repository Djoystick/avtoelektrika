package com.example.autoelectricai.ui.main

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.autoelectricai.BuildConfig
import com.example.autoelectricai.data.update.DownloadState
import com.example.autoelectricai.data.update.UpdateInfo
import com.example.autoelectricai.theme.AmberPrimary
import com.example.autoelectricai.theme.DarkBackground
import com.example.autoelectricai.theme.DarkSurface
import com.example.autoelectricai.theme.DarkSurface2
import com.example.autoelectricai.theme.ErrorRed
import com.example.autoelectricai.theme.SuccessGreen
import com.example.autoelectricai.theme.TextHint
import com.example.autoelectricai.theme.TextPrimary
import com.example.autoelectricai.theme.TextSecondary

@Composable
fun UpdateSection(
    updateInfo: UpdateInfo?,
    downloadState: DownloadState,
    isChecking: Boolean,
    onCheckForUpdates: () -> Unit,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(downloadState) {
        if (downloadState is DownloadState.Downloaded) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(downloadState.fileUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // We will remove padding here because the parent Card in SettingsScreen will provide padding.


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Обновление приложения",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (updateInfo == null && downloadState !is DownloadState.Downloading) {
                    TextButton(
                        onClick = onCheckForUpdates,
                        enabled = !isChecking,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (isChecking) "Проверка..." else "Проверить", color = AmberPrimary)
                    }
                }
            }

            if (updateInfo != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Загрузка ${updateInfo.versionName}",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "После завершения загрузки, установите APK",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (downloadState is DownloadState.Downloading) {
                            val dl = downloadState
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = String.format("Скачано %.1f MB из %.1f MB", dl.downloadedMb, dl.totalMb),
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = String.format("Скорость %.1f MB/s", dl.speedMbPerSec),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = { dl.progress / 100f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp),
                                    color = Color(0xFF4A80F5),
                                    trackColor = DarkSurface2
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = onCancelDownload,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Отмена",
                                        tint = ErrorRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = String.format("Осталось %.1f MB", dl.remainingMb),
                                color = TextHint,
                                fontSize = 12.sp
                            )
                        } else {
                            Button(
                                onClick = onStartDownload,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A80F5)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (downloadState is DownloadState.Error) "Повторить загрузку" else "Начать загрузку", color = Color.White)
                            }
                            
                            if (downloadState is DownloadState.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = downloadState.message, color = ErrorRed, fontSize = 12.sp)
                            } else if (downloadState is DownloadState.Downloaded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Готово к установке!", color = SuccessGreen, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else if (!isChecking) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("У вас установлена последняя версия.", color = TextHint, fontSize = 13.sp)
            }
    }
}
