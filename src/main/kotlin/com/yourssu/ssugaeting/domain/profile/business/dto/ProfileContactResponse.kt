package com.yourssu.ssugaeting.domain.profile.business.dto

import com.yourssu.ssugaeting.domain.profile.implement.Profile

class ProfileContactResponse(
    val profileId: Long? = null,
    val gender: String,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
) {
    companion object {
        fun from(profile: Profile): ProfileContactResponse {
            return ProfileContactResponse(
                profileId = profile.id,
                gender = profile.gender.name,
                animal = profile.animal,
                contact = profile.contact,
                mbti = profile.mbti.name,
                nickname = profile.nickname,
            )
        }
    }
}
