package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.ProfileValidator.validateBirthYear
import com.yourssu.signal.domain.profile.implement.ProfileValidator.validateIntroSentences
import com.yourssu.signal.domain.profile.implement.ProfileValidator.validateNickname

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
    val school: String,
) {
    init {
        validateNickname(nickname)
        validateIntroSentences(introSentences)
        validateBirthYear(birthYear)
    }

    fun copy(introSentences: List<String> = this.introSentences, contact: String = this.contact, nickname: String = this.nickname): Profile {
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
            introSentences = introSentences,
            school = school
        )
    }
}
