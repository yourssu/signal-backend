package com.yourssu.signal.domain.profile.implement

object AnimalCompatibilityTable {

    // 여자 동물상 기준: 내 동물 → 매칭되는 남자 동물
    private val femaleTable = mapOf(
        "고양이" to setOf("강아지", "곰"),
        "강아지" to setOf("고양이", "공룡"),
        "햄스터" to setOf("곰", "햄스터"),
        "여우" to setOf("공룡", "사슴"),
        "토끼" to setOf("사슴", "강아지"),
        "꼬부기" to setOf("햄스터", "고양이"),
    )

    // 남자 동물상 기준: 내 동물 → 매칭되는 여자 동물
    private val maleTable = mapOf(
        "고양이" to setOf("강아지", "꼬부기"),
        "강아지" to setOf("고양이", "토끼"),
        "햄스터" to setOf("꼬부기", "햄스터"),
        "공룡" to setOf("여우", "강아지"),
        "곰" to setOf("햄스터", "고양이"),
        "사슴" to setOf("토끼", "여우"),
    )

    fun isCompatible(myProfile: Profile, targetProfile: Profile): Boolean =
        if (myProfile.gender == Gender.FEMALE)
            femaleTable[myProfile.animal]?.contains(targetProfile.animal) == true
        else
            maleTable[myProfile.animal]?.contains(targetProfile.animal) == true
}
