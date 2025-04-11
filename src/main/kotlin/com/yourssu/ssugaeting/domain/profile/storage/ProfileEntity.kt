package com.yourssu.ssugaeting.domain.profile.storage

import com.yourssu.ssugaeting.domain.profile.implement.Gender
import com.yourssu.ssugaeting.domain.profile.implement.Mbti
import com.yourssu.ssugaeting.domain.profile.implement.Profile
import jakarta.persistence.*


@Entity
@Table(name = "profile")
class ProfileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var gender: Gender,

    @Column(nullable = false)
    val animal: String,

    @Column(nullable = false)
    val contact: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val mbti: Mbti,

    @Column(nullable = false)
    val nickname: String,
) {
    companion object {
        fun from(profile: Profile): ProfileEntity {
            return ProfileEntity(
                id = profile.id,
                gender = profile.gender,
                animal = profile.animal,
                contact = profile.contact,
                mbti = profile.mbti,
                nickname = profile.nickname,
            )
        }
    }

    fun toDomain(): Profile {
        return Profile(
            id = id,
            gender = gender,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
        )
    }
}
