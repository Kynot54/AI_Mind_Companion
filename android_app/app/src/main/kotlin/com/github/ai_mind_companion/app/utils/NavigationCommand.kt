package com.github.ai_mind_companion.app.utils

sealed class NavigationCommand {
    data class To(val destination: String, val userId: String? = null): NavigationCommand()
    object Back: NavigationCommand()
}