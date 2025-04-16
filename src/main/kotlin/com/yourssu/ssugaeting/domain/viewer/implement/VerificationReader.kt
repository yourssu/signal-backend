package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.domain.verification.implement.VerificationRepository
import com.yourssu.ssugaeting.domain.verification.implement.domain.Verification
import com.yourssu.ssugaeting.domain.verification.implement.domain.VerificationCode
import org.springframework.stereotype.Component

@Component
class VerificationReader(
    private val verificationRepository: VerificationRepository,
) {
    fun findByCode(code: VerificationCode): Verification {
        return verificationRepository.getByCode(code)
    }
}
