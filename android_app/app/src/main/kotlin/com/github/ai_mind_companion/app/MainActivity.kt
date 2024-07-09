package com.github.ai_mind_companion.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.ai_mind_companion.app.ai.AICompanion
import com.github.ai_mind_companion.app.audioPermission.AudioPermissionScreen
import com.github.ai_mind_companion.app.auth.CreateAccount
import com.github.ai_mind_companion.app.auth.CreateAccountViewModel
import com.github.ai_mind_companion.app.auth.Login
import com.github.ai_mind_companion.app.auth.LoginViewModel
import com.github.ai_mind_companion.app.chat.ChatLog
import com.github.ai_mind_companion.app.landing.LandingScreen
import com.github.ai_mind_companion.app.logout.LogOut
import com.github.ai_mind_companion.app.settings.Settings
import com.github.ai_mind_companion.app.ui.theme.AIMindCompanionTheme
import com.github.ai_mind_companion.app.utils.google.GoogleAuthUIClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun CheckUserAuthState(navController: NavController) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    LaunchedEffect (key1 = currentUser) {
        if (currentUser != null) {
            navController.navigate("AI") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
        else {
            navController.navigate("landingScreen") {
                popUpTo(0)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    private val googleAuthUIClient by lazy {
        GoogleAuthUIClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            account.idToken?.let { Log.d("Signed In: ", it) }
        } else {
            Log.d("Signed In: ", "Not signed in")
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            AIMindCompanionTheme {
                val navController = rememberNavController()

                CheckUserAuthState(navController)
                NavHost(navController = navController, startDestination = "audioPermission") {
                    composable("audioPermission") { AudioPermissionScreen(navController)}
                    composable("landingScreen") { LandingScreen(navController) }
                    composable("logIn") {
                        val viewModel = viewModel<LoginViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        LaunchedEffect(key1 = Unit) {
                            if (googleAuthUIClient.getSignedInUser() != null) {
                                navController.navigate("AI")
                            }
                        }
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult(),
                            onResult = { result ->
                                if(result.resultCode == RESULT_OK) {
                                    lifecycleScope.launch {
                                        val signInResult = googleAuthUIClient.signInWithIntent(
                                            intent = result.data ?: return@launch
                                        )
                                        viewModel.onSignInResult(signInResult)
                                    }
                                }
                            }
                        )

                        LaunchedEffect(key1 = state.isSignInSuccessful) {
                            if(state.isSignInSuccessful) {
                                Toast.makeText(
                                    applicationContext,
                                    "Sign in successful",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate("AI")
                                viewModel.resetState()
                            }
                        }

                        Login(
                            navController,
                            state = state,
                            onSignInClick = {
                                lifecycleScope.launch {
                                    val signInIntentSender = googleAuthUIClient.signIn()
                                    launcher.launch(
                                        IntentSenderRequest.Builder(
                                            signInIntentSender ?: return@launch
                                        ).build()
                                    )
                                }
                            }
                        )
                    }
                    composable("createAccount") {
                        val viewModel = viewModel<CreateAccountViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        LaunchedEffect(key1 = Unit) {
                            if (googleAuthUIClient.getSignedInUser() != null) {
                                navController.navigate("AI")
                            }
                        }

                        LaunchedEffect(key1 = state.isSignInSuccessful) {
                            if(state.isSignInSuccessful) {
                                Toast.makeText(
                                    applicationContext,
                                    "Sign in successful",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate("AI")
                                viewModel.resetState()
                            }
                        }

                        CreateAccount(navController, state = state)
                    }
                    composable("AI") {AICompanion(navController) }
                    composable("Log"){ ChatLog(navController)}
                    composable("Settings") { Settings(navController) }
                    composable("Logout") {
                        LogOut(
                            navController,
                            userData = googleAuthUIClient.getSignedInUser(),
                            onSignOut = {
                                lifecycleScope.launch {
                                    googleAuthUIClient.signOut()
                                    Toast.makeText(
                                        applicationContext,
                                        "Signed out",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                val popped =navController.popBackStack("logIn", false)
                                if (!popped) {
                                    // If the LogIn is not on the back stack; Make this the new default start location
                                    navController.navigate("logIn") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}