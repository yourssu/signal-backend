package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.profile.implement.domain.Profile
import com.yourssu.ssugaeting.domain.profile.implement.domain.PurchasedProfile
import com.yourssu.ssugaeting.domain.viewer.implement.Viewer
import com.yourssu.ssugaeting.domain.viewer.implement.ViewerRepository
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

    private fun savePurchasedProfile(purchasedProfile: PurchasedProfile) {
        purchasedProfileRepository.save(purchasedProfile)
    }

    private fun exists(purchasedProfile: PurchasedProfile): Boolean {
        return purchasedProfileRepository.exists(purchasedProfile)
    }
}
