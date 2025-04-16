package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid

interface IntroSentenceRepository {
    fun saveAll(introSentences: List<String>, uuid: Uuid): List<String>
    fun findAllByUuid(uuid: Uuid): List<String>
}
