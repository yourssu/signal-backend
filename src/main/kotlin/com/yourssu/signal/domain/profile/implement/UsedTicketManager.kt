package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.domain.Profile
import com.yourssu.signal.domain.profile.implement.domain.PurchasedProfile
import com.yourssu.signal.domain.profile.implement.exception.NoPurchasedProfileException
import com.yourssu.signal.domain.viewer.implement.ViewerRepository
import com.yourssu.signal.domain.viewer.implement.domain.Viewer
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UsedTicketManager(
    private val purchasedProfileRepository: PurchasedProfileRepository,
    private val viewerRepository: ViewerRepository,
) {
    @Transactional
    fun consumeTicket(viewer: Viewer, profile: Profile, ticket: Int): Viewer {
        val purchasedProfile = PurchasedProfile(viewerId = viewer.id!!, profileId = profile.id!!)
        if (exists(purchasedProfile)) {
            return viewer
        }
        val updatedViewer = viewerRepository.updateUsedTicket(viewer.consumeTicket(ticket))
        savePurchasedProfile(purchasedProfile)
        return updatedViewer
    }

    fun validatePurchasedProfile(viewer: Viewer, profile: Profile) {
        val purchasedProfile = PurchasedProfile(viewerId = viewer.id!!, profileId = profile.id!!)
        if (!exists(purchasedProfile)) {
            throw NoPurchasedProfileException()
        }
    }

    private fun savePurchasedProfile(purchasedProfile: PurchasedProfile) {
        purchasedProfileRepository.save(purchasedProfile)
//        purchasedProfileRepository.updateCacheIds()
    }

    private fun exists(purchasedProfile: PurchasedProfile): Boolean {
        return purchasedProfileRepository.exists(purchasedProfile)
    }
}
