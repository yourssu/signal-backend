package com.yourssu.signal.domain.referral.implement

import org.springframework.stereotype.Component

@Component
class ReferralWriter(
    private val referralRepository: ReferralRepository
) {
    fun save(referral: Referral): Referral {
        return referralRepository.save(referral)
    }
}
