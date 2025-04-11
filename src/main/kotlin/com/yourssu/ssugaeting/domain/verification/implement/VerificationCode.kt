package com.yourssu.ssugaeting.domain.verification.implement

import kotlin.random.Random

private const val MAXIMUM_VERIFICATION_CODE = 10000

class VerificationCode(
    val value: Int,
) {
    companion object {
        fun generateRandom(): VerificationCode {
            return VerificationCode(value = Random.nextInt(MAXIMUM_VERIFICATION_CODE))
        }
    }
}
