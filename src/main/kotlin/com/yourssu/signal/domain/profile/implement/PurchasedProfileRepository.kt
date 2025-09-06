package com.yourssu.signal.domain.profile.implement

interface PurchasedProfileRepository {
    fun save(purchasedProfile: PurchasedProfile): PurchasedProfile
    fun exists(purchasedProfile: PurchasedProfile): Boolean
    fun findByViewerId(viewerId: Long): List<PurchasedProfile>
    fun findProfileIdsOrderByPurchasedAsc(): List<Long>
    fun findProfileCountGroupByProfileId(): Map<Long, ProfileRanking>
}
