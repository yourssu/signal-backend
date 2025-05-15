package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.domain.Profile

class ProfileContactResponse(
    val profileId: Long,
    val gender: String,
    val department: String,
    val birthYear: Int,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
) {
    companion object {
        fun from(profile: Profile): ProfileContactResponse {
            return ProfileContactResponse(
                profileId = profile.id!!,
                gender = profile.gender.name,
                department = profile.department,
                birthYear = profile.birthYear,
                animal = profile.animal,
                contact = profile.contact,
                mbti = profile.mbti,
                nickname = profile.nickname,
                introSentences = profile.introSentences,
            )
        }
    }
}
