package com.example.autoelectricai.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.autoelectricai.BuildConfig
import com.example.autoelectricai.utils.AppLogger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(
        val progress: Int,
        val downloadedMb: Float,
        val totalMb: Float,
        val speedMbPerSec: Float,
        val remainingMb: Float
    ) : DownloadState()
    data class Downloaded(val fileUri: Uri) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    companion object {
        private const val TAG = "AppUpdateManager"
    }

    suspend fun checkForUpdate(): UpdateInfo? {
        return try {
            val doc = firestore.collection("app_updates").document("latest").get().await()
            if (doc.exists()) {
                val versionCode = doc.getLong("versionCode")?.toInt() ?: 0
                val versionName = doc.getString("versionName") ?: ""
                val downloadUrl = doc.getString("downloadUrl") ?: ""
                val releaseNotes = doc.getString("releaseNotes") ?: ""

                if (versionCode > BuildConfig.VERSION_CODE) {
                    UpdateInfo(versionCode, versionName, downloadUrl, releaseNotes)
                } else null
            } else null
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to check for updates", e)
            null
        }
    }

    suspend fun uploadUpdate(versionCode: Int, versionName: String, releaseNotes: String, downloadUrl: String): Result<Unit> {
        return try {
            val updateData = mapOf(
                "versionCode" to versionCode,
                "versionName" to versionName,
                "downloadUrl" to downloadUrl,
                "releaseNotes" to releaseNotes
            )

            firestore.collection("app_updates").document("latest").set(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to upload update", e)
            Result.failure(e)
        }
    }

    fun downloadUpdate(url: String, fileName: String): Flow<DownloadState> = kotlinx.coroutines.flow.flow {
        try {
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder().url(url).build()
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    emit(DownloadState.Error("Ошибка скачивания: ${response.code}"))
                    return@withContext
                }

                val body = response.body
                if (body == null) {
                    emit(DownloadState.Error("Пустой ответ от сервера"))
                    return@withContext
                }

                val contentLength = body.contentLength()
                val totalMb = if (contentLength > 0) contentLength / (1024f * 1024f) else 0f
                
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (dir != null && !dir.exists()) {
                    dir.mkdirs()
                }
                
                val file = File(dir, fileName)
                if (file.exists()) file.delete()
                
                val inputStream = body.byteStream()
                val outputStream = java.io.FileOutputStream(file)
                
                val buffer = ByteArray(8 * 1024)
                var bytesCopied = 0L
                var bytesCopiedInLastSecond = 0L
                var lastTime = System.currentTimeMillis()
                
                emit(DownloadState.Downloading(0, 0f, totalMb, 0f, totalMb))
                
                var bytes = inputStream.read(buffer)
                while (bytes >= 0) {
                    // Check for cancellation
                    kotlinx.coroutines.yield()
                    
                    outputStream.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    bytesCopiedInLastSecond += bytes
                    
                    val currentTime = System.currentTimeMillis()
                    val timeDiff = currentTime - lastTime
                    if (timeDiff >= 500) {
                        val speedBytesPerSec = (bytesCopiedInLastSecond * 1000f) / timeDiff
                        val speedMbPerSec = speedBytesPerSec / (1024f * 1024f)
                        
                        val downloadedMb = bytesCopied / (1024f * 1024f)
                        val remainingMb = totalMb - downloadedMb
                        val progress = if (contentLength > 0) ((bytesCopied * 100L) / contentLength).toInt() else 0
                        
                        emit(DownloadState.Downloading(
                            progress = progress,
                            downloadedMb = downloadedMb,
                            totalMb = totalMb,
                            speedMbPerSec = speedMbPerSec,
                            remainingMb = remainingMb
                        ))
                        
                        bytesCopiedInLastSecond = 0
                        lastTime = currentTime
                    }
                    bytes = inputStream.read(buffer)
                }
                
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                emit(DownloadState.Downloaded(uri))
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            AppLogger.i(TAG, "Download cancelled")
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Download error", e)
            emit(DownloadState.Error(e.message ?: "Неизвестная ошибка скачивания"))
        }
    }
}
