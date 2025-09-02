package com.yourssu.signal.domain.auth.storage.domain

import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.auth.implement.GoogleUser
import jakarta.persistence.*

@Entity
@Table(name = "google_user")
class GoogleUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val uuid: String,

    @Column(nullable = false, unique = true)
    val identifier: String,
): BaseEntity() {
    companion object {
        fun from(googleUser: GoogleUser): GoogleUserEntity {
            return GoogleUserEntity(
                id = googleUser.id,
                uuid = googleUser.uuid,
                identifier = googleUser.identifier,
            )
        }
    }

    fun toDomain(): GoogleUser {
        return GoogleUser(
            id = id,
            uuid = uuid,
            identifier = identifier,
        )
    }
}
