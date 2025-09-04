package com.yourssu.signal.domain.referral.implement.domain

data class ReferralOrder(
    val id: Long? = null,
    val referralCode: String,
    val viewerUuid: String,
)
