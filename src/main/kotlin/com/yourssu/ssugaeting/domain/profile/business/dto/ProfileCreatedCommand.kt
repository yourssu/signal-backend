package com.yourssu.ssugaeting.domain.profile.business.dto

import com.yourssu.ssugaeting.domain.profile.implement.Gender
import com.yourssu.ssugaeting.domain.profile.implement.Mbti
import com.yourssu.ssugaeting.domain.profile.implement.Profile

class ProfileCreatedCommand(
    val gender: String,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
) {
    fun toDomain(): Profile {
        return Profile(
            gender = Gender.of(gender),
            animal = animal,
            contact = contact,
            mbti = Mbti.of(mbti),
            nickname = nickname,
        )
    }
}
