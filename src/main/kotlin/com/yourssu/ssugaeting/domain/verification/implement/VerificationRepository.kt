package com.yourssu.ssugaeting.domain.verification.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid

interface VerificationRepository {
    fun issueVerificationCode(uuid: Uuid): VerificationCode
    fun existsVerificationCode(uuid: Uuid): Boolean
    fun findVerificationCode(uuid: Uuid): VerificationCode
}
