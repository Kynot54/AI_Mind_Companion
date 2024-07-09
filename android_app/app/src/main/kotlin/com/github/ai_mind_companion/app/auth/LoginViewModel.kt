package com.github.ai_mind_companion.app.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ai_mind_companion.app.utils.google.SignInResult
import com.github.ai_mind_companion.app.utils.google.SignInState
import com.github.ai_mind_companion.app.utils.google.UserData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    var accountID = mutableStateOf("")
    var passwd = mutableStateOf("")

    private val auth: FirebaseAuth = Firebase.auth

    private val _logInStatus = MutableLiveData<Boolean?>()
    val logInStatus: LiveData<Boolean?> = _logInStatus

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        ) }
    }

    fun resetState() {
        _state.update { SignInState() }
    }

    fun signInWithEmailAndPassword(accountID: String, passwd: String) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(accountID, passwd).await()
                val user = result.user
                val userId = user?.uid ?: throw Exception("UID not found")
                val name = checkUserProfileExists(userId)
                val signInResult = SignInResult(
                    data = user.run {
                        UserData(
                            userId = user.uid,
                            username = name,
                            profilePictureUrl = null
                        )
                    },
                    errorMessage = null
                )
                onSignInResult(signInResult)
            } catch (e: Exception) {
                _errorEvent.emit(e.message ?:"An Unknown Error has Occurred")
            }
        }
    }

    private suspend fun checkUserProfileExists(userId: String): String? {
        return try {
            val document =
                FirebaseFirestore.getInstance().collection("userProfiles").document(userId).get()
                    .await()
            if (document.exists()) {
                document.getString("firstname")
            } else {
                _errorEvent.emit("User Does Not Exist.")
                null
            }
        } catch (e: Exception) {
            _errorEvent.emit("Failed to check user profile: ${e.message}")
            null
        }
    }
}
