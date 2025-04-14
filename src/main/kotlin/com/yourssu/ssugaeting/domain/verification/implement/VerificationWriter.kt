package com.yourssu.ssugaeting.domain.verification.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
class VerificationWriter(
    private val verificationRepository: VerificationRepository,
    private val verificationCodePool: VerificationCodePool,
) {
    @Transactional
    fun issueVerificationCode(uuid: Uuid): VerificationCode {
        if (isUuidRegistered(uuid)) {
            return verificationRepository.getVerificationCode(uuid)
        }
        while (true) {
            val code = verificationCodePool.pop()
            if (!isVerificationCodeInUse(code)) {
                return verificationRepository.issueVerificationCode(uuid, code)
            }
        }
    }

    private fun isUuidRegistered(uuid: Uuid) = verificationRepository.existsByUuid(uuid)

    private fun isVerificationCodeInUse(code: VerificationCode): Boolean {
        return verificationRepository.existsByCode(code)
    }

    @Transactional
    fun remove(uuid: Uuid) {
        verificationRepository.removeByUuid(uuid)
    }
}
