package com.yourssu.signal.domain.profile.implement.domain

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.ProfileValidator.validateIntroSentences
import com.yourssu.signal.domain.profile.implement.domain.ProfileValidator.validateNickname
import com.yourssu.signal.domain.profile.implement.exception.GenderMismatchException

class Profile(
    val id: Long? = null,
    val uuid : Uuid,
    val gender: Gender,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
) {
    init {
        validateNickname(nickname)
        validateIntroSentences(introSentences)
    }

    companion object {
        fun ofNewProfile(gender: Gender, animal: String, contact: String, mbti: String, nickname: String, introSentences: List<String>): Profile {
            return Profile(
                uuid = Uuid.randomUUID(),
                gender = gender,
                animal = animal,
                contact = contact,
                mbti = mbti,
                nickname = nickname,
                introSentences = introSentences,
            )
        }
    }

    fun copy(introSentences: List<String> = this.introSentences, contact: String = this.contact): Profile {
        return Profile(
            id = id,
            uuid = uuid,
            gender = gender,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences
        )
    }

    fun validateSameGender(gender: Gender) {
        if (this.gender != gender) {
            throw GenderMismatchException()
        }
    }
}
