package com.yourssu.ssugaeting.domain.verification.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class VerificationIssuer(
    private val verificationRepository: VerificationRepository,
) {
    fun issueVerificationCode(uuid: Uuid): VerificationCode {
        if (verificationRepository.existsVerificationCode(uuid)) {
            return verificationRepository.findVerificationCode(uuid)
        }
        return verificationRepository.issueVerificationCode(uuid)
    }
}
