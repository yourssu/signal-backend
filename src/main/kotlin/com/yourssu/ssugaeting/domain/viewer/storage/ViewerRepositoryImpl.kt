package com.yourssu.ssugaeting.domain.viewer.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.viewer.implement.Viewer
import com.yourssu.ssugaeting.domain.viewer.implement.ViewerRepository
import com.yourssu.ssugaeting.domain.viewer.storage.QViewerEntity.viewerEntity
import com.yourssu.ssugaeting.domain.viewer.storage.exception.ViewerNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ViewerRepositoryImpl(
    private val viewerJpaRepository: ViewerJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : ViewerRepository {
    override fun save(viewer: Viewer): Viewer {
        return viewerJpaRepository.save(ViewerEntity.from(viewer)).toDomain()
    }

    override fun existsByUuid(uuid: Uuid): Boolean {
        return jpaQueryFactory.selectFrom(viewerEntity)
            .where(viewerEntity.uuid.eq(uuid.value))
            .fetchFirst() != null
    }

    override fun getByUuid(uuid: Uuid): Viewer {
        return jpaQueryFactory.selectFrom(viewerEntity)
            .where(viewerEntity.uuid.eq(uuid.value))
            .fetchFirst()
            ?.toDomain()
            ?: throw ViewerNotFoundException()
    }

    override fun updateTicket(viewer: Viewer): Viewer {
        val viewerEntity = viewerJpaRepository.findById(viewer.id!!)
            .orElseThrow { ViewerNotFoundException() }
        viewerEntity.updateTicket(viewer)
        return viewerEntity.toDomain()
    }

    override fun findAll(): List<Viewer> {
        return viewerJpaRepository.findAll()
            .map { it.toDomain() }
    }
}

interface ViewerJpaRepository : JpaRepository<ViewerEntity, Long> {
}
