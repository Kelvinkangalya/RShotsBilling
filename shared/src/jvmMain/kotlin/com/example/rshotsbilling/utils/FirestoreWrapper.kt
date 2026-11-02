package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

actual object FirestoreWrapper {
    actual fun getDb(): FirebaseFirestore {
        DesktopLogger.log("DEBUG: [Firestore] FirestoreWrapper.getDb() called.")
        if (FirebaseStatus.state.value != FirebaseState.Ready) {
            DesktopLogger.log("DEBUG: [Firestore] FAILED - Firebase not initialized.")
            throw IllegalStateException("Firebase must be initialized before accessing Firestore on JVM.")
        }
        return Firebase.firestore
    }
}
