package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid

interface IntroSentenceRepository {
    fun saveAll(introSentences: List<String>, uuid: Uuid): List<String>
    fun findAllByUuid(uuid: Uuid): List<String>
}
