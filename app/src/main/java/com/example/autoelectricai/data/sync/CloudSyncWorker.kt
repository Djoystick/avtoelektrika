package com.example.autoelectricai.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autoelectricai.utils.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: CloudSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "CloudSyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            AppLogger.i(TAG, "Starting periodic cloud sync")
            
            // Убеждаемся, что пользователь хотя бы анонимно авторизован
            syncRepository.signInAnonymouslyIfNeeded()

            // Скачиваем обновления
            syncRepository.pullCommunityUpdates()
            
            AppLogger.i(TAG, "Cloud sync finished successfully")
            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Cloud sync failed", e)
            Result.retry()
        }
    }
}
