package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.CompatibilityMatcher
import com.yourssu.signal.domain.profile.implement.Profile

class ProfileResponse(
    val profileId: Long,
    val gender: String,
    val department: String,
    val birthYear: Int,
    val animal: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
    val school: String,
    val egenTeto: String? = null,
    val compatibilityLabel: String? = null,
) {
    companion object {
        fun from(profile: Profile, myProfile: Profile? = null): ProfileResponse {
            return ProfileResponse(
                profileId = profile.id!!,
                gender = profile.gender.name,
                department = profile.department,
                birthYear = profile.birthYear,
                animal = profile.animal.name,
                mbti = profile.mbti,
                nickname = profile.nickname,
                introSentences = profile.introSentences,
                school = profile.school,
                egenTeto = profile.egenTeto?.name,
                compatibilityLabel = myProfile?.let { CompatibilityMatcher.match(it, profile)?.message },
            )
        }
    }
}
