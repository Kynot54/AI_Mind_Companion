package com.github.ai_mind_companion.app.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ai_mind_companion.app.utils.UserProfile
import com.github.ai_mind_companion.app.utils.google.SignInResult
import com.github.ai_mind_companion.app.utils.google.SignInState
import com.github.ai_mind_companion.app.utils.google.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("PropertyName")
class CreateAccountViewModel : ViewModel() {
    var accountID = mutableStateOf("")
    var passwd = mutableStateOf("")
    var fname = mutableStateOf("")
    var lname = mutableStateOf("")

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _selectedGender = MutableStateFlow<String?>(null)
    val gender: StateFlow<String?> = _selectedGender.asStateFlow()

    fun updateGender(gender: String) {
        _selectedGender.value = gender
    }
    val _selectedDate = MutableLiveData<String>(null)
    fun updateSelectedDate(date: String) {
        _selectedDate.value = date
    }

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

    private val auth = FirebaseAuth.getInstance()
    fun createAccount (accountID: String, passwd: String, fname: String, lname: String, date: String, gender: String) {
        viewModelScope.launch {
            try {
                // Regex Patterns
                val accountIDPattern =
                    "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z].{2,}\$".toRegex()

                @Suppress("RegExpRedundantEscape")
                val passwdPattern =
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[\\[\\]\\{\\}\\-,\\>=<_?~#+'%^$.@!]).{8,32}$".toRegex()

                val namePattern = "^[A-Za-zÀ-ÖØ-öø-ÿ'-]+(?:\\s[A-Za-zÀ-ÖØ-öø-ÿ'-]+)*$".toRegex()

                // Validate Email && Password
                if (!(accountIDPattern.matches(accountID)) || (!(passwdPattern.matches(passwd)))) {
                    viewModelScope.launch {
                        _errorEvent.emit("Username or Password is Invalid")
                    }
                }
                // Error Check for Valid Firstname and Lastname (Given-name / Sur-name)
                else if (!(namePattern.matches(fname)) || !(namePattern.matches(lname))) {
                    viewModelScope.launch {
                        _errorEvent.emit("Please Enter a Valid First and Last Name")
                    }
                }
                // Else No Error - Create Account
                else {
                    // Create the Email / Password Account
                    val strippedAccountID = accountID.replace("\\s+".toRegex(), "")
                    val strippedPasswd = passwd.replace("\\s+".toRegex(), "")
                    auth.createUserWithEmailAndPassword(strippedAccountID, strippedPasswd)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                user?.sendEmailVerification()?.addOnCompleteListener { verTask ->
                                    if (verTask.isSuccessful) {
                                        val userProfile =
                                            UserProfile(strippedAccountID, fname, lname, date, gender)
                                        FirebaseFirestore.getInstance().collection("users")
                                            .document(user.uid).set(userProfile)
                                            .addOnSuccessListener {
                                                val userId = task.result?.user?.uid
                                                val signInResult = SignInResult(
                                                    data = user.run {
                                                        userId?.let { it1 ->
                                                            UserData(
                                                                userId = it1,
                                                                username = fname,
                                                                profilePictureUrl = null
                                                            )
                                                        }
                                                    },
                                                    errorMessage = null
                                                )
                                                onSignInResult(signInResult)
                                            }.addOnFailureListener { e ->
                                                _errorEvent.tryEmit("Failed to create user profile: ${e.message}")
                                            }
                                    } else {
                                        _errorEvent.tryEmit("Failed to send verification email.")
                                    }
                                }
                            } else {
                                _errorEvent.tryEmit("Failed to create user account: ${task.exception?.message}")
                            }
                        }
                        .addOnFailureListener { e ->
                            _errorEvent.tryEmit("Firebase Auth Error: ${e.message}")
                        }
                }
            }
            catch (e: Exception) {
                _errorEvent.emit(e.message ?:"An Unknown Error has Occurred")
            }
        }
    }
}
