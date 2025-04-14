package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.profile.implement.exception.MbtiNotFoundException

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
    ENTJ
    ;

    companion object {
        fun of(value: String): Mbti {
            return entries.stream()
                .filter { it.name == value.uppercase() }
                .findFirst()
                .orElseThrow{ MbtiNotFoundException() }
        }
    }
}
