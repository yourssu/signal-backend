package com.yourssu.ssugaeting.domain.profile.business.dto

import com.yourssu.ssugaeting.domain.profile.implement.domain.PurchasedProfile

data class PurchasedProfileResponse(
    val profileId: Long,
    val createdTime: String,
) {
    companion object {
        fun from(purchasedProfile: PurchasedProfile): PurchasedProfileResponse {
            return PurchasedProfileResponse(
                profileId = purchasedProfile.profileId,
                createdTime = purchasedProfile.createdTime.toString(),
            )
        }
    }
}
