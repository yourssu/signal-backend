package com.yourssu.ssugaeting.domain.profile.implement.domain

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.exception.IntroSentenceLengthViolatedException
import com.yourssu.ssugaeting.domain.profile.implement.exception.IntroSentenceSizeViolatedException
import com.yourssu.ssugaeting.domain.profile.implement.exception.NicknameLengthViolatedException

private const val MAXIMUM_NICKNAME_LENGTH = 10
private const val MAXIMUM_INTRO_SENTENCES_SIZE = 3
private const val MAXIMUM_INTRO_SENTENCE_LENGTH = 23

class Profile(
    val id: Long? = null,
    val uuid : Uuid,
    val gender: Gender,
    val animal: String,
    val contact: String,
    val mbti: Mbti,
    val nickname: String,
    val introSentences: List<String>,
) {
    init {
        if (nickname.isEmpty() || nickname.length > MAXIMUM_NICKNAME_LENGTH) {
            throw NicknameLengthViolatedException()
        }
        if (introSentences.size > MAXIMUM_INTRO_SENTENCES_SIZE) {
            throw IntroSentenceSizeViolatedException()
        }
        for (introSentence in introSentences) {
            if (introSentence.length > MAXIMUM_INTRO_SENTENCE_LENGTH) {
                throw IntroSentenceLengthViolatedException()
            }
        }
    }

    companion object {
        fun ofNewProfile(gender: Gender, animal: String, contact: String, mbti: Mbti, nickname: String, introSentences: List<String>): Profile {
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

    fun copy(introSentences: List<String>): Profile {
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
}
