package com.yourssu.ssugaeting.domain.verification.implement.domain

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.domain.Gender

class Verification(
    val id: Long? = null,
    val verificationCode: VerificationCode,
    val uuid: Uuid,
    val gender: Gender,
) {
}
