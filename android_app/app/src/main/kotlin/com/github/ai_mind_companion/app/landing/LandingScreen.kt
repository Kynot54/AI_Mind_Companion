package com.github.ai_mind_companion.app.landing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.ai_mind_companion.R
import com.github.ai_mind_companion.app.ui.theme.AIMindCompanionTheme
import com.github.ai_mind_companion.app.utils.themeGradientBrush

@Composable
fun LandingScreen(
    navContoller: NavController,
    modifier: Modifier = Modifier
) {
    Box (
        modifier = modifier
            .fillMaxSize()
            .background(brush = themeGradientBrush())
            .padding(50.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Image(painter = painterResource(id = R.drawable.ic_launcher_round), contentDescription = "Main Logo")
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "AI Mind Companion",
                color = Color.White,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "Voice-Interactive AI Companion for Early Dementia Detection and Supportive Interaction",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier=Modifier.height(75.dp))
            OutlinedButton(onClick = { navContoller.navigate("logIn")},
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onTertiary),
                modifier = Modifier
                    .height(65.dp)
                    .width(200.dp),

                ) {
                Text(
                    text ="Get Started",
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewLandingScreen () {
    AIMindCompanionTheme {
     LandingScreen(navContoller = rememberNavController())
    }
}