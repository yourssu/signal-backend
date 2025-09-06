package com.yourssu.signal.domain.referral.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.referral.implement.ReferralRepository
import com.yourssu.signal.domain.referral.implement.Referral
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface ReferralJpaRepository : JpaRepository<ReferralEntity, Long> {
    fun findByReferralCode(referralCode: String): ReferralEntity?
    fun findByOrigin(origin: String): ReferralEntity?
}

@Repository
class ReferralRepositoryImpl(
    private val jpaRepository: ReferralJpaRepository
) : ReferralRepository {
    
    override fun save(referral: Referral): Referral {
        val entity = ReferralEntity.from(referral)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findByReferralCode(referralCode: String): Referral? {
        return jpaRepository.findByReferralCode(referralCode)?.toDomain()
    }

    override fun findByOrigin(origin: Uuid): Referral? {
        return jpaRepository.findByOrigin(origin.value)?.toDomain()
    }
}
