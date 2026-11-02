package com.example.rshotsbilling.utils

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual object PlatformAuth {
    actual val auth: FirebaseAuth get() = Firebase.auth
    
    actual val service: AuthService get() = object : AuthService {
        override val currentUserEmail: String? get() = Firebase.auth.currentUser?.email
        override val currentUserId: String? get() = Firebase.auth.currentUser?.uid
        
        override fun observeAuthState(): Flow<String?> = 
            Firebase.auth.authStateChanged.map { it?.uid }
            
        override suspend fun signIn(email: String, password: String): String {
            val result = Firebase.auth.signInWithEmailAndPassword(email, password)
            return result.user?.uid ?: throw Exception("Authentication failed: No user ID returned")
        }
        
        override suspend fun signOut() {
            Firebase.auth.signOut()
        }
    }
}
