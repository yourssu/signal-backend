package com.yourssu.signal.domain.verification.implement

import VerificationCode
import com.yourssu.signal.domain.common.implement.Uuid
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
                val verification = Verification(
                    uuid = uuid,
                    verificationCode = code,
                )
                return verificationRepository.issueVerificationCode(verification)
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
