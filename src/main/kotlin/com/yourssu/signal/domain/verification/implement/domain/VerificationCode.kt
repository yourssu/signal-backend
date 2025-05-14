package com.yourssu.signal.domain.verification.implement.domain

import com.yourssu.signal.domain.verification.implement.exception.InvalidVerificationCode

data class VerificationCode(
    val value: Int,
) {
    companion object {
        fun from(name: String): VerificationCode {
            val code = name.toIntOrNull() ?: throw InvalidVerificationCode()
            return VerificationCode(code)
        }
    }
}
