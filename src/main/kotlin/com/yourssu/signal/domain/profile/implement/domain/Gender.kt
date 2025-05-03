package com.yourssu.signal.domain.profile.implement.domain

import com.yourssu.signal.domain.profile.implement.exception.GenderNotFoundException

enum class Gender {
    FEMALE,
    MALE
    ;

    companion object {
        fun of(value: String): Gender {
            return entries.stream()
                .filter { it.name == value.uppercase() }
                .findFirst()
                .orElseThrow { GenderNotFoundException() }
        }
    }

    fun opposite(): Gender {
        return when (this) {
            FEMALE -> MALE
            MALE -> FEMALE
        }
    }
}
