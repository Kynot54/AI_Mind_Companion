package com.github.ai_mind_companion.app.logout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.ai_mind_companion.app.nav.BottomNavigationItem
import com.github.ai_mind_companion.app.utils.google.UserData
import com.github.ai_mind_companion.app.utils.themeGradientBrush

@Composable
fun LogOut(
    navController: NavController,
    userData: UserData?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
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
        mutableIntStateOf(3)
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
                    modifier = modifier
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    OutlinedButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiary),
                        modifier = Modifier
                            .height(40.dp)
                            .width(175.dp),

                        ) {
                        Text(
                            text ="Log Out",
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLogOutContent () {
    val mockSignOut = {}
    LogOut(navController = rememberNavController(), userData = null, onSignOut = mockSignOut)
}
