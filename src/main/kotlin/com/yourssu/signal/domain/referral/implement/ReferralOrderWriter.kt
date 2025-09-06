package com.yourssu.signal.domain.referral.implement

import org.springframework.stereotype.Component

@Component
class ReferralOrderWriter(
    private val referralOrderRepository: ReferralOrderRepository
) {
    fun save(referralOrder: ReferralOrder): ReferralOrder {
        return referralOrderRepository.save(referralOrder)
    }

    fun delete(referralOrder: ReferralOrder) {
        referralOrderRepository.delete(referralOrder)
    }
}
