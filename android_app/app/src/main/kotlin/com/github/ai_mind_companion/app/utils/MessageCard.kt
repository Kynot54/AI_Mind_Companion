package com.github.ai_mind_companion.app.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.ai_mind_companion.R

@Composable
fun MessageCard(msg: Message) {
    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(all = 8.dp),
        horizontalArrangement =  if (msg.type == MessageType.USER) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (msg.type == MessageType.BOT) Image(
            painter = painterResource(R.drawable.avatar_v2),
            contentDescription = "background_image",
            modifier = Modifier
                // Set Image Size to 40dp
                .size(40.dp)
                // Clip Image to Be Shaped as a Circle
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
        )

        // Add a Horizontal Space Between the Image and the Column
        Spacer(modifier = Modifier.width(8.dp))

        // Keep Track if Message is Expanded or Not
        var isExpanded by remember { mutableStateOf(false) }
        // surface color will be updated based on what is expanded
        val surfaceColor by animateColorAsState (
            if (isExpanded && msg.type == MessageType.BOT)
                MaterialTheme.colorScheme.surface
            else if (!isExpanded && msg.type == MessageType.BOT)
                MaterialTheme.colorScheme.secondary
            else if (isExpanded && msg.type == MessageType.USER)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primary,

            label = "AsState"
        )
        Column (modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            msg.author?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            // Add a Vertical Space Between the Author and the Message Texted
            Spacer(modifier = Modifier.height(4.dp))

            Surface (
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                // gradual change of Surface Color
                color = surfaceColor,
                // gradual Change in Surface Size
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                msg.body?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(all = 4.dp),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}