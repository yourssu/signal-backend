package com.yourssu.ssugaeting.domain.viewer.business

import com.yourssu.ssugaeting.domain.viewer.business.dto.VerificationResponse
import com.yourssu.ssugaeting.domain.verification.implement.VerificationIssuer
import org.springframework.stereotype.Service

@Service
class ViewerService(
    private val verificationIssuer: VerificationIssuer,
) {
    fun issueVerificationCode(command: VerificationCommand): VerificationResponse {
        val code = verificationIssuer.issueVerificationCode(command.toDomain())
        return VerificationResponse.from(code)
    }
}
