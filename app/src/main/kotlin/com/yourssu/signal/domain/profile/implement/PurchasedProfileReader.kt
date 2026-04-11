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

    fun getProfileRanking(profileId: Long, gender: Gender): ProfileRanking {
        val rankings = purchasedProfileRepository.findProfileCountGroupByProfileId(gender)
        return rankings[profileId] ?: throw ProfileRankingNotFoundException()
    }
}
