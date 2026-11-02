package com.example.rshotsbilling.utils

import dev.gitlive.firebase.firestore.FirebaseFirestore

expect object PlatformFirebase {
    val firestore: FirebaseFirestore
    fun configureSettings(db: FirebaseFirestore)
}
