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
    val uuid: String,

    @Column(nullable = false, unique = true)
    val verificationCode: Int,

) {
    companion object {
        fun from(
            uuid: Uuid,
            verificationCode: VerificationCode,
        ) = VerificationEntity(
            uuid = uuid.value,
            verificationCode = verificationCode.value,
        )
    }

    fun toUuid() = Uuid(
        value = uuid,
    )

    fun toVerificationCode() = VerificationCode(
        value = verificationCode,
    )
}
