package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

actual object PlatformFirebase {
    actual val firestore: FirebaseFirestore get() {
        if (FirebaseStatus.state.value != FirebaseState.Ready) {
            DesktopLogger.log("WARNING: [PlatformFirebase] Firestore accessed before initialization!")
        }
        return Firebase.firestore
    }

    actual fun configureSettings(db: FirebaseFirestore) {
        DesktopLogger.log("DEBUG: [PlatformFirebase] Configuring JVM Firestore Settings...")
        // On JVM, GitLive Firebase uses the Java SDK or a custom implementation.
        // We avoid reaching for android-specific classes here.
        DesktopLogger.log("DEBUG: [PlatformFirebase] Settings configuration skipped on JVM to avoid Android leak.")
    }
}
