package com.example.autoelectricai.utils

import com.google.firebase.auth.FirebaseAuth

object AuthUtils {
    val currentUserEmail: String
        get() {
            val user = FirebaseAuth.getInstance().currentUser ?: return ""
            if (!user.email.isNullOrBlank()) return user.email!!
            if (user.uid.startsWith("tg_")) return "${user.uid}@telegram.auth"
            return ""
        }
}
