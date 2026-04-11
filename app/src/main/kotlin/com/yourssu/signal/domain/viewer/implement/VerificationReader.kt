package com.yourssu.signal.domain.viewer.implement

import VerificationCode
import com.yourssu.signal.domain.verification.implement.VerificationRepository
import com.yourssu.signal.domain.verification.implement.Verification
import org.springframework.stereotype.Component

@Component
class VerificationReader(
    private val verificationRepository: VerificationRepository,
) {
    fun findByCode(code: VerificationCode): Verification {
        return verificationRepository.getByCode(code)
    }

    fun existsByCode(code: VerificationCode): Boolean {
        return verificationRepository.existsByCode(code)
    }
}
