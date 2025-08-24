package com.yourssu.signal.domain.auth.storage

import com.yourssu.signal.domain.auth.implement.User
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "users")
@Entity
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val uuid: String,
): BaseEntity(){
    fun toDomain(): User {
        return User(
            id = id,
            uuid = Uuid(uuid),
        )
    }

    companion object {
        fun from(user: User): UserEntity {
            return UserEntity(
                uuid = user.uuid.value,
            )
        }
    }
}
