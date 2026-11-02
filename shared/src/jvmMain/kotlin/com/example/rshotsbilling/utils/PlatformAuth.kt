package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual object PlatformAuth {
    actual val auth: FirebaseAuth get() {
        if (FirebaseStatus.state.value != FirebaseState.Ready) {
            DesktopLogger.log("WARNING: [PlatformAuth] Auth accessed before initialization!")
        }
        return Firebase.auth
    }

    actual val service: AuthService get() = object : AuthService {
        override val currentUserEmail: String? get() = try { auth.currentUser?.email } catch (e: Exception) { null }
        override val currentUserId: String? get() = try { auth.currentUser?.uid } catch (e: Exception) { null }
        
        override fun observeAuthState(): Flow<String?> = 
            auth.authStateChanged.map { it?.uid }
            
        override suspend fun signIn(email: String, password: String): String {
            val result = auth.signInWithEmailAndPassword(email, password)
            return result.user?.uid ?: throw Exception("Authentication failed: No user ID returned")
        }
        
        override suspend fun signOut() {
            auth.signOut()
        }
    }
}
