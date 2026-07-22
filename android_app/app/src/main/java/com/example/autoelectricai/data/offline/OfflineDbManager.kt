package com.example.autoelectricai.data.offline

import android.content.Context
import android.os.Environment
import com.example.autoelectricai.BuildConfig
import com.example.autoelectricai.utils.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class OfflineDownloadState {
    object Idle : OfflineDownloadState()
    data class Downloading(val progress: Int, val speedMbPerSec: Float, val totalMb: Float) : OfflineDownloadState()
    data class Extracting(val progress: Int) : OfflineDownloadState()
    object Success : OfflineDownloadState()
    data class Error(val message: String) : OfflineDownloadState()
}

data class DbDownloadConfig(
    val mainUrl: String,
    val fallbackUrl: String?
)

@Singleton
class OfflineDbManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "OfflineDbManager"
        private const val OFFLINE_DB_FOLDER = "offline_db"
    }

    private val dbDir: File
        get() = File(context.filesDir, OFFLINE_DB_FOLDER).apply {
            if (!exists()) mkdirs()
        }

    fun isDatabaseInstalled(brand: String): Boolean {
        val brandDir = File(dbDir, brand)
        return brandDir.exists() && brandDir.isDirectory && brandDir.list()?.isNotEmpty() == true
    }

    fun deleteDatabase(brand: String) {
        val brandDir = File(dbDir, brand)
        if (brandDir.exists()) {
            brandDir.deleteRecursively()
        }
    }

    suspend fun fetchDownloadConfig(brand: String): DbDownloadConfig? = withContext(Dispatchers.IO) {
        try {
            val url = "https://raw.githubusercontent.com/Djoystick/avtoelektrika/master/docs/offline_dbs.json"
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonString = response.body?.string() ?: return@withContext null
                val jsonObject = org.json.JSONObject(jsonString)
                val databases = jsonObject.getJSONArray("databases")
                for (i in 0 until databases.length()) {
                    val db = databases.getJSONObject(i)
                    if (db.getString("brand").equals(brand, ignoreCase = true)) {
                        val mainUrl = db.getString("download_url")
                        val fallbackUrl = if (db.has("fallback_url")) db.getString("fallback_url") else null
                        return@withContext DbDownloadConfig(mainUrl, fallbackUrl)
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to fetch index.json", e)
        }
        return@withContext null
    }

    private fun buildDriveApiUrl(fileId: String): String {
        val apiKey = BuildConfig.GDRIVE_API_KEY
        return "https://www.googleapis.com/drive/v3/files/$fileId?alt=media&key=$apiKey"
    }

    private fun getProcessedUrl(rawUrl: String): String {
        // Если это ID (fallback_url), конвертируем в API url
        if (rawUrl.matches(Regex("^[a-zA-Z0-9_-]{33}$"))) {
            return buildDriveApiUrl(rawUrl)
        }
        // Если это старый URL гугл диска с id=
        val driveRegex = Regex("drive\\.google\\.com/.*id=([a-zA-Z0-9_-]{33})")
        val match = driveRegex.find(rawUrl)
        if (match != null) {
            return buildDriveApiUrl(match.groupValues[1])
        }
        return rawUrl
    }

    fun downloadAndInstallDatabase(brand: String, config: DbDownloadConfig): Flow<OfflineDownloadState> = flow {
        val zipFile = File(dbDir, "${brand}_temp.zip")
        val extractDir = File(dbDir, brand)

        var currentUrlToTry = getProcessedUrl(config.mainUrl)
        var usedFallback = false

        try {
            AppLogger.d(TAG, "Starting download for $brand from $currentUrlToTry")
            var request = Request.Builder().url(currentUrlToTry).build()
            var response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful && config.fallbackUrl != null) {
                AppLogger.d(TAG, "Main URL failed, trying fallback: ${config.fallbackUrl}")
                currentUrlToTry = getProcessedUrl(config.fallbackUrl)
                usedFallback = true
                response.close()
                request = Request.Builder().url(currentUrlToTry).build()
                response = okHttpClient.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                emit(OfflineDownloadState.Error("Ошибка сервера: ${response.code}"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(OfflineDownloadState.Error("Пустой ответ"))
                return@flow
            }

            val contentLength = body.contentLength()
            val totalMb = if (contentLength > 0) contentLength / (1024f * 1024f) else 0f

            if (zipFile.exists()) zipFile.delete()

            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(zipFile)
            val buffer = ByteArray(8 * 1024)
            var bytesCopied = 0L
            var bytesCopiedInLastSecond = 0L
            var lastTime = System.currentTimeMillis()

            emit(OfflineDownloadState.Downloading(0, 0f, totalMb))

            var bytes = inputStream.read(buffer)
            while (bytes >= 0) {
                yield()
                outputStream.write(buffer, 0, bytes)
                bytesCopied += bytes
                bytesCopiedInLastSecond += bytes

                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - lastTime
                if (timeDiff >= 500) {
                    val speedBytesPerSec = (bytesCopiedInLastSecond * 1000f) / timeDiff
                    val speedMbPerSec = speedBytesPerSec / (1024f * 1024f)
                    val progress = if (contentLength > 0) ((bytesCopied * 100L) / contentLength).toInt() else 0

                    emit(OfflineDownloadState.Downloading(progress, speedMbPerSec, totalMb))
                    lastTime = currentTime
                    bytesCopiedInLastSecond = 0L
                }
                bytes = inputStream.read(buffer)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Extraction phase
            emit(OfflineDownloadState.Extracting(0))
            if (extractDir.exists()) extractDir.deleteRecursively()
            extractDir.mkdirs()

            unzipFile(zipFile, extractDir) { extractProgress ->
                emit(OfflineDownloadState.Extracting(extractProgress))
            }

            zipFile.delete() // Cleanup
            emit(OfflineDownloadState.Success)

        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to download database", e)
            zipFile.delete()
            extractDir.deleteRecursively()
            emit(OfflineDownloadState.Error(e.message ?: "Неизвестная ошибка"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun unzipFile(zipFile: File, targetDirectory: File, onProgress: suspend (Int) -> Unit) {
        val totalBytes = zipFile.length()
        var extractedBytes = 0L
        
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                yield()
                val newFile = File(targetDirectory, zipEntry.name)
                if (zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        val buffer = ByteArray(8 * 1024)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                            extractedBytes += len
                            // Estimate progress based on decompressed size vs compressed total
                            // It's a rough estimate
                            val progress = ((extractedBytes.toFloat() / (totalBytes * 2f)) * 100).toInt().coerceIn(0, 99)
                            onProgress(progress)
                        }
                    }
                }
                zis.closeEntry()
                zipEntry = zis.nextEntry
            }
        }
        onProgress(100)
    }
}
