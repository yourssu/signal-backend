package com.yourssu.ssugaeting.domain.profile.implement

class Profile(
    val id: Long? = null,
    val gender: Gender,
    val animal: String,
    val contact: String,
    val mbti: Mbti,
    val nickname: String,
) {
}
