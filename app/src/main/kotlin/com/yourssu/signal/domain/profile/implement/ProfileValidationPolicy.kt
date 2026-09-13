package com.yourssu.signal.domain.profile.implement

object ProfileValidationPolicy {
    const val MIN_DEPARTMENT_LENGTH = 1
    const val MAX_DEPARTMENT_LENGTH = 20
    const val MIN_BIRTH_YEAR = 1985L
    const val MAX_BIRTH_YEAR = 2008L
    const val MIN_NICKNAME_LENGTH = 1
    const val MAX_NICKNAME_LENGTH = 15
    const val MIN_INTRO_SENTENCES_SIZE = 0
    const val MAX_INTRO_SENTENCES_SIZE = 3
    const val MAX_INTRO_SENTENCE_LENGTH = 20

    const val CONTACT_PATTERN = "^(?:010\\d{8}|@[a-zA-Z0-9._]{1,30})$"
    private const val PHONE_NUMBER_PATTERN = "^010\\d{8}$"
    private const val PHONE_PREFIX_LENGTH = 3

    val maleAnimals = setOf(Animal.BEAR, Animal.DEER, Animal.DINOSAUR, Animal.DOG, Animal.CAT, Animal.WOLF)
    val femaleAnimals = setOf(Animal.FOX, Animal.RABBIT, Animal.TURTLE, Animal.DOG, Animal.CAT, Animal.HAMSTER)

    fun maximumBirthYear(): Int = MAX_BIRTH_YEAR.toInt()

    fun isValidContact(contact: String): Boolean = Regex(CONTACT_PATTERN).matches(contact)

    fun isDummyContact(contact: String): Boolean {
        if (!Regex(PHONE_NUMBER_PATTERN).matches(contact)) {
            return false
        }
        val subscriberNumber = contact.substring(PHONE_PREFIX_LENGTH)
        return isSameDigits(subscriberNumber) || isSequentialDigits(subscriberNumber)
    }

    private fun isSameDigits(digits: String): Boolean = digits.all { it == digits.first() }

    private fun isSequentialDigits(digits: String): Boolean {
        val step = digits[1] - digits[0]
        if (step != 1 && step != -1) {
            return false
        }
        return digits.zipWithNext().all { (previous, next) -> next - previous == step }
    }

    fun isValidAnimal(gender: Gender, animal: Animal): Boolean = when (gender) {
        Gender.MALE -> animal in maleAnimals
        Gender.FEMALE -> animal in femaleAnimals
    }
}
