package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

actual fun initializeFirebase() {
    Firebase.initialize(options = FirebaseConfig.options)
    FirebaseStatus.update(FirebaseState.Ready)
}
