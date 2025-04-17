package com.yourssu.signal.domain.profile.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.ProfileRepository
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
import com.yourssu.signal.domain.profile.storage.domain.ProfileEntity
import com.yourssu.signal.domain.profile.storage.domain.QProfileEntity.profileEntity
import com.yourssu.signal.domain.profile.storage.execption.ProfileNotFoundException
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Repository

@Repository
class ProfileRepositoryImpl(
    private val profileJpaRepository: ProfileJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : ProfileRepository {
    override fun save(profile: Profile): Profile {
        val saveProfile = profileJpaRepository.save(ProfileEntity.from(profile))
        return saveProfile.toDomain(introSentences = profile.introSentences)
    }

    override fun getByUuid(uuid: Uuid): Profile {
        return jpaQueryFactory.selectFrom(profileEntity)
            .where(profileEntity.uuid.eq(uuid.value))
            .fetchFirst()
            ?.toDomain()
            ?: throw ProfileNotFoundException()
    }

    override fun getById(id: Long): Profile {
        return profileJpaRepository.findById(id)
            .orElseThrow { ProfileNotFoundException() }
            .toDomain()
    }

    override fun existsByUuid(uuid: Uuid): Boolean {
        return jpaQueryFactory.selectFrom(profileEntity)
            .where(profileEntity.uuid.eq(uuid.value))
            .fetchFirst() != null
    }

    override fun findAll(): List<Profile> {
        return profileJpaRepository.findAll().map { it.toDomain() }
    }

    @Cacheable(cacheNames = ["profileCache"], key = "#gender.name")
    override fun findAllOppositeGenderIds(gender: Gender): List<Long> {
        return jpaQueryFactory.select(profileEntity.id)
            .from(profileEntity)
            .where(!profileEntity.gender.eq(gender))
            .fetch()
    }

    @CachePut(cacheNames = ["profileCache"], key = "#gender.name")
    override fun updateCacheOppositeGenderIds(gender: Gender): List<Long> {
        return jpaQueryFactory.select(profileEntity.id)
            .from(profileEntity)
            .where(!profileEntity.gender.eq(gender))
            .fetch()
    }
}

interface ProfileJpaRepository: JpaRepository<ProfileEntity, Long> {
}
