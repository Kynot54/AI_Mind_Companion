package com.github.ai_mind_companion.app.utils

data class Message(
    val author: String?= null,
    val body: String?= null,
    val type: MessageType?= null,
    val timestamp: Long = System.currentTimeMillis()
)
