package com.yourssu.ssugaeting.domain.verification.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.verification.implement.VerificationCode
import com.yourssu.ssugaeting.domain.verification.implement.VerificationRepository
import com.yourssu.ssugaeting.domain.verification.storage.QVerificationEntity.verificationEntity
import com.yourssu.ssugaeting.domain.verification.storage.exception.VerificationCodeNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class VerificationRepositoryImpl(
    private val verificationJpaRepository: VerificationJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : VerificationRepository {
    override fun issueVerificationCode(uuid: Uuid): VerificationCode {
        val verificationEntity =
            VerificationEntity.from(
                uuid = uuid,
                verificationCode = VerificationCode.generateRandom()
            )
        return verificationJpaRepository.save(verificationEntity).toDomain()
    }

    override fun existsVerificationCode(uuid: Uuid): Boolean {
        return jpaQueryFactory.select(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
            .fetchFirst() != null
    }

    override fun findVerificationCode(uuid: Uuid): VerificationCode {
        return jpaQueryFactory.select(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
            .fetchFirst()
            ?.toDomain()
            ?: throw VerificationCodeNotFoundException()
    }
}

interface VerificationJpaRepository : JpaRepository<VerificationEntity, Long> {
}
