package com.yourssu.signal.domain.profile.implement

object MbtiCompatibilityTable {

    private val table = mapOf(
        "INFP" to setOf("ENFJ", "ENTJ"),
        "ENFP" to setOf("INFJ", "INTJ"),
        "INFJ" to setOf("ENFP", "ENTP"),
        "ENFJ" to setOf("INFP", "ISFP"),
        "INTJ" to setOf("ENFP", "ENTP"),
        "ENTJ" to setOf("INFP", "INTP"),
        "INTP" to setOf("ENTJ", "ESTJ"),
        "ENTP" to setOf("INFJ", "INTJ"),
        "ISFP" to setOf("ENFJ", "ESFJ"),
        "ESFP" to setOf("ISFJ", "ISTJ"),
        "ISTP" to setOf("ESFJ", "ESTJ"),
        "ESTP" to setOf("ISFJ", "ISTJ"),
        "ISFJ" to setOf("ESFP", "ESTP"),
        "ESFJ" to setOf("ISFP", "ISTP"),
        "ISTJ" to setOf("ESFP", "ESTP"),
        "ESTJ" to setOf("INTP", "ISFP"),
    )

    fun isCompatible(my: String, target: String): Boolean =
        table[my]?.contains(target) == true

    fun isValid(mbti: String): Boolean = table.containsKey(mbti)
}
