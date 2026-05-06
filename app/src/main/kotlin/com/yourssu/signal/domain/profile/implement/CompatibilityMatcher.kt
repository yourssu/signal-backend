package com.yourssu.signal.domain.profile.implement

object CompatibilityMatcher {

    fun match(myProfile: Profile, targetProfile: Profile): CompatibilityLabel? {
        if (!isAgeCompatible(myProfile, targetProfile)) return null
        val mbtiMatch = MbtiCompatibilityTable.isCompatible(myProfile.mbti, targetProfile.mbti)
        val animalMatch = AnimalCompatibilityTable.isCompatible(myProfile, targetProfile)
        return when {
            mbtiMatch && animalMatch -> CompatibilityLabel.PERFECT_MATCH
            mbtiMatch || animalMatch -> CompatibilityLabel.GOOD_MATCH
            else -> null
        }
    }

    private fun isAgeCompatible(myProfile: Profile, targetProfile: Profile): Boolean {
        val (male, female) = if (myProfile.gender == Gender.MALE) myProfile to targetProfile
        else targetProfile to myProfile
        return male.birthYear in (female.birthYear - 3)..female.birthYear
    }
}
