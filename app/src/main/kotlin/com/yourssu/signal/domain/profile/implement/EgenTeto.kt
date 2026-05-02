package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.EgenTetoNotFoundException

enum class EgenTeto {
    EGEN,
    TETO,
    NOT_SELECTED
    ;

    companion object {
        fun of(value: String?): EgenTeto? {
            if (value == null) return null
            return entries.find { it.name == value.uppercase() }
                ?: throw EgenTetoNotFoundException()
        }
    }
}
