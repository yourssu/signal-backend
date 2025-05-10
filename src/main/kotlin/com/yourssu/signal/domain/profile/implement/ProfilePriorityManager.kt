package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
import com.yourssu.signal.domain.profile.implement.exception.RandomProfileNotFoundException
import org.springframework.stereotype.Component

@Component
class ProfilePriorityManager(
    private val profileReader: ProfileReader,
    private val purchasedProfileReader: PurchasedProfileReader,
) {
    fun pickRandomProfile(
        excludeProfileIds: HashSet<Long>,
        targetGender: Gender,
    ): Profile {
        val oppositeIds = profileReader.findIdsByGender(targetGender).toHashSet()
        val purchasedProfileIds = purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()
            .filter { it in oppositeIds }
            .toSet()
        val profileIdsOrderByPurchased = (oppositeIds - purchasedProfileIds - excludeProfileIds).shuffled() + (purchasedProfileIds - excludeProfileIds)
        val randomProfileId = profileIdsOrderByPurchased.firstOrNull() ?: throw RandomProfileNotFoundException()
        return profileReader.getById(randomProfileId)
    }
}
