package com.yourssu.signal.domain.payment.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.domain.payment.implement.KakaoPayOrderRepository
import com.yourssu.signal.domain.payment.implement.domain.KakaoPayOrder
import com.yourssu.signal.domain.payment.storage.domain.KakaoPayOrderEntity
import com.yourssu.signal.domain.payment.storage.domain.QKakaoPayOrderEntity.*
import com.yourssu.signal.domain.payment.storage.exception.NotFoundKakaoPayOrderException
import com.yourssu.signal.domain.viewer.implement.domain.Viewer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class KakaoPayOrderRepositoryImpl(
    private val jpaRepository: KakaoPayPaymentJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : KakaoPayOrderRepository {

    override fun save(order: KakaoPayOrder): KakaoPayOrder {
        val entity = jpaRepository.save(KakaoPayOrderEntity.from(order))
        return entity.toDomain()
    }
    
    override fun getByViewerUuidAndTid(viewer: Viewer, tid: String): KakaoPayOrder {
        val entity = jpaQueryFactory.selectFrom(kakaoPayOrderEntity)
            .where(
                kakaoPayOrderEntity.uuid.eq(viewer.uuid.value),
                kakaoPayOrderEntity.tid.eq(tid)
            )
            .fetchOne()
            ?: throw NotFoundKakaoPayOrderException()
        return entity.toDomain()
    }

    override fun getByOrderId(orderId: String): KakaoPayOrder {
        val entity = jpaQueryFactory.selectFrom(kakaoPayOrderEntity)
            .where(kakaoPayOrderEntity.orderId.eq(orderId))
            .fetchOne()
            ?: throw NotFoundKakaoPayOrderException()
        return entity.toDomain()
    }
}

interface KakaoPayPaymentJpaRepository : JpaRepository<KakaoPayOrderEntity, String> {
}
