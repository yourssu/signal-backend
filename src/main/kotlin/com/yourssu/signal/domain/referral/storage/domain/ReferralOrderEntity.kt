package com.yourssu.signal.domain.referral.storage.domain

import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.referral.implement.domain.ReferralOrder
import jakarta.persistence.*

@Entity
@Table(name = "referral_order")
class ReferralOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, name = "referral_code")
    val referralCode: String,

    @Column(nullable = false, name = "viewer_uuid")
    val viewerUuid: String,
): BaseEntity() {
    companion object {
        fun from(referralOrder: ReferralOrder): ReferralOrderEntity {
            return ReferralOrderEntity(
                id = referralOrder.id,
                referralCode = referralOrder.referralCode,
                viewerUuid = referralOrder.viewerUuid,
            )
        }
    }

    fun toDomain(): ReferralOrder {
        return ReferralOrder(
            id = id,
            referralCode = referralCode,
            viewerUuid = viewerUuid,
        )
    }
}
