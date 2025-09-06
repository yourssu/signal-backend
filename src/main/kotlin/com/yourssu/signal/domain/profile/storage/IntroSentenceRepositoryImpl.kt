package com.yourssu.signal.domain.profile.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.IntroSentenceRepository
import com.yourssu.signal.domain.profile.storage.QIntroSentenceEntity.introSentenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class IntroSentenceRepositoryImpl(
    private val introSentenceJpaRepository: IntroSentenceJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
): IntroSentenceRepository {
    override fun saveAll(introSentences: List<String>, uuid: Uuid): List<String> {
        return introSentenceJpaRepository.saveAll(introSentences.map { IntroSentenceEntity.from(it, uuid.value) })
            .map { it.introSentence }
            .toList()
    }

    override fun findAllByUuid(uuid: Uuid): List<String> {
        return jpaQueryFactory.select(introSentenceEntity.introSentence)
            .from(introSentenceEntity)
            .where(introSentenceEntity.uuid.eq(uuid.value))
            .fetch()
            .toList()
    }
}

interface IntroSentenceJpaRepository : JpaRepository<IntroSentenceEntity, Long> {
}
