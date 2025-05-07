package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.domain.PurchasedProfile

interface PurchasedProfileRepository {
    fun save(purchasedProfile: PurchasedProfile): PurchasedProfile
    fun exists(purchasedProfile: PurchasedProfile): Boolean
    fun findByViewerId(viewerId: Long): List<PurchasedProfile>
    fun findProfileIdsOrderByPurchasedAsc(): List<Long>
    fun updateCacheIds(): List<Long>
}
