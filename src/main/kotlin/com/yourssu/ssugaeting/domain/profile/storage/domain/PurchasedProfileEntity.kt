package com.yourssu.ssugaeting.domain.profile.storage.domain

import com.yourssu.ssugaeting.domain.profile.implement.domain.PurchasedProfile
import jakarta.persistence.*
import java.time.ZonedDateTime

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

    @Column(nullable = false, name = "created_time")
    val createdTime: ZonedDateTime,
) {
    companion object {
        fun from(purchasedProfile: PurchasedProfile) = PurchasedProfileEntity(
            id = purchasedProfile.id,
            profileId = purchasedProfile.profileId,
            viewerId = purchasedProfile.viewerId,
            createdTime = purchasedProfile.createdTime,
        )
    }

    fun toDomain() = PurchasedProfile(
        id = id,
        profileId = profileId,
        viewerId = viewerId,
        createdTime = createdTime,
    )
}
