package com.yourssu.signal.domain.profile.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile

class ProfileCreatedCommand(
    val uuid: String,
    val gender: String,
    val department: String,
    val birthYear: Int,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
    val school: String
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun toDomain(): Profile {
        return Profile(
            uuid = Uuid(uuid),
            gender = Gender.of(gender),
            department = department,
            birthYear = birthYear,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences,
            school = school,
        )
    }
}
