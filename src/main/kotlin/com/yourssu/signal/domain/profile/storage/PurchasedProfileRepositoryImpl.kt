package com.yourssu.signal.domain.profile.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.domain.profile.implement.PurchasedProfileRepository
import com.yourssu.signal.domain.profile.implement.ProfileRanking
import com.yourssu.signal.domain.profile.implement.PurchasedProfile
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.storage.QPurchasedProfileEntity.purchasedProfileEntity
import com.yourssu.signal.domain.profile.storage.QProfileEntity.profileEntity
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class PurchasedProfileRepositoryImpl(
    private val purchasedProfileJpaRepository: PurchasedProfileJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
): PurchasedProfileRepository {
    override fun save(purchasedProfile: PurchasedProfile): PurchasedProfile {
        return purchasedProfileJpaRepository.save(PurchasedProfileEntity.from(purchasedProfile))
            .toDomain()
    }

    override fun exists(purchasedProfile: PurchasedProfile): Boolean {
        return purchasedProfileJpaRepository.existsByProfileIdAndViewerId(
            profileId = purchasedProfile.profileId,
            viewerId = purchasedProfile.viewerId,
        )
    }

    override fun findByViewerId(viewerId: Long): List<PurchasedProfile> {
        return purchasedProfileJpaRepository.findByViewerId(viewerId)
            .map { it.toDomain() }
    }

    override fun findProfileIdsOrderByPurchasedAsc(): List<Long> {
        return jpaQueryFactory
            .select(purchasedProfileEntity.profileId)
            .from(purchasedProfileEntity)
            .groupBy(purchasedProfileEntity.profileId)
            .orderBy(purchasedProfileEntity.profileId.count().asc())
            .fetch()
    }

//    @Cacheable("profileRankingCache", key = "#gender")
    override fun findProfileCountGroupByProfileId(gender: Gender): Map<Long, ProfileRanking> {
        val profileIdsWithGender = jpaQueryFactory
            .select(profileEntity.id)
            .from(profileEntity)
            .where(profileEntity.gender.eq(gender))
            .fetch()
            .toSet()

        val results = jpaQueryFactory
            .select(
                purchasedProfileEntity.profileId,
                purchasedProfileEntity.profileId.count().castToNum(Int::class.java)
            )
            .from(purchasedProfileEntity)
            .where(purchasedProfileEntity.profileId.`in`(profileIdsWithGender))
            .groupBy(purchasedProfileEntity.profileId)
            .orderBy(purchasedProfileEntity.profileId.count().desc())
            .fetch()

        val countToRankMap = results
            .map { it.get(1, Int::class.java)!! }
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }

        return results.associate { tuple ->
            val profileId = tuple.get(0, Long::class.java)!!
            val count = tuple.get(1, Int::class.java)!!
            val rank = countToRankMap[count]!!
            profileId to ProfileRanking(
                profileId = profileId,
                rank = rank,
                purchaseCount = count
            )
        }
    }
}

interface PurchasedProfileJpaRepository : JpaRepository<PurchasedProfileEntity, Long> {
    fun existsByProfileIdAndViewerId(profileId: Long, viewerId: Long): Boolean
    fun findByViewerId(viewerId: Long): List<PurchasedProfileEntity>
}
