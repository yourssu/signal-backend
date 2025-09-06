package com.yourssu.signal.domain.verification.implement

import VerificationCode
import com.yourssu.signal.domain.common.implement.Uuid

class Verification(
    val id: Long? = null,
    val verificationCode: VerificationCode,
    val uuid: Uuid,
) {
}
