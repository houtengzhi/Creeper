package com.cloud.creeper.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 *
 * Created by cloud on 2024/6/6.
 */
class AuthRepos constructor(private val auth: FirebaseAuth) {

    fun getAuthState() = callbackFlow {
        val stateListener = AuthStateListener {
            trySend(it.currentUser)
        }
        auth.addAuthStateListener(stateListener)
        awaitClose {
            auth.removeAuthStateListener(stateListener)
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun signOut() {
        auth.signOut()
    }

}