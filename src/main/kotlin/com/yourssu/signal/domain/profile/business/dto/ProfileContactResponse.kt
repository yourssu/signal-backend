package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.domain.Profile

class ProfileContactResponse(
    val profileId: Long,
    val uuid: String,
    val gender: String,
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
                uuid = profile.uuid.value,
                gender = profile.gender.name,
                animal = profile.animal,
                contact = profile.contact,
                mbti = profile.mbti,
                nickname = profile.nickname,
                introSentences = profile.introSentences,
            )
        }
    }
}
