package com.yourssu.ssugaeting.domain.verification.storage

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.verification.implement.VerificationCode
import jakarta.persistence.*

@Entity
@Table(name = "verification")
class VerificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val verificationCode: Int,

    @Column(nullable = false, unique = true)
    val uuid: String,
) {
    companion object {
        fun from(verificationCode: VerificationCode, uuid: Uuid) = VerificationEntity(
            verificationCode = verificationCode.value,
            uuid = uuid.value,
        )
    }

    fun toDomain() = VerificationCode(
        value = verificationCode,
    )
}
