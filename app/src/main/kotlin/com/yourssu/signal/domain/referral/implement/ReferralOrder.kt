package com.yourssu.signal.domain.referral.implement

import com.yourssu.signal.domain.common.implement.Uuid

data class ReferralOrder(
    val id: Long? = null,
    val referralCode: String,
    val viewerUuid: Uuid,
)
