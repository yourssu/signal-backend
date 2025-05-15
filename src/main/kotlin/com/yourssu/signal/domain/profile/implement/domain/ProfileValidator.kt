package com.yourssu.signal.domain.profile.implement.domain

import com.yourssu.signal.domain.profile.implement.domain.exception.BirthYearViolatedException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceLengthViolatedException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceSizeViolatedException
import com.yourssu.signal.domain.profile.implement.exception.NicknameLengthViolatedException
import java.time.LocalDate

private const val MAXIMUM_NICKNAME_LENGTH = 15
private const val MAXIMUM_INTRO_SENTENCES_SIZE = 3
private const val MAXIMUM_INTRO_SENTENCE_LENGTH = 20

private const val MINIMUM_BIRTH_YEAR = 1900
private const val MINIMUM_AGE = 20

object ProfileValidator {
    fun validateNickname(nickname: String) {
        if (nickname.isEmpty() || nickname.length > MAXIMUM_NICKNAME_LENGTH) {
            throw NicknameLengthViolatedException()
        }
    }

    fun validateIntroSentences(introSentences: List<String>) {
        if (introSentences.size > MAXIMUM_INTRO_SENTENCES_SIZE) {
            throw IntroSentenceSizeViolatedException()
        }
        for (introSentence in introSentences) {
            if (introSentence.length > MAXIMUM_INTRO_SENTENCE_LENGTH) {
                throw IntroSentenceLengthViolatedException()
            }
        }
    }

    fun validateBirthYear(birthYear: Int) {
        if (birthYear < MINIMUM_BIRTH_YEAR || birthYear > LocalDate.now().year - MINIMUM_AGE) {
            throw BirthYearViolatedException()
        }
    }
}
