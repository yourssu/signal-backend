package com.yourssu.signal.domain.verification.storage

import VerificationCode
import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.verification.implement.VerificationRepository
import com.yourssu.signal.domain.verification.implement.Verification
import com.yourssu.signal.domain.verification.storage.QVerificationEntity.verificationEntity
import com.yourssu.signal.domain.verification.storage.exception.VerificationCodeNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class VerificationRepositoryImpl(
    private val verificationJpaRepository: VerificationJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : VerificationRepository {
    override fun issueVerificationCode(verification: Verification): VerificationCode {
        val entity = VerificationEntity.from(verification)
        return verificationJpaRepository.save(entity).toVerificationCode()
    }

    override fun existsByUuid(uuid: Uuid): Boolean {
        return jpaQueryFactory.selectFrom(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
            .fetchFirst() != null
    }

    override fun getVerificationCode(uuid: Uuid): VerificationCode {
        return jpaQueryFactory.selectFrom(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
            .fetchFirst()
            ?.toVerificationCode()
            ?: throw VerificationCodeNotFoundException()
    }

    override fun getByCode(verificationCode: VerificationCode): Verification {
        return jpaQueryFactory.selectFrom(verificationEntity)
            .where(verificationEntity.verificationCode.eq(verificationCode.value))
            .fetchFirst()
            ?.toDomain()
            ?: throw VerificationCodeNotFoundException()
    }

    override fun existsByCode(code: VerificationCode): Boolean {
        return jpaQueryFactory.selectFrom(verificationEntity)
            .where(verificationEntity.verificationCode.eq(code.value))
            .fetchFirst() != null
    }

    override fun removeByUuid(uuid: Uuid) {
        jpaQueryFactory.delete(verificationEntity)
            .where(verificationEntity.uuid.eq(uuid.value))
            .execute()
    }

    override fun clear() {
        verificationJpaRepository.deleteAll()
    }

    override fun findAllVerificationCodes(): List<Int> {
        return jpaQueryFactory.select(verificationEntity.verificationCode)
            .from(verificationEntity)
            .fetch()
    }
}

interface VerificationJpaRepository : JpaRepository<VerificationEntity, Long> {
}
