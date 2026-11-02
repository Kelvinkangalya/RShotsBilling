package com.example.rshotsbilling.utils

import dev.gitlive.firebase.firestore.FirebaseFirestore

/**
 * A wrapper to handle Firestore access across platforms.
 * Bridges GitLive (Android/JS) and potentially Admin SDK (JVM).
 */
expect object FirestoreWrapper {
    fun getDb(): FirebaseFirestore
}
