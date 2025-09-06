package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.Profile

class MyProfileResponse(
    val profileId: Long,
    val uuid: String,
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
        fun from(profile: Profile): MyProfileResponse {
            return MyProfileResponse(
                profileId = profile.id!!,
                uuid = profile.uuid.value,
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
