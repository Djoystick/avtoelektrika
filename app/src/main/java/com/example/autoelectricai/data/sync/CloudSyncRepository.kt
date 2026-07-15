package com.example.autoelectricai.data.sync

import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.utils.AppLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import com.example.autoelectricai.data.ai.AiService
import com.example.autoelectricai.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val dao: DiagnosisDao,
    private val aiService: AiService,
    private val settings: SettingsRepository
) {
    companion object {
        private const val TAG = "CloudSyncRepo"
        private const val COLLECTION_SOLUTIONS = "community_solutions"
        private const val COLLECTION_ROLES = "user_roles"
        private const val COLLECTION_USERNAMES = "usernames"
    }

    suspend fun signInAnonymouslyIfNeeded() {
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
                AppLogger.i(TAG, "Signed in anonymously: ${auth.currentUser?.uid}")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to sign in anonymously", e)
            }
        }
    }

    suspend fun getUserRole(email: String): String {
        if (email.isBlank()) return "user"
        return try {
            val doc = firestore.collection(COLLECTION_ROLES).document(email).get().await()
            if (doc.exists()) {
                doc.getString("role") ?: "user"
            } else {
                "user"
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get user role", e)
            "user"
        }
    }

    suspend fun setRole(email: String, role: String) {
        if (email.isBlank()) return
        try {
            val data = mapOf("role" to role)
            firestore.collection(COLLECTION_ROLES).document(email).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            AppLogger.i(TAG, "Successfully set role $role for $email")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to set role", e)
        }
    }

    suspend fun deleteRole(email: String) {
        if (email.isBlank()) return
        try {
            firestore.collection(COLLECTION_ROLES).document(email).delete().await()
            AppLogger.i(TAG, "Successfully deleted role for $email")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete role", e)
        }
    }

    suspend fun getNickname(email: String): String? {
        if (email.isBlank()) return null
        
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val localNick = prefs.getString("nickname_$email", null)
        
        return try {
            val doc = firestore.collection(COLLECTION_ROLES).document(email).get().await()
            if (doc.exists()) {
                val nick = doc.getString("username")
                if (nick != null) {
                    prefs.edit { putString("nickname_$email", nick) }
                }
                nick
            } else {
                localNick
            }
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                AppLogger.e(TAG, "Failed to get username", e)
            }
            localNick
        }
    }

    suspend fun checkNicknameUnique(nickname: String, email: String? = null): Boolean {
        if (nickname.isBlank()) return false
        val doc = firestore.collection(COLLECTION_USERNAMES).document(nickname.lowercase()).get(com.google.firebase.firestore.Source.SERVER).await()
        if (doc.exists()) {
            val ownerEmail = doc.getString("email")
            if (ownerEmail != null && email != null && ownerEmail == email) {
                return true // User is claiming their own nickname
            }
            return false
        }
        return true
    }

    suspend fun setNickname(email: String, nickname: String): Boolean {
        if (email.isBlank() || nickname.isBlank()) return false
        val lowerNick = nickname.lowercase()
        val doc = firestore.collection(COLLECTION_USERNAMES).document(lowerNick).get(com.google.firebase.firestore.Source.SERVER).await()
        if (doc.exists()) {
            val ownerEmail = doc.getString("email")
            if (ownerEmail != email) return false
        }

        firestore.collection(COLLECTION_USERNAMES).document(lowerNick).set(mapOf("email" to email)).await()
        firestore.collection(COLLECTION_ROLES).document(email).set(mapOf("username" to nickname), com.google.firebase.firestore.SetOptions.merge()).await()
        
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit { putString("nickname_$email", nickname) }
            
        return true
    }

    suspend fun pushSolution(entity: DiagnosisEntity) {
        val user = auth.currentUser
        if (user == null) {
            AppLogger.w(TAG, "Cannot push solution: user is not authorized (null)")
            return
        }

        // BUGFIX: Block anonymous users from pushing solutions.
        // This happens when Firebase restores an anonymous session on cold start
        // before the real Google/Email account is fully restored.
        if (user.isAnonymous || user.email.isNullOrBlank()) {
            AppLogger.w(TAG, "Cannot push solution: user is anonymous or has no email (uid=${user.uid}). Skipping push.")
            return
        }

        AppLogger.i(TAG, "Pushing solution as author: ${user.email}")
        val role = getUserRole(user.email!!)
        val status = if (role == "admin" || role == "specialist") "approved" else "pending"

        try {
            val authorUsername = if (user.isAnonymous || user.email.isNullOrBlank()) "Аноним" else getNickname(user.email!!)

            // Run AI Verification before pushing to cloud
            var finalScore = entity.aiConfidenceScore
            var finalReason = entity.aiConfidenceReason

            if (finalScore == null) {
                try {
                    val geminiKey = settings.geminiApiKey.first()
                    val geminiProxyUrl = settings.geminiProxyUrl.first()
                    val openAiKey = settings.openAiApiKey.first()
                    val preferredAi = settings.preferredAi.first()

                    val aiVerify = aiService.verifySolution(
                        symptoms = entity.symptoms,
                        errorCodes = entity.errorCodes,
                        solutionText = entity.solution,
                        geminiKey = geminiKey,
                        geminiProxyUrl = geminiProxyUrl,
                        openAiKey = openAiKey,
                        preferredAi = preferredAi
                    )
                    
                    if (aiVerify != null) {
                        finalScore = aiVerify.score
                        finalReason = aiVerify.reason
                        AppLogger.i(TAG, "AI Verification success: $finalScore - $finalReason")
                    } else {
                        AppLogger.w(TAG, "AI Verification returned null")
                    }
                } catch (e: Exception) {
                    AppLogger.e(TAG, "AI Verification failed", e)
                }
            }

            val cloudData = mutableMapOf<String, Any>(
                "carBrand" to entity.carBrand,
                "carModel" to entity.carModel,
                "carYear" to entity.carYear,
                "system" to entity.system,
                "symptoms" to entity.symptoms,
                "errorCodes" to entity.errorCodes,
                "solution" to entity.solution,
                "aiProvider" to entity.aiProvider,
                "syncVersion" to System.currentTimeMillis()
            )
            
            if (finalScore != null) {
                cloudData["aiConfidenceScore"] = finalScore
            }
            if (finalReason != null) {
                cloudData["aiConfidenceReason"] = finalReason
            }

            val docRef = if (entity.cloudId != null) {
                firestore.collection(COLLECTION_SOLUTIONS).document(entity.cloudId)
            } else {
                cloudData["authorUid"] = user.uid
                cloudData["authorEmail"] = user.email ?: ""
                cloudData["authorUsername"] = authorUsername ?: "Аноним"
                cloudData["likes"] = 0
                cloudData["dislikes"] = 0
                cloudData["status"] = status
                cloudData["createdAt"] = FieldValue.serverTimestamp()
                firestore.collection(COLLECTION_SOLUTIONS).document()
            }

            docRef.set(cloudData, com.google.firebase.firestore.SetOptions.merge()).await()
            AppLogger.i(TAG, "Successfully pushed solution to cloud: ${docRef.id}")
            
            // Обновляем локальную сущность
            if (entity.cloudId == null) {
                dao.update(
                    entity.copy(
                        cloudId = docRef.id,
                        aiConfidenceScore = finalScore,
                        aiConfidenceReason = finalReason
                    )
                )
            } else if (finalScore != entity.aiConfidenceScore || finalReason != entity.aiConfidenceReason) {
                // If it already had a cloudId but was just verified now
                dao.update(
                    entity.copy(
                        aiConfidenceScore = finalScore,
                        aiConfidenceReason = finalReason
                    )
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to push solution", e)
        }
    }

    suspend fun appendAddendum(cloudId: String, addendumText: String): Boolean {
        val user = auth.currentUser ?: return false
        val email = user.email ?: return false
        val role = getUserRole(email)
        val nickname = getNickname(email) ?: "Эксперт"
        
        return try {
            val docRef = firestore.collection(COLLECTION_SOLUTIONS).document(cloudId)
            val success = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) return@runTransaction false
                
                // Проверка прав (как в плане)
                val authorEmail = snapshot.getString("authorEmail")
                if (role != "admin" && role != "specialist" && authorEmail != email) {
                    throw Exception("Permission denied")
                }
                
                val currentSolution = snapshot.getString("solution") ?: ""
                val newSolution = "$currentSolution\n\n> **[Дополнение от $nickname]**:\n> $addendumText"
                
                transaction.update(docRef, "solution", newSolution)
                transaction.update(docRef, "syncVersion", System.currentTimeMillis())
                
                // Увеличиваем счетчик дополнений
                val roleRef = firestore.collection(COLLECTION_ROLES).document(email)
                transaction.set(roleRef, mapOf("addendumsCount" to com.google.firebase.firestore.FieldValue.increment(1)), com.google.firebase.firestore.SetOptions.merge())
                true
            }.await()
            if (success) checkAchievements(email)
            success
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to append addendum", e)
            false
        }
    }

    suspend fun pullCommunityUpdates() {
        try {
            // TODO: Реализовать инкрементальную синхронизацию по syncVersion
            val snapshot = firestore.collection(COLLECTION_SOLUTIONS)
                .whereEqualTo("status", "approved")
                .limit(500)
                .get()
                .await()

            val sortedDocs = snapshot.documents.sortedByDescending { it.getLong("syncVersion") ?: 0L }
            for (doc in sortedDocs) {
                val cloudId = doc.id
                val existing = dao.getByCloudId(cloudId)
                
                val entity = DiagnosisEntity(
                    id = existing?.id ?: 0,
                    cloudId = cloudId,
                    carBrand = doc.getString("carBrand") ?: "",
                    carModel = doc.getString("carModel") ?: "",
                    carYear = doc.getString("carYear") ?: "",
                    system = doc.getString("system") ?: "",
                    symptoms = doc.getString("symptoms") ?: "",
                    errorCodes = doc.getString("errorCodes") ?: "",
                    solution = doc.getString("solution") ?: "",
                    aiProvider = doc.getString("aiProvider") ?: "gemini",
                    isFromCommunity = true,
                    isOfflineReady = true,
                    likes = doc.getLong("likes")?.toInt() ?: 0,
                    dislikes = doc.getLong("dislikes")?.toInt() ?: 0,
                    userVote = existing?.userVote,
                    successCount = existing?.successCount ?: 1,
                    authorEmail = doc.getString("authorEmail") ?: "",
                    authorUsername = doc.getString("authorUsername")
                )
                if (existing != null) {
                    dao.update(entity)
                } else {
                    dao.insert(entity)
                }
            }
            AppLogger.i(TAG, "Pulled ${snapshot.size()} updates from community")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to pull community updates", e)
        }
    }

    suspend fun vote(cloudId: String, entityId: Long, isLike: Boolean): Boolean {
        val user = auth.currentUser
        if (user == null) {
            AppLogger.w(TAG, "Cannot vote: not signed in")
            return false
        }

        return try {
            val voteRef = firestore.collection(COLLECTION_SOLUTIONS).document(cloudId)
                .collection("votes").document(user.uid)
            
            firestore.runTransaction { transaction ->
                val solutionRef = firestore.collection(COLLECTION_SOLUTIONS).document(cloudId)
                val solSnapshot = transaction.get(solutionRef)
                val authorEmail = solSnapshot.getString("authorEmail") ?: ""
                var roleRef: com.google.firebase.firestore.DocumentReference? = null
                if (authorEmail.isNotBlank()) {
                    roleRef = firestore.collection(COLLECTION_ROLES).document(authorEmail)
                }
                
                val voteSnapshot = transaction.get(voteRef)
                val oldVote = if (voteSnapshot.exists()) voteSnapshot.getString("vote") else null
                val newVote = if (isLike) "like" else "dislike"
                
                if (oldVote == newVote) {
                    // снимаем голос
                    transaction.delete(voteRef)
                    if (isLike) {
                        transaction.update(solutionRef, "likes", com.google.firebase.firestore.FieldValue.increment(-1))
                        roleRef?.let { transaction.set(it, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(-10)), com.google.firebase.firestore.SetOptions.merge()) }
                    } else {
                        transaction.update(solutionRef, "dislikes", com.google.firebase.firestore.FieldValue.increment(-1))
                        roleRef?.let { transaction.set(it, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(2)), com.google.firebase.firestore.SetOptions.merge()) }
                    }
                } else {
                    // меняем или ставим новый
                    transaction.set(voteRef, mapOf("vote" to newVote, "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
                    
                    if (isLike) {
                        transaction.update(solutionRef, "likes", com.google.firebase.firestore.FieldValue.increment(1))
                        roleRef?.let { transaction.set(it, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(10)), com.google.firebase.firestore.SetOptions.merge()) }
                        if (oldVote == "dislike") {
                            transaction.update(solutionRef, "dislikes", com.google.firebase.firestore.FieldValue.increment(-1))
                            roleRef?.let { transaction.set(it, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(2)), com.google.firebase.firestore.SetOptions.merge()) }
                        }
                    } else {
                        transaction.update(solutionRef, "dislikes", com.google.firebase.firestore.FieldValue.increment(1))
                        roleRef?.let { transaction.set(it, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(-2)), com.google.firebase.firestore.SetOptions.merge()) }
                        if (oldVote == "like") {
                            transaction.update(solutionRef, "likes", com.google.firebase.firestore.FieldValue.increment(-1))
                            roleRef?.let { transaction.set(it, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(-10)), com.google.firebase.firestore.SetOptions.merge()) }
                        }
                    }
                }
            }.await()
            
            val authorEmail = firestore.collection(COLLECTION_SOLUTIONS).document(cloudId).get().await().getString("authorEmail")
            if (authorEmail != null) checkAchievements(authorEmail)
            
            AppLogger.i(TAG, "Successfully voted ($isLike) for $cloudId")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to vote", e)
            false
        }
    }
    suspend fun getPendingSolutions(): List<DiagnosisEntity> {
        val result = mutableListOf<DiagnosisEntity>()
        try {
            val snapshot = firestore.collection(COLLECTION_SOLUTIONS)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            for (doc in snapshot.documents) {
                result.add(
                    DiagnosisEntity(
                        cloudId = doc.id,
                        carBrand = doc.getString("carBrand") ?: "",
                        carModel = doc.getString("carModel") ?: "",
                        carYear = doc.getString("carYear") ?: "",
                        system = doc.getString("system") ?: "",
                        symptoms = doc.getString("symptoms") ?: "",
                        errorCodes = doc.getString("errorCodes") ?: "",
                        solution = doc.getString("solution") ?: "",
                        aiProvider = doc.getString("aiProvider") ?: "gemini",
                        isFromCommunity = true,
                        isOfflineReady = false,
                        likes = doc.getLong("likes")?.toInt() ?: 0,
                        dislikes = doc.getLong("dislikes")?.toInt() ?: 0,
                        successCount = 1,
                        authorEmail = doc.getString("authorEmail") ?: "",
                        aiConfidenceScore = doc.getLong("aiConfidenceScore")?.toInt(),
                        aiConfidenceReason = doc.getString("aiConfidenceReason")
                    )
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get pending solutions", e)
        }
        return result
    }

    suspend fun approveSolution(cloudId: String) {
        try {
            firestore.runTransaction { transaction ->
                val solutionRef = firestore.collection(COLLECTION_SOLUTIONS).document(cloudId)
                val solSnapshot = transaction.get(solutionRef)
                val authorEmail = solSnapshot.getString("authorEmail") ?: ""
                
                transaction.update(
                    solutionRef, 
                    "status", "approved",
                    "syncVersion", System.currentTimeMillis()
                )
                
                if (authorEmail.isNotBlank()) {
                    val roleRef = firestore.collection(COLLECTION_ROLES).document(authorEmail)
                    transaction.set(roleRef, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(50)), com.google.firebase.firestore.SetOptions.merge())
                }
                
                val approverEmail = com.example.autoelectricai.utils.AuthUtils.currentUserEmail
                if (approverEmail.isNotBlank()) {
                    val approverRef = firestore.collection(COLLECTION_ROLES).document(approverEmail)
                    transaction.set(approverRef, mapOf("approvedCount" to com.google.firebase.firestore.FieldValue.increment(1)), com.google.firebase.firestore.SetOptions.merge())
                }
            }.await()
            
            val approverEmail = com.example.autoelectricai.utils.AuthUtils.currentUserEmail
            if (approverEmail.isNotBlank()) checkAchievements(approverEmail)
            
            AppLogger.i(TAG, "Approved solution $cloudId and assigned +50 Karma")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to approve solution $cloudId", e)
        }
    }

    suspend fun rejectSolution(cloudId: String) {
        try {
            firestore.collection(COLLECTION_SOLUTIONS).document(cloudId).delete().await()
            AppLogger.i(TAG, "Rejected and deleted solution $cloudId")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to reject solution $cloudId", e)
        }
    }

    suspend fun updateAndApproveSolution(cloudId: String, updated: com.example.autoelectricai.data.db.DiagnosisEntity) {
        try {
            firestore.runTransaction { transaction ->
                val solutionRef = firestore.collection(COLLECTION_SOLUTIONS).document(cloudId)
                val solSnapshot = transaction.get(solutionRef)
                val authorEmail = solSnapshot.getString("authorEmail") ?: ""

                transaction.update(
                    solutionRef,
                    "status", "approved",
                    "solution", updated.solution,
                    "symptoms", updated.symptoms,
                    "editedByModerator", true,
                    "syncVersion", System.currentTimeMillis()
                )

                if (authorEmail.isNotBlank()) {
                    val roleRef = firestore.collection(COLLECTION_ROLES).document(authorEmail)
                    transaction.set(roleRef, mapOf("karma" to com.google.firebase.firestore.FieldValue.increment(50)), com.google.firebase.firestore.SetOptions.merge())
                }

                val approverEmail = auth.currentUser?.email
                if (!approverEmail.isNullOrBlank()) {
                    val approverRef = firestore.collection(COLLECTION_ROLES).document(approverEmail)
                    transaction.set(approverRef, mapOf("approvedCount" to com.google.firebase.firestore.FieldValue.increment(1)), com.google.firebase.firestore.SetOptions.merge())
                }
            }.await()
            AppLogger.i(TAG, "Updated and approved solution $cloudId")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update and approve solution $cloudId", e)
        }
    }

    suspend fun getUserKarma(email: String): Int {
        if (email.isBlank()) return 0
        return try {
            val doc = firestore.collection(COLLECTION_ROLES).document(email).get().await()
            if (doc.exists()) {
                doc.getLong("karma")?.toInt() ?: 0
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTopExperts(): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        try {
            val snapshot = firestore.collection(COLLECTION_ROLES)
                .orderBy("karma", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            for (doc in snapshot.documents) {
                val email = doc.id
                val karma = doc.getLong("karma")?.toInt() ?: 0
                val username = doc.getString("username")
                val displayName = if (username.isNullOrBlank()) email.substringBefore("@") else username
                if (karma > 0) {
                    list.add(displayName to karma)
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get top experts", e)
        }
        return list
    }

    suspend fun getUserAwards(email: String): List<String> {
        if (email.isBlank()) return emptyList()
        return try {
            val doc = firestore.collection(COLLECTION_ROLES).document(email).get().await()
            if (doc.exists()) {
                val awards = doc.get("awards") as? List<*>
                awards?.mapNotNull { it as? String } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDisplayedAwards(email: String): List<String> {
        if (email.isBlank()) return emptyList()
        return try {
            val doc = firestore.collection(COLLECTION_ROLES).document(email).get().await()
            if (doc.exists()) {
                val awards = doc.get("displayedAwards") as? List<*>
                awards?.mapNotNull { it as? String } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setDisplayedAwards(email: String, awardIds: List<String>) {
        if (email.isBlank()) return
        try {
            firestore.collection(COLLECTION_ROLES).document(email).set(
                mapOf("displayedAwards" to awardIds.take(7)),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            AppLogger.i(TAG, "Saved displayed awards for $email")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save displayed awards", e)
        }
    }

    suspend fun grantAllAwards(email: String) {
        if (email.isBlank()) return
        try {
            val allAwards = listOf(
                "first_scan", "seeker_of_truth", "mechanic_apprentice", "cyber_diagnost", "ecu_oracle", "system_architect",
                "first_blood", "helpful_neighbor", "voice_of_people", "respected_master", "knowledge_patron", "community_pillar", "garage_legend",
                "eagle_eye", "detail_hunter", "chief_archivist", "omniscient",
                "gate_guard", "base_keeper", "grand_inquisitor"
            )
            firestore.collection(COLLECTION_ROLES).document(email).set(
                mapOf("awards" to allAwards),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            AppLogger.i(TAG, "Granted all awards to $email")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to grant awards", e)
        }
    }

    suspend fun checkAchievements(email: String) {
        if (email.isBlank()) return
        try {
            val roleDoc = firestore.collection(COLLECTION_ROLES).document(email).get().await()
            if (!roleDoc.exists()) return

            val currentAwards = (roleDoc.get("awards") as? List<*>)?.mapNotNull { it as? String }?.toMutableSet() ?: mutableSetOf()
            val karma = roleDoc.getLong("karma")?.toInt() ?: 0
            val approvedCount = roleDoc.getLong("approvedCount")?.toInt() ?: 0
            val addendumsCount = roleDoc.getLong("addendumsCount")?.toInt() ?: 0

            val newAwards = mutableSetOf<String>()

            if (karma > 0) newAwards.add("first_blood")
            if (karma >= 10) newAwards.add("helpful_neighbor")
            if (karma >= 50) newAwards.add("voice_of_people")
            if (karma >= 100) newAwards.add("respected_master")
            if (karma >= 200) newAwards.add("knowledge_patron")
            if (karma >= 500) newAwards.add("community_pillar")
            if (karma >= 1000) newAwards.add("garage_legend")

            if (approvedCount >= 1) newAwards.add("gate_guard")
            if (approvedCount >= 10) newAwards.add("base_keeper")
            if (approvedCount >= 50) newAwards.add("grand_inquisitor")
            
            if (addendumsCount >= 1) newAwards.add("eagle_eye")
            if (addendumsCount >= 5) newAwards.add("detail_hunter")
            if (addendumsCount >= 15) newAwards.add("chief_archivist")
            if (addendumsCount >= 50) newAwards.add("omniscient")

            val localSavedCount = dao.getSavedCount()
            if (localSavedCount >= 1) newAwards.add("first_scan")
            if (localSavedCount >= 5) newAwards.add("seeker_of_truth")
            if (localSavedCount >= 10) newAwards.add("mechanic_apprentice")
            if (localSavedCount >= 20) newAwards.add("cyber_diagnost")
            if (localSavedCount >= 50) newAwards.add("ecu_oracle")
            if (localSavedCount >= 100) newAwards.add("system_architect")

            val toAdd = newAwards - currentAwards
            if (toAdd.isNotEmpty()) {
                currentAwards.addAll(toAdd)
                firestore.collection(COLLECTION_ROLES).document(email)
                    .set(mapOf("awards" to currentAwards.toList()), com.google.firebase.firestore.SetOptions.merge())
                    .await()
                AppLogger.i(TAG, "Granted new awards to $email: $toAdd")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to check achievements", e)
        }
    }
}
