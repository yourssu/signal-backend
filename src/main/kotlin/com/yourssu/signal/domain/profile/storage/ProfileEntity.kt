package com.yourssu.signal.domain.profile.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile
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
    val department: String,

    @Column(nullable = false, name = "birth_year")
    val birthYear: Int,

    @Column(nullable = false)
    val animal: String,

    @Column(nullable = false)
    val contact: String,

    @Column(nullable = false)
    val mbti: String,

    @Column(nullable = false)
    val nickname: String,

    @Column(nullable = false)
    val school: String,
): BaseEntity() {
    companion object {
        fun from(profile: Profile, encryptedContact: String): ProfileEntity {
            return ProfileEntity(
                id = profile.id,
                gender = profile.gender,
                uuid = profile.uuid.value,
                department = profile.department,
                birthYear = profile.birthYear,
                animal = profile.animal,
                contact = encryptedContact,
                mbti = profile.mbti,
                nickname = profile.nickname,
                school = profile.school,
            )
        }
    }

    fun toDomain(introSentences: List<String> = emptyList()): Profile {
        return Profile(
            id = id,
            gender = gender,
            uuid = Uuid(uuid),
            department = department,
            birthYear = birthYear,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences,
            school = school,
        )
    }
}
