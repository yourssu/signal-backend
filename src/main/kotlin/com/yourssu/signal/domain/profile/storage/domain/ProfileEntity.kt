package com.yourssu.signal.domain.profile.storage.domain

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
import jakarta.persistence.*


@Entity
@Table(name = "profile")
class ProfileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var uuid: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var gender: Gender,

    @Column(nullable = false)
    val animal: String,

    @Column(nullable = false)
    val contact: String,

    @Column(nullable = false)
    val mbti: String,

    @Column(nullable = false)
    val nickname: String,
) {
    companion object {
        fun from(profile: Profile, encryptedContact: String): ProfileEntity {
            return ProfileEntity(
                id = profile.id,
                gender = profile.gender,
                uuid = profile.uuid.value,
                animal = profile.animal,
                contact = encryptedContact,
                mbti = profile.mbti,
                nickname = profile.nickname,
            )
        }
    }

    fun toDomain(introSentences: List<String> = emptyList()): Profile {
        return Profile(
            id = id,
            gender = gender,
            uuid = Uuid(uuid),
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences,
        )
    }
}
