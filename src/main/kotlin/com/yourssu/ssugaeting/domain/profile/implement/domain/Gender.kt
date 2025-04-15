package com.yourssu.ssugaeting.domain.profile.implement.domain

import com.yourssu.ssugaeting.domain.profile.implement.exception.GenderNotFoundException

enum class Gender {
    FEMALE,
    MALE
    ;

    companion object {
        fun of(value: String): Gender {
            return entries.stream()
                .filter { it.name == value.uppercase() }
                .findFirst()
                .orElseThrow{ GenderNotFoundException() }
        }
    }
}
