package com.github.ai_mind_companion.app.utils

import com.google.firebase.Timestamp

data class UserProfile(
    val accountID: String,
    val firstname: String,
    val lastName: String,
    val dateOfBirth: String,
    val gender: String,
    val accountCreationDate: Timestamp = Timestamp.now()
)
