package com.yourssu.signal.domain.referral.implement

interface ReferralOrderRepository {
    fun save(referralOrder: ReferralOrder): ReferralOrder
    fun findByReferralCode(referralCode: String): List<ReferralOrder>
    fun findByViewerUuid(viewerUuid: String): ReferralOrder?
    fun delete(referralOrder: ReferralOrder)
}
