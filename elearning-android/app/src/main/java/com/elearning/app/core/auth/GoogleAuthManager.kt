package com.elearning.app.core.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleAuthManager(private val context: Context) {

    private val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("524576002214-celf4un38v2hdk4a7585rugkrl7gbufr.apps.googleusercontent.com")
        .requestEmail()
        .requestProfile()
        .build()

    val client: GoogleSignInClient = 
        GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent = client.signInIntent

    fun handleSignInResult(data: Intent?): Result<String> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken ?: return Result.failure(Exception("No ID token returned"))
            Result.success(idToken)
        } catch (e: ApiException) {
            Result.failure(e)
        }
    }

    fun signOut() {
        client.signOut()
        client.revokeAccess()
    }
}