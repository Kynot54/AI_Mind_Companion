package com.github.ai_mind_companion.app.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.ai_mind_companion.R
import com.github.ai_mind_companion.app.utils.google.SignInState
import com.github.ai_mind_companion.app.utils.themeGradientBrush
import kotlinx.coroutines.launch

@Composable
fun Login(
    navController: NavController,
    modifier: Modifier = Modifier,
    state: SignInState,
    onSignInClick: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(loginViewModel) {
        loginViewModel.errorEvent.collect { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = themeGradientBrush())
            .padding(75.dp)
    ) {
        LazyColumn(
            modifier = modifier
                .align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_round),
                    contentDescription = "Main Logo"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sign In",
                    color = Color.White,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                LoginField(loginViewModel)
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedButton(
                    onClick = {
                        val email = loginViewModel.accountID.value
                        val password = loginViewModel.passwd.value

                        loginViewModel.signInWithEmailAndPassword(email, password)
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiary),
                    modifier = Modifier
                        .height(40.dp)
                        .width(175.dp),

                    ) {
                    Text(
                        text = "Enter",
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onSignInClick,
                    contentPadding = PaddingValues(0.dp),
                    elevation = null
                ) {
                    val imageRes = if(isSystemInDarkTheme()) {
                        R.drawable.android_dark_rd_si
                    } else {
                        R.drawable.android_light_rd_si
                    }
                    Image(
                        painter = painterResource(
                            id = imageRes
                        ),
                        contentDescription = "Goggle Sign-In"
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Or",
                    color = Color.White,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = { navController.navigate("createAccount") },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiary),
                    modifier = Modifier
                        .height(40.dp)
                        .width(175.dp)
                ) {
                    Text(
                        text = "Create Account",
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontSize = 18.sp
                    )
                }
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen () {
    val onMockSignInClick = {}
    Login(navController = rememberNavController(), onSignInClick = onMockSignInClick, state = SignInState())
}

@Composable
fun LoginField(
    loginViewModel: LoginViewModel
) {
    var uname by remember { mutableStateOf(TextFieldValue(loginViewModel.accountID.value)) }
    TextField(
        value = uname,
        onValueChange = {
            uname = it
            loginViewModel.accountID.value = it.text
        },
        label = { Text(text = "User Email") },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            disabledTextColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
    )
    Spacer(modifier = Modifier.height(20.dp))
    var phrase by remember { mutableStateOf(TextFieldValue(loginViewModel.passwd.value)) }
    TextField(
        value = phrase,
        onValueChange = {
            phrase = it
            loginViewModel.passwd.value = it.text
        },
        visualTransformation = { inputAnnotatedString ->
            TransformedText (
                text = AnnotatedString("·".repeat(inputAnnotatedString.text.length)),
                offsetMapping = OffsetMapping.Identity
            )
        },
        label = { Text(text = "Password") },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            disabledTextColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
    )
}