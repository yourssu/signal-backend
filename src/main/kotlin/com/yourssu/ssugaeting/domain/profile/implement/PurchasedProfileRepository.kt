package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.profile.implement.domain.PurchasedProfile

interface PurchasedProfileRepository {
    fun save(purchasedProfile: PurchasedProfile): PurchasedProfile
    fun exists(purchasedProfile: PurchasedProfile): Boolean
    fun findByViewerId(viewerId: Long): List<PurchasedProfile>
}
