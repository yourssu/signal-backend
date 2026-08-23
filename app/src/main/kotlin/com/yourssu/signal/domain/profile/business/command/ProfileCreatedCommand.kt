package com.yourssu.signal.domain.profile.business.command

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.EgenTeto
import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.profile.implement.ProfileValidator

class ProfileCreatedCommand(
    val uuid: String,
    val gender: String,
    val department: String,
    val birthYear: Int,
    val animal: String,
    val contact: String,
    val mbti: String,
    val nickname: String,
    val introSentences: List<String>,
    val school: String,
    val egenTeto: String? = null
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }

    fun toDomain(): Profile {
        val parsedGender = Gender.of(gender)
        val parsedAnimal = Animal.of(animal)
        ProfileValidator.validateAnimal(parsedGender, parsedAnimal)
        ProfileValidator.validateContact(contact)
        return Profile(
            uuid = Uuid(uuid),
            gender = parsedGender,
            department = department,
            birthYear = birthYear,
            animal = parsedAnimal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences,
            school = school,
            egenTeto = egenTeto?.let { EgenTeto.of(it) },
        )
    }
}
