package com.yourssu.ssugaeting.domain.profile.storage

import com.yourssu.ssugaeting.domain.profile.implement.PurchasedProfile
import jakarta.persistence.*

@Entity
@Table(name = "purchased_profile")
class PurchasedProfileEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, name = "purchase_id")
    val profileId: Long,

    @Column(nullable = false, name = "viewer_id")
    val viewerId: Long,
) {
    companion object {
        fun from(purchasedProfile: PurchasedProfile) = PurchasedProfileEntity(
            id = purchasedProfile.id,
            profileId = purchasedProfile.profileId,
            viewerId = purchasedProfile.viewerId,
        )
    }

    fun toDomain() = PurchasedProfile(
        id = id,
        profileId = profileId,
        viewerId = viewerId,
    )
}
