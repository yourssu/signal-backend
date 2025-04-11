package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.profile.implement.exception.NicknameLengthViolatedException

private const val NICKNAME_MAXIMUM_LENGTH = 32

class Profile(
    val id: Long? = null,
    val gender: Gender,
    val animal: String,
    val contact: String,
    val mbti: Mbti,
    val nickname: String,
) {
    init {
        if (nickname.length > NICKNAME_MAXIMUM_LENGTH) {
            throw NicknameLengthViolatedException()
        }
    }
}
