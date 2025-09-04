package com.yourssu.signal.domain.referral.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.referral.implement.domain.Referral

interface ReferralRepository {
    fun save(referral: Referral): Referral
    fun findByReferralCode(referralCode: String): Referral?
    fun findByOrigin(origin: Uuid): Referral?
}