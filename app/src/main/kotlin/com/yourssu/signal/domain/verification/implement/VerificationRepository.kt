package com.yourssu.signal.domain.verification.implement

import VerificationCode
import com.yourssu.signal.domain.common.implement.Uuid

interface VerificationRepository {
    fun issueVerificationCode(verification: Verification): VerificationCode
    fun existsByUuid(uuid: Uuid): Boolean
    fun getVerificationCode(uuid: Uuid): VerificationCode
    fun getByCode(verificationCode: VerificationCode): Verification
    fun existsByCode(code: VerificationCode): Boolean
    fun removeByUuid(uuid: Uuid)
    fun clear()
    fun findAllVerificationCodes(): List<Int>
}
