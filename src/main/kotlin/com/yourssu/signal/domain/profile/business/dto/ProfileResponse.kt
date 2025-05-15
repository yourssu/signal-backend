package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.domain.Profile

class ProfileResponse(
    val profileId: Long,
    val gender: String,
    val department: String,
    val birthYear: Int,
    val animal: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
) {
    companion object {
        fun from(profile: Profile): ProfileResponse {
            return ProfileResponse(
                profileId = profile.id!!,
                gender = profile.gender.name,
                department = profile.department,
                birthYear = profile.birthYear,
                animal = profile.animal,
                mbti = profile.mbti,
                nickname = profile.nickname,
                introSentences = profile.introSentences,
            )
        }
    }
}
