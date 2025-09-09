package com.yourssu.signal.domain.auth.storage

import com.yourssu.signal.domain.auth.implement.GoogleUser
import com.yourssu.signal.domain.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "google_user")
class GoogleUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
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
