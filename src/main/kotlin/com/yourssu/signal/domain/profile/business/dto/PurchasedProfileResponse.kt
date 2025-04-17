package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.domain.PurchasedProfile

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
