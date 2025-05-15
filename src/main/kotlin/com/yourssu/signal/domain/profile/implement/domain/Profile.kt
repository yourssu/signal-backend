package com.yourssu.signal.domain.profile.implement.domain

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.ProfileValidator.validateBirthYear
import com.yourssu.signal.domain.profile.implement.domain.ProfileValidator.validateIntroSentences
import com.yourssu.signal.domain.profile.implement.domain.ProfileValidator.validateNickname

class Profile(
    val id: Long? = null,
    val uuid : Uuid,
    val gender: Gender,
    val department: String,
    val birthYear: Int,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
) {
    init {
        validateNickname(nickname)
        validateIntroSentences(introSentences)
        validateBirthYear(birthYear)
    }

    companion object {
        fun ofNewProfile(gender: Gender, department: String, birthYear: Int, animal: String, contact: String, mbti: String, nickname: String, introSentences: List<String>): Profile {
            return Profile(
                uuid = Uuid.randomUUID(),
                gender = gender,
                department = department,
                birthYear = birthYear,
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
            department = department,
            birthYear = birthYear,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences
        )
    }
}
