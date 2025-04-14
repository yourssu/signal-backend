package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.verification.implement.VerificationCode
import com.yourssu.ssugaeting.domain.verification.implement.VerificationRepository
import org.springframework.stereotype.Component

@Component
class VerificationReader(
    private val verificationRepository: VerificationRepository,
) {
    fun findByCode(code: VerificationCode): Uuid {
        return verificationRepository.getUuid(code)
    }
}
