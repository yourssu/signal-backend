package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.BirthYearViolatedException
import com.yourssu.signal.domain.profile.implement.exception.AnimalGenderMismatchException
import com.yourssu.signal.domain.profile.implement.exception.ContactFormatViolatedException
import com.yourssu.signal.domain.profile.implement.exception.ContactLimitExceededException
import com.yourssu.signal.domain.profile.implement.exception.DepartmentLengthViolatedException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceLengthViolatedException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceSizeViolatedException
import com.yourssu.signal.domain.profile.implement.exception.MbtiNotFoundException
import com.yourssu.signal.domain.profile.implement.exception.NicknameLengthViolatedException
import com.yourssu.signal.infrastructure.logging.Notification
private const val UNLIMITED_CONTACT_POLICY = 0

object ProfileValidator {
    fun validateNickname(nickname: String) {
        if (nickname.length !in ProfileValidationPolicy.MIN_NICKNAME_LENGTH..ProfileValidationPolicy.MAX_NICKNAME_LENGTH) {
            throw NicknameLengthViolatedException()
        }
    }

    fun validateIntroSentences(introSentences: List<String>) {
        if (introSentences.size !in ProfileValidationPolicy.MIN_INTRO_SENTENCES_SIZE..ProfileValidationPolicy.MAX_INTRO_SENTENCES_SIZE) {
            throw IntroSentenceSizeViolatedException()
        }
        for (introSentence in introSentences) {
            if (introSentence.length > ProfileValidationPolicy.MAX_INTRO_SENTENCE_LENGTH) {
                throw IntroSentenceLengthViolatedException()
            }
        }
    }

    fun validateBirthYear(birthYear: Int) {
        if (birthYear !in ProfileValidationPolicy.MIN_BIRTH_YEAR.toInt()..ProfileValidationPolicy.maximumBirthYear()) {
            throw BirthYearViolatedException()
        }
    }

    fun validateDepartment(department: String) {
        if (department.length !in ProfileValidationPolicy.MIN_DEPARTMENT_LENGTH..ProfileValidationPolicy.MAX_DEPARTMENT_LENGTH) {
            throw DepartmentLengthViolatedException()
        }
    }

    fun validateContact(contact: String) {
        if (!ProfileValidationPolicy.isValidContact(contact)) {
            throw ContactFormatViolatedException()
        }
    }

    fun validateAnimal(gender: Gender, animal: Animal) {
        if (!ProfileValidationPolicy.isValidAnimal(gender, animal)) {
            throw AnimalGenderMismatchException()
        }
    }

    fun validateMbti(mbti: String) {
        if (!MbtiCompatibilityTable.isValid(mbti)) {
            throw MbtiNotFoundException()
        }
    }

    fun checkContactLimit(countContact: Int, contactLimitPolicy: Int) {
        if (contactLimitPolicy == UNLIMITED_CONTACT_POLICY) {
            return
        }
        if (countContact >= contactLimitPolicy) {
            throw ContactLimitExceededException(contactLimitPolicy)
        }
    }

    fun checkContactLimitWarning(countContact: Int, contactLimitWarning: Int) {
        if (contactLimitWarning == UNLIMITED_CONTACT_POLICY) {
            return
        }
        if (countContact >= contactLimitWarning) {
            Notification.notifyContactExceedsLimitWarning(contactLimitWarning)
        }
    }

    fun validateNicknameBannedWord(nickname: String, bannedWords: Set<String>) {
        BannedWordValidator.validate(nickname, bannedWords)
    }

    fun validateIntroSentencesBannedWord(introSentences: List<String>, bannedWords: Set<String>) {
        introSentences.forEach { BannedWordValidator.validate(it, bannedWords) }
    }
}
