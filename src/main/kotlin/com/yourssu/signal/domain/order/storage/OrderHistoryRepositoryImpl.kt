package com.yourssu.signal.domain.order.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.domain.order.implement.OrderHistoryRepository
import com.yourssu.signal.domain.order.implement.OrderHistory
import com.yourssu.signal.domain.order.implement.OrderStatus
import com.yourssu.signal.domain.order.implement.exception.OrderHistoryNotFoundException
import com.yourssu.signal.domain.order.implement.exception.OrderHistoryUpdateFailedException
import com.yourssu.signal.domain.order.storage.QOrderHistoryEntity.orderHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class OrderHistoryRepositoryImpl(
    private val orderHistoryJpaRepository: OrderHistoryJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
): OrderHistoryRepository {
    override fun save(orderHistory: OrderHistory): OrderHistory {
        return orderHistoryJpaRepository.save(OrderHistoryEntity.from(orderHistory))
            .toDomain()
    }

    override fun getByOrderId(orderId: String): OrderHistory {
        return jpaQueryFactory.selectFrom(orderHistoryEntity)
            .where(orderHistoryEntity.orderId.eq(orderId))
            .fetchFirst()
            ?.toDomain()
            ?: throw OrderHistoryNotFoundException()
    }

    override fun findByViewerUuid(viewerUuid: String): List<OrderHistory> {
        return jpaQueryFactory.selectFrom(orderHistoryEntity)
            .where(orderHistoryEntity.uuid.eq(viewerUuid))
            .fetch()
            .map { it.toDomain() }
    }

    override fun existsByOrderId(orderId: String): Boolean {
        return jpaQueryFactory.selectFrom(orderHistoryEntity)
            .where(orderHistoryEntity.orderId.eq(orderId))
            .fetchFirst() != null
    }

    @Transactional
    override fun updateStatus(id: Long, status: OrderStatus) {
        val updatedCount = jpaQueryFactory
            .update(orderHistoryEntity)
            .set(orderHistoryEntity.status, status)
            .where(orderHistoryEntity.id.eq(id))
            .execute()
        
        if (updatedCount == 0L) {
            throw OrderHistoryUpdateFailedException()
        }
    }
}

interface OrderHistoryJpaRepository : JpaRepository<OrderHistoryEntity, Long>
