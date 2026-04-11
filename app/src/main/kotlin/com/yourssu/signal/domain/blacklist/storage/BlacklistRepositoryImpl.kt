package com.yourssu.signal.domain.blacklist.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.domain.blacklist.implement.Blacklist
import com.yourssu.signal.domain.blacklist.implement.BlacklistRepository
import com.yourssu.signal.domain.blacklist.storage.QBlacklistEntity.blacklistEntity
import com.yourssu.signal.domain.blacklist.storage.exception.BlacklistNotFoundException
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class BlacklistRepositoryImpl(
    private val blacklistJpaRepository: BlacklistJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : BlacklistRepository {
    override fun save(blacklist: Blacklist): Blacklist {
        return blacklistJpaRepository.save(BlacklistEntity.from(blacklist))
            .toDomain()
    }

    override fun existsByProfileId(profileId: Long): Boolean {
        return jpaQueryFactory.from(blacklistEntity)
            .where(blacklistEntity.profileId.eq(profileId))
            .fetchOne() != null
    }

    override fun getByProfileId(profileId: Long): Blacklist {
        return jpaQueryFactory.selectFrom(blacklistEntity)
            .where(blacklistEntity.profileId.eq(profileId))
            .fetchOne()
            ?.toDomain()
            ?: throw BlacklistNotFoundException()
    }

    override fun deleteByProfileId(profileId: Long) {
        jpaQueryFactory.delete(blacklistEntity)
            .where(blacklistEntity.profileId.eq(profileId))
            .execute()
    }

    @Cacheable(cacheNames = ["blacklistCache"])
    override fun findAll(): Set<Long> {
        return blacklistJpaRepository.findAll()
            .map { it.profileId }
            .toSet()
    }

    @CachePut(cacheNames = ["blacklistCache"])
    override fun updateCache(): Set<Long> {
        return blacklistJpaRepository.findAll()
            .map { it.profileId }
            .toSet()
    }

    override fun isAddedByAdmin(profileId: Long): Boolean {
        return jpaQueryFactory.selectFrom(blacklistEntity)
            .where(blacklistEntity.profileId.eq(profileId))
            .fetchOne()
            ?.createdByAdmin
            ?: false
    }

    override fun updateToAdminBlacklist(profileId: Long) {
        jpaQueryFactory.update(blacklistEntity)
            .where(blacklistEntity.profileId.eq(profileId))
            .set(blacklistEntity.createdByAdmin, true)
            .execute()
    }
}

interface BlacklistJpaRepository : JpaRepository<BlacklistEntity, Long> {
}
