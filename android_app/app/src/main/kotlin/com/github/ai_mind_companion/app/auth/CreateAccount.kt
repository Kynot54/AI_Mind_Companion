package com.github.ai_mind_companion.app.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
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
import com.github.ai_mind_companion.app.utils.Gender
import com.github.ai_mind_companion.app.utils.getGenderList
import com.github.ai_mind_companion.app.utils.google.SignInState
import com.github.ai_mind_companion.app.utils.themeGradientBrush
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccount(
    navController: NavController,
    modifier: Modifier = Modifier,
    state: SignInState,
    createAccountViewModel: CreateAccountViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf("Select Date of Birth") }
    var showDatePicker by remember { mutableStateOf(false) }
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

    LaunchedEffect(createAccountViewModel) {
        createAccountViewModel.errorEvent.collect { errorMessage ->
            scope.launch{
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Box (
        modifier
            .background(brush = themeGradientBrush())
            .fillMaxSize()
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        LazyColumn(
            modifier = Modifier
                .padding(75.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_round),
                    contentDescription = "Main Logo"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                CreateAccountField(
                    createAccountViewModel,
                    snackbarHostState,
                    date,
                    onDateChange = { newDate ->
                        date = newDate
                    },
                    onShowDatePicker = { showDatePicker = true })
                if (showDatePicker) {
                    DOBPickerDialog(
                        onDateSelected = { selectedDate ->
                            date = selectedDate
                            showDatePicker = false
                            createAccountViewModel.updateSelectedDate(selectedDate)
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = {

                        val email = createAccountViewModel.accountID.value
                        val password = createAccountViewModel.passwd.value
                        val firstName = createAccountViewModel.fname.value
                        val lastName = createAccountViewModel.lname.value
                        val dob = createAccountViewModel._selectedDate.value ?: ""
                        val gender = createAccountViewModel.gender.value ?: ""

                        createAccountViewModel.createAccount(
                            email,
                            password,
                            firstName,
                            lastName,
                            dob,
                            gender
                        )
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiary),
                    modifier = Modifier
                        .height(40.dp)
                        .width(175.dp),

                    ) {
                    Text(
                        text = "Create Account",
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateAccountScreen () {
        CreateAccount(state = SignInState(), navController = rememberNavController())
}

@Composable
fun CreateAccountField(
    createAccountViewModel: CreateAccountViewModel,
    @Suppress("UNUSED_PARAMETER") snackbarHostState: SnackbarHostState,
    selectedDate: String,
    @Suppress("UNUSED_PARAMETER") onDateChange: (String) -> Unit,
    onShowDatePicker: () -> Unit
) {
    var uname by remember { mutableStateOf(TextFieldValue(createAccountViewModel.accountID.value)) }
    TextField(
        value = uname,
        onValueChange = {
            uname = it
            createAccountViewModel.accountID.value = it.text
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
    Spacer(modifier = Modifier.height(26.dp))
    var passwd by remember { mutableStateOf(TextFieldValue(createAccountViewModel.passwd.value)) }
    TextField(
        value = passwd,
        onValueChange = {
            passwd = it
            createAccountViewModel.passwd.value = it.text
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
    Spacer(modifier = Modifier.height(26.dp))
    var fname by remember { mutableStateOf(TextFieldValue(createAccountViewModel.fname.value)) }
    TextField(
        value = fname,
        onValueChange = {
            fname = it
            createAccountViewModel.fname.value = it.text
        },
        label = { Text(text = "First Name") },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            disabledTextColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
    )
    Spacer(modifier = Modifier.height(26.dp))
    var lname by remember { mutableStateOf(TextFieldValue(createAccountViewModel.lname.value)) }
    TextField(
        value = lname,
        onValueChange = {
            lname = it
            createAccountViewModel.lname.value = it.text
        },
        label = { Text(text = "Last Name") },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            disabledTextColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
    )
    Spacer(modifier = Modifier.height(26.dp))
    OutlinedButton(onClick = onShowDatePicker,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonColors(
            disabledContainerColor = Color.Transparent,
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3F),
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = Color.White
        ),
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = selectedDate,
            style = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
        )
    }
    Spacer(modifier = Modifier.height(26.dp))
    var gender by remember { mutableStateOf(Gender.Male.name) }
    GenderDropDownMenu{ selectedGender ->
        gender = selectedGender
        createAccountViewModel.updateGender(selectedGender)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropDownMenu(gender: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = getGenderList().map { it.name }
        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember { mutableStateOf(options[0]) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                shape = RoundedCornerShape(20.dp),
                value = selectedOptionText,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    disabledTextColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = MaterialTheme.colorScheme.onTertiary,
                    focusedTextColor = MaterialTheme.colorScheme.onTertiary
                ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(
                            selectionOption,
                            color = MaterialTheme.colorScheme.onTertiary) },
                        onClick = {
                            selectedOptionText = selectionOption
                            gender(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun DOBPickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= System.currentTimeMillis()
        }
    })

    val selectedDate = datePickerState.selectedDateMillis?.let {
        // Current Bug in Android Studio had to add a Day in Milliseconds to get the Correct Date
        convertMillisToDate(it + 86400000)
    } ?: ""

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                onDateSelected(selectedDate)
                onDismiss()
            }

            ) {
                Text(
                    text = "Ok",
                    style = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
                )
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text(
                    text = "Cancel",
                    style = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                dayContentColor = MaterialTheme.colorScheme.onTertiary,
                todayContentColor = MaterialTheme.colorScheme.onPrimary,
                todayDateBorderColor = MaterialTheme.colorScheme.onPrimary,
                dateTextFieldColors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        )
    }
}


private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    return formatter.format(Date(millis))
}