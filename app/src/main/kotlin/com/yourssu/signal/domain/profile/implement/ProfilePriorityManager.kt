package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.profile.implement.exception.RandomProfileNotFoundException
import com.yourssu.signal.utils.GaussianDistributionUtils.calculateProbabilities
import com.yourssu.signal.utils.GaussianDistributionUtils.selectIndexByProbabilityDistribution
import org.springframework.stereotype.Component

private const val STANDARD_DEVIATION = 50.0

@Component
class ProfilePriorityManager(
    private val profileReader: ProfileReader,
    private val purchasedProfileReader: PurchasedProfileReader,
    private val blacklistReader: BlacklistReader,
) {
    fun pickRandomProfile(
        excludeProfileIds: HashSet<Long>,
        targetGender: Gender,
    ): Profile {
        val profileIdsByGender = profileReader.findIdsByGender(targetGender).toSet()
        val purchasedProfileIds = purchasedProfileReader.findProfileIdsOrderByPurchasedAsc().toSet()
        val blacklistProfileIds = blacklistReader.getAllBlacklistIds()
        val candidateProfileId = profileIdsByGender.filter { it !in purchasedProfileIds && it !in excludeProfileIds && it !in blacklistProfileIds}.shuffled() +
                purchasedProfileIds.filter { it !in excludeProfileIds && it in profileIdsByGender && it !in blacklistProfileIds}
        validateEmpty(candidateProfileId)
        val probabilities = calculateProbabilities(size = candidateProfileId.size, stdDev = STANDARD_DEVIATION)
        val profileIndex = selectIndexByProbabilityDistribution(probabilities)
        return profileReader.getById(candidateProfileId[profileIndex])
    }

    private fun validateEmpty(candidateProfileId: List<Long>) {
        if (candidateProfileId.isEmpty()) {
            throw RandomProfileNotFoundException()
        }
    }
}
