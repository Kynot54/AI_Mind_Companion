package com.github.ai_mind_companion.app.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.ai_mind_companion.R
import com.github.ai_mind_companion.app.nav.BottomNavigationItem
import com.github.ai_mind_companion.app.shared.ChatViewModel
import com.github.ai_mind_companion.app.utils.AudioRecorder
import kotlinx.coroutines.launch

@Composable
fun AICompanion(
    navController: NavController,
    modifier:  Modifier = Modifier,
    chatViewModel: ChatViewModel =  viewModel()
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
        mutableIntStateOf(0)
    }

    var isRecording by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    LaunchedEffect(true) {
        chatViewModel.setAudioRecorder(audioRecorder)
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
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .paint(
                        painter = painterResource(R.drawable.living_room_v2_blurred),
                        contentScale = ContentScale.FillBounds
                    )
                    .fillMaxSize()
            ) {
                Box(
                    modifier = modifier
                        .paint(
                            painter = painterResource(id = R.drawable.avatar_v2),
                            sizeToIntrinsics = false,
                            alignment = Alignment.BottomCenter,
                            contentScale = ContentScale.FillWidth
                        )
                ) {
                    Box (
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(25.dp)
                            .size(55.dp)
                            .background(if (isRecording) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary, CircleShape)
                            .paint(
                                painterResource(id = R.drawable.baseline_mic_24),
                                sizeToIntrinsics = true,
                                alignment = Alignment.Center,
                                contentScale = ContentScale.Inside
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isRecording = true
                                        coroutineScope.launch {
                                            chatViewModel.initSpeechRecognizer(speechRecognizer = null)
                                            chatViewModel.startRecording()
                                            chatViewModel.startSpeechToText()
                                        }
                                        tryAwaitRelease()
                                        coroutineScope.launch {
                                            val audioFile = chatViewModel.stopRecording()
                                            chatViewModel.stopSpeechToText()
                                            audioFile?.let {
                                                chatViewModel.sendAudioFileToServer(it)
                                            }
                                            isRecording = false
                                        }
                                    }
                                )
                            }
                    ) {
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAIContent (){
    val mockNavController = rememberNavController()
    AICompanion(mockNavController)
}