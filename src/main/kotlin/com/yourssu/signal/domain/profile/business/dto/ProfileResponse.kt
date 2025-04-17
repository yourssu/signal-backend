package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.domain.Profile

class ProfileResponse(
    val profileId: Long? = null,
    val gender: String,
    val animal: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
) {
    companion object {
        fun from(profile: Profile): ProfileResponse {
            return ProfileResponse(
                profileId = profile.id,
                gender = profile.gender.name,
                animal = profile.animal,
                mbti = profile.mbti,
                nickname = profile.nickname,
                introSentences = profile.introSentences,
            )
        }
    }
}
