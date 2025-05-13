package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
import com.yourssu.signal.domain.profile.implement.exception.RandomProfileNotFoundException
import com.yourssu.signal.utils.GaussianDistributionUtils.calculateProbabilities
import com.yourssu.signal.utils.GaussianDistributionUtils.selectIndexByProbabilityDistribution
import org.springframework.stereotype.Component

private const val STANDARD_DEVIATION = 10.0

@Component
class ProfilePriorityManager(
    private val profileReader: ProfileReader,
    private val purchasedProfileReader: PurchasedProfileReader,
) {
    fun pickRandomProfile(
        excludeProfileIds: HashSet<Long>,
        targetGender: Gender,
    ): Profile {
        val profileIdsByGender = profileReader.findIdsByGender(targetGender).toSet()
        val purchasedProfileIds = purchasedProfileReader.findProfileIdsOrderByPurchasedAsc().toSet()
        val candidateProfileId = profileIdsByGender.filter { it !in purchasedProfileIds && it !in excludeProfileIds }.shuffled() +
                purchasedProfileIds.filter { it !in excludeProfileIds && it in profileIdsByGender }
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
