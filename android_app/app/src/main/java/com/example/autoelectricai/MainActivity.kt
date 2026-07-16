package com.example.autoelectricai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.autoelectricai.data.sync.CloudSyncWorker
import com.example.autoelectricai.theme.AutoElectricTheme
import com.example.autoelectricai.ui.diagnosis.DiagnosisScreen
import com.example.autoelectricai.ui.knowledgebase.KnowledgeBaseScreen
import com.example.autoelectricai.ui.leaderboard.LeaderboardScreen
import com.example.autoelectricai.ui.profile.ProfileScreen
import com.example.autoelectricai.ui.moderation.ModerationScreen
import com.example.autoelectricai.ui.settings.SettingsScreen
import com.example.autoelectricai.utils.AppLogger
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        signInAnonymouslyIfNeeded()
        scheduleCloudSync()

        setContent {
            val startDestination = "diagnosis"
            AutoElectricTheme {
                com.example.autoelectricai.ui.main.MainScreen(startDestination = startDestination)
            }
        }
    }

    private fun signInAnonymouslyIfNeeded() {
        // Firebase automatically restores a cached session on startup via AuthStateListener.
        // We only sign in anonymously if there is truly no user (including no cached session).
        // This check is deferred slightly to allow Firebase to restore the session first.
        if (auth.currentUser == null) {
            auth.addAuthStateListener(object : FirebaseAuth.AuthStateListener {
                override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                    // Firebase has responded — remove this one-shot listener
                    firebaseAuth.removeAuthStateListener(this)
                    // Only sign in anonymously if still no user after Firebase checked cache
                    if (firebaseAuth.currentUser == null) {
                        firebaseAuth.signInAnonymously()
                            .addOnSuccessListener {
                                AppLogger.i("MainActivity", "No cached session found, signed in anonymously: ${firebaseAuth.currentUser?.uid}")
                            }
                            .addOnFailureListener { e ->
                                AppLogger.e("MainActivity", "Anonymous sign-in failed", e)
                            }
                    } else {
                        AppLogger.i("MainActivity", "Session restored from cache: ${firebaseAuth.currentUser?.email ?: "anonymous"}")
                    }
                }
            })
        } else {
            AppLogger.i("MainActivity", "User already present on start: ${auth.currentUser?.email ?: "anonymous"}")
        }
    }

    private fun scheduleCloudSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<CloudSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CloudSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

// Removed AutoElectricAppNavHost as it is moved to MainScreen.kt
