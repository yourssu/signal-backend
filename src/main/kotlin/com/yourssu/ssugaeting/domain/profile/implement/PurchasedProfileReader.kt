package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.profile.implement.domain.PurchasedProfile
import org.springframework.stereotype.Component

@Component
class PurchasedProfileReader(
    private val purchasedProfileRepository: PurchasedProfileRepository,
) {
    fun findByViewerId(viewerId: Long): List<PurchasedProfile> {
        return purchasedProfileRepository.findByViewerId(viewerId)
    }
}
