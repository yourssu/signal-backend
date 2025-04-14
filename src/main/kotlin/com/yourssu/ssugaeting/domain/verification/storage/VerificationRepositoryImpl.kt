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
    override fun issueVerificationCode(uuid: Uuid, verificationCode: VerificationCode): VerificationCode {
        val verification = VerificationEntity.from(uuid, verificationCode)
        return verificationJpaRepository.save(verification).toVerificationCode()
    }

    override fun existsByUuid(uuid: Uuid): Boolean {
        return jpaQueryFactory.select(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
            .fetchFirst() != null
    }

    override fun getVerificationCode(uuid: Uuid): VerificationCode {
        return jpaQueryFactory.select(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
            .fetchFirst()
            ?.toVerificationCode()
            ?: throw VerificationCodeNotFoundException()
    }

    override fun getUuid(verificationCode: VerificationCode): Uuid {
        return jpaQueryFactory.select(verificationEntity)
            .where(verificationEntity.verificationCode.eq(verificationCode.value))
            .fetchFirst()
            ?.toUuid()
            ?: throw VerificationCodeNotFoundException()
    }

    override fun existsByCode(code: VerificationCode): Boolean {
        return jpaQueryFactory.select(verificationEntity)
            .where(verificationEntity.verificationCode.eq(code.value))
            .fetchFirst() != null
    }

    override fun removeByUuid(uuid: Uuid) {
        jpaQueryFactory.delete(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
    }
}

interface VerificationJpaRepository : JpaRepository<VerificationEntity, Long> {
}
