package com.github.ai_mind_companion.app.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.ai_mind_companion.app.nav.BottomNavigationItem
import com.github.ai_mind_companion.app.shared.ChatViewModel
import com.github.ai_mind_companion.app.utils.MessageCard
import com.github.ai_mind_companion.app.utils.themeGradientBrush

@Composable
fun ChatLog(
    navController: NavController,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = viewModel()
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
        mutableIntStateOf(1)
    }

    val userId = chatViewModel.fetchUserId()
    LaunchedEffect(key1 = true) {
        if (userId != null) {
            chatViewModel.fetchMessages(userId)
        }
    }

    Surface{
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
                            }
                        )
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
                LazyColumn {
                    val messages = chatViewModel.messages
                    items(messages) { message ->
                        MessageCard(message)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChatLogContent () {
    val mockNavController = rememberNavController()
    ChatLog(mockNavController)
}