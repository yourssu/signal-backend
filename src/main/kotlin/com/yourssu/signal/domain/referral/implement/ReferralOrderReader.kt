package com.yourssu.signal.domain.referral.implement

import com.yourssu.signal.domain.referral.implement.domain.ReferralOrder
import org.springframework.stereotype.Component

@Component
class ReferralOrderReader(
    private val referralOrderRepository: ReferralOrderRepository
) {
    fun findByReferralCode(referralCode: String): List<ReferralOrder> {
        return referralOrderRepository.findByReferralCode(referralCode)
    }

    fun findByViewerUuid(viewerUuid: String): ReferralOrder? {
        return referralOrderRepository.findByViewerUuid(viewerUuid)
    }
}