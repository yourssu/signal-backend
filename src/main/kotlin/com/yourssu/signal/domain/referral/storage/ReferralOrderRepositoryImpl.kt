package com.yourssu.signal.domain.referral.storage

import com.yourssu.signal.domain.referral.implement.ReferralOrderRepository
import com.yourssu.signal.domain.referral.implement.domain.ReferralOrder
import com.yourssu.signal.domain.referral.storage.domain.ReferralOrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface ReferralOrderJpaRepository : JpaRepository<ReferralOrderEntity, Long> {
    fun findByReferralCode(referralCode: String): List<ReferralOrderEntity>
    fun findByViewerUuid(viewerUuid: String): ReferralOrderEntity?
}

@Repository
class ReferralOrderRepositoryImpl(
    private val jpaRepository: ReferralOrderJpaRepository
) : ReferralOrderRepository {
    
    override fun save(referralOrder: ReferralOrder): ReferralOrder {
        val entity = ReferralOrderEntity.from(referralOrder)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findByReferralCode(referralCode: String): List<ReferralOrder> {
        return jpaRepository.findByReferralCode(referralCode).map { it.toDomain() }
    }

    override fun findByViewerUuid(viewerUuid: String): ReferralOrder? {
        return jpaRepository.findByViewerUuid(viewerUuid)?.toDomain()
    }

    override fun delete(referralOrder: ReferralOrder) {
        jpaRepository.delete(ReferralOrderEntity.from(referralOrder))
    }
}
