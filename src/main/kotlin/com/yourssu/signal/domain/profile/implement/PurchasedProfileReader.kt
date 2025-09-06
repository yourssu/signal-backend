package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.ProfileRankingNotFoundException
import org.springframework.stereotype.Component

@Component
class PurchasedProfileReader(
    private val purchasedProfileRepository: PurchasedProfileRepository,
) {
    fun findByViewerId(viewerId: Long): List<PurchasedProfile> {
        return purchasedProfileRepository.findByViewerId(viewerId)
    }

    fun findProfileIdsOrderByPurchasedAsc(): List<Long> {
        return purchasedProfileRepository.findProfileIdsOrderByPurchasedAsc()
    }

    fun getProfileRanking(profileId: Long): ProfileRanking {
        val rankings = purchasedProfileRepository.findProfileCountGroupByProfileId()
        return rankings[profileId] ?: throw ProfileRankingNotFoundException()
    }
}
