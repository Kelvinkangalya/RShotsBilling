package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

actual object PlatformFirebase {
    actual val firestore: FirebaseFirestore get() = Firebase.firestore
    actual fun configureSettings(db: FirebaseFirestore) {
        // Android settings handled by FirebaseApp typically
    }
}
