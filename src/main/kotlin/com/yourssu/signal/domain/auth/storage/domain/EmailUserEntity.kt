package com.yourssu.signal.domain.auth.storage.domain

import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.auth.implement.EmailUser
import com.yourssu.signal.domain.common.implement.Uuid
import jakarta.persistence.*

@Entity
@Table(name = "email_user")
class EmailUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val uuid: String,

    @Column(nullable = false, unique = true)
    val email: String,
): BaseEntity() {
    companion object {
        fun from(emailUser: EmailUser): EmailUserEntity {
            return EmailUserEntity(
                id = emailUser.id,
                uuid = emailUser.uuid,
                email = emailUser.email,
            )
        }
    }

    fun toDomain(): EmailUser {
        return EmailUser(
            id = id,
            uuid = uuid,
            email = email,
        )
    }
}
