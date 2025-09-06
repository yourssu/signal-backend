package com.yourssu.signal.domain.profile.storage

import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.profile.implement.PurchasedProfile
import jakarta.persistence.*
import java.time.ZoneId

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
): BaseEntity() {
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
        createdTime = createdTime?.atZone(ZoneId.systemDefault())
    )
}
