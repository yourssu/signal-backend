package com.yourssu.ssugaeting.domain.verification.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.verification.implement.domain.VerificationCode

interface VerificationRepository {
    fun issueVerificationCode(uuid: Uuid, verificationCode: VerificationCode): VerificationCode
    fun existsByUuid(uuid: Uuid): Boolean
    fun getVerificationCode(uuid: Uuid): VerificationCode
    fun getUuid(verificationCode: VerificationCode): Uuid
    fun existsByCode(code: VerificationCode): Boolean
    fun removeByUuid(uuid: Uuid)
    fun clear()
    fun findAllVerificationCodes(): List<Int>
}
