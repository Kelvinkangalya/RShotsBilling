package com.example.rshotsbilling.utils

import dev.gitlive.firebase.auth.FirebaseAuth

expect object PlatformAuth {
    val auth: FirebaseAuth
    val service: AuthService
}
