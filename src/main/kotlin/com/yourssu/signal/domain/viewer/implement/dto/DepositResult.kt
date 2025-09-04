package com.yourssu.signal.domain.viewer.implement.dto

import com.yourssu.signal.domain.verification.implement.domain.VerificationCode

data class DepositResult(
    val verificationCode: VerificationCode,
    val ticket: Int,
    val depositAmount: Int,
)