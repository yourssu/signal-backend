package com.yourssu.ssugaeting.domain.profile.implement

enum class Mbti {
    ISTJ,
    ISFJ,
    INFJ,
    INTJ,
    ISTP,
    ISFP,
    INFP,
    INTP,
    ESTP,
    ESFP,
    ENFP,
    ENTP,
    ESTJ,
    ESFJ,
    ENFJ,
    ENTJ;

    companion object {
        fun fromString(value: String): Mbti {
            return valueOf(value.uppercase())
        }
    }
}
