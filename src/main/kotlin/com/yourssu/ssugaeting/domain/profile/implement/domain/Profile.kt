package com.yourssu.ssugaeting.domain.profile.implement.domain

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.exception.NicknameLengthViolatedException


private const val NICKNAME_MAXIMUM_LENGTH = 10

class Profile(
    val id: Long? = null,
    val uuid : Uuid,
    val gender: Gender,
    val animal: String,
    val contact: String,
    val mbti: Mbti,
    val nickname: String,
) {
    init {
        if (nickname.isEmpty() || nickname.length > NICKNAME_MAXIMUM_LENGTH) {
            throw NicknameLengthViolatedException()
        }
    }

    companion object {
        fun ofNewProfile(gender: Gender, animal: String, contact: String, mbti: Mbti, nickname: String): Profile {
            return Profile(
                uuid = Uuid.randomUUID(),
                gender = gender,
                animal = animal,
                contact = contact,
                mbti = mbti,
                nickname = nickname,
            )
        }
    }
}
