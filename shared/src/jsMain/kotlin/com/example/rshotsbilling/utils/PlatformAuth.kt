package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth

actual object PlatformAuth {
    actual val auth: FirebaseAuth get() = Firebase.auth
}
