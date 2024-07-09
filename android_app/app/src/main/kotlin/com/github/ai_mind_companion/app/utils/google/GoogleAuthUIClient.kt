package com.github.ai_mind_companion.app.utils.google

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.github.ai_mind_companion.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUIClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth
    suspend fun signIn(): IntentSender? {
        val signInRequest = buildSignInRequest()
        return try {
            val result = oneTapClient.beginSignIn(signInRequest).await()
            result.pendingIntent.intentSender
        } catch(e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
    }

    suspend fun signInWithIntent(intent: Intent?): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            val userData = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                }
            if (userData != null) {
                // Update Firestore with the user's information
                createUserDocument(userData)
            }
            SignInResult(data = userData, errorMessage = null)
        } catch(e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data =null,
                errorMessage = e.message
            )
        }
    }

    private suspend fun createUserDocument(userData: UserData) {
        val db = FirebaseFirestore.getInstance()
        val userMap = hashMapOf(
            "userId" to userData.userId,
            "username" to userData.username,
            "profilePictureUrl" to userData.profilePictureUrl,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(userData.userId).set(userMap)
            .await()
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }
fun getSignedInUser(): UserData? = auth.currentUser?.run {
    UserData(
        userId = uid,
        username = displayName,
        profilePictureUrl = photoUrl?.toString()
    )
}

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
            )
            .build()
    }

}
