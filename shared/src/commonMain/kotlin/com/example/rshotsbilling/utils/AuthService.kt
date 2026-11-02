package com.example.rshotsbilling.utils

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic interface for Firebase Authentication operations.
 */
interface AuthService {
    /**
     * Current authenticated user's email, if any.
     */
    val currentUserEmail: String?

    /**
     * Current authenticated user's unique ID (UID), if any.
     */
    val currentUserId: String?

    /**
     * A flow that emits the current user's UID when the authentication state changes.
     * Emits null if the user is signed out.
     */
    fun observeAuthState(): Flow<String?>

    /**
     * Signs in a user with email and password.
     * Returns the UID of the successfully authenticated user.
     * Throws an exception on failure.
     */
    suspend fun signIn(email: String, password: String): String

    /**
     * Signs out the current user.
     */
    suspend fun signOut()
}
