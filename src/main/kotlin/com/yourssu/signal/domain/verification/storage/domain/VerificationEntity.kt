package com.yourssu.signal.domain.verification.storage.domain

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.verification.implement.domain.Verification
import com.yourssu.signal.domain.verification.implement.domain.VerificationCode
import jakarta.persistence.*

@Entity
@Table(name = "verification")
class VerificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val uuid: String,

    @Column(nullable = false, unique = true)
    val verificationCode: Int,

    ): BaseEntity() {
    companion object {
        fun from(
            verification: Verification
        ) = VerificationEntity(
            uuid = verification.uuid.value,
            verificationCode = verification.verificationCode.value,
        )
    }

    fun toDomain(): Verification {
        return Verification(
            uuid = Uuid(uuid),
            verificationCode = VerificationCode(verificationCode),
        )
    }

    fun toVerificationCode() = VerificationCode(
        value = verificationCode,
    )
}
