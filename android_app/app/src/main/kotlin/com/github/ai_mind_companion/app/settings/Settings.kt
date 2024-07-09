package com.github.ai_mind_companion.app.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.ai_mind_companion.app.nav.BottomNavigationItem
import com.github.ai_mind_companion.app.shared.ChatViewModel
import com.github.ai_mind_companion.app.utils.ConfirmDeleteDialog
import com.github.ai_mind_companion.app.utils.TrustedContact
import com.github.ai_mind_companion.app.utils.themeGradientBrush

@Composable
fun Settings(
    navController: NavController,
    modifier: Modifier = Modifier,
    chatViewModel : ChatViewModel = viewModel()
) {

    val items = listOf(
        BottomNavigationItem(
            title = "AI",
            selectedIcon = Icons.Outlined.AccountCircle,
            unselectedIcon = Icons.Filled.AccountCircle,
            hasNews = false
        ),
        BottomNavigationItem(
            title = "Log",
            selectedIcon = Icons.AutoMirrored.Outlined.List,
            unselectedIcon = Icons.AutoMirrored.Filled.List,
            hasNews = false
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Outlined.Settings,
            unselectedIcon = Icons.Filled.Settings,
            hasNews = false
        ),
        BottomNavigationItem(
            title = "Logout",
            selectedIcon = Icons.AutoMirrored.Outlined.ExitToApp,
            unselectedIcon = Icons.AutoMirrored.Filled.ExitToApp,
            hasNews = false
        )
    )

    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(2)
    }

    Surface {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = modifier
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(selected = selectedItemIndex == index,
                            onClick = {
                                selectedItemIndex = index
                                navController.navigate(item.title)
                            },
                            label = {
                                Text(item.title)
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount != null) {
                                            Badge {
                                                Text(text = item.badgeCount.toString())
                                            }
                                        } else if (item.hasNews) {
                                            Badge()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            })
                    }
                }
            }
        ) {
            innerPadding ->
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .background(brush = themeGradientBrush())
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    val userId = chatViewModel.fetchUserId()
                    var showDialog by remember { mutableStateOf(false) }
                    var trustedContactName by remember { mutableStateOf(TextFieldValue(chatViewModel.contactName.value)) }
                    var contactInfo by remember { mutableStateOf(TextFieldValue(chatViewModel.contactEmail.value)) }
                    Text(
                        text = "Add a Trusted Contact",
                        color = Color.White,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    TextField(
                        value = trustedContactName,
                        onValueChange = {
                            trustedContactName = it
                            chatViewModel.contactName.value = it.text
                        },
                        label = { Text(text = "Trusted Contact Name") },
                        shape = RoundedCornerShape(40.dp),
                        colors = TextFieldDefaults.colors(
                            disabledTextColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    TextField(
                        value = contactInfo,
                        onValueChange = {
                            contactInfo = it
                            chatViewModel.contactEmail.value = it.text
                        },
                        label = { Text(text = "Trusted Contact Email") },
                        shape = RoundedCornerShape(40.dp),
                        colors = TextFieldDefaults.colors(
                            disabledTextColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onTertiary)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    OutlinedButton(
                        onClick = {
                            val contactName = chatViewModel.contactName.value
                            val contactEmail = chatViewModel.contactEmail.value
                            val trustedContact = TrustedContact(contactName,contactEmail)
                            chatViewModel.addTrustedContact(trustedContact)
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
                    Spacer(modifier = Modifier.height(200.dp))
                    Button(
                        modifier = Modifier
                            .height(75.dp)
                            .width(300.dp),
                        onClick = { showDialog = true }
                    ) {
                        Text(
                            text ="Delete Message History",
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (showDialog) {
                        ConfirmDeleteDialog(onConfirm = {
                            if (userId != null) {
                                chatViewModel.deleteMessages(userId)
                            }
                        }, onDismiss = {
                            showDialog = false
                        })
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsContent () {
    val mockNavController = rememberNavController()
    Settings(mockNavController)
}