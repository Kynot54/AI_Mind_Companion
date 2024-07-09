package com.github.ai_mind_companion.app.utils

enum class Gender {
    Male,
    Female
}

fun getGenderList(): List<Gender> {
    val genderList = mutableListOf<Gender>()

    genderList.add(Gender.Male)
    genderList.add(Gender.Female)

    return genderList
}