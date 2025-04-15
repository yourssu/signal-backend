package com.yourssu.ssugaeting.domain.profile.implement

interface PurchasedProfileRepository {
    fun save(purchasedProfile: PurchasedProfile): PurchasedProfile
    fun exists(purchasedProfile: PurchasedProfile): Boolean
}
