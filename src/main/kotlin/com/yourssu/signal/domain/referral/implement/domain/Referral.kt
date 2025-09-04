package com.yourssu.signal.domain.referral.implement.domain

import com.yourssu.signal.domain.common.implement.Uuid

data class Referral(
    val id: Long? = null,
    val origin: Uuid,
    val referralCode: String,
)