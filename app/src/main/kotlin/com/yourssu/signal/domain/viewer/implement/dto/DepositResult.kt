package com.yourssu.signal.domain.viewer.implement.dto

import VerificationCode

data class DepositResult(
    val verificationCode: VerificationCode,
    val ticket: Int,
    val depositAmount: Int,
)
