package com.yourssu.signal.domain.referral.storage.domain

import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.referral.implement.domain.Referral
import jakarta.persistence.*

@Entity
@Table(name = "referral")
class ReferralEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val origin: String,

    @Column(nullable = false, unique = true, name = "referral_code")
    val referralCode: String,
): BaseEntity() {
    companion object {
        fun from(referral: Referral): ReferralEntity {
            return ReferralEntity(
                id = referral.id,
                origin = referral.origin.value,
                referralCode = referral.referralCode,
            )
        }
    }

    fun toDomain(): Referral {
        return Referral(
            id = id,
            origin = com.yourssu.signal.domain.common.implement.Uuid(origin),
            referralCode = referralCode,
        )
    }
}