package com.yourssu.signal.domain.referral.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.referral.implement.domain.Referral
import org.springframework.stereotype.Component

@Component
class ReferralReader(
    private val referralRepository: ReferralRepository
) {
    fun findByReferralCode(referralCode: String): Referral? {
        return referralRepository.findByReferralCode(referralCode)
    }

    fun findByOrigin(origin: Uuid): Referral? {
        return referralRepository.findByOrigin(origin)
    }
}