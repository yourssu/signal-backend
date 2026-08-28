package com.yourssu.signal.domain.profile.implement

import java.time.LocalDate

object ProfileValidationPolicy {
    const val MIN_DEPARTMENT_LENGTH = 1
    const val MAX_DEPARTMENT_LENGTH = 20
    const val MIN_BIRTH_YEAR = 1900L
    const val MIN_NICKNAME_LENGTH = 1
    const val MAX_NICKNAME_LENGTH = 15
    const val MIN_INTRO_SENTENCES_SIZE = 0
    const val MAX_INTRO_SENTENCES_SIZE = 3
    const val MAX_INTRO_SENTENCE_LENGTH = 20

    const val CONTACT_PATTERN = "^(?:010[2-9]\\d{7}|@[a-zA-Z0-9._]{1,30})$"
    const val PHONE_PATTERN = "^010[2-9]\\d{7}$"
    const val INSTAGRAM_PATTERN = "^@[a-zA-Z0-9._]{1,30}$"

    val maleAnimals = setOf(Animal.BEAR, Animal.DEER, Animal.DINOSAUR, Animal.DOG, Animal.CAT, Animal.WOLF)
    val femaleAnimals = setOf(Animal.FOX, Animal.RABBIT, Animal.TURTLE, Animal.DOG, Animal.CAT, Animal.HAMSTER)

    fun maximumBirthYear(): Int = LocalDate.now().year

    fun isValidContact(contact: String): Boolean = Regex(CONTACT_PATTERN).matches(contact)

    fun isValidAnimal(gender: Gender, animal: Animal): Boolean = when (gender) {
        Gender.MALE -> animal in maleAnimals
        Gender.FEMALE -> animal in femaleAnimals
    }
}
