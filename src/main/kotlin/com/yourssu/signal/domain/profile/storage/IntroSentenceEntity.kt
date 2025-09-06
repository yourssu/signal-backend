package com.yourssu.signal.domain.profile.storage

import com.yourssu.signal.domain.common.storage.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "intro_sentence")
class IntroSentenceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val introSentence: String,

    @Column(nullable = false)
    val uuid: String,
): BaseEntity() {
    companion object {
        fun from(introSentence: String, uuid: String): IntroSentenceEntity {
            return IntroSentenceEntity(
                introSentence = introSentence,
                uuid = uuid,
            )
        }
    }

    fun toDomain(): String {
        return introSentence
    }
}
