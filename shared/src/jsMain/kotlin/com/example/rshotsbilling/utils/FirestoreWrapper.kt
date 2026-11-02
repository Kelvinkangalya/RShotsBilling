package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

actual object FirestoreWrapper {
    actual fun getDb(): FirebaseFirestore {
        return Firebase.firestore
    }
}
