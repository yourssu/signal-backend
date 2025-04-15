package com.yourssu.ssugaeting.domain.profile.business.command

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.domain.Gender
import com.yourssu.ssugaeting.domain.profile.implement.domain.Mbti
import com.yourssu.ssugaeting.domain.profile.implement.domain.Profile

class ProfileCreatedCommand(
    val uuid: String?,
    val gender: String,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
) {
    fun toDomain(): Profile {
        if (uuid != null) {
            return Profile(
                uuid = Uuid(uuid),
                gender = Gender.of(gender),
                animal = animal,
                contact = contact,
                mbti = Mbti.of(mbti),
                nickname = nickname,
            )
        }
        return Profile.ofNewProfile(
            gender = Gender.of(gender),
            animal = animal,
            contact = contact,
            mbti = Mbti.of(mbti),
            nickname = nickname,
        )
    }
}
